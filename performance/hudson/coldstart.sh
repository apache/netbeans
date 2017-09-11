#!bash -x

# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 2011, 2016 Oracle and/or its affiliates. All rights reserved.
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