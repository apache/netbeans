# -*- Makefile -*-
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

JDK_HOME = c:\jdk1.3.1_03

INCLUDES = -I. -I$(JDK_HOME)/include -I$(JDK_HOME)/include/win32

SRC_DIR = ../src

all: jtrace.dll

jtrace.dll: $(SRC_DIR)/jtrace.cpp
	cl -LD -MD -DWIN32_LEAN_AND_MEAN $(INCLUDES) $(SRC_DIR)/jtrace.cpp -o $@

clean:
	del *.dll *.obj
