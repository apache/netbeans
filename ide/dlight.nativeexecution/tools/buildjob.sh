#! /bin/bash
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

if [ -z $1 ]; then
  echo "No platform specified"
  exit 1
fi

if [ -z $2 ]; then
  echo "No CPU specified"
  exit 2
fi

OSFAMILY=$1
CPUFAMILY=$2
PLATFORM=${OSFAMILY}-${CPUFAMILY}

echo "Platform : ${PLATFORM}"

SOURCES=". pty killall unbuffer"

MAKE=`which gmake || which make`

for dir in $SOURCES; do
    (
        cd $dir
        $MAKE CONF=${PLATFORM} OSFAMILY=${OSFAMILY}
        cd -
    )
done

build_all_dir="buildall"

mkdir -p "$build_all_dir"

find "../release/bin/nativeexecution/" "unbuffer/dist/" "pty/dist" "killall/dist" -not -name "*.sh" -type f -exec cp {} $build_all_dir \;

echo "================================================"
find "$build_all_dir" -exec file {} + 

