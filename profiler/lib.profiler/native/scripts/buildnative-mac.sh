#!/bin/bash

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

DEST="../../release/lib/deployed/jdk16/mac/"

UNILIB="$DEST/libprofilerinterface.jnilib"

if [ ! -f "$UNILIB" ]; then
  echo "Error: This script expects Universal Library $UNILIB to exist."
  exit 1
fi


cc $CPPFLAGS ../src-jdk15/config.c -o ../build/config && ../build/config > ../build/config.h

echo "Content of config.h :"
cat ../build/config.h

cc $CFLAGS $SOURCES -o ../build/libprofilerinterface.x86_64.dylib


# Now we have a library specific to 'x86_64'. However what we use in lib.profiler
# is a so-called Universal Library (one library file which contains embedded libraries for
# multiple architectures .. this is an Apple speciality .. also known as a 'fat library').
#
# At some point in the history of NetBeans someone has created a Universal Library file
# for the Profiler Interface which as of Jan 2021 contains libraries for the following platforms: 
#    ppc 
#    ppc64 
#    i386 
#    x86_64
# Nowadays, Jan 2021, only 'x86_64' is relevant. Apple has abandonned the PowerPC architecture.
# Coming up is the Apple Sillicon architecture which is named 'arm64' in short form. No attempt
# is made here to support that, yet. 
#
# We re-use the existing Universal Library file and simply replace its contents but only
# for the 'x86_64' architecture. This means that libraries for other architectures will
# continue to exist in the bundle, albeit in the older form (meaning they'll not receive bug fixes)
#



# List architecture of the produced ".dylib" file  
# (just to be sure .. we expect it to be 'x86_64')
lipo ../build/libprofilerinterface.x86_64.dylib -info

# List content of existing universal library
lipo "$UNILIB" -info

# Update the existing universal library so that the content for x86_64 is replaced
# with our recent build
lipo "$UNILIB" -replace x86_64 ../build/libprofilerinterface.x86_64.dylib -output "$UNILIB"
