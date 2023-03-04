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

    if [ -z "$5" ]; then
        options="-q -r"
    else
        options=$5
    fi

    zip $options $dist/$base_name-$component.zip $filter
}

###################################################################
#
# Pack all the components
#
###################################################################

pack_all_components()
{
    DIST_DIR=${1}
    NAME=${2}

    mkdir $DIST_DIR/zip/moduleclusters

    cd $NB_ALL/nbbuild

    #Pack the distributions
    ant zip-cluster-config -Dcluster.config=full -Dzip.name=$DIST_DIR/zip/$NAME.zip || exit 1    
    #ant zip-cluster-config -Dcluster.config=platform -Dzip.name=$DIST_DIR/zip/$NAME-platform.zip || exit 1
    ant zip-cluster-config -Dcluster.config=basic -Dzip.name=$DIST_DIR/zip/$NAME-javase.zip || exit 1
    ant zip-cluster-config -Dcluster.config=standard -Dzip.name=$DIST_DIR/zip/$NAME-javaee.zip || exit 1
    ant zip-cluster-config -Dcluster.config=php -Dzip.name=$DIST_DIR/zip/$NAME-php.zip || exit 1
    ant zip-cluster-config -Dcluster.config=cnd -Dzip.name=$DIST_DIR/zip/$NAME-cpp.zip || exit 1
    
    ln -s $NAME-php.zip $DIST_DIR/zip/$NAME-html.zip

    cd $NB_ALL/nbbuild/netbeans

    rm -rf extra

    pack_component $DIST_DIR/zip/moduleclusters $NAME xml "xml*"
    rm -rf xml*

    pack_component $DIST_DIR/zip/moduleclusters $NAME javacard "javacard*"
    rm -rf javacard*

    cd $NB_ALL/nbbuild

    #Pack all the NetBeans
    pack_component $DIST_DIR/zip/moduleclusters $NAME all-in-one netbeans

    cd $NB_ALL/nbbuild/netbeans

    #Continue with individual component
    pack_component $DIST_DIR/zip/moduleclusters $NAME dlight "dlight*"
    rm -rf dlight*

    pack_component $DIST_DIR/zip/moduleclusters $NAME webcommon "webcommon*"
    rm -rf webcommon*

    pack_component $DIST_DIR/zip/moduleclusters $NAME websvccommon "websvccommon*"
    rm -rf websvccommon*

    pack_component $DIST_DIR/zip/moduleclusters $NAME groovy "groovy*"
    rm -rf groovy*

    pack_component $DIST_DIR/zip/moduleclusters $NAME php "php*"
    rm -rf php*

    pack_component $DIST_DIR/zip/moduleclusters $NAME profiler "profiler*"
    rm -rf profiler*

    pack_component $DIST_DIR/zip/moduleclusters $NAME platform "platform*"
    rm -rf platform*

    pack_component $DIST_DIR/zip/moduleclusters $NAME mobility "mobility*"
    rm -rf mobility*

    pack_component $DIST_DIR/zip/moduleclusters $NAME identity "identity*"
    rm -rf identity*

    pack_component $DIST_DIR/zip/moduleclusters $NAME ide "ide*"
    rm -rf ide*

    pack_component $DIST_DIR/zip/moduleclusters $NAME extide "extide*"
    rm -rf extide*

    pack_component $DIST_DIR/zip/moduleclusters $NAME harness "harness*"
    rm -rf harness*

    pack_component $DIST_DIR/zip/moduleclusters $NAME enterprise "enterprise*"
    rm -rf enterprise*

    pack_component $DIST_DIR/zip/moduleclusters $NAME ergonomics "ergonomics*"
    rm -rf ergonomics*

    pack_component $DIST_DIR/zip/moduleclusters $NAME apisupport "apisupport*"
    rm -rf apisupport*

    pack_component $DIST_DIR/zip/moduleclusters $NAME java "java*"
    rm -rf java*

    pack_component $DIST_DIR/zip/moduleclusters $NAME cndext "cndext*"
    rm -rf cndext*

    pack_component $DIST_DIR/zip/moduleclusters $NAME cnd "cnd*"
    rm -rf cnd*

    pack_component $DIST_DIR/zip/moduleclusters $NAME python "python*"
    rm -rf python*
    rm -rf ruby*

    pack_component $DIST_DIR/zip/moduleclusters $NAME nb-etc "bin* etc* nb*"
    pack_component $DIST_DIR/zip/moduleclusters $NAME nb-etc "*" "-q -u"
}

pack_all_components $DIST $BASENAME
