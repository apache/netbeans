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
