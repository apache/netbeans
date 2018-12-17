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
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
set -x -e

if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ]; then
    echo "usage: $0 srcdir dmgname tmpdir"
    exit 1
fi

dmg=$1
tmpdir=$2
shift 2
srcdirs=$*

volname="NetBeans 6.1"

rm -f $tmpdir/template.sparseimage
bunzip2 -d -c `dirname $0`/template.sparseimage.bz2 > $tmpdir/template.sparseimage
rm -rf $tmpdir/mountpoint
mkdir $tmpdir/mountpoint
hdiutil mount -verbose -mountpoint $tmpdir/mountpoint $tmpdir/template.sparseimage
rm -rf $tmpdir/mountpoint/*
rsync -a $srcdirs --exclude .DS_Store $tmpdir/mountpoint/
diskutil rename $tmpdir/mountpoint "$volname"
hdiutil detach -verbose $tmpdir/mountpoint
rm -f "$dmg"
hdiutil create -verbose -srcdevice $tmpdir/template.sparseimage "$dmg"
rm -f $tmpdir/template.sparseimage
rmdir $tmpdir/mountpoint
