#!bash -x

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

env

echo PATH=$PATH
export PATH="/bin:"$PATH
echo PATH=$PATH

# kill all unwanted processes
case $OSTYPE in
    msdos | windows | cygwin)
        ps -efW| egrep -i "junit|netbeans|test|anagram|ant|jusched"|egrep -vi system32|awk '{print $2}'| xargs /bin/kill -f;
        ;;
    linux*)
        ps -e| egrep -i "junit|netbeans|test|anagram|ant|jusched"|egrep -vi system32|awk '{print $2}'| xargs /bin/kill -9;
        ;;
    darwin11)
        ps -e| egrep -i "junit|netbeans|test|anagram|ant|jusched"|egrep -vi system32|awk '{print $1}'| xargs /bin/kill -9;
        ;;
    *)
        jps| egrep -i "junit|netbeans|test|anagram|ant|jusched"|egrep -vi system32| xargs kill;
        ;;
esac
jps | grep JUnitTestRunner | cut -d' ' -f1 | xargs kill -9
sleep 5

export ANT_OPTS=-Xmx1024m
export j2se_enabled=1
export j2ee_enabled=1
export languages_enabled=1
export web_enabled=1

case $OSTYPE in
    msdos*)
#  linux*|Linux*|cygwin*)
    export mobility_enabled=1
esac

cd "$WORKSPACE"/../../../../../
reposdir=`pwd`
export reposdir=`cygpath -m $reposdir`

project_root=$reposdir/../ergonomics
export project_root=`cygpath -m $project_root`

netbeans_dest=$reposdir/netbeans
export netbeans_dest=`cygpath -m $netbeans_dest`

platdefharness=$netbeans_dest/harness
export platdefharness=`cygpath -m $platdefharness`
export nbplatform.default.harness.dir=$platdefharness

performance=$project_root/performance
export performance=`cygpath -m $performance`

perfjar=$netbeans_dest/extra/modules/org-netbeans-modules-performance.jar
export perfjar=`cygpath -m $perfjar`

execdir=$netbeans_dest/bin/
export execdir=`cygpath -m $execdir`

# copy netbeans.conf to netbeans dir
cp $performance/hudson/netbeans.conf $netbeans_dest/etc/

# fix the permissions; they get reset after each hg pull ...
chmod a+x $performance/hudson/*.sh
chmod a+x $netbeans_dest/bin/*

# clean
cd $project_root
rm -rf nbbuild/nbproject/private
rm -rf performance/build
rm -rf performance/*/build
pwd

$performance/hudson/setupenv.sh
$performance/hudson/j2se.sh
$performance/hudson/j2ee.sh
$performance/hudson/web.sh
$performance/hudson/languages.sh
