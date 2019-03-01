#!/bin/sh
#
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
# 
################################################################################

################################################################################
# get the path to the current directory and change to it
set -x
DIRNAME=`dirname $0`
cd ${DIRNAME}

################################################################################
# load the properties
if [ -f "../../../../build-private.sh" ] ; then
source ../../../../build-private.sh
else
    echo "No build-private.sh file exist"
fi
################################################################################
# define the environment for running ant

if [ -z "$BUILD_NETBEANS" ] ; then
    #build NetBeans bundles by default
    BUILD_NETBEANS=1
fi

if [ -z "$BUILD_NB" ] ; then
    #build NetBeans full bundle by default
    BUILD_NB=0
fi

if [ -z "$BUILD_NBJDK7" ] ; then
    #do not build NetBeans/JDK7 bundles by default
    BUILD_NBJDK7=0
fi

if [ -z "$BUILD_NBJDK8" ] ; then
    #do not build NetBeans/JDK8 bundles by default
    BUILD_NBJDK8=0
fi

if [ -z "$BUILD_NBJDK11" ] ; then
    #do not build NetBeans/JDK11 bundles by default
    BUILD_NBJDK11=0
fi

if [ -z "$BUNDLE_JDK_PLATFORM" ] ; then
    #build NetBeans/JDK for windows,windows-x64,linux,linux-x64 by default (Mac has own native build)
    #BUNDLE_JDK_PLATFORM="windows,windows-x64,linux,linux-x64"
    
    BUNDLE_JDK_PLATFORM="windows-x64,linux-x64"
fi

if [ -z "$COMMUNITY_ML_BUILD" ] ; then
    COMMUNITY_ML_BUILD=0
fi

if [ -z "$BUILD_JTB" ] ; then
    #do not build Java Tools Bundle installers by default
    BUILD_JTB=0
fi

if [ -z "$BUILD_MYSQL" ] ; then
    #do not build NetBeans/GlassFish/MySQL bundles by default
    BUILD_MYSQL=0
fi

if [ -z "$BUILD_WEBLOGIC" ] ; then
    #do not build NetBeans/WebLogic bundles by default
    BUILD_WEBLOGIC=0
fi

if [ -z "$RUNJAVAFX" ] ; then
    #do not build NetBeans/JavaFX bundles by default
    RUNJAVAFX=0
fi

if [ -z "$CACHE_DIR" ] ; then
    curdir=`pwd`  
    CACHE_DIR="$DIRNAME/../../../../NBI-cache"
    mkdir -p "$CACHE_DIR"
    cd "$CACHE_DIR"
    CACHE_DIR=`pwd`
    cd "$curdir"
    echo "NBI Cache : $CACHE_DIR"
    export CACHE_DIR
fi




if [ -n "$JDK_HOME" ] ; then
JAVA_HOME="$JDK_HOME"
fi

if [ -z "$JDK_HOME" ] && [ -n "$JAVA_HOME" ] ; then
JDK_HOME="$JAVA_HOME"
fi


JAVA_HOME=`echo "$JAVA_HOME" | sed "s/\\\\\\/\//g"`
JDK_HOME=`echo "$JDK_HOME"   | sed "s/\\\\\\/\//g"`

export JAVA_HOME JDK_HOME


if [ -z "$ANT_OPTS" ] ; then
    ANT_OPTS="-Xmx768m"
fi

java8output=`"$JAVA_HOME/bin/java" -version 2>&1 | grep 1.8.0`

if [ -n "$java7output" ] ; then
    ANT_OPTS="$ANT_OPTS -Djavac.target=1.8 -Djavac.source=1.8"
fi

export ANT_OPTS

if [ -z "$USE_JARSIGNER" ] ; then
    if [ -n "$JARSIGNER_KEYSTORE" ] ; then
        USE_JARSIGNER=true
    else
        USE_JARSIGNER=false
    fi
fi


if [ -z "$OUTPUT_DIR" ] ; then
    OUTPUT_DIR="$DIRNAME/../../../../dist/installers"
    mkdir -p "$OUTPUT_DIR"
    cd "$OUTPUT_DIR"
    OUTPUT_DIR=`pwd`
    cd "$curdir"
    echo "NBI Output Dir : $OUTPUT_DIR"
fi

if [ -n "$BASE_DIR" ] ; then
    if [ -z "$NB_BUILDS_HOST_EN" ] ; then
        NB_BUILDS_HOST_EN="file:$BASE_DIR/dist/zip/moduleclusters"
    fi
    if [ -z "$NB_BUILDS_HOST_ML" ] ; then
        NB_BUILDS_HOST_ML="file:$BASE_DIR/dist/ml/zip/moduleclusters"
    fi
fi

if [ -n "$DATESTAMP" ] ; then
    NB_BUILD_NUMBER="$DATESTAMP"
fi

if [ -z "$NB_BUILD_NUMBER" ] ; then
    echo "Build number is not defined"
    exit 1;
fi

