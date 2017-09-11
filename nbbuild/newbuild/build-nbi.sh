#!/bin/bash

 # DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 #
 # Copyright 2012-2013 Oracle and/or its affiliates. All rights reserved.
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
 #
 # Portions Copyrighted 2012 Sun Microsystems, Inc.

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
   gtar c installer/mac | ssh $NATIVE_MAC_MACHINE "( cd $MAC_PATH; tar x )"

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
   ls $DIST/zip/moduleclusters | grep -v "all-in-one" | xargs -I {} scp -q -v $DIST/zip/moduleclusters/{} $NATIVE_MAC_MACHINE:$MAC_PATH/zip/moduleclusters/

   ERROR_CODE=$?
   if [ $ERROR_CODE != 0 ]; then
       echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't put the zips"
       exit $ERROR_CODE;
   fi

   # Run new builds
   sh $NB_ALL/installer/mac/newbuild/init.sh | ssh $NATIVE_MAC_MACHINE "cat > $MAC_PATH/installer/mac/newbuild/build-private.sh"
   ssh $NATIVE_MAC_MACHINE chmod a+x $MAC_PATH/installer/mac/newbuild/build.sh

   if [ ! -z $SIGNING_PASSWORD ] ; then
       UNLOCK_COMMAND="security unlock-keychain -p $SIGNING_PASSWORD ;"
   else
       SIGNING_IDENTITY=0
   fi

   BASE_COMMAND="$MAC_PATH/installer/mac/newbuild/build.sh $MAC_PATH $BASENAME_PREFIX $BUILDNUMBER $BUILD_NBJDK7 $BUILD_NBJDK8 '"$SIGNING_IDENTITY"' $LOCALES"
   
   ssh $NATIVE_MAC_MACHINE "$UNLOCK_COMMAND $BASE_COMMAND" > $MAC_LOG_NEW 2>&1 &
   REMOTE_MAC_PID=$!

fi

cd $NB_ALL/installer/infra/build

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

        rsync -avz -e ssh $NATIVE_MAC_MACHINE:$MAC_PATH/installer/mac/newbuild/dist_en/* $DIST/bundles
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

bash ${SCRIPTS_DIR}/files-info.sh bundles bundles/jdk bundles/weblogic zip zip/moduleclusters
ERROR_CODE=$?
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Counting of MD5 sums and size failed"
fi
