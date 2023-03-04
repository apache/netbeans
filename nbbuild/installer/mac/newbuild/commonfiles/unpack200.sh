#!/bin/sh

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
unpack_dir=$1
jdk_home=$2
set -e

echo Calling unpack200 in $unpack_dir
if [ -d "$unpack_dir" ]; then
    cd "$unpack_dir"
    for x in `find . -name \*.jar.pack` ; do
        jar=`echo $x | sed 's/jar.pack/jar/'`
        "$jdk_home/bin/unpack200" $x $jar
        touch -r $x $jar
        rm $x
    done
else
    echo "$unpack_dir" does not exist.
fi

exit 0
