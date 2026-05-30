@echo off
cd /d "%~dp0"

if not exist "lib\mysql-connector-j*.jar" (
    if not exist "lib\mysql-connector-java*.jar" (
        echo.
        echo  WARNING: No MySQL JDBC driver in lib folder.
        echo  Download mysql-connector-j.jar from:
        echo  https://dev.mysql.com/downloads/connector/j/
        echo  and place it in: %~dp0lib\
        echo.
    )
)

cd src
javac -cp ".;..\lib\*" com\royalenfield\service\*.java
if errorlevel 1 (
    echo Compile failed.
    pause
    exit /b 1
)

java -cp ".;..\lib\*" com.royalenfield.service.Main
pause
