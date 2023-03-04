#!/bin/sh
#
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

#set -x

#
# It is used as automatical update of XML web pages.
#
# You must specify $CVS_ROOT, which should contain nb_all/xml,
#   nb_all/nbbuild with compiled nbantext.jar and plans folder.
# e.g.: CVS_ROOT=/tmp/cvs_netbeans_org
# If necessary, you can specify proxy host and port
#   HTTP_PROXYHOST, HTTP_PROXYPORT variables.
#
# You can run it anywhere, typically it is run by cron.
#


#
# Date stamp
#
DATE_STAMP=`date +%Y.%m.%d-%H:%M`

### DEBUG
set > $CVS_ROOT/"${DATE_STAMP}__0.set.txt"


#
# update libs
#
cd $CVS_ROOT/nb_all/libs
echo "#################" >> $CVS_ROOT/"${DATE_STAMP}__1.update.txt"
echo "# cvs update libs" >> $CVS_ROOT/"${DATE_STAMP}__1.update.txt"
cvs update -A -d -P 2>&1 >> $CVS_ROOT/"${DATE_STAMP}__1.update.txt"


#
# update xml
#
cd $CVS_ROOT/nb_all/xml
echo "################" >> $CVS_ROOT/"${DATE_STAMP}__1.update.txt"
echo "# cvs update xml" >> $CVS_ROOT/"${DATE_STAMP}__1.update.txt"
cvs update -A -d -P 2>&1 >> $CVS_ROOT/"${DATE_STAMP}__1.update.txt"


#
# add xalan and xerces on classpath
#
cd bin
. init-xalan.sh
cd ..


#
# update content
#
cd www
ant -Dhttp.proxyPort=${HTTP_PROXYPORT} -Dhttp.proxyHost=${HTTP_PROXYHOST} -logfile $CVS_ROOT/"${DATE_STAMP}__2.ant_all.txt" all 2>&1 >> $CVS_ROOT/"${DATE_STAMP}__2.ant_error.txt"


#
# commit changes
#
cvs commit -m "Automatic update -- ${DATE_STAMP}." 2>&1 > $CVS_ROOT/"${DATE_STAMP}__3.commit.txt"


#
# post update xml - to log status after commit
#
cvs update -A -d -P 2>&1 >> $CVS_ROOT/"${DATE_STAMP}__4.post-update.txt"
