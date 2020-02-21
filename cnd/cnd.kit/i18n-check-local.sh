#!/bin/sh -x
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

WORKSPACE=$1
LOG=$2
MAILTO=$3

WORKSPACE=${WORKSPACE:-..};
LOG=${LOG:-${WORKSPACE}/i18n-check.log};


# check workspace  existence and access
if [ ! -d ${WORKSPACE} ]; then
	echo "Error: ${WORKSPACE} is not a directory"
	exit 4
fi
if [ ! -r ${WORKSPACE} ]; then
	echo "Error: can not read ${WORKSPACE}"
	exit 8
fi

#WORKSPACE=`(cd ${WORKSPACE}; pwd)`

#echo WORKSPACE=$WORKSPACE
#echo LOG=$LOG

cd ${WORKSPACE}

perl nbbuild/misc/i18ncheck.pl `pwd`/cnd* `pwd`/asm* `pwd`/dlight* `pwd`/remotefs* `pwd`/mercurial.remote/src `pwd`/lib.terminalemulator/src `pwd`/terminal\
 | grep -v "/versioning/core/" | grep -v "/test/" | grep -v "cnd.antlr/" | grep -v "generated/"\
 | grep -v "Catalog.get" | grep -v "Catalog.format"\
 | grep -v "parser/FortranLexicalPrepass.java" | grep -v "parser/FortranTokenStream.java" | tee ${LOG}
cnt=`cat ${LOG} | wc -l`
if [ ${cnt} -gt 0 ]; then
	echo "I18n check FAILED"
	if [ -n "${MAILTO}" ]; then
		mailx -s "I18n check FAILED" -r "${MAILTO}" "${MAILTO}" < ${LOG}
	fi
else
	echo "I18n check SUCCEEDED - no warnings"
fi
