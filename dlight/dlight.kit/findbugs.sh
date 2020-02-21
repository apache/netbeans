#!/bin/sh -x
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

fb_home="$1"
workspace="$2"
out="$3"

prj="/tmp/dlight.fbp"
echo "<Project filename=\"DLight\" projectName=\"DLight\">" > ${prj}
for D in `ls -d ${workspace}/dlight*`; do
	if [ -d $D/build/classes ]; then
		echo "    <Jar>$D/build/classes</Jar>" >> ${prj}
		echo "    <AuxClasspathEntry>$D/src</AuxClasspathEntry>" >> ${prj}
	fi
done
echo "<SuppressionFilter>"  >> ${prj}
echo "    <LastVersion value=\"-1\" relOp=\"NEQ\"/>"  >> ${prj}
echo "</SuppressionFilter>"  >> ${prj}
echo "</Project>" >> ${prj}

${fb_home}/bin/findbugs -maxHeap 1536 -textui -project ${prj} -xml -output ${out}
