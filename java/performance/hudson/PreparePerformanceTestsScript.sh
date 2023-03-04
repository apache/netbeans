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

echo "Deleting user and cache dirs"
touch "$HOME"/.netbeans/perfdev
touch "$HOME"/.cache/perfdev
rm -rf "$HOME"/.netbeans/perfdev
rm -rf "$HOME"/.cache/perfdev

cd "$WORKSPACE"/../../../../../
reposdir=`pwd`
reposdir=`cygpath -m $reposdir`
netbeansdir="$reposdir"/netbeans
netbeansdir=`cygpath -m $netbeansdir`

# Parse build number
filename=`ls "$reposdir"/zip`
echo "filename=$filename"
pnum=`echo $filename | sed -e "s/^.*-//" -e "s/.zip//"`
echo "pnum=$pnum"
echo -n ${pnum}>$reposdir/build.number
cp -f $reposdir/build.number $reposdir/../build.number
cp -f $reposdir/build.number $reposdir/../../build.number

# Update repository
if [ ! -d $reposdir/../ergonomics ]
then
  cd $reposdir/..
  hg clone http://hg.netbeans.org/ergonomics
fi
cd $reposdir/../ergonomics
pwd
hg pull -u
hg update -C

# Clean repository
ant clean
rm -rf nbbuild/nbproject/private
rm -rf performance/build
rm -rf performance/*/build

# Build tools and tests
ant bootstrap
ant -f libs.junit4 -Dnetbeans.dest.dir=$netbeansdir
ant -f performance -Dnetbeans.dest.dir=$netbeansdir
ant -f performance/j2se -Dnetbeans.dest.dir=$netbeansdir
ant -f performance/j2ee -Dnetbeans.dest.dir=$netbeansdir
ant -f performance/enterprise -Dnetbeans.dest.dir=$netbeansdir
ant -f performance/languages -Dnetbeans.dest.dir=$netbeansdir
ant -f performance/mobility -Dnetbeans.dest.dir=$netbeansdir
ant -f performance/web -Dnetbeans.dest.dir=$netbeansdir
