@echo off
cd /d "%~dp0src"
echo Compiling...
javac -cp ".;..\lib\*" com\royalenfield\service\*.java
if errorlevel 1 exit /b 1
echo.
echo Setting up database...
java -cp ".;..\lib\*" com.royalenfield.service.DbSetup
pause
