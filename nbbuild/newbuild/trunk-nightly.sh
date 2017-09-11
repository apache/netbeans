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
