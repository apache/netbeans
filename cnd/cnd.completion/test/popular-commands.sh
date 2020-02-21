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


base="<change me>"
module=${base}/completion
unit=${module}/test/unit

golden_dir="${unit}/data/goldenfiles/org/netbeans/modules/cnd/completion/impl/xref/ReferencesTestCase/"


log=/tmp/log

copied_golden_files=`grep "Files differ" ${log} | grep "AssertionFailedError" | grep "completion" | awk '{print $6}' `

for F in `echo $copied_golden_files`; do D=`dirname $F`; BASE=`basename $F`; REF=`ls $D/*.ref`; GOLD=`ls $D/*.golden`; echo "==================== ${BASE} ===================="; diff ${REF} ${GOLD}; done


# moving ALL golden files from the work directory to the reference directory
for F in `echo $copied_golden_files`; do D=`dirname $F`; BASE=`basename $F`; REF=`ls $D/*.ref`; GOLD=`ls $D/*.golden`; cp $REF $golden_dir ; done          
