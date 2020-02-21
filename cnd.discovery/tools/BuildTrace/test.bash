#!/bin/bash
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2016 Oracle and/or its affiliates. All rights reserved.
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

export STUDIO_LIB=/opt/solarisstudiodev/lib/compilers
export __CND_TOOLS__=gcc
export __CND_BUILD_LOG__=`pwd`/dist/SunOS-Previse/OracleSolarisStudio-Solaris-x86/log.txt
rm -rf ${__CND_BUILD_LOG__}
rm -rf dist/SunOS-Previse/OracleSolarisStudio-Solaris-x86/out*
export LD_PRELOAD=libdiscover.so:libBuildTrace.so
export LD_LIBRARY_PATH_32=${STUDIO_LIB}:`pwd`/dist/SunOS-Previse/OracleSolarisStudio-Solaris-x86:${LD_LIBRARY_PATH}
export LD_LIBRARY_PATH_64=${STUDIO_LIB}/amd64:`pwd`/dist/SunOS-Previse_64/OracleSolarisStudio-Solaris-x86:${LD_LIBRARY_PATH_64}
export SUNW_DISCOVER_OPTIONS="-w -"

csh compile.bash 2&> dist/SunOS-Previse/OracleSolarisStudio-Solaris-x86/out1.txt
bash compile.bash 2&> dist/SunOS-Previse/OracleSolarisStudio-Solaris-x86/out2.txt
sh compile.bash 2&> dist/SunOS-Previse/OracleSolarisStudio-Solaris-x86/out3.txt

export LD_PRELOAD=

cat ${__CND_BUILD_LOG__} | awk -f ./test.awk
rc=$?
if [ $rc != 0 ]
then
    exit $rc
fi

res=0
cat dist/SunOS-Previse/OracleSolarisStudio-Solaris-x86/out*.txt | egrep "__logprint\(|execl\(|execle\(|execlp\(|execv\(|execve\(|execvp\(|posix_spawn\(|posix_spawnp\("
rc=$?
if [ $rc = 0 ]
then
    res=1
fi
exit $res