<#
.SYNOPSIS
    把 LLM API key 从环境变量注入 sk-knowledge 的 chat_model 表。

.DESCRIPTION
    版本库里只保存占位符 REPLACE_WITH_ENV_KEY，真实密钥在启动后注入，
    避免密钥随 schema 一起进入版本库。幂等，可重复执行。

    密钥取值优先级：进程环境变量 SK_LLM_API_KEY > 本目录下的 .env。

    注意：本脚本原先读取评测平台的 eval/.env。deploy/ 划归被测对象目录后，
    改为读取同目录的 .env —— 这个密钥是 RAG 服务自身的凭据，本就该与 RAG 放在一起。

.NOTES
    本文件必须以「UTF-8 带 BOM」保存：Windows PowerShell 5.1 读取无 BOM 的
    UTF-8 脚本时会按系统 ANSI 代码页解码，导致中文乱码并使脚本解析失败。
#>

[CmdletBinding()]
param(
    [string]$EnvFile,
    [string]$Container = 'sk-eval-mysql',
    [string]$Database = 'sk_knowledge'
)

$ErrorActionPreference = 'Stop'

if (-not $EnvFile) {
    $EnvFile = Join-Path $PSScriptRoot '.env'
}

function Get-EnvValue {
    param([string]$Name, [string]$FallbackFile)

    $fromProcess = [Environment]::GetEnvironmentVariable($Name)
    if ($fromProcess) { return $fromProcess }

    if (-not (Test-Path -LiteralPath $FallbackFile)) {
        throw "Environment variable $Name is not set, and $FallbackFile does not exist."
    }
    foreach ($line in Get-Content -LiteralPath $FallbackFile) {
        $trimmed = $line.Trim()
        if (-not $trimmed -or $trimmed.StartsWith('#') -or $trimmed.IndexOf('=') -lt 1) { continue }
        $eq = $trimmed.IndexOf('=')
        $key = $trimmed.Substring(0, $eq).Trim()
        $value = $trimmed.Substring($eq + 1).Trim().Trim('"').Trim("'")
        if ($key -eq $Name) { return $value }
    }
    throw "$Name not found in $FallbackFile"
}

$apiKey = Get-EnvValue -Name 'SK_LLM_API_KEY' -FallbackFile $EnvFile
if ($apiKey -eq 'REPLACE_WITH_ENV_KEY' -or $apiKey.Length -lt 16) {
    throw "SK_LLM_API_KEY does not look valid (length $($apiKey.Length))."
}

$running = docker ps --filter "name=$Container" --format '{{.Names}}'
if ($running -ne $Container) {
    throw "Container $Container is not running. Start it with: docker compose -f .\deploy\docker-compose.yml up -d"
}

# 用 MYSQL_PWD 传密码，避免 mysql 打印 "Using a password ... insecure" 警告，
# 否则 PowerShell 会把该警告当成错误记录并因 ErrorActionPreference=Stop 中断。
$sql = "UPDATE chat_model SET api_host='https://api.siliconflow.cn/', api_key='$apiKey' WHERE type='chat'; " +
       "SELECT id, name, type, CONCAT(LEFT(api_key, 10), '...') AS key_prefix FROM chat_model ORDER BY id;"

Write-Host "Injecting API key into $Container/$Database ..." -ForegroundColor Cyan
docker exec -e MYSQL_PWD=123456 $Container mysql -uroot $Database -e $sql
if ($LASTEXITCODE -ne 0) { throw "Injection failed (docker exec returned $LASTEXITCODE)." }
Write-Host "Done. Prefixes above are masked, just enough to confirm the write." -ForegroundColor Green
