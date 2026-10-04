<#
.SYNOPSIS
    拉取 sk-knowledge 依赖的中间件镜像。

.DESCRIPTION
    Docker Hub 在国内不可达（registry-1.docker.io 被 DNS 污染），因此走国内加速器
    拉取，再重打成规范标签，使 docker-compose.yml 保持干净的官方镜像名。

    三个工程细节：
      1. 多源轮询 —— 单个加速器对未缓存的层会回源到 Docker Hub 而卡死，
         换源往往能补齐（已下载的层按 digest 缓存，跨源复用，不会重下）。
      2. 每源超时 —— docker pull 卡住时不会自己退出，必须外部计时并杀掉。
      3. 幂等续传 —— 目标镜像已存在就跳过，可反复执行。
#>

[CmdletBinding()]
param(
    [string[]]$Mirrors = @('docker.m.daocloud.io', 'hub.rat.dev'),
    [int]$TimeoutSeconds = 420
)

$ErrorActionPreference = 'Continue'
$log = Join-Path $PSScriptRoot 'pull-images.log'

function Write-Log {
    param([string]$Message, [string]$Color = 'Gray')
    $line = "$(Get-Date -Format 'HH:mm:ss')  $Message"
    Write-Host $line -ForegroundColor $Color
    $line | Out-File -FilePath $log -Encoding utf8 -Append
}

# 官方镜像名 -> 加速器上的路径（library/ 前缀只对官方镜像需要）
$images = @(
    @{ dst = 'redis:7.0.11';        path = 'library/redis:7.0.11' }
    @{ dst = 'mongo:8.0.0';         path = 'library/mongo:8.0.0' }
    @{ dst = 'mysql:8.4.0';         path = 'library/mysql:8.4.0' }
    @{ dst = 'minio/mc:latest';     path = 'minio/mc:latest' }
    @{ dst = 'minio/minio:RELEASE.2025-06-13T11-33-47Z'; path = 'minio/minio:RELEASE.2025-06-13T11-33-47Z' }
    @{ dst = 'semitechnologies/weaviate:1.33.2';         path = 'semitechnologies/weaviate:1.33.2' }
    @{ dst = 'temporalio/ui:2.39.0';                     path = 'temporalio/ui:2.39.0' }
    @{ dst = 'temporalio/auto-setup:1.29.0';             path = 'temporalio/auto-setup:1.29.0' }
)

Write-Log "=== START  镜像源: $($Mirrors -join ', ')  单源超时 ${TimeoutSeconds}s ===" 'Cyan'

$failed = @()
$index = 0
foreach ($img in $images) {
    $index++
    docker image inspect $img.dst *> $null
    if ($LASTEXITCODE -eq 0) {
        Write-Log "[$index/$($images.Count)] SKIP $($img.dst)（已存在）"
        continue
    }

    $ok = $false
    foreach ($mirror in $Mirrors) {
        $src = "$mirror/$($img.path)"
        Write-Log "[$index/$($images.Count)] PULL $($img.dst)  <- $mirror"

        $out = Join-Path $PSScriptRoot '.pull-out.log'
        $proc = Start-Process -FilePath 'docker' -ArgumentList @('pull', $src) `
            -NoNewWindow -PassThru `
            -RedirectStandardOutput $out -RedirectStandardError "$out.err"

        $sw = [System.Diagnostics.Stopwatch]::StartNew()
        while (-not $proc.HasExited -and $sw.Elapsed.TotalSeconds -lt $TimeoutSeconds) {
            Start-Sleep -Seconds 3
        }
        if (-not $proc.HasExited) {
            $proc.Kill()
            Write-Log "       超时 ${TimeoutSeconds}s，换下一个源" 'Yellow'
            continue
        }
        if ($proc.ExitCode -ne 0) {
            $tail = (Get-Content "$out.err" -Tail 2 -ErrorAction SilentlyContinue) -join ' '
            Write-Log "       失败 exit=$($proc.ExitCode)  $tail" 'Yellow'
            continue
        }

        docker tag $src $img.dst *> $null
        if ($LASTEXITCODE -eq 0) {
            Write-Log "       OK -> $($img.dst)  用时 $([int]$sw.Elapsed.TotalSeconds)s" 'Green'
            $ok = $true
            break
        }
    }
    if (-not $ok) {
        Write-Log "[$index/$($images.Count)] FAIL $($img.dst)" 'Red'
        $failed += $img.dst
    }
}

if ($failed.Count -eq 0) {
    Write-Log "=== DONE 全部就绪 ===" 'Green'
} else {
    Write-Log "=== DONE 但有失败: $($failed -join ', ') ===" 'Red'
}
