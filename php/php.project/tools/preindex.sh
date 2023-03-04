#!/bin/bash
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

# Script which updates the PHP preindexed files
#
# In advance, create a PHP project. Then configure the $INDEXING_PROJECT below
# to the full path to this project. It will then be used for indexing purposes.
#
# Configure the following parameters:
# Location of your netbeans Mercurial clone:

# Configure the following parameters:
# Location of your netbeans Mercurial clone:
NBHGHOME=/space/mercurial/trunk/main

# Location of a PHP project
INDEXING_PROJECT=/home/petr/NetBeansProjects/PhpProject1

# Any flags to pass to the IDE
VMFLAGS=-J-Xmx1024m

# You probably don't want to change these:
NB=$NBHGHOME/nbbuild/netbeans/bin/netbeans
SCRATCHFILE=/tmp/native.zip
USERDIR=/tmp/preindexing

#############################################################################################
# No user-configurable parts beyond this point...
CLUSTERS=$NBHGHOME/nbbuild/netbeans
PHP=$CLUSTERS/php

if test ! -f $CLUSTERS/extra/modules/org-netbeans-modules-gsf-tools.jar ; then
  echo "You should build contrib/gsf.tools first, which will automate the indexing process within the IDE when this script is run."
  exit 0
fi

find $CLUSTERS . -name "netbeans-index*.zip" -exec rm {} \;

rm -rf $USERDIR

echo "Running NetBeans .... ";
$NB $VMFLAGS -J-Dgsf.preindexing=true -J-Druby.computeindex -J-Dgsf.preindexing.projectpath=$INDEXING_PROJECT -J-Dnetbeans.full.hack=true --userdir $USERDIR

# Pack preindexed.zip
cd $PHP
rm -f preindexed-php.zip
zip -r preindexed-php.zip `find . -name "netbeans-index-*php*"`
find . -name "netbeans-index-*.zip" -exec rm {} \;
mv preindexed-php.zip $NBHGHOME/php.project/external/preindexed-php.zip
rm -f preindexed-php.zip
