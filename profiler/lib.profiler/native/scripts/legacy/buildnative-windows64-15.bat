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

SET BUILD_SRC_15=..\src-jdk15
SET BUILD_SRC=..\src
SET BUILD_JDK=C:\PROGRA~1\java\jdk1.5.0_15
SET BUILD_OUTPUT=%TEMP%\dist
SET BUILD_DEPLOY=..\..\release\lib

mkdir %BUILD_OUTPUT%\deployed\jdk15\windows-amd64

cl /I%BUILD_JDK%\include /I%BUILD_JDK%\include\win32 ^
%BUILD_SRC_15%\class_file_cache.c ^
%BUILD_SRC_15%\attach.c ^
%BUILD_SRC_15%\Classes.c ^
%BUILD_SRC_15%\HeapDump.c ^
%BUILD_SRC_15%\Timers.c ^
%BUILD_SRC_15%\GC.c ^
%BUILD_SRC_15%\Threads.c ^
%BUILD_SRC_15%\Stacks.c ^
%BUILD_SRC_15%\common_functions.c ^
/D WIN32 /MD /Ox /c

rc /fo version.res %BUILD_SRC_15%\windows\version.rc

link /DLL /MAP:%BUILD_OUTPUT%\deployed\jdk15\windows-amd64\profilerinterface.map /OUT:%BUILD_OUTPUT%\deployed\jdk15\windows-amd64\profilerinterface.dll ^
Classes.obj HeapDump.obj Timers.obj GC.obj Threads.obj Stacks.obj common_functions.obj class_file_cache.obj attach.obj version.res

del vc60.pdb
del *.obj *.res
del %BUILD_OUTPUT%\deployed\jdk15\windows-amd64\*.lib %BUILD_OUTPUT%\deployed\jdk15\windows-amd64\*.exp %BUILD_OUTPUT%\deployed\jdk15\windows-amd64\*.ilk %BUILD_OUTPUT%\deployed\jdk15\windows-amd64\*.pdb

copy %BUILD_OUTPUT%\deployed\jdk15\windows-amd64\*.dll %BUILD_DEPLOY%\deployed\jdk15\windows-amd64
copy %BUILD_OUTPUT%\deployed\jdk15\windows-amd64\*.map %BUILD_DEPLOY%\deployed\jdk15\windows-amd64
