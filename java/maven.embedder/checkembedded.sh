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

EXTERNALFILE=external/binariesembedded-list

while read -r LINE ; do
   if [[ $LINE =~ ^# ]]; then continue; fi
   if [[ -z $LINE ]]; then continue; fi
     printf 'checking dependency %s\n' "$LINE"
     artifact=`echo $LINE | cut -d ";" -f 2`
     groupId=`echo ${artifact} | cut  -d ":" -f 1`
     artifactId=`echo ${artifact} | cut -d ":" -f 2`
     version=`echo ${artifact} | cut -d ":" -f 3`
     mvn dependency:get -DgroupId=${groupId} -DartifactId=${artifactId} -Dversion=${version} | grep 'BUILD FAILURE'
done <  "$EXTERNALFILE"
