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

rem A script to generate JNI *.h files for classes that contain native methods

SET BUILD_SRC_15=..\src-jdk15
SET BUILD_JDK=C:\PROGRA~1\java\jdk1.5.0_10

%BUILD_JDK%\bin\javah -d %BUILD_SRC_15% -classpath ..\..\src;..\..\src-jdk15 org.netbeans.lib.profiler.server.system.Classes org.netbeans.lib.profiler.server.system.HeapDump org.netbeans.lib.profiler.server.system.GC org.netbeans.lib.profiler.server.system.Timers org.netbeans.lib.profiler.server.system.Stacks org.netbeans.lib.profiler.server.system.Threads
