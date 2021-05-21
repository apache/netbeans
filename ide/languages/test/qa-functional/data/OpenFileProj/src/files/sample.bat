@echo off
rem ---------------------------------------------------------------------------
rem Version script for the CATALINA Server
rem
rem $Id$
rem ---------------------------------------------------------------------------

rem Guess CATALINA_HOME if not defined
set CURRENT_DIR=%cd%
if not "%CATALINA_HOME%" == """" goto gotHome
set CATALINA_HOME=%CURRENT_DIR%
if exist "%CATALINA_HOME%\bin\catalina.bat" goto okHome
cd ..
set CATALINA_HOME=%cd%
cd %CURRENT_DIR%
:gotHome
if exist "%CATALINA_HOME%\bin\catalina.bat" goto okHome
echo The CATALINA_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okHome

set EXECUTABLE=%CATALINA_HOME%\bin\catalina.bat

rem Check that target executable exists
if exist "%EXECUTABLE%" goto okExec
echo Cannot find %EXECUTABLE%
echo This file is needed to run this program
goto end
:okExec

rem Get remaining unshifted command line arguments and save them in the
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

call "%EXECUTABLE%" version %CMD_LINE_ARGS%

:end

rem another program:)
set _original=a
set _retry=b

:start
set /p _original="Type in your new password: "
set /p _retry="Retype the password, please: "
if %_original% equ %_retry% (
   echo password changed succesfully.
        goto end1
) else (
   echo The passwords don't match, please try again.
        echo.
        goto start
)

:end1
set _original=
set _retry=
