#!/bin/bash

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

set -e

# test platform
echo Making sure platform is built
if ! ant -Dcluster.config=platform build >build.log 2>&1; then
    tail -n 1000 build.log
    exit 1
fi
echo Generating signature files
if ! ant -Dcluster.config=platform gen-sigtests-release >gen.log 2>&1; then
    tail -n 1000 gen.log
    exit 2
fi
echo Platform is OK

