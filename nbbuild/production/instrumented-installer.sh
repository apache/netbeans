#!/bin/bash

 # DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 #
 # Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
cd ${WORKSPACE}
rm -rf installer nbi nbextracted zipdist dist
hg up

cd $LAST_BITS

BUILD_NUMBER=`ls | grep netbeans | cut -f 4 -d "-" | uniq`

cd ${WORKSPACE}
#ssh $NATIVE_MAC_MACHINE rm -rf $MAC_PATH/installer
#ssh $NATIVE_MAC_MACHINE mkdir -p $MAC_PATH/installer
#gtar c installer/mac | ssh $NATIVE_MAC_MACHINE "( cd $MAC_PATH; tar x )"
#ssh $NATIVE_MAC_MACHINE rm -rf $MAC_PATH/zip/*

EMMA_DIR=${WORKSPACE}/../emma
EMMA_SH="$EMMA_DIR/emma.sh"
EMMA_TXT="$EMMA_DIR/emma.txt"
EMMA_JAR="$EMMA_DIR/emma.jar"

EXTRACTED_DIR=$BASE_DIR/nbextracted

mkdir -p ${EXTRACTED_DIR}
NB_EXTRACTED=${EXTRACTED_DIR}/netbeans

unzip -d ${EXTRACTED_DIR} ${LAST_BITS}/${BUILD_DESC}-${BUILD_NUMBER}-all-in-one.zip
mkdir ${NB_EXTRACTED}/emma-lib
chmod a+w ${NB_EXTRACTED}/emma-lib
cp ${EMMA_JAR} ${NB_EXTRACTED}/emma-lib/
cp ${EMMA_TXT} ${NB_EXTRACTED}/emma-lib/netbeans_coverage.em

cd ${BASE_DIR}
bash ${EMMA_SH} ${NB_EXTRACTED} ${NB_EXTRACTED}/emma-lib/netbeans_coverage.em ${EMMA_JAR}

sed -i -e "s/^netbeans_default_options=\"/netbeans_default_options=\"--cp:p \"\\\\\"\"$\{NETBEANS_HOME\}\/emma-lib\/emma.jar\"\\\\\"\" -J-Demma.coverage.file=\"\\\\\"\"$\{NETBEANS_HOME\}\/emma-lib\/netbeans_coverage.ec\"\\\\\"\" -J-Dnetbeans.security.nocheck=true /" ${NB_EXTRACTED}/etc/netbeans.conf

cp ${EMMA_JAR} ${NB_EXTRACTED}/platform?/lib/
BASENAME=${BUILD_DESC}-${BUILD_NUMBER}
export DIST=${WORKSPACE}/dist/zip
mkdir -p ${DIST}
#cd ${NB_EXTRACTED}
#expat='extra'
#for c in platform ide java apisupport harness enterprise profiler mobility cnd identity gsf php groovy webcommon websvccommon; do
#    find * | egrep "^$c[0-9]*/" | zip -q $DIST/$BASENAME-$c.zip -@ || exit
#    expat="$expat|$c[0-9]*"
#done
#find * | egrep -v "^($expat)(/|$)" | zip -q $DIST/$BASENAME-nb7.0-etc.zip -@ || exit
cd ${EXTRACTED_DIR}
zip -q -r $DIST/$BASENAME-all-in-one.zip netbeans/*
cd ${WORKSPACE}

rm -rf ${EXTRACTED_DIR}

#ssh $NATIVE_MAC_MACHINE mkdir -p $MAC_PATH/zip/moduleclusters
#scp -q -v ${DIST}/*.zip $NATIVE_MAC_MACHINE:$MAC_PATH/zip/moduleclusters/
#scp -q -v ${BASE_DIR}/../build-private.sh $NATIVE_MAC_MACHINE:$MAC_PATH/installer/mac/newbuild
#ssh $NATIVE_MAC_MACHINE sh $MAC_PATH/installer/mac/newbuild/build.sh $MAC_PATH/zip/moduleclusters ${BUILD_DESC} $BUILD_NUMBER 0

#cd ${BASE_DIR}/installer/infra/build
#bash build.sh

#scp $NATIVE_MAC_MACHINE:$MAC_PATH/installer/mac/newbuild/dist/* ${WORKSPACE}/dist/installers/bundles
