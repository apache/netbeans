#!/bin/sh
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
