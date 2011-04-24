@ECHO OFF

SETLOCAL
CALL :setvars
SET cmd=%1
IF "%cmd%"=="" SET cmd=all
CALL :%cmd%
GOTO :eof

:setvars
SET cc=javac
SET cflags=-g -Xlint:deprecation
SET src=src
SET lib=lib
SET res=resources
SET out=bin
SET dist=RSBot.jar
SET lstf=temp.txt
SET imgdir=%res%\images
SET manifest=%res%\Manifest.txt
SET versionfile=%res%\version.txt
FOR /F %%G IN (%versionfile%) DO SET version=%%G
SET scripts=scripts
CALL "%res%\FindJDK.bat"
GOTO :eof

:all
CALL :clean 2>NUL
ECHO Compiling bot
CALL :Bot
ECHO Compiling scripts
CALL :Scripts 2>NUL
ECHO Packing JAR
GOTO :pack
GOTO :end
GOTO :eof

:Bot
IF EXIST "%lstf%" DEL /F /Q "%lstf%"
FOR /F "usebackq tokens=*" %%G IN (`DIR /B /S "%src%\*.java"`) DO CALL :append "%%G"
IF EXIST "%out%" RMDIR /S /Q "%out%" > NUL
MKDIR "%out%"
"%cc%" %cflags% -d "%out%" "@%lstf%" 2>NUL
DEL /F /Q "%lstf%"
GOTO :eof

:Scripts
CALL :mostlyclean
"%cc%" %cflags% -cp "%out%" %scripts%\*.java
GOTO :eof

:pack
IF EXIST "%dist%" DEL /F /Q "%dist%"
IF EXIST "%lstf%" DEL /F /Q "%lstf%"
COPY "%manifest%" "%lstf%"
ECHO Specification-Version: "%version%" >> "%lstf%"
ECHO Implementation-Version: "%version%" >> "%lstf%"
jar cfm "%dist%" "%lstf%" -C "%out%" . %versionfile% %scripts%\*.class %imgdir%\* %res%\*.bat %res%\*.sh
DEL /F /Q "%lstf%"
GOTO :eof

:end
CALL :clean 2>NUL
ECHO Compilation successful.
GOTO :eof

:append
SET gx=%1
SET gx=%gx:\=\\%
ECHO %gx% >> %lstf%
GOTO :eof

:mostlyclean
ECHO. > "%scripts%\.class"
DEL /F /Q %scripts%\*.class
GOTO :eof

:clean
CALL :mostlyclean
RMDIR /S /Q "%out%" 2>NUL
GOTO :eof
