@ECHO OFF

SET cc=javac
SET cflags=-g -Xlint:deprecation
SET src=src
SET lib=lib
SET res=resources
SET out=bin
SET dist=RSBot.jar

CALL :clean 2>NUL
CALL "%res%\FindJDK.bat"

SET lstf=temp.txt
SET imgdir=%res%\images
SET manifest=%res%\Manifest.txt
SET versionfile=%res%\version.txt
FOR /F %%G IN (%versionfile%) DO SET version=%%G
SET scripts=scripts

ECHO Compiling bot
IF EXIST "%lstf%" DEL /F /Q "%lstf%"
FOR /F "usebackq tokens=*" %%G IN (`DIR /B /S "%src%\*.java"`) DO CALL :append "%%G"
IF EXIST "%out%" RMDIR /S /Q "%out%" > NUL
MKDIR "%out%"
"%cc%" %cflags% -d "%out%" "@%lstf%" 2>NUL
DEL /F /Q "%lstf%"

:scripts
ECHO Compiling scripts
ECHO. > "%scripts%\.class"
DEL /F /Q "%scripts%\*.class" > NUL
"%cc%" %cflags% -cp "%out%" %scripts%\*.java

ECHO Packing JAR

IF EXIST "%dist%" DEL /F /Q "%dist%"
IF EXIST "%lstf%" DEL /F /Q "%lstf%"
COPY "%manifest%" "%lstf%"
ECHO Specification-Version: "%version%" >> "%lstf%"
ECHO Implementation-Version: "%version%" >> "%lstf%"
jar cfm "%dist%" "%lstf%" -C "%out%" . %versionfile% %scripts%\*.class %imgdir%\* %res%\*.bat %res%\*.sh
DEL /F /Q "%lstf%"

:end
CALL :clean 2>NUL
ECHO Compilation successful.
GOTO :eof

:append
SET gx=%1
SET gx=%gx:\=\\%
ECHO %gx% >> %lstf%
GOTO :eof

:clean
RMDIR /S /Q "%out%"
DEL /F /Q %scripts%\*.class
GOTO :eof
