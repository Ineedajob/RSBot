@ECHO OFF
TITLE RSBot Javadoc Generator
CD src
CHOICE /m "Generate Javadoc in this directory?"
IF ERRORLEVEL 2 GOTO :exit
IF ERRORLEVEL 1 GOTO :gen

:gen
COLOR 8f
ECHO Generating your javadoc...
javadoc -d ..\javadoc -subpackages org.rsbot.event org.rsbot.script org.rsbot.script.internal org.rsbot.script.methods org.rsbot.script.util org.rsbot.script.wrappers org.rsbot.script org.rsbot.util -verbose
CLS
ECHO.
ECHO.
ECHO Complete. To view the Javadoc, navigate to Javadoc and open "index.html"
COLOR 0f
ECHO.
CHOICE /m "Open Javadoc now?"
IF ERRORLEVEL 2 GOTO :exit
IF ERRORLEVEL 1 GOTO :open

:open
cd ..\javadoc
index.html
GOTO :exit

:exit
CLS
EXIT