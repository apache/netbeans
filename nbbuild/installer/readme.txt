#
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
#

#Build script for generating Apache NetBeans IDE installers
BASE_DIR=`pwd`
NB_ALL=$BASE_DIR
export BASE_DIR NB_ALL
 
DIST=$BASE_DIR/dist
export DIST
 
if [ -d $DIST ] ; then
rm -rf $DIST
fi
 
mkdir -p $DIST/zip/moduleclusters
mkdir -p $DIST/logs
 
BINARY_NAME=incubating-netbeans-11.0-vc4-bin.zip
export BINARY_NAME
cp $BINARY_NAME $DIST/zip/moduleclusters
cd $BASE_DIR
NB_BUILD_NUMBER=1
BUILDNUMBER=$NB_BUILD_NUMBER
DATESTAMP=$BUILDNUMBER
NB_VER_NUMBER=11.1 
BASENAME_PREFIX=Apache-NetBeans-$NB_VER_NUMBER-bin
BUILD_DESC=$BASENAME_PREFIX
#export NB_VER_NUMBER BUILDNUMBER BASENAME_PREFIX NB_BUILD_NUMBER DATESTAMP BUILD_DESC
export BUILDNUMBER BASENAME_PREFIX NB_BUILD_NUMBER DATESTAMP BUILD_DESC

#To build MAC installer on mac host set BUILD_MAC to 1
BUILD_MAC=0
export BUILD_MAC
#Set INSTALLER_SIGN_IDENTITY_NAME to the path to file containing Common Name of your certificate or set to 0 to not sign mac installer
INSTALLER_SIGN_IDENTITY_NAME=/Users/john/Apache/installer_certificate.txt
export INSTALLER_SIGN_IDENTITY_NAME
#Set Application_SIGN_IDENTITY_NAME to the path to file containing Common Name of your certificate or set to 0 to not sign mac application
APPLICATION_SIGN_IDENTITY_NAME=/Users/john/Apache/application_certificate.txt
export APPLICATION_SIGN_IDENTITY_NAME

#To build linux and windows installers set BUILD_NB=1
BUILD_NB=1
BUILD_NETBEANS=0
BUILD_NBJDK6=0
BUILD_NBJDK7=0
BUILD_NBJDK8=0
BUILD_NBJDK11=0
 
export BUILD_NETBEANS BUILD_NB
export BUILD_NBJDK6 BUILD_NBJDK7 BUILD_NBJDK8 BUILD_NBJDK11
 
OUTPUT_DIR=${NB_ALL}/dist/installers
export OUTPUT_DIR
 
DONT_SIGN_INSTALLER=y
export DONT_SIGN_INSTALLER
 
bash -x $NB_ALL/nbbuild/newbuild/build-nbi.sh
