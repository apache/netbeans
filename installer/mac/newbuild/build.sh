#!/bin/bash

# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2008, 2016 Oracle and/or its affiliates. All rights reserved.
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

set -x -e

echo Given parameters: $1 $2 $3 $4 $5 $6

if [ -z "$1" ] || [ -z "$2" ]|| [ -z "$3" ] || [ -z "$4" ] || [ -z "$5" ] || [ -z "$6" ] || [ -z "$7" ]; then
    echo "usage: $0 zipdir prefix buildnumber build_jdk7 build_jdk8 signing_identity [nb_locales]"
    echo ""
    echo "zipdir is the dir which contains the zip/modulclusters
    echo "prefix is the distro filename prefix, e.g. netbeans-hudson-trunk in netbeans-hudson-trunk-2464"
    echo "buildnumber is the distro buildnumber, e.g. 2464 in netbeans-hudson-trunk-2464"
    echo "build_jdk7 is 1 if bundle jdk7 are required and 0 if not"
    echo "build_jdk8 is 1 if bundle jdk8 are required and 0 if not"
    echo "signing_identity is a digital identity for signing the OS X installer or 0 if not signing"
    echo "nb_locales is the string with the list of locales
    exit 1
fi

work_dir=$1
prefix=$2
buildnumber=$3
build_jdk7=$4
build_jdk8=$5
signing_identity=$6
if [ -n "$7" ] ; then
  nb_locales=",$7"
fi

basename=`dirname "$0"`

if [ -f "$basename"/build-private.sh ]; then
  . "$basename"/build-private.sh
fi

cd "$basename"
chmod -R a+x *.sh

commonname=$work_dir/zip/moduleclusters/$prefix-$buildnumber 
if [[ ( -z $build_jdk7 || 0 -eq $build_jdk7 ) && ( -z $build_jdk8 || 0 -eq $build_jdk8 ) ]]; then
    target="build-all-dmg"
    build_jdk7=0
    build_jdk8=0
else
    target="build-jdk-bundle-dmg"
fi

if [ -z $en_build ] ; then
    en_build=1
fi

if [ 0 = "${signing_identity}" ] ; then
    signing_identity=0
fi

rm -rf "$basename"/dist_en
ant -f $basename/build.xml $target -Dlocales=$nb_locales -Dcommon.name=$commonname -Dprefix=$prefix -Dbuildnumber=$buildnumber  -Dsigning_identity="${signing_identity}" -Dbuild.jdk7=$build_jdk7 -Dbuild.jdk8=$build_jdk8 -Dgf_builds_host=$GLASSFISH_BUILDS_HOST -Djdk_builds_host=$JDK_BUILDS_HOST -Djre_builds_path=$JRE_BUILDS_PATH -Djdk7_builds_path=$JDK7_BUILDS_PATH -Djdk8_builds_path=$JDK8_BUILDS_PATH -Dbinary_cache_host=$BINARY_CACHE_HOST
mv -f "$basename"/dist "$basename"/dist_en
