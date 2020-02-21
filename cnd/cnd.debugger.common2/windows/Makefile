#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

#
# The cnd.dll comes premade in a C/C++ pack Windows distribution. This
# Makefile and the sources are included only for users interested in
# rebuilding the dll or for users wondering what is included in the
# dll.
#
# The cnd.dll includes a signal handler which is needed to ensure we ca
# sucessfully interupt the inferior process in a debugging session.

all: GdbKillProc

GdbKillProc: GdbKillProc.o
	gcc -O -o GdbKillProc GdbKillProc.o -mno-cygwin -mthreads

GdbKillProc.o: GdbKillProc.c
	gcc -c -O GdbKillProc.c -mno-cygwin -mthreads

clean:
	rm -f GdbKillProc.exe GdbKillProc.o

