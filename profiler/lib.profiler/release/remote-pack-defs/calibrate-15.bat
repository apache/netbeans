@rem Licensed to the Apache Software Foundation (ASF) under one
@rem or more contributor license agreements.  See the NOTICE file
@rem distributed with this work for additional information
@rem regarding copyright ownership.  The ASF licenses this file
@rem to you under the Apache License, Version 2.0 (the
@rem "License"); you may not use this file except in compliance
@rem with the License.  You may obtain a copy of the License at
@rem
@rem   http://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing,
@rem software distributed under the License is distributed on an
@rem "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@rem KIND, either express or implied.  See the License for the
@rem specific language governing permissions and limitations
@rem under the License.

@echo off

REM This script expects JAVA_HOME to point to the correct JDK 5.0 installation
REM A JDK 5.0 update 4 (JDK 1.5_04) or newer is needed for the profiler to work correctly
REM In case you need to customize it, please uncomment and modify the following line
REM set JAVA_HOME=C:\Software\jdk15_04

"%JAVA_HOME%\bin\java.exe" -javaagent:"%~dp0\..\lib\jfluid-server-15.jar" org.netbeans.lib.profiler.server.ProfilerCalibrator
