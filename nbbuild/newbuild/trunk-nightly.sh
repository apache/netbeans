#!/bin/bash

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

#Initialize basic structure
DIRNAME=`dirname $0`
cd ${DIRNAME}
TRUNK_NIGHTLY_DIRNAME=`pwd`

if [ -z ${BUILD_DESC} ]; then
    export BUILD_DESC=trunk-nightly
fi

source init.sh

rm -rf $DIST

if [ ! -z $WORKSPACE ]; then
    #I'm under hudson and have sources here, I need to clone them
    #Clean obsolete sources first
    rm -rf $NB_ALL
    hg clone -U $WORKSPACE $NB_ALL
    hg -R $NB_ALL update $NB_BRANCH
fi
TIP=`hg tip --template '{rev}'`
export TIP

cd $NB_ALL
# clone and update l10n - hg clone -r $L10N_BRANCH $ML_REPO $NB_ALL/l10n

hg clone -U $ML_REPO $NB_ALL/l10n
HG_ERROR_CODE=$?
if [ $HG_ERROR_CODE != 0 ]; then
    echo "ERROR: $HG_ERROR_CODE - hg clone l10n failed"
    exit $HG_ERROR_CODE;
fi

hg -R $NB_ALL/l10n update $L10N_BRANCH

# clone and update otherlicenses - hg clone -r $OTHER_LICENCES_BRANCH $OTHER_LICENCES_REPO $NB_ALL/otherlicenses
hg clone -U $OTHER_LICENCES_REPO $NB_ALL/otherlicenses
HG_ERROR_CODE=$?
if [ $HG_ERROR_CODE != 0 ]; then
    echo "ERROR: $HG_ERROR_CODE - hg clone otherlicenses failed"
    exit $HG_ERROR_CODE;
fi

hg -R $NB_ALL/otherlicenses update $OTHER_LICENCES_BRANCH

###################################################################
#
# Build all the components
#
###################################################################

cd $TRUNK_NIGHTLY_DIRNAME
bash build-all-components.sh
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Build failed"
    exit $ERROR_CODE;
fi

###################################################################
#
# Pack all the components
#
###################################################################

cd $TRUNK_NIGHTLY_DIRNAME
bash pack-all-components.sh
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Packaging failed"
    exit $ERROR_CODE;
fi

###################################################################
#
# Deploy bits to the storage server
#
###################################################################

if [ -n $BUILD_ID ]; then
    mkdir -p $DIST_SERVER2/${BUILD_ID}/zip
    cp -rp $DIST/*  $DIST_SERVER2/${BUILD_ID}
    if [ -n "${TESTING_SCRIPT}" ]; then
        cd $NB_ALL
        TIP_REV=`hg tip --template "{node}"`
        ssh $TESTING_SCRIPT $TIP_REV
        cd $DIRNAME
    fi
fi

if [ $UPLOAD_ML == 1 ]; then
    cp $DIST/zip/$BASENAME-platform-src.zip $DIST/ml/zip/
    cp $DIST/zip/$BASENAME-src.zip $DIST/ml/zip/
    cp $DIST/zip/hg-l10n-$BUILDNUMBER.zip $DIST/ml/zip/
    cp $DIST/zip/ide-l10n-$BUILDNUMBER.zip $DIST/ml/zip/
    cp $DIST/zip/stable-UC-l10n-$BUILDNUMBER.zip $DIST/ml/zip/
    cp $DIST/zip/testdist-$BUILDNUMBER.zip $DIST/ml/zip/
fi

cd $TRUNK_NIGHTLY_DIRNAME

bash build-nbi.sh
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - NBI installers build failed"
    exit $ERROR_CODE;
fi

if [ -n $BUILD_ID ]; then
    mkdir -p $DIST_SERVER2/${BUILD_ID}/zip
    cp -rp $DIST/*  $DIST_SERVER2/${BUILD_ID}
    rm $DIST_SERVER2/latest.old
    mv $DIST_SERVER2/latest $DIST_SERVER2/latest.old
    ln -s $DIST_SERVER2/${BUILD_ID} $DIST_SERVER2/latest
    if [ $UPLOAD_ML == 0 -a $ML_BUILD != 0 ]; then
        rm -r $DIST/ml
    fi
fi

if [ -z $DIST_SERVER ]; then
    exit 0;
fi
