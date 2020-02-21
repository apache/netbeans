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

function scp_from_one_host() {
  HOST=$1
  SRC=$2
  DST=$3
  OPTIONS=$4
  if [ -z ${HOST} ]; then 
    echo "host is not set"
    return
  fi
  if [ -z ${DST} ]; then 
    echo "destination is not set"
    return
  fi
  if [ -z ${SRC} ]; then 
    echo "source is not set"
    return
  fi
  
  CMD="scp -r ${OPTIONS} ${HOST}:${SRC} ${DST}/"
  echo $CMD
  eval $CMD
  if [ $? -eq 0 ]; then
    echo "OK"
  else
    echo "Error"
  fi
}

if [ -z "$NB" ]; then
  echo "NB environmane variable is not set"
  exit 2
fi
BASE=$NB/cnd.remote
if [ ! -d "${BASE}" ]; then
  echo "Directory ${BASE} does not exist"
  exit 2
fi

DAT=${NB}/cnd.remote/tools/nbproject/private/download-binaries.dat
if [ ! -r "${DAT}" ]; then
  echo "Can not read ${DAT}"
  exit 2
fi

DST=${BASE}/release/bin
echo "Going to copy rfs_* binaries to ${DST}"
echo "  using hosts and directories specified in"
echo "  ${DAT}"
echo "Press enter to continue or ^C to cancel"
read t

echo
echo "Copying rfs_* binaries to ${DST} ..."
mkdir -p ${DST}

while read -r L
do
  if [ ! -z "$L" ]; then
    if [ ! "`expr substr "$L" 1 1`" = "#" ]; then    
      HOST=`echo $L | awk '{ print $1 }'`
      SRC=`echo $L | awk '{ print $2 }'`
      OPT=`echo $L | awk '{ print $3 $4 $5 $6 }'`
      echo
      echo "Copying from ${HOST} ..."
      scp_from_one_host ${HOST} ${SRC} ${DST} ${OPT}
    fi
  fi  
done < ${DAT}
