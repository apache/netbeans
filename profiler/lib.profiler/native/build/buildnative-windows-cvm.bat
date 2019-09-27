rem Licensed to the Apache Software Foundation (ASF) under one
rem or more contributor license agreements.  See the NOTICE file
rem distributed with this work for additional information
rem regarding copyright ownership.  The ASF licenses this file
rem to you under the Apache License, Version 2.0 (the
rem "License"); you may not use this file except in compliance
rem with the License.  You may obtain a copy of the License at
rem
rem   http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing,
rem software distributed under the License is distributed on an
rem "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
rem KIND, either express or implied.  See the License for the
rem specific language governing permissions and limitations
rem under the License.

SET BUILD_SRC_15=..\src-jdk15
SET BUILD_OUTPUT=dist
SET BUILD_DEPLOY=..\..\release\lib

set VS2005_COMMON_PATH=C:\Program Files\Microsoft Visual Studio 8\vc\bin

call "%VS2005_COMMON_PATH%\vcvars32"

mkdir %BUILD_OUTPUT%\deployed\cvm\windows

cl /I%CVM_HOME%/src/share/javavm/export /I%CVM_HOME%/src/share /I%CVM_HOME%/src ^
/I%CVM_HOME%/src/win32-x86 /I%CVM_HOME%/src/win32 ^
%BUILD_SRC_15%\class_file_cache.c ^
%BUILD_SRC_15%\attach.c ^
%BUILD_SRC_15%\Classes.c ^
%BUILD_SRC_15%\Timers.c ^
%BUILD_SRC_15%\GC.c ^
%BUILD_SRC_15%\Threads.c ^
%BUILD_SRC_15%\Stacks.c ^
%BUILD_SRC_15%\common_functions.c ^
/D WIN32 /D CVM /MD /Ox /c

link /DLL /MAP:%BUILD_OUTPUT%\deployed\cvm\windows\profilerinterface.map /OUT:%BUILD_OUTPUT%\deployed\cvm\windows\profilerinterface.dll ^
Classes.obj Timers.obj GC.obj Threads.obj Stacks.obj common_functions.obj class_file_cache.obj attach.obj
mt.exe -nologo -manifest %BUILD_OUTPUT%\deployed\cvm\windows\profilerinterface.dll.manifest -outputresource:%BUILD_OUTPUT%\deployed\cvm\windows\profilerinterface.dll;#2

cl /I%CVM_HOME%/src/share/javavm/export /I%CVM_HOME%/src/share /I%CVM_HOME%/src ^
/I%CVM_HOME%/src/win32-x86 /I%CVM_HOME%/src/win32 ^
%BUILD_SRC_15%\class_file_cache.c ^
%BUILD_SRC_15%\attach.c ^
%BUILD_SRC_15%\Classes.c ^
%BUILD_SRC_15%\Timers.c ^
%BUILD_SRC_15%\GC.c ^
%BUILD_SRC_15%\Threads.c ^
%BUILD_SRC_15%\Stacks.c ^
%BUILD_SRC_15%\common_functions.c ^
/D WIN32 /D CVM /MDd /Zi /c

link /DLL /DEBUG /MAP:%BUILD_OUTPUT%\deployed\cvm\windows\profilerinterface_g.map /OUT:%BUILD_OUTPUT%\deployed\cvm\windows\profilerinterface_g.dll ^
Classes.obj Timers.obj GC.obj Threads.obj Stacks.obj common_functions.obj class_file_cache.obj attach.obj
mt.exe -nologo -manifest %BUILD_OUTPUT%\deployed\cvm\windows\profilerinterface_g.dll.manifest -outputresource:%BUILD_OUTPUT%\deployed\cvm\windows\profilerinterface_g.dll;#2

del vc60.pdb
del *.obj
del %BUILD_OUTPUT%\deployed\cvm\windows\*.lib %BUILD_OUTPUT%\deployed\cvm\windows\*.exp %BUILD_OUTPUT%\deployed\cvm\windows\*.ilk %BUILD_OUTPUT%\deployed\cvm\windows\*.pdb

copy %BUILD_OUTPUT%\deployed\cvm\windows\*.dll %BUILD_DEPLOY%\deployed\cvm\windows
copy %BUILD_OUTPUT%\deployed\cvm\windows\*.map %BUILD_DEPLOY%\deployed\cvm\windows

