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

DIR=${REGRESSION_SRC}
FILES=`(cd ${DIR}; ls *.ppp *.cc *.cpp *.c)`
#FILES=`(cd ${DIR}; ls *.cc)`
TEMP="/tmp/uniq"

rm -rf ${TEMP} > /dev/null
mkdir -p ${TEMP} > /dev/null

failures="${TEMP}/__failures"
rm -rf ${failures} > /dev/null

for F in ${FILES}; do 
 	file_std="${TEMP}/${F}.dat"
 	file_err="${TEMP}/${F}.err"
	./tracemodel.sh ${DIR}/${F} -fu >  ${file_std} 2>${file_err}
	err=`cat ${file_err}`
	if [ -z "${err}" ]; then
		rm ${file_err}
	fi
	cnt=`grep "Unique name check failed" ${file_std} | wc -l`
	text="${F} ${cnt}"
	echo ${text}
	if [ ${cnt} -gt 0 ]; then
		echo ${text} >> "${failures}"
	fi
done

echo "FAILURES:"
cat ${failures}
