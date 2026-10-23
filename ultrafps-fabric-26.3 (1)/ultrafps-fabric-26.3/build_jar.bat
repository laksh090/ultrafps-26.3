@echo off
title UltraFPS 26.3 JAR Builder
echo ===================================================
echo   Building UltraFPS Boost Mod (.jar) for MC 26.3
echo ===================================================
echo.

where gradle >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if exist gradlew.bat (
        echo Running Gradle Wrapper...
        call gradlew.bat build
    ) else (
        echo [ERROR] Gradle is not installed or not in your PATH.
        echo Please install Gradle 9.6.0+ or open this folder in IntelliJ IDEA and run the 'build' task.
        pause
        exit /b 1
    )
) else (
    echo Running Gradle build...
    call gradle build
)

echo.
if exist "build\libs\ultrafps-boost-1.0.0+26.3.jar" (
    echo [SUCCESS] Your mod JAR is ready at:
    echo   build\libs\ultrafps-boost-1.0.0+26.3.jar
    echo.
    echo Opening output folder...
    explorer "build\libs"
) else (
    echo [ERROR] Build failed. Make sure Java 25 (JDK 25) is installed and set as JAVA_HOME.
)
pause
