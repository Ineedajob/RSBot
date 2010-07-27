@ECHO OFF

TITLE RSBot Scripts

SET cc=javac
SET cflags=
SET scripts=Scripts
SET scriptspre=%scripts%\Precompiled
SET jarpathfile=Settings\path.txt

IF NOT EXIST "%jarpathfile%" (
	ECHO Path file does not exist. Please run RSBot and try again.
	GOTO end
)

FOR /F "delims=" %%G IN (%jarpathfile%) DO SET jarpath=%%G

CALL FindJDK.bat

IF NOT EXIST %scripts%\*.java (
	ECHO No .java script source files found.
	GOTO end
)

ECHO Compiling scripts
ECHO. > "%scripts%\.class"
DEL /F /Q "%scripts%\*.class" > NUL
"%cc%" %cflags% -cp "%jarpath%" %scripts%\*.java

:end
PAUSE
EXIT
