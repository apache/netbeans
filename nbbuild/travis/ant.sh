#!/bin/bash

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

set -e

function install() {
    mkdir -p nbbuild/
    cd nbbuild/
    rm -rf travisbuildjdk*
    wget https://raw.githubusercontent.com/sormuras/bach/master/install-jdk.sh
    chmod a+x install-jdk.sh
    ./install-jdk.sh --feature 11 --license GPL --target travisbuildjdk
}

function download() {
    JDKURL=https://cdn.azul.com/zulu/bin/zulu11.37.17-ca-jdk11.0.6-linux_x64.zip
    JDKSHA=0038707cd44ae1d3f535b20aab35e3cf7286371c0c7644e5d7392b52f45caa0a
    mkdir -p nbbuild/
    cd nbbuild/
    rm -rf travisbuildjdk*
    echo Downloading Java from $JDKURL
    curl -s $JDKURL -o travisbuildjdk.zip
    SUM=`sha256sum travisbuildjdk.zip | cut -f 1 -d " "`
    echo SHA-256: $SUM
    if [ "$SUM" != "$JDKSHA" ]; then
        echo Expecting $JDKSHA
        exit 1
    fi
    unzip -q travisbuildjdk.zip
    mv zulu*jdk*linux* travisbuildjdk
}

AT=`pwd`
if ! `pwd`/nbbuild/travisbuildjdk/bin/java -version >/dev/null 2>/dev/null; then
    # download
    install
    cd travisbuildjdk
    echo Java is ready in `pwd`
    ./bin/java -version
fi
cd "$AT"

echo Running ant -q $* at `pwd`
JAVA_HOME=`pwd`/nbbuild/travisbuildjdk/ ant -q $*
