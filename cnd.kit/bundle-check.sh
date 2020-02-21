#!/bin/sh -x
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
#
# Contributor(s):

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
