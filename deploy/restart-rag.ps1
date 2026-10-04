<#
.SYNOPSIS
    重启被评测对象 sk-knowledge 的后端与前端。

.DESCRIPTION
    封装了四个从踩坑中得来的必要步骤：

      1. 后端不能用 fat jar 启动。pom.xml 没有配置 spring-boot-maven-plugin，
         产不出可执行 jar。

      2. 必须用 JDK 17。项目用 Lombok 1.18.26，不兼容 JDK 21。

      3. classpath 必须走 target\lib。本地 Maven 仓库路径含中文用户名
         （C:\Users\鲁瑜\.m2\...），无论通过 CLASSPATH 环境变量还是 java 的
         @argfile 传递，非 ASCII 路径都会被破坏，表现为
         NoClassDefFoundError: org/springframework/boot/CommandLineRunner。
         因此先用 dependency:copy-dependencies 把依赖复制到项目内的 ASCII
         目录 target\lib，classpath 随之缩到 100 字符左右且全为 ASCII。
         另外这还顺带避开了 Windows 进程环境块 32767 字符的总长限制。

      4. 传参必须拼成单个带引号的字符串。Start-Process -ArgumentList 传数组时
         只用空格拼接、不会自动加引号，而项目路径含空格
         （D:\Project-self\AI qiyexiangmu\...），参数会被拆断。

.NOTES
    本文件必须以「UTF-8 带 BOM」保存，否则 Windows PowerShell 5.1 会按 ANSI
    代码页解码，中文全部乱码并导致解析失败。
#>

[CmdletBinding()]
param(
    [string]$RagProject  = 'D:\Project-self\AI qiyexiangmu\sk-knowledge',
    [string]$RagFrontend = 'D:\Project-self\AI qiyexiangmu\sk-knowledge-frontend',
    [string]$Jdk17,
    [int]$BackendPort = 8082,
    [int]$FrontendPort = 8080,
    [switch]$Rebuild
)

$ErrorActionPreference = 'Stop'
$MainClass = 'com.skcto.skknowledge.SkKnowledgeApplication'

function Write-Step { param([string]$Text, [string]$Color = 'Cyan') Write-Host "`n=== $Text ===" -ForegroundColor $Color }
function Test-Port { param([int]$Port) [bool](Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue) }
function Get-Mvn {
    $mvn = Get-ChildItem (Join-Path $env:USERPROFILE '.m2\wrapper\dists') -Recurse -Filter 'mvn.cmd' -ErrorAction SilentlyContinue |
        Select-Object -First 1 -ExpandProperty FullName
    if (-not $mvn) { throw "未找到缓存的 Maven（~/.m2/wrapper/dists）。" }
    return $mvn
}

if (-not $Jdk17) {
    $candidate = Get-ChildItem -Path (Join-Path $env:USERPROFILE '.jdks') -Directory -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -match '17' } |
        Where-Object { Test-Path (Join-Path $_.FullName 'bin\java.exe') } |
        Select-Object -First 1
    if (-not $candidate) { throw "未找到 JDK 17，请用 -Jdk17 指定。" }
    $Jdk17 = $candidate.FullName
}
$env:JAVA_HOME = $Jdk17

Write-Step '环境'
Write-Host "JDK  : $Jdk17"
Write-Host "后端 : $RagProject"
Write-Host "前端 : $RagFrontend"

Write-Step '停止占用端口的进程'
foreach ($port in @($BackendPort, $FrontendPort)) {
    $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if ($conn) {
        $ownerId = $conn[0].OwningProcess
        $proc = Get-Process -Id $ownerId -ErrorAction SilentlyContinue
        Write-Host "  $port <- 停止 $($proc.ProcessName) (pid $ownerId)"
        Stop-Process -Id $ownerId -Force -ErrorAction SilentlyContinue
    } else {
        Write-Host "  $port 未被占用"
    }
}
Start-Sleep -Seconds 3

$classesDir = Join-Path $RagProject 'target\classes'
$libDir     = Join-Path $RagProject 'target\lib'
$mvn = Get-Mvn

