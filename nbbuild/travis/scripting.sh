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
  BASE=graalvm-ce-1.0.0-rc12
  URL=https://github.com/oracle/graal/releases/download/vm-1.0.0-rc12/$BASE-linux-amd64.tar.gz
  curl -L $URL --output graalvm.tgz
  tar fxz graalvm.tgz
  GRAALVM=`pwd`/$BASE
fi

# test on regular VM

ant -f platform/api.scripting/build.xml test
ant -f ide/libs.graalsdk/build.xml test
# may also following Graal fixes:
# commit 6c2ea38719a68fb8bb258a8acf76420d0e99a963
#    Also query the contextClassLoader for available languages
# commit e38aa347e1de8f8f0474247eb90d193e5ea373d0
#    Always try to locate the implementation by service loader
#
ant -f webcommon/libs.graaljs/build.xml test
ant -f profiler/profiler.oql/build.xml test
ant -f platform/api.htmlui/build.xml test

$GRAALVM/bin/gu install python
$GRAALVM/bin/gu install R

# test on GraalVM

JAVA_HOME=$GRAALVM ant -f platform/api.scripting/build.xml test
JAVA_HOME=$GRAALVM ant -f ide/libs.graalsdk/build.xml test

JAVA_HOME=$GRAALVM ant -f webcommon/libs.graaljs/build.xml test
JAVA_HOME=$GRAALVM ant -f profiler/profiler.oql/build.xml test

