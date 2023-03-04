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

set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}

. ./build-private.sh

export ANT_OPTS

if [ -z "$JVM_BUILDS_HOST" ] ; then
      echo "JVM_BUILDS_HOST environment variable is not specified"
      exit 1
fi

OPTIONS=--noconfig

ant ${OPTIONS}  -f ${DIRNAME}/build.xml\
      -Dtarget.platform=${TARGET_PLATFORM}\
      -Dglassfish.builds.host=\"${JVM_BUILDS_HOST}\"\
      $*

ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
     echo "ERROR: $ERROR_CODE - Bundle JVM creation build failed"
     exit $ERROR_CODE;
fi