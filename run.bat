@echo off
setlocal

set "PROJECT_ROOT=%~dp0"
set "DRIVER_JAR=%PROJECT_ROOT%lib\sqlite-jdbc.jar"
set "OUT_DIR=%PROJECT_ROOT%out"

if not exist "%DRIVER_JAR%" (
    echo Missing SQLite JDBC driver: %DRIVER_JAR%
    exit /b 1
)

if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

del "%TEMP%\parking_sources.txt" >nul 2>&1
for /r "%PROJECT_ROOT%src" %%f in (*.java) do (
    echo %%f>>"%TEMP%\parking_sources.txt"
)

javac -d "%OUT_DIR%" @"%TEMP%\parking_sources.txt"
if errorlevel 1 exit /b 1

java -cp "%OUT_DIR%;%DRIVER_JAR%" com.parking.Main
del "%TEMP%\parking_sources.txt" >nul 2>&1
