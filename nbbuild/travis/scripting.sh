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
  BASE=graalvm-ce-1.0.0-rc10
  URL=https://github.com/oracle/graal/releases/download/vm-1.0.0-rc10/$BASE-linux-amd64.tar.gz
  curl -L $URL --output graalvm.tgz
  tar fxz graalvm.tgz
  GRAALVM=`pwd`/$BASE
fi

# test on regular VM

ant -f platform/api.scripting/build.xml test
ant -f ide/libs.graalsdk/build.xml test
ant -f webcommon/libs.graaljs/build.xml test
ant -f platform/core.network/build.xml test

$GRAALVM/bin/gu install python
$GRAALVM/bin/gu install R

# test on GraalVM

JAVA_HOME=$GRAALVM ant -f platform/api.scripting/build.xml test
JAVA_HOME=$GRAALVM ant -f ide/libs.graalsdk/build.xml test

# currently broken. fixed by
# https://github.com/oracle/graal/commit/4c217f2b2fba77c55d05c7aa3654e13c215b5ddb
# which is likely to appear in GraalVM RC12
JAVA_HOME=$GRAALVM ant -f platform/core.network/build.xml test || echo "==== May fail ===="
JAVA_HOME=$GRAALVM ant -f webcommon/libs.graaljs/build.xml test || echo "==== Expected failure ===="

