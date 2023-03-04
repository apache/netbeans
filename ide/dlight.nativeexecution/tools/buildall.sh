#/bin/bash
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

