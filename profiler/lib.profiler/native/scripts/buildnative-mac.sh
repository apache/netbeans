#!/bin/bash

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

if [ -z "$JDK_HOME" ]; then
  jdk_home="$(/usr/libexec/java_home)"
else
  echo "JDK_HOME variable was set prior to invoking this script. It will be used."
  jdk_home="$JDK_HOME"
fi


echo "Value of jdk_home : $jdk_home"


CPPFLAGS="-I $jdk_home/include -I $jdk_home/include/darwin -I../build -DLINUX "
CFLAGS="$CPPFLAGS -mmacosx-version-min=10.4 -fpic -shared -O2"
      
SOURCES="../src-jdk15/class_file_cache.c \
	../src-jdk15/attach.c \
	../src-jdk15/Classes.c \
	../src-jdk15/HeapDump.c \
	../src-jdk15/Timers.c \
	../src-jdk15/GC.c \
	../src-jdk15/Threads.c \
	../src-jdk15/Stacks.c \
	../src-jdk15/common_functions.c"

DEST="../../release/lib/deployed/jdk16/mac"

mkdir -p $DEST

UNILIB="$DEST/libprofilerinterface.jnilib"

cc $CPPFLAGS ../src-jdk15/config.c -o ../build/config && ../build/config > ../build/config.h

echo "Content of config.h :"
cat ../build/config.h

# build universal libary for x86_64 and arm64
cc $CFLAGS -arch x86_64 -arch arm64 $SOURCES -o "$UNILIB"

# List content of existing universal library
lipo "$UNILIB" -info
