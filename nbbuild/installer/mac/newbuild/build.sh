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
#
set -x -e

echo Given parameters: $1 $2 $3 $4 $5 $6

if [ -z "$1" ] || [ -z "$2" ]|| [ -z "$3" ] || [ -z "$4" ] || [ -z "$5" ] || [ -z "$6" ] || [ -z "$7" ]; then
    echo "usage: $0 zipdir prefix buildnumber binaryname installer_sign_idname application_sign_idname version_number [nb_locales]"
    echo ""
    echo "zipdir is the dir which contains the zip/modulclusters"
    echo "prefix is the distro filename prefix, e.g. netbeans-hudson-trunk in netbeans-hudson-trunk-2464"
    echo "buildnumber is the distro buildnumber, e.g. 2464 in netbeans-hudson-trunk-2464"
    echo "binaryname is the basename of the zip file"
    echo "installer_sign_idname are required if the packages are to be signed, 0 if not"
    echo "application_sign_idname are required if the packages are to be signed, 0 if not"
    echo "version_number the netbeans version number"
    echo "[nb_locales] is the string with the list of locales"
    exit 1
fi

work_dir=$1
prefix=$2
buildnumber=$3
build_jdk7=0
build_jdk8=0
build_jdk11=0
binaryname=$4
installer_sign_idname=$5
application_sign_idname=$6
version_number=$7
if [ -n "${8}" ] ; then
  nb_locales=",${8}"
fi

basename=`dirname "$0"`

if [ -f "$basename"/build-private.sh ]; then
  . "$basename"/build-private.sh
fi

cd "$basename"
chmod -R a+x *.sh

commonname=$work_dir/zip/moduleclusters/ 
if [[ ( -z $build_jdk7 || 0 -eq $build_jdk7 ) && ( -z $build_jdk8 || 0 -eq $build_jdk8 )  && ( -z $build_jdk11 || 0 -eq $build_jdk11 )]]; then
    target="build-nb-dmg"
    build_jdk7=0
    build_jdk8=0
    build_jdk11=0
else
    target="build-jdk-bundle-dmg"
fi

if [ -z $en_build ] ; then
    en_build=1
fi

rm -rf "$basename"/dist_en

ant -f $basename/build.xml $target -Dlocales=$nb_locales -Dcommon.name=$commonname -Dnb.binary.name=$binaryname -Dinstaller.sign.idname="$installer_sign_idname" -Dapplication.sign.idname="$application_sign_idname" -Dprefix=$prefix -Dbuildnumber=$buildnumber -Dversionnumber=$version_number -Dbuild.jdk7=$build_jdk7 -Dbuild.jdk8=$build_jdk8 -Dbuild.jdk11=$build_jdk11 -Dgf_builds_host=$GLASSFISH_BUILDS_HOST -Djre_builds_host=$JRE_BUILDS_HOST -Djdk_builds_host=$JDK_BUILDS_HOST -Djre_builds_path=$JRE_BUILDS_PATH -Djdk7_builds_path=$JDK7_BUILDS_PATH -Djdk8_builds_path=$JDK8_BUILDS_PATH -Djdk11_builds_path=$JDK11_BUILDS_PATH -Dbinary_cache_host=$BINARY_CACHE_HOST
mv -f "$basename"/dist "$basename"/dist_en
