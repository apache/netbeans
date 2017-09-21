#!/usr/bin/bash

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

export PROC=`uname -p`
echo PROC is $PROC

if [ $PROC = "i386" ]; then 
  export PROC_FLAGS="-xregs=no%frameptr"
elif [ $PROC = "sparc" ]; then
  export PROC_FLAGS="-xregs=no%appl -xmemalign=4s -xarch=v8"
else 
  echo "Invalid architecture " $PROC
fi
mkdir -p ../../dist/deployed/cvm/solaris

CC_FLAGS="-I$CVM_HOME/src/share/javavm/export -I$CVM_HOME/src/share \
-I$CVM_HOME/src/solaris -I$CVM_HOME/src -I$CVM_HOME/src/solaris-x86 \
-DSOLARIS -G -lrt -xO2 -v -mt -xc99=%none -xCC -Xa -xstrconst"
 
cc $CC_FLAGS $PROC_FLAGS \
-o ../../dist/deployed/cvm/solaris-$PROC/libprofilerinterface.so \
../src-jdk15/class_file_cache.c \
../src-jdk15/attach.c \
../src-jdk15/Classes.c \
../src-jdk15/Timers.c \
../src-jdk15/GC.c \
../src-jdk15/Threads.c \
../src-jdk15/Stacks.c \
../src-jdk15/common_functions.c

cc $CC_FLAGS -g $PROC_FLAGS \
-o ../../dist/deployed/cvm/solaris-$PROC/libprofilerinterface_g.so \
../src-jdk15/class_file_cache.c \
../src-jdk15/attach.c \
../src-jdk15/Classes.c \
../src-jdk15/Timers.c \
../src-jdk15/GC.c \
../src-jdk15/Threads.c \
../src-jdk15/Stacks.c \
../src-jdk15/common_functions.c


cc $CC_FLAGS $PROC_FLAGS \
-o ../../dist/deployed/cvm/solaris-$PROC/libclient.so \
../src/ProfilerClient.c

cc $CC_FLAGS -g $PROC_FLAGS \
-o ../../dist/deployed/cvm/solaris-$PROC/libclient_g.so \
../src/ProfilerClient.c

rm -f *.o
