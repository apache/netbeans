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


if [ -z "$GRAALVM" ]; then
  if [ -z "$GRAAL_VERSION" ]; then
        echo "GraalVM version not set. Please specify a valid $GRAAL_VERSION string."
        exit 1
  fi
  BASE=graalvm-ce-java8-$GRAAL_VERSION
  URL=https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-$GRAAL_VERSION/graalvm-ce-java8-linux-amd64-$GRAAL_VERSION.tar.gz
  curl -L $URL --output graalvm.tgz
  tar fxz graalvm.tgz
  ls -l
  GRAALVM=`pwd`/$BASE
fi

$GRAALVM/bin/gu install python
$GRAALVM/bin/gu install R

# Test on GraalVM

JAVA_HOME=$GRAALVM ant -f platform/api.scripting/build.xml test
JAVA_HOME=$GRAALVM ant -f ide/libs.graalsdk/build.xml test

JAVA_HOME=$GRAALVM ant -f platform/core.network/build.xml test
JAVA_HOME=$GRAALVM ant -f webcommon/libs.graaljs/build.xml test
JAVA_HOME=$GRAALVM ant -f profiler/profiler.oql/build.xml test
JAVA_HOME=$GRAALVM ant -f java/nashorn.execution/build.xml test
