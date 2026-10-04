<#
.SYNOPSIS
    修复并重启 Docker Desktop。

.DESCRIPTION
    这台机器上的 Docker Desktop 有一套反复出现的故障，已经踩到四次，
    所以固化成脚本。典型症状会分阶段变化：

      阶段一（启动即崩）：
        "starting services: initializing Ingest server: listening on
        unix://.../sailor-ingest.sock: rename ... .stale: The file cannot be
        accessed by the system." —— 后端进程 exit 150，反复重启。
        根因：%LOCALAPPDATA%\Docker\run 下残留了孤立 reparse point
        （AF_UNIX socket 的残骸），Windows 无法解析也无法删除。
        解法：把整个目录改名让开，让 Docker 重建。

      阶段二（引擎活着但网络死了）：
        docker ps / docker info 秒回，容器显示 Up，
        但主机访问容器端口（如 18088）超时，docker exec 挂死。
        根因：WSL 网络层故障。
        解法：wsl --shutdown 后整体重启 Docker Desktop。

    本脚本把两个阶段的处理合在一起，可反复执行。

.NOTES
    必须以「UTF-8 带 BOM」保存：Windows PowerShell 5.1 读无 BOM 的 UTF-8
    脚本会按 ANSI 解码，中文乱码并导致解析失败。
#>

[CmdletBinding()]
param(
    [int]$TimeoutSeconds = 240,
    [switch]$SkipWslShutdown
)

$ErrorActionPreference = 'Continue'
$stamp = Get-Date -Format 'MMddHHmmss'

function Write-Step { param([string]$Text, [string]$Color = 'Cyan') Write-Host "`n=== $Text ===" -ForegroundColor $Color }

function Test-Engine {
    $v = docker version --format '{{.Server.Version}}' 2>$null
    return ($LASTEXITCODE -eq 0 -and $v)
}

Write-Step '停止 Docker 相关进程'
foreach ($name in @('Docker Desktop', 'com.docker.backend', 'com.docker.build',
                    'com.docker.proxy', 'docker')) {
    Get-Process -Name $name -ErrorAction SilentlyContinue | ForEach-Object {
        Write-Host "  kill $($_.ProcessName) (pid $($_.Id))"
        Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
    }
}
Start-Sleep -Seconds 4

if (-not $SkipWslShutdown) {
    Write-Step '关闭 WSL（释放 socket 句柄与网络层）'
    wsl --shutdown 2>&1 | Out-Null
    Start-Sleep -Seconds 8
}

Write-Step '让开残留的 socket 目录'
foreach ($item in @(
    @{ Path = "$env:LOCALAPPDATA\Docker\run";         Name = "run.orphan-$stamp" }
    @{ Path = "$env:LOCALAPPDATA\docker-secrets-engine"; Name = "secrets.orphan-$stamp" }
)) {
    if (-not (Test-Path -LiteralPath $item.Path)) {
        Write-Host "  不存在 $($item.Path)"
        continue
    }
    try {
        Rename-Item -LiteralPath $item.Path -NewName $item.Name -ErrorAction Stop
        Write-Host "  已让开 $($item.Path)" -ForegroundColor Green
    } catch {
        Write-Host "  让开失败 $($item.Path)：$($_.Exception.Message)" -ForegroundColor Yellow
    }
}

Write-Step '启动 Docker Desktop'
Start-Process -FilePath 'C:\Program Files\Docker\Docker\Docker Desktop.exe'

$startTime = Get-Date
$deadline = $startTime.AddSeconds($TimeoutSeconds)
$ready = $false
while ((Get-Date) -lt $deadline) {
    Start-Sleep -Seconds 8
    if (Test-Engine) {
        Write-Host "  引擎就绪（已等待 $([int]((Get-Date) - $startTime).TotalSeconds)s）" -ForegroundColor Green
        $ready = $true
        break
    }
}

if (-not $ready) {
    Write-Host "  未在 ${TimeoutSeconds}s 内就绪。最近崩溃记录：" -ForegroundColor Red
    Get-Content "$env:LOCALAPPDATA\Docker\log\host\com.docker.backend.exe.log" -Tail 200 -ErrorAction SilentlyContinue |
        Select-String -Pattern 'backend crashed' |
        Select-Object -Last 1 | ForEach-Object { Write-Host "  $($_.Line)" }
    exit 1
}

Write-Step '引擎信息'
docker info --format '  容器 {{.Containers}}（运行 {{.ContainersRunning}}）  镜像 {{.Images}}' 2>&1

Write-Host "`n完成。若容器已在运行，接下来用 deploy\docker-compose.yml 起中间件。" -ForegroundColor Green

