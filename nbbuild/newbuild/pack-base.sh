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


pack_component() 
{
    dist=$1
    base_name=$2
    component=$3
    filter=$4
    zip -q -r $dist/$base_name-$component.zip $filter
}

mkdir $DIST/zip/moduleclusters

cd $NB_ALL/nbbuild
ant zip-cluster-config -Dcluster.config=basic -Dzip.name=$DIST/zip/$BASENAME-javase.zip || exit 1
cd $NB_ALL/nbbuild/netbeans
pack_component $DIST/zip/moduleclusters $BASENAME ergonomics "ergonomics*"

cp -r $DIST/zip /net/smetiste.czech/space/${BASE_FOR_JAVAFX}
touch /net/smetiste.czech/space/${BASE_FOR_JAVAFX}/ready

