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

DIRNAME=`dirname $0`
cd ${DIRNAME}
SCRIPTS_DIR=`pwd`
source init.sh

if [ -z $BUILD_NBJDK7 ]; then
    BUILD_NBJDK7=0
fi

if [ -z $BUILD_NBJDK8 ]; then
    BUILD_NBJDK8=0
fi

if [ -z $BUILD_NBJDK11 ]; then
    BUILD_NBJDK11=0
fi

OUTPUT_DIR="$DIST/installers"
export OUTPUT_DIR

if [ ! -z $NATIVE_MAC_MACHINE ] && [ ! -z $MAC_PATH ]; then
   ssh $NATIVE_MAC_MACHINE rm -rf $MAC_PATH/installer
   ERROR_CODE=$?
   if [ $ERROR_CODE != 0 ]; then
       echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't remove old scripts"
       exit $ERROR_CODE;
   fi
   ssh $NATIVE_MAC_MACHINE mkdir -p $MAC_PATH/installer
   cd $NB_ALL
   gtar c nbbuild/installer/mac | ssh $NATIVE_MAC_MACHINE "( cd $MAC_PATH; tar x )"

    cd $NB_ALL/l10n
    gtar c src/*/other/installer/mac/* | ssh $NATIVE_MAC_MACHINE "( cd $MAC_PATH; tar x)"
    cd $NB_ALL

   ssh $NATIVE_MAC_MACHINE rm -rf $MAC_PATH/zip/* 
   ERROR_CODE=$?
   if [ $ERROR_CODE != 0 ]; then
       echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't remove old bits"
       exit $ERROR_CODE;
   fi
   ssh $NATIVE_MAC_MACHINE mkdir -p $MAC_PATH/zip/moduleclusters
   ls $DIST/zip/moduleclusters | xargs -I {} scp -q -v $DIST/zip/moduleclusters/{} $NATIVE_MAC_MACHINE:$MAC_PATH/zip/moduleclusters/

   ERROR_CODE=$?
   if [ $ERROR_CODE != 0 ]; then
       echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't put the zips"
       exit $ERROR_CODE;
   fi


   # Run new builds
   sh $NB_ALL/nbbuild/installer/mac/newbuild/init.sh | ssh $NATIVE_MAC_MACHINE "cat > $MAC_PATH/nbbuild/installer/mac/newbuild/build-private.sh"
   ssh $NATIVE_MAC_MACHINE chmod a+x $MAC_PATH/nbbuild/installer/mac/newbuild/build.sh

   BASE_COMMAND="$MAC_PATH/nbbuild/installer/mac/newbuild/build.sh $DIST $BASENAME_PREFIX $BUILDNUMBER $BINARY_NAME $INSTALLER_SIGN_IDENTITY_NAME $APPLICATION_SIGN_IDENTITY_NAME $NB_VER_NUMBER $LOCALES"
   
   ssh $NATIVE_MAC_MACHINE "$UNLOCK_COMMAND $BASE_COMMAND" > $MAC_LOG_NEW 2>&1 &
   REMOTE_MAC_PID=$!

fi
if [ ! -z $BUILD_MAC ]; then
   # Run new builds
   sh $NB_ALL/nbbuild/installer/mac/newbuild/init.sh | cat > $NB_ALL/nbbuild/installer/mac/newbuild/build-private.sh
   chmod a+x $NB_ALL/nbbuild/installer/mac/newbuild/build.sh

   BASE_COMMAND="$NB_ALL/nbbuild/installer/mac/newbuild/build.sh $DIST $BASENAME_PREFIX $BUILDNUMBER $BINARY_NAME $INSTALLER_SIGN_IDENTITY_NAME $APPLICATION_SIGN_IDENTITY_NAME $NB_VER_NUMBER $LOCALES" 
   $BASE_COMMAND
fi
cd $NB_ALL/nbbuild/installer/infra/build

bash build.sh
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - NBI installers build failed"
    exit $ERROR_CODE;
fi

if [ ! -z $NATIVE_MAC_MACHINE ] && [ ! -z $MAC_PATH ]; then
    tail -f $MAC_LOG_NEW &
    TAIL_PID=$!

    set +x
    RUNNING_JOBS_COUNT=`ps --pid $REMOTE_MAC_PID | wc -l | tr " " "\n" | grep -v '^$'`
    echo "Entering loop with count of running jobs: " $RUNNING_JOBS_COUNT
    #Wait for the end of native mac build
    while [ $RUNNING_JOBS_COUNT -ge 2 ]; do
        #1 or more jobs
        sleep 10
        RUNNING_JOBS_COUNT=`ps --pid $REMOTE_MAC_PID | wc -l | tr " " "\n" | grep -v '^$'`
        echo "----> count of running jobs: " $RUNNING_JOBS_COUNT
    done
    set -x
    echo "Will kill "  $TAIL_PID
    kill -s 9 $TAIL_PID
fi

mv $OUTPUT_DIR/* $DIST
rmdir $OUTPUT_DIR

#Check if Mac installer was OK, 10 "BUILD SUCCESSFUL" messages should be in Mac log
if [ ! -z $NATIVE_MAC_MACHINE ] && [ ! -z $MAC_PATH ]; then

    IS_NEW_MAC_FAILED=`cat $MAC_LOG_NEW | grep "BUILD FAILED" | wc -l | tr " " "\n" | grep -v '^$'`
    IS_NEW_MAC_CONNECT=`cat $MAC_LOG_NEW | grep "Connection timed out" | wc -l | tr " " "\n" | grep -v '^$'`

    if [ $IS_NEW_MAC_FAILED -eq 0 ] && [ $IS_NEW_MAC_CONNECT -eq 0 ]; then
        #copy the bits back
        mkdir -p $DIST/bundles

        rsync -avz -e ssh $NATIVE_MAC_MACHINE:$MAC_PATH/nbbuild/installer/mac/newbuild/dist_en/* $DIST/bundles
        ERROR_CODE=$?
        if [ $ERROR_CODE != 0 ]; then
            echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't get installers"
            exit $ERROR_CODE;
        fi
    else
        tail -100 $MAC_LOG_NEW
        echo "ERROR: - Native Mac Installers build failed"
        exit 1;
    fi
fi

if [ ! -z $BUILD_MAC ]; then
        rsync -avz $NB_ALL/nbbuild/installer/mac/newbuild/dist_en/*.dmg $DIST/bundles
        ERROR_CODE=$?
        if [ $ERROR_CODE != 0 ]; then
            exit $ERROR_CODE;
        fi
fi
###################################################################
#
# Sign Windows ML installers
#
###################################################################

if [ -z $DONT_SIGN_INSTALLER ]; then

    if [ -z $SIGN_CLIENT ]; then
        echo "ERROR: SIGN_CLIENT not defined - Signing failed"
        exit 1;
    fi

    if [ -z $SIGN_USR ]; then
        echo "ERROR: SIGN_USR not defined - Signing failed"
        exit 1;
    fi

    if [ -z $SIGN_PASS ]; then
        echo "ERROR: SIGN_PASS not defined - Signing failed"
        exit 1;
    fi

    find $DIST/bundles -name "netbeans-*-windows.exe" | xargs -t -I [] java -Xmx2048m -jar $SIGN_CLIENT/Client.jar -file_to_sign [] -user $SIGN_USR -pass $SIGN_PASS -signed_location $DIST/bundles -sign_method microsoft
    ERROR_CODE=$?

    if [ $ERROR_CODE != 0 ]; then
        echo "ERROR: $ERROR_CODE - Signing failed"
        exit $ERROR_CODE;
    fi

fi

cd $DIST

bash ${SCRIPTS_DIR}/files-info.sh bundles zip zip/moduleclusters
ERROR_CODE=$?
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Counting of MD5 sums and size failed"
fi
