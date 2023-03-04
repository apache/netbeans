# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
SCRIPT_DIR=`pwd`
source init.sh

#Clean old tests results
if [ -n $WORKSPACE ]; then
    rm -rf $WORKSPACE/results
fi

cd  $NB_ALL

###################################################################
#
# Build all the components
#
###################################################################

mkdir -p nbbuild/netbeans

#Build source packages
ant ${CLUSTER_CONFIG:--Dcluster.config=full} -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -Dlocales=$LOCALES -f nbbuild/build.xml -Dmerge.dependent.modules=false build-source-config
ERROR_CODE=$?

create_test_result "build.source.package" "Build Source package" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot build all source package"
#    exit $ERROR_CODE;
else
    mv nbbuild/build/*-src-* $DIST/zip/$BASENAME-src.zip
fi

ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -Dlocales=$LOCALES -f nbbuild/build.xml -Dmerge.dependent.modules=false -Dcluster.config=platform build-source-config
ERROR_CODE=$?

create_test_result "build.source.platform" "Build Platform Source package" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot build basic platform source package"
#    exit $ERROR_CODE;
else
    mv nbbuild/build/*-src-* $DIST/zip/$BASENAME-platform-src.zip
fi

#Build the NB IDE first - no validation tests!
ant $CLUSTER_CONFIG -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -Dlocales=$LOCALES -f nbbuild/build.xml build-nozip -Dbuild.compiler.debuglevel=${DEBUGLEVEL}
ERROR_CODE=$?

create_test_result "build.IDE" "Build IDE" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot build IDE"
    exit $ERROR_CODE;
fi

###############  Commit validation tests  ##########################
cp -rp $NB_ALL/nbbuild/netbeans $NB_ALL/nbbuild/netbeans-vanilla

TESTS_STARTED=`date`
# Different JDK for tests because JVM crashes often (see 6598709, 6607038)
JDK_TESTS=$JDK_HOME
# standard NetBeans unit and UI validation tests
ant -v -f nbbuild/build.xml -Dlocales=$LOCALES -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER commit-validation
ERROR_CODE=$?

create_test_result "test.commit-validation" "Commit Validation" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Commit validation failed"
    #TEST_CODE=1;
fi

if [ -n $WORKSPACE ]; then
    cp -r $NB_ALL/nbbuild/build/test/results $WORKSPACE
fi

echo TESTS STARTED: $TESTS_STARTED
echo TESTS FINISHED: `date`
if [ "${TEST_CODE}" = 1 ]; then
    echo "ERROR: At least one of validation tests failed"
    exit 1;
fi

#Remove file created during commit validation
rm -rf $NB_ALL/nbbuild/netbeans/nb/servicetag
rm -rf $NB_ALL/nbbuild/netbeans/enterprise/config/GlassFishEE6

ant $CLUSTER_CONFIG -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -Dlocales=$LOCALES -f nbbuild/build.xml build-test-dist -Dtest.fail.on.error=false
ERROR_CODE=$?

create_test_result "build.test.dist" "Build Test Distribution" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Building of Test Distrubution failed"
    exit $ERROR_CODE;
else
    mv nbbuild/build/testdist.zip $DIST/zip/testdist-${BUILDNUMBER}.zip
fi

rm -rf $NB_ALL/nbbuild/netbeans
cp -rp $NB_ALL/nbbuild/netbeans-vanilla $NB_ALL/nbbuild/netbeans
cd $NB_ALL

#Build all NBMs for stable UC - IDE + UC-only
ant ${CLUSTER_CONFIG:--Dcluster.config=stableuc} -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -Dlocales=$LOCALES -f nbbuild/build.xml build-nonsigned-nbms -Dbase.nbm.target.dir=${DIST}/uc2 -Dkeystore=$KEYSTORE -Dstorepass=$STOREPASS -Dbuild.compiler.debuglevel=${DEBUGLEVEL}
ERROR_CODE=$?

create_test_result "build.NBMs" "Build all NBMs" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot build all stable UC NBMs"
    exit $ERROR_CODE;
fi

cd $NB_ALL

#Rebuild ODCS NBMs for stable UC with all available locales
ant ${CLUSTER_CONFIG:--Dcluster.config=odcs} -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -Dlocales=$LOCALES,de,es,fr,it,ko,zh_TW -f nbbuild/build.xml build-nonsigned-nbms -Dbase.nbm.target.dir=${DIST}/odcs -Dkeystore=$KEYSTORE -Dstorepass=$STOREPASS -Dbuild.compiler.debuglevel=${DEBUGLEVEL}
ERROR_CODE=$?

create_test_result "build.NBMs" "Build ODCS NBMs" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot build ODCS NBMs"
    exit $ERROR_CODE;
fi

mv ${DIST}/odcs/extra/org-netbeans-modules-odcs* ${DIST}/uc2/extra/
mv ${DIST}/odcs/extra/org-netbeans-modules-team-server* ${DIST}/uc2/extra/
rm -rf ${DIST}/odcs

rm -rf $NB_ALL/nbbuild/netbeans
mv $NB_ALL/nbbuild/netbeans-vanilla $NB_ALL/nbbuild/netbeans

cd $NB_ALL

# Separate IDE nbms from stableuc nbms.
ant $CLUSTER_CONFIG -f nbbuild/build.xml move-ide-nbms -Dnbms.source.location=${DIST}/uc2 -Dnbms.target.location=${DIST}/uc
ERROR_CODE=$?

create_test_result "get.ide.NBMs" "Extract IDE NBMs from all the built NBMs" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot extract IDE NBMs"
    exit $ERROR_CODE;
fi


#Build 110n kit for HG files
ant $CLUSTER_CONFIG -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -Dlocales=$LOCALES -f build.xml hg-l10n-kit -Dl10n.kit=${DIST}/zip/hg-l10n-$BUILDNUMBER.zip
ERROR_CODE=$?

create_test_result "build.hg.l10n" "Build 110n kit for HG files" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot build l10n kits for HG files"
#    exit $ERROR_CODE;
fi

#Build l10n kit for IDE modules
ant $CLUSTER_CONFIG -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -Dlocales=$LOCALES -f build.xml l10n-kit -Dnbms.location=${DIST}/uc -Dl10n.kit=${DIST}/zip/ide-l10n-$BUILDNUMBER.zip
ERROR_CODE=$?

create_test_result "build.modules.l10n" "Build l10n kit for IDE modules" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot build l10n kits for IDE modules"
#    exit $ERROR_CODE;
fi

#Build l10n kit for stable uc modules
ant $CLUSTER_CONFIG -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -Dlocales=$LOCALES -f build.xml l10n-kit -Dnbms.location=${DIST}/uc2 -Dl10n.kit=${DIST}/zip/stableuc-l10n-$BUILDNUMBER.zip
ERROR_CODE=$?

create_test_result "build.modules.l10n" "Build l10n kit for stable uc modules" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot build l10n kits for stable uc modules"
#    exit $ERROR_CODE;
fi

cd nbbuild
#Build catalog for IDE NBMs
ant $CLUSTER_CONFIG -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml generate-uc-catalog -Dnbms.location=${DIST}/uc -Dcatalog.file=${DIST}/uc/catalog.xml
ERROR_CODE=$?

create_test_result "build.ide.catalog" "Build UC catalog for IDE modules" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot build UC catalog for IDE module"
    exit $ERROR_CODE;
fi

#Build catalog for Stable UC NBMs
ant $CLUSTER_CONFIG -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml generate-uc-catalog -Dnbms.location=${DIST}/uc2 -Dcatalog.file=${DIST}/uc2/catalog.xml
ERROR_CODE=$?

create_test_result "build.stableuc.catalog" "Build UC catalog for stable UC modules" $ERROR_CODE
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot build UC catalog for stable UC modules"
    exit $ERROR_CODE;
fi
cd ..

cd $NB_ALL/nbbuild

if [ ! -z $UC_NBMS_DIR ]; then
   for UC_CLUSTER in $UC_EXTRA_CLUSTERS; do
      cp -r ${UC_NBMS_DIR}/${UC_CLUSTER} ${DIST}/uc
   done
fi

#Remove the build helper files
rm -f netbeans/nb.cluster.*
#rm -f netbeans/build_info
#rm -rf netbeans/extra
