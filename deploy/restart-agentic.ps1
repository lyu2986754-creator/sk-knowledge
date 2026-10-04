<#
.SYNOPSIS
    启动 / 重启 Agentic RAG 服务（Python，默认 8092）。

.DESCRIPTION
    与 restart-rag.ps1 同一套思路：按端口找进程停止、后台启动、等就绪、做连通性验证。
    Agentic 服务依赖上游 Java RAG 的 /api/retrieve 接口，所以启动后会一并检查上游是否可达。

.NOTES
    本文件必须以「UTF-8 带 BOM」保存：Windows PowerShell 5.1 读取无 BOM 的 UTF-8
    脚本时会按系统 ANSI 代码页解码，导致中文乱码并使脚本解析失败。
#>

[CmdletBinding()]
param(
    [string]$ProjectRoot = 'D:\Project-self\AI qiyexiangmu\agentic-rag',
    [int]$Port = 8092,
    [string]$UpstreamRag = 'http://127.0.0.1:8082'
)

$ErrorActionPreference = 'Stop'

function Write-Step { param([string]$Text, [string]$Color = 'Cyan') Write-Host "`n=== $Text ===" -ForegroundColor $Color }
function Test-Port { param([int]$P) [bool](Get-NetTCPConnection -LocalPort $P -State Listen -ErrorAction SilentlyContinue) }

Write-Step '环境'
Write-Host "项目: $ProjectRoot"
Write-Host "端口: $Port"
Write-Host "上游: $UpstreamRag"

$python = Join-Path $ProjectRoot '.venv\Scripts\python.exe'
if (-not (Test-Path $python)) {
    throw "找不到虚拟环境 $python。请先在 agentic-rag 目录执行 python -m venv .venv 并安装依赖。"
}
if (-not (Test-Path (Join-Path $ProjectRoot '.env'))) {
    throw "找不到 .env。请复制 .env.example 为 .env 并填写 DEEPSEEK_API_KEY。"
}

Write-Step '停止占用端口的进程'
$conn = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
if ($conn) {
    $ownerId = $conn[0].OwningProcess
    $proc = Get-Process -Id $ownerId -ErrorAction SilentlyContinue
    Write-Host "  $Port <- 停止 $($proc.ProcessName) (pid $ownerId)"
    Stop-Process -Id $ownerId -Force -ErrorAction SilentlyContinue
} else {
    Write-Host "  $Port 未被占用"
}
Start-Sleep -Seconds 3

Write-Step '检查上游 Java RAG'
try {
    $health = Invoke-WebRequest "$UpstreamRag/api/login" -Method Post -TimeoutSec 10 -UseBasicParsing `
        -ContentType 'application/x-www-form-urlencoded' -Body 'username=admin&password=123456'
    Write-Host "  上游可达（登录返回 HTTP $($health.StatusCode)）" -ForegroundColor Green
} catch {
    Write-Host "  上游不可达：$($_.Exception.Message)" -ForegroundColor Yellow
    Write-Host "  Agentic 服务可以启动，但检索会失败。请先启动 Java RAG。" -ForegroundColor Yellow
}

$env:PYTHONPATH = Join-Path $ProjectRoot 'src'
$env:PYTHONIOENCODING = 'utf-8'

Write-Step '启动 Agentic 服务'
Start-Process -FilePath $python -ArgumentList @('-m', 'agentic_rag.app') `
    -WorkingDirectory $ProjectRoot `
    -RedirectStandardOutput (Join-Path $ProjectRoot 'app.log') `
    -RedirectStandardError  (Join-Path $ProjectRoot 'app.err') `
    -WindowStyle Hidden

for ($i = 1; $i -le 25; $i++) {
    Start-Sleep -Seconds 2
    if (Test-Port $Port) { Write-Host "  就绪（$($i*2)s）"; break }
}
if (-not (Test-Port $Port)) {
    Write-Host "  未就绪。app.err：" -ForegroundColor Red
    Get-Content (Join-Path $ProjectRoot 'app.err') -Tail 15 -ErrorAction SilentlyContinue
    throw "Agentic 服务启动失败。"
}

Write-Step '连通性验证' 'Green'
try {
    $r = Invoke-WebRequest "http://127.0.0.1:$Port/api/health" -TimeoutSec 10 -UseBasicParsing
    Write-Host "  /api/health -> $($r.Content)"
} catch {
    Write-Host "  健康检查失败：$($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n完成。Agentic RAG 在 http://127.0.0.1:$Port" -ForegroundColor Green
Write-Host "在 RAG 前端（http://localhost:8080/chat）把「模式」切到 Agentic RAG 即可使用。" -ForegroundColor Green