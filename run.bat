@echo off
echo ===================================================
echo   PassTheBook - Java PBL Application Launcher
echo ===================================================
echo.

set MAVEN_CMD="C:\Users\edeva\maven\apache-maven-3.9.6\bin\mvn.cmd"

if exist %MAVEN_CMD% (
    echo [INFO] Running PassTheBook with Maven...
    %MAVEN_CMD% javafx:run
) else (
    echo [INFO] Running with system mvn...
    mvn javafx:run
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Application finished or failed with exit code %ERRORLEVEL%.
    pause
)
