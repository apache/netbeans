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

if [ "$1" -eq "64" ]; then
    suffix_64="_64"
else
    suffix_64=
fi

osname=`uname -s`
uname_p=`uname -p`
if [ "${uname_p}" = "unknown" ]; then
    uname_p=`uname -m`
fi

case "${uname_p}" in
    --i86pc|i386|i686|x86_64)
        platform_name="${osname}-x86"
        ;;
    --sparc)
        platform_name="${osname}-sparc"
        ;;
    *)
        echo "Unknown platform: ${uname_p}"
        exit 4
        ;;
esac

platform_name="${platform_name}${suffix_64}"

#echo platform_name: ${platform_name}

#base_dir="`pwd`/../release/bin"
base_dir="`pwd`"
platform_dir=${base_dir}/${platform_name}

output=`mktemp`

if [ ! -x ${platform_dir}/rfs_test_env ]; then
    chmod u+x ${platform_dir}/rfs_test_env
fi

LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${platform_dir} \
    LD_PRELOAD=rfs_preload.so \
    RFS_TEST_ENV=1 \
    ${platform_dir}/rfs_test_env > ${output}

#cat ${output}

gold=`mktemp`
fact=`mktemp`

grep "RFS_TEST_PRELOAD" ${output} | awk '{print $2}' > ${fact}
grep "RFS_TEST_CLIENT" ${output} | awk '{print $2}' > ${gold}

while read func_name; do
    real_func_name=`echo "${func_name}" | awk -F/ '{ print $NF }'`
    grep "${func_name}" ${fact} > /dev/null
    rc=$?
    if [ $rc = 0 ]; then
        echo "${real_func_name} OK"
    else
        echo "${real_func_name} FAILED"
    fi
done < ${gold}

rm ${output}
rm ${gold}
rm ${fact}
