@rem Licensed to the Apache Software Foundation (ASF) under one
@rem or more contributor license agreements.  See the NOTICE file
@rem distributed with this work for additional information
@rem regarding copyright ownership.  The ASF licenses this file
@rem to you under the Apache License, Version 2.0 (the
@rem "License"); you may not use this file except in compliance
@rem with the License.  You may obtain a copy of the License at
@rem
@rem   https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing,
@rem software distributed under the License is distributed on an
@rem "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@rem KIND, either express or implied.  See the License for the
@rem specific language governing permissions and limitations
@rem under the License.

@echo off

REM This script expects CVM_HOME to point to the correct CVM installation
REM In case you need to customize it, please uncomment and modify the following line
REM set CVM_HOME=C:\Software\CVM

"%CVM_HOME%\bin\cvm.exe" -agentpath:"%~dp0\..\lib\deployed\cvm\windows\profilerinterface.dll"="\"%~dp0\..\lib\"",5140 %*
