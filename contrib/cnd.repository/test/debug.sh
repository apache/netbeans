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
export JAR_PATH=../../suite/build/cluster/modules/org-netbeans-modules-cnd-repository.jar
export DEBUG="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5858"
java $DEBUG -cp $JAR_PATH org.netbeans.modules.cnd.repository.testbench.TestBench -p 1 -i 2 -l 0.05 -t 0 -m 0 -x 500 -lf 0.5 -f /tmp/cache
