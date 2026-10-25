@echo off

REM Run from the directory containing this script
cd /d "%~dp0"

set "JDBC_JAR=lib\sqlite-jdbc-3.53.4.0.jar"

REM Verify that the SQLite JDBC driver exists
if not exist "%JDBC_JAR%" (
    echo ERROR: SQLite JDBC driver not found at "%JDBC_JAR%" 1>&2
    exit /b 1
)

REM Compile the program with the SQLite JDBC driver
javac --release 25 -cp ".;%JDBC_JAR%" *.java

REM Execute the program with native access enabled
java --enable-native-access=ALL-UNNAMED -cp ".;%JDBC_JAR%" Main

echo "Successfully compiled! Use '.\run.bat' to execute."