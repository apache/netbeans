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

#Initialize all the environment

#Create test result xml file - call:
#create_test_result( testname, message, failures=0 )
create_test_result() {
    if [ -z "$3" ]; then
        FAILURES="0"
    else
        FAILURES="$3"
    fi

    mkdir -p $WORKSPACE/results

    FILE="$WORKSPACE/results/TEST-$1.xml"
    echo '<?xml version="1.0" encoding="UTF-8" ?>' >$FILE
    echo '<testsuite errors="0" failures="'$FAILURES'" name="'$1'" tests="1" time="1">' >>$FILE
    echo '  <properties>' >>$FILE
    echo '  </properties>' >>$FILE
    echo '  <testcase classname="'$1'" name="'$1'" time="1">' >>$FILE
    if [ "$FAILURES" -gt "0" ]; then
        echo '  <failure message="Failed"/>' >>$FILE
    fi
    echo '  </testcase>' >>$FILE
    echo '  <system-out><![CDATA[' >>$FILE
    echo "$2" >>$FILE
    echo ']]></system-out>' >>$FILE
    echo '  <system-err></system-err>' >>$FILE
    echo '</testsuite>' >>$FILE
}

#NB_BRANCH default
if [ -z ${NB_BRANCH} ]; then
    export NB_BRANCH=default
fi

#L10N_BRANCH default
if [ -z ${L10N_BRANCH} ]; then
    export L10N_BRANCH=default
fi

#OTHER_LICENCES_BRANCH default
if [ -z ${OTHER_LICENCES_BRANCH} ]; then
    export OTHER_LICENCES_BRANCH=default
fi

#JAVAFX build 1/0
if [ -z ${RUNJAVAFX} ]; then
    export RUNJAVAFX=0
fi

#ML_BUILD yes/no 1/0
if [ -z ${ML_BUILD} ]; then
    export ML_BUILD=0
fi
#EN_BUILD yes/no 1/0
if [ -z ${EN_BUILD} ]; then
    export EN_BUILD=1
fi
if [ -z ${LOCALES} ]; then
    export LOCALES=ja,zh_CN,pt_BR,ru
fi
if [ -z ${NB_VER_NUMBER} ]; then
    export NB_VER_NUMBER=11
fi
if [ -z ${UPLOAD_ML} ]; then
    export UPLOAD_ML=0
fi

#GLASSFISH_BUILDS_HOST=http://jre.us.oracle.com
if [ -z ${GLASSFISH_BUILDS_HOST} ]; then
    GLASSFISH_BUILDS_HOST=http://jre.us.oracle.com
    export GLASSFISH_BUILDS_HOST
fi

#JRE_BUILDS_HOST=http://jre.us.oracle.com
if [ -z ${JRE_BUILDS_HOST} ]; then
    JRE_BUILDS_HOST=http://jre.us.oracle.com
    export JRE_BUILDS_HOST
fi

#JRE_BUILDS_PATH=http://jre.us.oracle.com
if [ -z ${JRE_BUILDS_PATH} ]; then
    JRE_BUILDS_PATH=java/re/jdk/8u101/promoted/
    export JRE_BUILDS_PATH
fi

#JDK_BUILDS_HOST=http://jre.us.oracle.com
if [ -z ${JDK_BUILDS_HOST} ]; then
    JDK_BUILDS_HOST=https://java.se.oracle.com/
    export JDK_BUILDS_HOST
fi

#JDK7_BUILDS_PATH=http://jre.us.oracle.com/java/re/jdk/7u75/promoted/all
if [ -z ${JDK7_BUILDS_PATH} ]; then
    JDK7_BUILDS_PATH=java/re/jdk/7u75/promoted/
    export JDK7_BUILDS_PATH
fi

#JDK8_BUILDS_PATH=http://jre.us.oracle.com/java/re/jdk/8u141/promoted/all/
if [ -z ${JDK8_BUILDS_PATH} ]; then
#    JDK8_BUILDS_PATH=java/re/jdk/8u141/promoted/ #for builds before 8u141
    JDK8_BUILDS_PATH=artifactory/re-release-local/jdk/8u171/b11/bundles/
    export JDK8_BUILDS_PATH
fi

if [ -z ${JDK11_BUILDS_PATH} ]; then
    JDK11_BUILDS_PATH=artifactory/re-release-local/jdk/11.0.1/13/bundles/
    export JDK11_BUILDS_PATH
fi

if [ -z ${DEBUGLEVEL} ]; then
    DEBUGLEVEL=source,lines,vars
    export DEBUGLEVEL
fi

if [ -z ${DONT_PACK_LOCALIZATION_JARS_ON_MAC} ]; then
    DONT_PACK_LOCALIZATION_JARS_ON_MAC=y
    export DONT_PACK_LOCALIZATION_JARS_ON_MAC
fi

export ANT_OPTS=$ANT_OPTS" -Xmx2G"

if [ -n ${JDK_HOME} ] && [ -z ${JAVA_HOME} ] ; then
    export JAVA_HOME=$JDK_HOME
elif [ -n ${JAVA_HOME} ] && [ -z ${JDK_HOME} ]; then
    export JDK_HOME=$JAVA_HOME
fi

if [ -z ${DATESTAMP} ]; then
    if [ -z ${BUILD_ID} ]; then
        export DATESTAMP=`date -u +%Y%m%d%H%M`
    else
        #Use BUILD_ID from hudson, remove all "-" and "_" and cut it to 12 chars
        export DATESTAMP=`echo ${BUILD_ID} | sed -e "s/[-_]//g" | cut -c 1-12`
    fi
fi

BUILDNUM=$BUILD_DESC
BUILDNUMBER=$DATESTAMP

if [ -z $BASE_DIR ]; then
    echo BASE_DIR variable not defined, using the default one: /space/NB-IDE
    echo if you want to use another base dir for the whole build feel free
    echo to define a BASE_DIR variable in your environment
    
    export BASE_DIR=/space/NB-IDE
fi

if [ -z $NB_ALL ]; then
    NB_ALL=$BASE_DIR/main
fi

DIST=$BASE_DIR/dist
LOGS=$DIST/logs
BASENAME=$BUILDNUM
export BASENAME_PREFIX=$BUILD_DESC

mkdir -p $DIST/zip
mkdir -p $LOGS

#LOGS
IDE_BUILD_LOG=$LOGS/$BASENAME-build-ide.log
MOBILITY_BUILD_LOG=$LOGS/$BASENAME-build-mobility.log
NBMS_BUILD_LOC=$LOGS/$BASENAME-build-nbms.log
SCP_LOG=$LOGS/$BASENAME-scp.log
MAC_LOG=$LOGS/$BASENAME-native_mac.log
MAC_LOG_NEW=$LOGS/$BASENAME-native_mac_new.log
INSTALLER_LOG=$LOGS/$BASENAME-installers.log
