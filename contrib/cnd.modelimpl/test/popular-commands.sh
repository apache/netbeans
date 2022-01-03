#! /bin/bash -x
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

base="<change-me>"
module=${base}/modelimpl
unit=${module}/test/unit

golden_dir="${unit}/data/goldenfiles/org/netbeans/modules/cnd/modelimpl/trace/FileModelTest"


log=/tmp/log
new_golden_files=`grep "OUTPUT Difference" ${log} | grep "AssertionFailedError" |  awk '{print $8}' `

# total diff
for F in ${new_golden_files}; do BASE=`basename $F`; GOLD=${golden_dir}/${BASE}; echo "==================== ${BASE} ===================="; diff $F ${GOLD}; done 

echo ${golden_dir}
# moving ALL golden files from the work directory to the reference directory
cp -f ${new_golden_files} ${golden_dir}
