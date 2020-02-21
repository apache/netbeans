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
PRODUCT_VERSION=$4

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

perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.analysis.api/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.analysis.impl/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.antlr/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.api.model/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.api.project/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.api.remote/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.api.remote.ui/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.apt/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.asm/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.callgraph/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.classview/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.cncppunit/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.completion/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.debugger.common2/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.debugger.gdb2/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.discovery/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.dwarfdiscovery/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.dwarfdump/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.editor/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.folding/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.gizmo/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.gotodeclaration/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.highlight/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.indexing/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.kit/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.lexer/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.makeproject/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.makeproject.source.bridge/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.makeproject.ui/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.model.services/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.modeldiscovery/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.mixeddev/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.model.jclank.bridge/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.modelimpl/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.modelui/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.modelutil/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.navigation/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.qnavigator/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.refactoring/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.remote/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.remote.projectui/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.remote.ui/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.repository/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.repository.api/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.script/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.search/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.simpleunit/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.source/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.spellchecker.bindings/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.testrunner/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.toolchain/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.toolchain.ui/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.ui/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.utils/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/cnd.utils.ui/src | tee ${LOG}

perl nbbuild/misc/bundlecheck.pl -q `pwd`/dlight/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/dlight.annotationsupport/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/dlight.core.stack/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/dlight.db.derby/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/dlight.db.h2/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/dlight.kit/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/dlight.libs.common/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/dlight.libs.h2/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/dlight.nativeexecution/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/dlight.remote/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/dlight.remote.impl/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/dlight.terminal/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/dlight.toolsui/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/dlight.util/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/dlight.visualizers/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/lib.terminalemulator/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/terminal/src | tee ${LOG}

perl nbbuild/misc/bundlecheck.pl -q `pwd`/remotefs.versioning/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/remotefs.versioning.api/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/subversion.remote/src | tee ${LOG}
perl nbbuild/misc/bundlecheck.pl -q `pwd`/mercurial.remote/src | tee ${LOG}

cnt=`cat ${LOG} | wc -l`
if [ ${cnt} -gt 0 ]; then
	echo "bundle check FAILED"
	if [ -n "${MAILTO}" ]; then
		mailx -s "bundle check FAILED - ${PRODUCT_VERSION}" -r "${MAILTO}" "${MAILTO}" < ${LOG}
	fi
else
	echo "bundle check SUCCEEDED - no warnings - ${PRODUCT_VERSION}"
fi
