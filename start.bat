@echo off
setlocal
cd /d "%~dp0"

echo Starting CoralCove Resort Management...
where java >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Java (JDK 21 or newer^) was not found on your PATH.
    pause
    exit /b 1
)

call "%~dp0mvnw.cmd" javafx:run
if errorlevel 1 (
    echo.
    echo [ERROR] CoralCove failed to start. Check data\coralcove.log for details.
    pause
    exit /b 1
)
