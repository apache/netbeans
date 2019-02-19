#!/bin/sh -x

#
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
#
chown_dir=$1
unpack_dir=$2
set -e

echo Changing ownership for $chown_dir
chown -R root:admin "$chown_dir"

echo Calling unpack200 in $unpack_dir
cd "$unpack_dir"
for x in `find . -name \*.jar.pack` ; do
    jar=`echo $x | sed 's/jar.pack/jar/'`
    /System/Library/Frameworks/JavaVM.framework/Versions/1.5/Home/bin/unpack200 -r $x $jar
done

exit 0
