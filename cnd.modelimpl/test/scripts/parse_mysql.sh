#!/bin/bash

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
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

project=$1
shift

absdir="${project}"
if [ `expr match ${absdir} "\/"` == 0 ]; then
    absdir="`pwd`/${absdir}"
fi

defines="";
defines="${defines} -DDATADIR=\"\"";
defines="${defines} -DDATADIR=\"\""
defines="${defines} -DDBUG_OFF"
defines="${defines} -DDEFAULT_BASEDIR=\"/usr/local\""
defines="${defines} -DDEFAULT_CHARSET_HOME=\"\""
defines="${defines} -DDEFAULT_GROUP_SUFFIX_ENV=MYSQL_GROUP_SUFFIX"
defines="${defines} -DDEFAULT_HOME_ENV=MYSQL_HOME"
defines="${defines} -DDEFAULT_MYSQL_HOME=\"\""
defines="${defines} -DHAVE_CONFIG_H"
defines="${defines} -DHAVE_RWLOCK_T"
defines="${defines} -DMYSQL_SERVER"
defines="${defines} -DNO_KILL_INTR"
defines="${defines} -DSHAREDIR=\"\""
defines="${defines} -DTZINFO2SQL"
defines="${defines} -DUNDEF_THREADS_HACK"
defines="${defines} -DMAP_TO_USE_RAID"
defines="${defines} -DDEFAULT_GROUP_SUFFIX_ENV=MYSQL_GROUP_SUFFIX"
defines="${defines} -DMAIN"

defines=""
includes="${includes} -I${absdir}"
includes="${includes} -I${absdir}/client"
includes="${includes} -I${absdir}/include"
includes="${includes} -I${absdir}/sql"
includes="${includes} -I${absdir}/regex"
includes="${includes} -I${absdir}/ndb/src/kernel/vm"
includes="${includes} -I${absdir}/ndb/src/kernel/blocks"
includes="${includes} -I${absdir}/ndb/include"
includes="${includes} -I${absdir}/ndb/include/kernel"
includes="${includes} -I${absdir}/ndb/include/util"
includes="${includes} -I${absdir}/ndb/include/kernel"
includes="${includes} -I${absdir}/ndb/include/mgmapi"
includes="${includes} -I${absdir}/ndb/include/ndbapi"
includes="${includes} -I${absdir}/ndb/include/portlib"
includes="${includes} -I${absdir}/ndb/include/debugger"
includes="${includes} -I${absdir}/ndb/include/editline"
includes="${includes} -I${absdir}/ndb/include/mgmcommon"
includes="${includes} -I${absdir}/ndb/include/logger"
includes="${includes} -I${absdir}/ndb/src/mgmapi"
includes="${includes} -I${absdir}/ndb/src/kernel/error"
includes="${includes} -I${absdir}/ndb/include/transporter"
includes="${includes} -I${absdir}/ndb/test/include"
includes="${includes} -I${absdir}/ndb/src/ndbapi"
includes="${includes} -I${absdir}/bdb/build_unix"
includes="${includes} -I${absdir}/extra/yassl/taocrypt"
includes="${includes} -I${absdir}/extra/yassl/include"
includes="${includes} -I${absdir}/extra/yassl/taocrypt/include"
includes="${includes} -I${absdir}/extra/yassl/mySTL"
includes="${includes} -I${absdir}/innobase/include"
includes="${includes} -I${absdir}/os2/include"
includes="${includes} -I${absdir}/mysql-test/include"
includes="${includes} -I${absdir}/cmd-line-utils/libedit/np"
includes="${includes} -I${absdir}/cmd-line-utils"
includes="${includes} -I${absdir}/cmd-line-utils/libedit"

export JVMOPTS="-J-Xms512m -J-Xmx1536m -J-XX:PermSize=256m -J-XX:MaxPermSize=512m -J-XX:NewSize=256m"

./_parse_project.sh $project ${defines} ${includes} $@
