$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

if (-not (Test-Path ".\tools\apache-maven-3.9.9\bin\mvn.cmd")) {
    Write-Error "Khong tim thay Maven portable tai .\tools\apache-maven-3.9.9\bin\mvn.cmd"
}

$dbServer = if ($env:DB_SERVER) { $env:DB_SERVER } else { "DESKTOP-1C12BB8\SQLEXPRESS" }
$dbName = if ($env:DB_NAME) { $env:DB_NAME } else { "WaveBeatDB" }
$dbAuthMode = if ($env:DB_AUTH_MODE) { $env:DB_AUTH_MODE.ToLowerInvariant() } else { "sql" }
$dbHost = if ($env:DB_HOST) { $env:DB_HOST } else { "localhost" }
$dbPort = if ($env:DB_PORT) { $env:DB_PORT } else { "1433" }

if ($dbAuthMode -eq "windows") {
    if ($dbPort) {
        $env:DB_URL = "jdbc:sqlserver://${dbHost}:${dbPort};databaseName=$dbName;encrypt=true;trustServerCertificate=true;integratedSecurity=true;authenticationScheme=NativeAuthentication"
    }
    else {
        $env:DB_URL = "jdbc:sqlserver://$dbServer;databaseName=$dbName;encrypt=true;trustServerCertificate=true;integratedSecurity=true;authenticationScheme=NativeAuthentication"
    }
    Remove-Item Env:DB_USERNAME -ErrorAction SilentlyContinue
    Remove-Item Env:DB_PASSWORD -ErrorAction SilentlyContinue
}
else {
    if ($dbPort) {
        $env:DB_URL = "jdbc:sqlserver://${dbHost}:${dbPort};databaseName=$dbName;encrypt=true;trustServerCertificate=true"
    }
    else {
        $env:DB_URL = "jdbc:sqlserver://$dbServer;databaseName=$dbName;encrypt=true;trustServerCertificate=true"
    }
}

if ($dbAuthMode -ne "windows" -and -not $env:DB_USERNAME) {
    $env:DB_USERNAME = "sa"
}

if ($dbAuthMode -ne "windows" -and -not $env:DB_PASSWORD) {
    $env:DB_PASSWORD = "new_password"
}

Write-Host "Dang chay WaveBeat voi SQL Server..." -ForegroundColor Cyan
Write-Host "DB_SERVER   = $dbServer"
if ($dbPort) {
    Write-Host "DB_HOST     = $dbHost"
    Write-Host "DB_PORT     = $dbPort"
}
Write-Host "DB_NAME     = $dbName"
Write-Host "DB_AUTH     = $dbAuthMode"
Write-Host "DB_URL      = $env:DB_URL"
if ($dbAuthMode -ne "windows") {
    Write-Host "DB_USERNAME = $env:DB_USERNAME"
}
Write-Host "Mo web tai  = http://127.0.0.1:8080" -ForegroundColor Green

& ".\tools\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run
