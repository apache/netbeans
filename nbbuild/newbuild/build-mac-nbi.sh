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

if [ ! -z $TIP ] ; then
    echo Update to $TIP
    hg up --rev $TIP
fi

DIRNAME=`dirname $0`
cd ${DIRNAME}
SCRIPTS_DIR=`pwd`
source init.sh

if [ -z $BUILD_NBJDK7 ]; then
    BUILD_NBJDK7=0
fi

if [ -z "$SIGNING_IDENTITY" ]; then
    SIGNING_IDENTITY=0
fi

if [ ! -z $SIGNING_PASSWORD ] ; then
    security unlock-keychain -p $SIGNING_PASSWORD
fi

   if [ 1 -eq $ML_BUILD ] ; then
       cd $NB_ALL/l10n
       tar c src/*/other/installer/mac/* | ( cd $NB_ALL; tar x )
       cd $NB_ALL
   fi


# Run new builds
sh $NB_ALL/installer/mac/newbuild/init.sh
sh $NB_ALL/installer/mac/newbuild/build.sh $MAC_PATH $BASENAME_PREFIX $BUILDNUMBER $BUILD_NBJDK7 "$SIGNING_IDENTITY" $LOCALES
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - NBI installers build failed"
    exit $ERROR_CODE;
fi

mkdir -p $DIST/bundles
cp -r $WORKSPACE/installer/mac/newbuild/dist_en/* $DIST/bundles
ERROR_CODE=$?
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Cannot copy installers"
    exit $ERROR_CODE;
fi

cd $DIST

bash ${SCRIPTS_DIR}/files-info.sh bundles bundles/jdk
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Counting of MD5 sums and size failed"
fi

if [ ! -z $SIGNING_PASSWORD ] ; then
    security lock-keychain
fi
