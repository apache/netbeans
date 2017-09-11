#!/bin/sh
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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

cd `dirname $0`
MYDIR=`pwd`

WDIR=/tmp/${USER}/ps$$
rm -rf ${WDIR}
mkdir -p ${WDIR}
LWDIR=/tmp/${USER}/lps$$
rm -rf ${LWDIR}
mkdir -p ${LWDIR}
LHOST=`hostname`.`cat /etc/resolv.conf | grep domain| sed 's/domain \(.*\)/\1/'`
LUSER=$USER

build() {
   HOST=$1
   shift
   CONFIGS=$@

   printf "Building ${CONFIGS} on host ${HOST} ... "
   do_build $HOST $CONFIGS
   if [ $? -eq 0 ]; then
      echo OK
   else
      echo FAIL
      exit 1
   fi
}

do_build() {
   HOST=$1
   shift
   CONFIGS=$@

   cd ${MYDIR}
   tar cf ${LWDIR}/ps.tar ./PtySupport >> build.log 2>&1
   cat << EOF | ssh ${HOST} sh -s 
   rm -rf ${WDIR}
   mkdir -p ${WDIR}
EOF

   scp ${LWDIR}/ps.tar ${HOST}:${WDIR} >> build.log 2>&1

   cat << EOF | ssh ${HOST} sh -s 
   cd ${WDIR}
   tar xf ./ps.tar || return 1 
   cd PtySupport
   PATH=${PATH}:/usr/ccs/bin
   export PATH

   for i in ${CONFIGS}; do
      make CONF=\$i >> build.log 2>&1 || return 1
   done
   cd dist
   scp -r * ${LUSER}@${LHOST}:${LWDIR} >> build.log 2>&1
EOF
}

copy_to_release() {
   echo Copying executables...
   set -x
   cp ${LWDIR}/Solaris_x64/GNU-Solaris-x86/ptysupport ../release/bin/nativeexecution/SunOS-x86_64/pty
   cp ${LWDIR}/Solaris_x86/GNU-Solaris-x86/ptysupport ../release/bin/nativeexecution/SunOS-x86/pty
   cp ${LWDIR}/MacOS_x64/GNU-MacOSX/ptysupport ../release/bin/nativeexecution/MacOSX-x86_64/pty
   cp ${LWDIR}/MacOS_x86/GNU-MacOSX/ptysupport ../release/bin/nativeexecution/MacOSX-x86/pty
   cp ${LWDIR}/Linux_x86/GNU-Linux-x86/ptysupport ../release/bin/nativeexecution/Linux-x86/pty
   cp ${LWDIR}/Linux_x64/GNU-Linux-x86/ptysupport ../release/bin/nativeexecution/Linux-x86_64/pty
   cp ${LWDIR}/Linux_sparc64/GNU-Linux-Sparc/ptysupport ../release/bin/nativeexecution/Linux-sparc_64/pty
   cp ${LWDIR}/Solaris_sparc/GNU-Solaris-Sparc/ptysupport ../release/bin/nativeexecution/SunOS-sparc/pty
   cp ${LWDIR}/Solaris_sparc64/GNU-Solaris-Sparc/ptysupport ../release/bin/nativeexecution/SunOS-sparc_64/pty
   # Only 32-bit version for Windows...
   cp ${LWDIR}/Windows_x86/Cygwin-Windows/ptysupport.exe ../release/bin/nativeexecution/Windows-x86/pty
   cp ${LWDIR}/Windows_x86/Cygwin-Windows/ptysupport.exe ../release/bin/nativeexecution/Windows-x86_64/pty
}


