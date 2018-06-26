#!/bin/bash
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
# Contributor(s):
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
# Microsystems, Inc. All Rights Reserved.
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
