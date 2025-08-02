@echo off
echo === D5700 Computer Emulator (Windows Batch Launcher) ===
echo.
echo This batch file launches the D5700 emulator with proper console input support.
echo.

cd /d "%~dp0"

echo Running emulator...
.\gradlew.bat run --console=plain

echo.
echo Emulator finished.
pause