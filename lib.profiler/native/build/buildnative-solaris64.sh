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

BuildForJDK()
{
        JAVA_HOME=$1
        JDK_ID=$2
        echo $JAVA_HOME $JDK_ID
	CC_FLAGS="-I$JAVA_HOME/include -I$JAVA_HOME/include/solaris -DSOLARIS -G -lrt \
	-xO2 -v -mt -Kpic -xCC -Xa -xstrconst -errwarn=%all"

	cc $CC_FLAGS $PROC_FLAGS \
	-o ../../release/lib/deployed/$JDK_ID/solaris-$PROC/libprofilerinterface.so \
	../src-jdk15/class_file_cache.c \
	../src-jdk15/attach.c \
	../src-jdk15/Classes.c \
	../src-jdk15/HeapDump.c \
	../src-jdk15/Timers.c \
	../src-jdk15/GC.c \
	../src-jdk15/Threads.c \
	../src-jdk15/Stacks.c \
	../src-jdk15/common_functions.c

	rm -f *.o
}

export PROC=`uname -p`
echo PROC is $PROC

if [ $PROC = "i386" ]; then 
  export PROC_FLAGS="-xregs=no%frameptr"
  PROC="amd64"
elif [ $PROC = "sparc" ]; then
  export PROC_FLAGS="-xregs=no%appl -xarch=v9"
  PROC="sparcv9"
else 
  echo "Invalid architecture " $PROC
fi

BuildForJDK "$JAVA_HOME_15" "jdk15"
BuildForJDK "$JAVA_HOME_16" "jdk16"

