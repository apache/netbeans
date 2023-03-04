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

cd  ${DIST}/zip

nbsrc_file=`ls netbeans*platform-src.zip`
zip_file=`basename $nbsrc_file -platform-src.zip`.zip

cd  $NB_ALL

cd nbbuild
cp ${DIST}/zip/${zip_file} .
unzip -oq ${zip_file}
cd ..

#Build napackaged NBMs for stable UC - IDE + UC-only
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml build-nbms -Dcluster.config=stableuc -Duse.pack200=false -Dbase.nbm.target.dir=${DIST}/uc-unpackaged -Dkeystore=$KEYSTORE -Dstorepass=$STOREPASS -Dbuild.compiler.debuglevel=source,lines
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build unpackaged all stable UC NBMs"
    exit $ERROR_CODE;
fi

cd nbbuild
Build catalog for unpackaged NBMs
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml generate-uc-catalog -Dnbms.location=${DIST}/uc-unpackaged -Dcatalog.file=${DIST}/uc-unpackaged/catalog.xml
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build stable UC catalog for unpackaged NBMs"
    exit $ERROR_CODE;
fi
cd ..

