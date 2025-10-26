@echo off
REM P2PChat-CLI Local Client Runner for Windows

echo Starting P2PChat-CLI Client...

REM Set Java options
set JAVA_OPTS=-Xmx256m -Dlog.level=INFO

REM Run the application
mvn -q -DskipTests exec:java -Dexec.mainClass="com.p2pchat.cli.CLIApp" -Dexec.args=%*