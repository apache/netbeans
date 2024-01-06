#!/bin/sh

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

JDK_ID=jdk16
CPPFLAGS="-I$JDK_HOME/include -I$JDK_HOME/include/linux -I../build -DLINUX"
CFLAGS="$CPPFLAGS -pthread -fPIC -shared -O3 -Wall -m32"
SOURCES="../src-jdk15/class_file_cache.c \
	../src-jdk15/attach.c \
	../src-jdk15/Classes.c \
	../src-jdk15/HeapDump.c \
	../src-jdk15/Timers.c \
	../src-jdk15/GC.c \
	../src-jdk15/Threads.c \
	../src-jdk15/Stacks.c \
	../src-jdk15/common_functions.c"
DEST="../../release/lib/deployed/jdk16/linux/"

mkdir -p $DEST

cc $CPPFLAGS -m32 -o ../build/config ../src-jdk15/config.c && ../build/config > ../build/config.h

echo "Content of config.h :"
cat ../build/config.h

cc $CFLAGS -o $DEST/libprofilerinterface.so \
   $SOURCES
