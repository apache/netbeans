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

#if test ! -e /space/hudsonserver/master 
env
#sanitize any orphaned JUnitTestRunners
jps | grep JUnitTestRunner | cut -d' ' -f1 | xargs kill -9

#environment setup
cd "$WORKSPACE"/../../../../../
reposdir=`pwd`
export reposdir=`cygpath -m $reposdir`
project_root=$reposdir/ergonomics
export project_root=`cygpath -m $project_root`
netbeans_dest=$reposdir/netbeans
export netbeans_dest=`cygpath -m $netbeans_dest`
filename=`ls "$reposdir"/zip`
echo "filename=$filename"
pnum=`echo $filename | sed -e "s/^.*-//" -e "s/.zip//"`
echo "pnum=$pnum"
echo -n ${pnum}>$reposdir/build.number

#update repository
cd  $project_root
hg pull -u

# copy netbeans.conf to netbeans dir
cp -f $project_root/performance/hudson/netbeans.conf $netbeans_dest/etc/

# delete all netbeans userdirs
rm -rf $HOME/.netbeans

# start netbeans first time
/home/hudson/scripts/nb_start.bat

# start nb second time + open project
/home/hudson/scripts/nb_open_pr.bat

# start netbeans third time
/home/hudson/scripts/nb_start.bat

#start script dealing with post-startup run
/home/hudson/scripts/preparecold.bat

# restart 
/home/hudson/scripts/restart.bat
#fi