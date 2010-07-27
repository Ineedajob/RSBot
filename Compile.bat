@ECHO OFF

SET cc=javac
SET cflags=
SET src=src
SET lib=lib
SET res=resources
SET out=bin
SET jarout=data

CALL :clean 2>NUL
CALL "%res%\FindJDK.bat"

SET lstf=temp.txt
SET imgdir=%res%\images
SET manifest=%res%\Manifest.txt
SET versionfile=.version
FOR /F %%G IN (%versionfile%) DO SET version=%%G
SET scripts=scripts
SET dist=RSBot.jar
SET full=1

IF "%1"=="/S" (
        SET full=0
        GOTO :scripts
)

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
IF "%full%"=="0" GOTO :end

ECHO Packing JAR

IF EXIST "%dist%" DEL /F /Q "%dist%"
IF EXIST "%lstf%" DEL /F /Q "%lstf%"
COPY "%manifest%" "%lstf%"
ECHO Specification-Version: "%version%" >> "%lstf%"
ECHO Implementation-Version: "%version%" >> "%lstf%"
IF NOT EXIST "%jarout%" MKDIR "%jarout%"
jar cfm "%jarout%\%dist%" "%lstf%" -C "%out%" . %scripts%\*.class %res%\version.dat %imgdir%\*.png %res%\*.bat %res%\*.sh
DEL /F /Q "%lstf%"

:end
ECHO Compilation successful.
GOTO :eof

:append
SET gx=%1
SET gx=%gx:\=\\%
ECHO %gx% >> %lstf%
GOTO :eof

:clean
RD /S /Q bin
RD /S /Q data
DEL /F /Q scripts\*.class
GOTO :eof
