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

function run() {

	project="$1"
	shift
	params="$@"
	
	###### System includes
	
	sys_incl=`g++ -E -v -x c++ /dev/null  2>&1 | awk '\
		BEGIN { cnt=0; inside = 0; } \
		/#include "..." search starts here:/ { inside = 1; } \
		/#include <...> search starts here:/ { inside = 1; } \
		/End of search list/ { inside = 0; } \
		/^[^#].*/ { if( inside ) print "-I" $1 }'`
	
	
	uname=`uname`
	#uname_prefix=`expr substr "${uname}" 1 6`
	uname_prefix=${uname:0:6}
	if [ "${uname_prefix}" = "CYGWIN" ]; then
		sys_incl=""
		sys_incl="${sys_incl} -IC:/cygwin/lib/gcc/i686-pc-cygwin/3.4.4/include/c++"
		sys_incl="${sys_incl} -IC:/cygwin/lib/gcc/i686-pc-cygwin/3.4.4/include/c++/i686-pc-cygwin"
		sys_incl="${sys_incl} -IC:/cygwin/lib/gcc/i686-pc-cygwin/3.4.4/include/c++/backward"
		sys_incl="${sys_incl} -IC:/cygwin/lib/gcc/i686-pc-cygwin/3.4.4/include"
		sys_incl="${sys_incl} -IC:/cygwin/usr/include"
		sys_incl="${sys_incl} -IC:/cygwin/lib/gcc/i686-pc-cygwin/3.4.4/../../../../include/w32api"
	fi
	
	###### Source files
	
	dir="${project}"
	files=`find ${dir} -name "*.c" -o -name "*.cc" -o -name "*.cpp" -o -name "*.C"`
	
	###### Options
	
	options="-I${dir} -I${dir}/src -I${dir}/include -I${dir}/test -DHAVE_CONFIG_H"
	
	defs=""
	#defs="${defs} -Dparser.report.include.failures=true"
	#defs="${defs} -Dparser.report.errors=false"
	defs="${defs} -J-Dcnd.modelimpl.parser.threads=1"
	
	jvmopts=${JVMOPTS-"-J-Xms512m -J-Xmx512m -J-XX:PermSize=128m -J-XX:MaxPermSize=256m -J-XX:NewSize=256m"}
	
	###### Go!
	
	TRACEMODEL_SH=${TRACEMODEL_SH-"../../tracemodel.sh"}
	
	if [ ! -r ${TRACEMODEL_SH} ]; then
		echo "Can not find file tracemodel.sh."
		echo "Set TRACEMODEL_SH variable to point to this script."
		return
	fi
	
	#set -x
	bash ${TRACEMODEL_SH} ${files} ${sys_incl} ${options} ${defs} ${jvmopts} ${params}
}

run $@
