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
source init.sh

cd  $NB_ALL

#build source zip files for particular modules
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml build-source-zips
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build source zips"
    exit $ERROR_CODE;
fi

###################################################################
#
# Deploy sources to the storage server
#
###################################################################

if [ -n $BUILD_ID ]; then
    mkdir -p $DIST_SERVER2/source-zips/${BUILD_ID}
    cp -rp $NB_ALL/nbbuild/build/source-zips/*  $DIST_SERVER2/source-zips/${BUILD_ID}
    rm $DIST_SERVER2/source-zips/latest.old
    mv $DIST_SERVER2/source-zips/latest $DIST_SERVER2/source-zips/latest.old
    ln -s $DIST_SERVER2/source-zips/${BUILD_ID} $DIST_SERVER2/source-zips/latest
fi
