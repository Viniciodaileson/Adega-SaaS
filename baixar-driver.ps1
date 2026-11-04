# Baixa o driver JDBC do SQLite para a pasta lib/.
# Uso: .\baixar-driver.ps1

$versao = "3.54.1.0"
$pasta = Join-Path $PSScriptRoot "lib"
$destino = Join-Path $pasta "sqlite-jdbc-$versao.jar"

if (Test-Path $destino) {
    Write-Host "Driver ja existe: $destino"
    exit 0
}

New-Item -ItemType Directory -Force -Path $pasta | Out-Null

$url = "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/$versao/sqlite-jdbc-$versao.jar"

Write-Host "Baixando $url ..."
Invoke-WebRequest -Uri $url -OutFile $destino -UseBasicParsing

Write-Host "Driver baixado em: $destino"
