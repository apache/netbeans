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

USETS=
LOGPATH=${LOGPATH-/tmp}
TEST_PROJECT=
DOTIME=time

while [ -n "$1" ]
    do case "$1" in
        -ignorets)
		USETS=".1"
		;;
        -log)
		shift
		LOGPATH="$1"		
		;;
	-notime)
		DOTIME=
		;;
	*)
		TEST_PROJECT="${TEST_PROJECT} $1"
		;;
    esac
    shift
done

test ! -d ${LOGPATH} && mkdir -p ${LOGPATH}

PROJECT_TEMP_FILE=${LOGPATH}/repositorytest
REPOSITORY_CACHE_PATH=${LOGPATH}/repositorytest-cache/
echo Testing on project: ${TEST_PROJECT}
echo Logs stored to: ${PROJECT_TEMP_FILE}
echo "Running without repository..."
echo "./_parse_project.sh ${TEST_PROJECT} -fq  -J-Dcnd.repository.hardrefs=true  1> ${PROJECT_TEMP_FILE}.original.out 2>${PROJECT_TEMP_FILE}.original.err" > ${LOGPATH}/run.sh; chmod a+x ${LOGPATH}/run.sh
${DOTIME} ${LOGPATH}/run.sh
echo
echo "Running with repository..."
echo "./_parse_project.sh ${TEST_PROJECT} -fq --cl${PROJECT_TEMP_FILE}.original.erreanrepository -J-Dcnd.repository.cache.path=${REPOSITORY_CACHE_PATH} 1> ${PROJECT_TEMP_FILE}.repository.out 2>${PROJECT_TEMP_FILE}.repository.err" > ${LOGPATH}/run.sh; chmod a+x ${LOGPATH}/run.sh
${DOTIME} ${LOGPATH}/run.sh

if [ -n "${USETS}" ]; then
#rough dated logs filter, only for stderr
DAYEAR=`date +%Y`
cat ${PROJECT_TEMP_FILE}.original.err | grep -v ${DAYEAR} > ${PROJECT_TEMP_FILE}.original.err${USETS}
cat ${PROJECT_TEMP_FILE}.repository.err | grep -v ${DAYEAR} > ${PROJECT_TEMP_FILE}.repository.err${USETS}
fi

diff ${PROJECT_TEMP_FILE}.original.out ${PROJECT_TEMP_FILE}.repository.out > ${PROJECT_TEMP_FILE}.diff.out
diff ${PROJECT_TEMP_FILE}.original.err${USETS} ${PROJECT_TEMP_FILE}.repository.err${USETS} > ${PROJECT_TEMP_FILE}.diff.err
echo 

echo "Lines in diff file:"
cat ${PROJECT_TEMP_FILE}.diff.out ${PROJECT_TEMP_FILE}.diff.err  | wc -l

if [ -s ${PROJECT_TEMP_FILE}.diff.out -o -s ${PROJECT_TEMP_FILE}.diff.err ]; then
    reptestresult="failed"
    echo To see detailed results do
    echo "echo \"*** OUT ***\"; cat ${PROJECT_TEMP_FILE}.diff.out; echo \"*** ERR ***\"; cat ${PROJECT_TEMP_FILE}.diff.err"
    echo
else
    reptestresult="passed"
fi

echo Repository correctness test: ${reptestresult}




