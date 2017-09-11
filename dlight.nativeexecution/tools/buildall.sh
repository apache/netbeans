#/bin/bash
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

# To build in debug mode export DEBUG=Y variable

# To get this script work, set variables:
#  dlight   - if in DEBUG mode           - path to temporary dlight directory (where pty is stored)
#  CC       - if building with DevStudio - path to CC binary

CFLAGS_EXTRA=""
LDFLAGS_EXTRA=""


case "$1" in
  -d | --debug )
      DEBUG=TRUE;
      dlight=${dlight:-"/tmp/dlight_ilia/eb486d37"}
      CFLAGS_EXTRA="${CFLAGS_EXTRA} -g -O0"
      shift
      ;;
esac

if [ -z "$DEBUG" ]; then
    CFLAGS_EXTRA="${CFLAGS_EXTRA} -s -O2"
    LDFLAGS_EXTRA="${LDFLAGS_EXTRA} -s"
fi

sources=". pty killall unbuffer"

script_dir=`pwd`

for dir in $sources; do
    (
        cd $dir
        sh "${script_dir}/build.sh" clean
        sh "${script_dir}/build.sh" clean-all 2> /dev/null
        sh "${script_dir}/build.sh" $@ CFLAGS_EXTRA=\"$CFLAGS_EXTRA\" LDFLAGS_EXTRA=\"$LDFLAGS_EXTRA\"
        cd -
    )
done

build_all_dir="buildall"

rm -rf "$build_all_dir"
mkdir -p "$build_all_dir"

find "../release/bin/nativeexecution/" "unbuffer/dist/" "pty/dist" "killall/dist" -not -name "*.sh" -type f -exec cp {} $build_all_dir \;

if [ "x$DEBUG" != "x" ]; then
    sed -i '/copyFile(localFile, safeLocalFile);/c\ /* copyFile(localFile, safeLocalFile); */' ../src/org/netbeans/modules/nativeexecution/api/util/HelperUtility.java    
    PTY=`find "pty/dist" -name pty`
    find "${dlight}" -name pty -exec sh -c 'lsof -t $1 | xargs kill 2> /dev/null' - {} \; 
    find "${dlight}" -name pty -exec cp $PTY {} \;
    find "${dlight}" -name pty -exec file {} +
fi

echo "================================================"
find "$build_all_dir" -exec file {} + 

