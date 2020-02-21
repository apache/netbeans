#!/bin/bash

# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
#
# Oracle and Java are registered trademarks of Oracle and/or its affiliates.
# Other names may be trademarks of their respective owners.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common
# Development and Distribution License("CDDL") (collectively, the
# "License"). You may not use this file except in compliance with the
# License. You can obtain a copy of the License at
# http://www.netbeans.org/cddl-gplv2.html
# or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
# specific language governing permissions and limitations under the
# License.  When distributing the software, include this License Header
# Notice in each file and include the License file at
# nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
# particular file as subject to the "Classpath" exception as provided
# by Oracle in the GPL Version 2 section of the License file that
# accompanied this code. If applicable, add the following below the
# License Header, with the fields enclosed by brackets [] replaced by
# your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# Contributor(s):
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
# Microsystems, Inc. All Rights Reserved.
#
# If you wish your version of this file to be governed by only the CDDL
# or only the GPL Version 2, indicate your decision by adding
# "[Contributor] elects to include this software in this distribution
# under the [CDDL or GPL Version 2] license." If you do not indicate a
# single choice of license, a recipient has the option to distribute
# your version of this file under either the CDDL, the GPL Version 2 or
# to extend the choice of license to its licensees as provided above.
# However, if you add GPL Version 2 code and therefore, elected the GPL
# Version 2 license, then the option applies only if the new code is
# made subject to such option by the copyright holder.

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
