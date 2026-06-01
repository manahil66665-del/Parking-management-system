$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$DriverJar = Join-Path $ProjectRoot "lib\sqlite-jdbc.jar"
$OutDir = Join-Path $ProjectRoot "out"

if (!(Test-Path $DriverJar)) {
    Write-Error "Missing SQLite JDBC driver: $DriverJar"
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
$DriverZip = [IO.Compression.ZipFile]::OpenRead($DriverJar)
try {
    $HasDriverClass = $DriverZip.Entries |
        Where-Object { $_.FullName -eq "org/sqlite/JDBC.class" } |
        Select-Object -First 1
    if ($null -eq $HasDriverClass) {
        Write-Error "Invalid SQLite JDBC JAR. Download the main sqlite-jdbc binary JAR, not the javadoc or sources JAR."
    }
}
finally {
    $DriverZip.Dispose()
}

if (!(Test-Path $OutDir)) {
    New-Item -ItemType Directory -Path $OutDir | Out-Null
}

$Sources = Get-ChildItem -Path (Join-Path $ProjectRoot "src") -Recurse -Filter *.java |
    ForEach-Object { $_.FullName }

javac -d $OutDir $Sources
java -cp "$OutDir;$DriverJar" com.parking.Main