if [ -z "$NB_FILES_PREFIX" ] && [ -n "$BASENAME_PREFIX" ] ; then
    NB_FILES_PREFIX="$BASENAME_PREFIX"
fi

run() {
    
    ################################################################################
    # run the build
    ant build\
            \"-Dbuild.number=${NB_BUILD_NUMBER}\" \
            \"-Doutput.dir=${OUTPUT_DIR}\" \
            \"-Dbinary.cache.host=${BINARY_CACHE_HOST}\" \
            \"-Dnb.builds.host=${NB_BUILDS_HOST}\" \
            \"-Dnb.files.prefix=${BASENAME_PREFIX}\" \
            \"-Dnb.locales=${LOCALES}\" \
            \"-Dcommunity.mlbuild=${COMMUNITY_ML_BUILD}\" \
            \"-Dglassfish.builds.host=${GLASSFISH_BUILDS_HOST}\" \
            \"-Djdk.builds.host=${JDK_BUILDS_HOST}\" \
            \"-Djdk7.builds.path=${JDK7_BUILDS_PATH}\" \
            \"-Djdk8.builds.path=${JDK8_BUILDS_PATH}\" \
            \"-Djdk11.builds.path=${JDK11_BUILDS_PATH}\" \
            \"-Dopenesb.builds.host=${OPENESB_BUILDS_HOST}\" \
            \"-Dsjsam.builds.host=${SJSAM_BUILDS_HOST}\" \
            \"-Dportalpack.builds.host=${PORTALPACK_BUILDS_HOST}\" \
    	    \"-Dwtk.builds.host=${WTK_BUILDS_HOST}\" \
            \"-Djavafx.builds.host=${JAVAFX_BUILDS_HOST}\" \
            \"-Djdk.home=${JDK_HOME}\" \
            \"-Dcvs.root=${CVS_ROOT}\" \
            \"-Dcvs.timestamp=${CVS_STAMP}\" \
            \"-Dcvs.branch=${CVS_BRANCH}\" \
            \"-Dbuild.jdk7=${BUILD_NBJDK7}\" \
            \"-Dbuild.jdk8=${BUILD_NBJDK8}\" \
            \"-Dbuild.jdk11=${BUILD_NBJDK11}\" \
            \"-Dbundle.jdk.platform=${BUNDLE_JDK_PLATFORM}\" \
            \"-Dbuild.jtb=${BUILD_JTB}\" \
            \"-Dbuild.mysql=${BUILD_MYSQL}\" \
            \"-Dbuild.weblogic=${BUILD_WEBLOGIC}\" \
            \"-Dbuild.netbeans.bundles=${BUILD_NETBEANS}\" \
            \"-Dbuild.nb=${BUILD_NB}\" \
            \"-Dbuild.javafx=${RUNJAVAFX}\" \
            \"-Dglassfish.home=${GLASSFISH_HOME}\" \
            \"-Dglassfish.asadmin=${GLASSFISH_ASADMIN}\" \
            \"-Dglassfish.http.port=${GLASSFISH_HTTP_PORT}\" \
            \"-Dglassfish.admin.port=${GLASSFISH_ADMIN_PORT}\" \
            \"-Dglassfish.host=${GLASSFISH_HOST}\" \
            \"-Dglassfish.user=${GLASSFISH_USER}\" \
            \"-Dglassfish.password=${GLASSFISH_PASSWORD}\" \
            \"-Djarsigner.enabled=${USE_JARSIGNER}\" \
            \"-Djarsigner.keystore=${JARSIGNER_KEYSTORE}\" \
            \"-Djarsigner.alias=${JARSIGNER_ALIAS}\" \
            \"-Djarsigner.storepass=${JARSIGNER_STOREPASS}\" \
            \"-Dpack200.enabled=${USE_PACK200}\" \
	    \"-Dmakedmg.remote.port=${MAKEDMG_MACOSX_SYSTEM_PORT}\" \
	    \"-Dmakedmg.remote.host=${MAKEDMG_MACOSX_SYSTEM_HOST}\" \
	    \"-Dmakedmg.remote.user=${MAKEDMG_MACOSX_SYSTEM_USER}\" \
	    \"-Dmakedmg.ssh.keyfile=${MAKEDMG_MACOSX_SYSTEM_SSH_KEYFILE}\" \
	    \"-Dmakedmg.ssh.keypass=${MAKEDMG_MACOSX_SYSTEM_SSH_PASSPHRASE}\" \
            \"-Dnbi.cache.dir=${CACHE_DIR}\" \
            ${ADDITIONAL_PARAMETERS} \
            $*
            ERROR_CODE=$?

            if [ $ERROR_CODE != 0 ]; then
                 echo "ERROR: $ERROR_CODE - NBI installers build failed"
                 exit $ERROR_CODE;
            fi
}

NB_BUILDS_HOST=${NB_BUILDS_HOST_EN}
export NB_BUILDS_HOST
sh init.sh | cat > build-private.properties
cat build-private.properties
run $*

################################################################################
