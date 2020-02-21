#!/bin/sh
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

# This script downloads the latest CND incremental build and runs its tests.
# Any argument(s) given to this script will be passed to the JVM
# executing unit tests.
#
# Important environment variables:
#
# WORKSPACE (required, set by Hudson)
#   Full path to directory where all files will be stored.
#   Note that all existing files from WORKSPACE are deleted first.
# GET (optional)
#   If set to something non-empty, then download and unpack build artifacts
#   from Hudson first. If empty, then just run the tests without downloading
#   anything.
# HUDSON_URL (optional, set by Hudson)
#   URL of hudson instance that produces netbeans.zip and testdist.zip
#   Default value is http://elif:8080/hudson/
# UPSTREAM_NO (optional, set by Hudson)
#   Build number to download and test.
#   If not specified, then last successful build is used.
# EXECUTOR_NUMBER (optional, set by Hudson)
#   Hudson executor number. Useful if several executors are running the
#   tests in parallel.
# MODULES (optional)
#   Colon-separated list of NB modules to run unit tests for.
#   By default includes all modules of DLight, CND, internal terminal,
#   and native execution.
# ANT (optional)
#   Path to ant executable. By default "ant".
# STABLE_ONLY (optional)
#   if false then also run tests marked as "RandomlyFails"
# UNSTABLE_ONLY (optional)
#   if true then run only tests marked as "RandomlyFails"

if [ -z "${WORKSPACE}" ]; then
    echo "WORKSPACE is not set!"
    echo "Beware: if GET is set, this script will remove everything in WORKSPACE first!"
    exit
fi

BASE_REPO=${BASE_REPO:-cnd-build}

if [ -n "${GET}" ]; then
    cd "${WORKSPACE}" && rm -rf *

    HUDSON_URL=${HUDSON_URL:-http://elif:8080/hudson/}
    BUILD_NUM=${UPSTREAM_NO:-`wget -qO - ${HUDSON_URL}job/${BASE_REPO}/lastSuccessfulBuild/buildNumber`}
    wget -q "${HUDSON_URL}job/${BASE_REPO}/${BUILD_NUM}/artifact/netbeans.zip"
    wget -q "${HUDSON_URL}job/${BASE_REPO}/${BUILD_NUM}/artifact/testdist.zip"
    unzip -qo netbeans.zip
    unzip -qo testdist.zip
fi

STABLE_ONLY=${STABLE_ONLY:-true}
UNSTABLE_ONLY=${UNSTABLE_ONLY:-false}
DISABLE_WATCHER=${DISABLE_WATCHER:-false}

cd "${WORKSPACE}/unit"
MODULES=${MODULES:-`ls -d dlight/* cnd/* ide/*terminal* ide/*nativeex* | paste -s -d : -`}
cd "${WORKSPACE}"

RUNSTR="${ANT:-ant} -f ${WORKSPACE}/all-tests.xml \
-Dbasedir=${WORKSPACE}/unit \
-Dnetbeans.dest.dir=${WORKSPACE}/netbeans \
-Dmodules.list=${MODULES} \
-Dtest.disable.fails=true \
-Dtest.dist.timeout=1000000 \
-Dtest.run.args=\"-ea -XX:PermSize=32m -XX:MaxPermSize=200m -Xmx512m \
-Dnetbeans.keyring.no.master=true \
-Dorg.netbeans.modules.masterfs.watcher.disable=${DISABLE_WATCHER} \
-Djava.io.tmpdir=/var/tmp/hudson${EXECUTOR_NUMBER} \
-Dcnd.remote.sync.root.postfix=hudson${EXECUTOR_NUMBER} \
-Dignore.random.failures=${STABLE_ONLY} \
-Drandom.failures.only=${UNSTABLE_ONLY} \
$*\""

if [ -z "${GENERATE_BAT}" ]; then
    ${RUNSTR}
else
    echo "${RUNSTR}" > start.bat
fi
