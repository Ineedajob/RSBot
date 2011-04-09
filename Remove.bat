@ECHO OFF

SET name=RSBot
TITLE %name% Remover
ECHO.
ECHO Please close all instances of %name% before continuing.
CHOICE /m "Remove all %name% files from your system?"
IF ERRORLEVEL 2 GOTO :eof
IF ERRORLEVEL 1 GOTO remove
PAUSE

:remove
ECHO.

ECHO Removing accounts files...
IF EXIST "%name%_Accounts.ini" DEL "%name%_Accounts.ini"
IF EXIST "%name% Accounts.ini" DEL "%name% Accounts.ini"

ECHO Removing program directory...
IF EXIST "%RSBOT_HOME%" RMDIR /S /Q "%RSBOT_HOME%"
FOR /F "tokens=3" %%G IN ('REG QUERY "HKCU\Software\Microsoft\Windows\CurrentVersion\Explorer\Shell Folders" /v "Personal"') DO (SET docs=%%G)
IF EXIST "%docs%\%name%" RMDIR /S /Q "%docs%\%name%"

ECHO.
ECHO Completed.
PAUSE > NUL
