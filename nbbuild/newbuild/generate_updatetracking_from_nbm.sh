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

unzip $1 -d tmp
find tmp/netbeans -name "*.jar.pack.gz" | xargs -I [] unpack200 -r [] [].jar
find tmp/netbeans -name "*.pack.gz.jar" | grep .pack.gz.jar | sed 's/\(.*\).pack.gz.jar/mv & \1/' | sh
ant -Dnbm.filename=$1 -f $BASE_DIR/main/nbbuild/build.xml refresh-update_tracking-ml
rm -rf tmp