if ($Rebuild -or -not (Test-Path $classesDir)) {
    Write-Step '编译后端'
    Push-Location $RagProject
    & $mvn -B -q -DskipTests package
    $buildCode = $LASTEXITCODE
    Pop-Location
    if ($buildCode -ne 0) { throw "编译失败（mvn 返回 $buildCode）。" }
    Write-Host "  编译成功"
} else {
    Write-Step '编译后端'
    Write-Host "  跳过（target\classes 已存在，需要重编请加 -Rebuild）"
}

$needCopy = $true
if (Test-Path $libDir) {
    $existing = (Get-ChildItem $libDir -Filter *.jar -ErrorAction SilentlyContinue).Count
    if ($existing -gt 0) { $needCopy = $false; Write-Host "`n  target\lib 已有 $existing 个 jar，跳过复制" }
}
if ($needCopy) {
    Write-Step '复制依赖到 target\lib（避开中文路径）'
    Push-Location $RagProject
    & $mvn -B -q dependency:copy-dependencies "-DoutputDirectory=target\lib"
    $copyCode = $LASTEXITCODE
    Pop-Location
    if ($copyCode -ne 0) { throw "复制依赖失败（mvn 返回 $copyCode）。" }
    $count = (Get-ChildItem $libDir -Filter *.jar).Count
    Write-Host "  已复制 $count 个 jar"
}

$cp = "$classesDir;$libDir\*"
$launchArgs = "-cp `"$cp`" $MainClass"

Write-Step '启动后端'
Write-Host "  classpath: $cp"
Start-Process -FilePath (Join-Path $Jdk17 'bin\java.exe') -ArgumentList $launchArgs `
    -WorkingDirectory $RagProject `
    -RedirectStandardOutput (Join-Path $RagProject 'target\app.log') `
    -RedirectStandardError  (Join-Path $RagProject 'target\app.err') `
    -WindowStyle Hidden
for ($i = 1; $i -le 45; $i++) {
    Start-Sleep -Seconds 2
    if (Test-Port $BackendPort) { Write-Host "  就绪（$($i*2)s）"; break }
}
if (-not (Test-Port $BackendPort)) {
    Write-Host "  未就绪。app.err：" -ForegroundColor Red
    Get-Content (Join-Path $RagProject 'target\app.err') -Tail 10 -ErrorAction SilentlyContinue
    Write-Host "  app.log：" -ForegroundColor Red
    Get-Content (Join-Path $RagProject 'target\app.log') -Tail 10 -ErrorAction SilentlyContinue
    throw "后端启动失败。"
}

Write-Step '启动前端'
Start-Process -FilePath 'npm.cmd' -ArgumentList @('run', 'dev') -WorkingDirectory $RagFrontend `
    -RedirectStandardOutput (Join-Path $RagFrontend 'dev-server.log') `
    -RedirectStandardError  (Join-Path $RagFrontend 'dev-server.err') `
    -WindowStyle Hidden
for ($i = 1; $i -le 30; $i++) {
    Start-Sleep -Seconds 2
    if (Test-Port $FrontendPort) { Write-Host "  就绪（$($i*2)s）"; break }
}
if (-not (Test-Port $FrontendPort)) { throw "前端启动失败，见 dev-server.log。" }

Write-Step '连通性验证' 'Green'
try {
    $r = Invoke-WebRequest "http://localhost:$FrontendPort/" -TimeoutSec 10 -UseBasicParsing
    Write-Host "  前端 HTTP $($r.StatusCode)"
} catch { Write-Host "  前端失败: $($_.Exception.Message)" -ForegroundColor Red }

try {
    $r = Invoke-WebRequest "http://localhost:$BackendPort/api/login" -Method Post -TimeoutSec 20 -UseBasicParsing `
        -ContentType 'application/x-www-form-urlencoded' -Body 'username=admin&password=123456'
    $j = $r.Content | ConvertFrom-Json
    Write-Host "  后端登录 code=$($j.code)  token 长度=$($j.data.Length)"
} catch { Write-Host "  后端失败: $($_.Exception.Message)" -ForegroundColor Red }

Write-Host "`n完成。前端 http://localhost:$FrontendPort/   后端 http://localhost:$BackendPort/" -ForegroundColor Green