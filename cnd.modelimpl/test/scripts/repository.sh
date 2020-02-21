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




