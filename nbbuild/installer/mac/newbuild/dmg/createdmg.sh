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

dmgname=$1
volname=$2
outputdir=$3
pwd

bunzip2 -d -c `dirname $0`/template.sparseimage.bz2 > ./dist/template.sparseimage

mkdir ./dist/mountpoint
hdiutil mount -verbose -mountpoint ./dist/mountpoint ./dist/template.sparseimage

rm -rf ./dist/mountpoint/*
echo "Running rsync..."
rsync -a ./dist_pkg/inst_package/ ./dist/mountpoint/
echo "Running diskutil rename..."
diskutil rename `pwd`/dist/mountpoint "$volname"
echo "Running hdiutil detach..."
hdiutil detach -verbose ./dist/mountpoint

if [ ! -z $outputdir ]; then
    mkdir -p ./dist/$outputdir
fi

echo "Running hdiutil create..."
hdiutil create -verbose -srcdevice `pwd`/dist/template.sparseimage ./dist/"$outputdir$dmgname"
rm -f ./dist/template.sparseimage
rmdir ./dist/mountpoint
