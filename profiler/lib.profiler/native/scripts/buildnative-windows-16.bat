rem Licensed to the Apache Software Foundation (ASF) under one
rem or more contributor license agreements.  See the NOTICE file
rem distributed with this work for additional information
rem regarding copyright ownership.  The ASF licenses this file
rem to you under the Apache License, Version 2.0 (the
rem "License"); you may not use this file except in compliance
rem with the License.  You may obtain a copy of the License at
rem
rem   https://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing,
rem software distributed under the License is distributed on an
rem "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
rem KIND, either express or implied.  See the License for the
rem specific language governing permissions and limitations
rem under the License.

rem Edit the following line to set the JDK location (for JNI headers)
rem or leave if you have JDK_HOME set.
SET BUILD_JDK=%JDK_HOME%
rem Edit the following line to affect the deployment directory.
rem Should not need to be changed as JNI is compatible for all versions 
rem after 1.6.
SET JDK_DEPLOY=jdk16
SET PLATFORM=windows

rem Do not alter below this line
SET BUILD_SRC_15=..\src-jdk15
SET BUILD_SRC=..\src
SET BUILD_DEPLOY=..\..\release\lib

if not exist "%BUILD_DEPLOY%\deployed\%JDK_DEPLOY%\%PLATFORM%" mkdir "%BUILD_DEPLOY%\deployed\%JDK_DEPLOY%\%PLATFORM%"

echo on

rem Generate 'config.h' file
cl /I"%BUILD_JDK%\include" /I"%BUILD_JDK%\include\win32" ^
  ..\src-jdk15\config.c /link /out:..\build\config.exe && ^
..\build\config.exe > ..\build\config.h || ^
exit /b 1

echo Content of config.h :
type ..\build\config.h



rc /Fo .\version.res "%BUILD_SRC_15%\windows\version.rc"

cl /I"%BUILD_JDK%\include" /I"%BUILD_JDK%\include\win32" /I..\build  ^
  "%BUILD_SRC_15%\class_file_cache.c" ^
  "%BUILD_SRC_15%\attach.c" ^
  "%BUILD_SRC_15%\Classes.c" ^
  "%BUILD_SRC_15%\HeapDump.c" ^
  "%BUILD_SRC_15%\Timers.c" ^
  "%BUILD_SRC_15%\GC.c" ^
  "%BUILD_SRC_15%\Threads.c" ^
  "%BUILD_SRC_15%\Stacks.c" ^
  "%BUILD_SRC_15%\common_functions.c" ^
  version.res ^
  /D WIN32 /D NDEBUG /LD /MD /O2 ^
  /Fe:"%BUILD_DEPLOY%\deployed\%JDK_DEPLOY%\%PLATFORM%\profilerinterface.dll" ^
  /Fm:"%BUILD_DEPLOY%\deployed\%JDK_DEPLOY%\%PLATFORM%\profilerinterface.map" ^
  /link /DYNAMICBASE || ^
exit /b 1

del version.res