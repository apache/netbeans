#!/bin/sh
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

if [ -z "${MAKE}" ]; then
    if [ `uname -s` = SunOS ]; then
	MAKE=gmake
    else
        MAKE=make
    fi
fi

if [ "$1" = "-t" ]; then
	defs="TRACE=1"
else
	defs=""
fi

${MAKE} ${defs} clean all
rc32=$?
${MAKE} ${defs} 64BITS=1 clean all
rc64=$?
if [ ${rc32} -eq 0 ]; then bash -c "echo -e '\E[;32m' 32-bit build: OK"; else bash -c "echo -e '\E[;31m' 32-bit build: FAILURE"; fi
if [ ${rc64} -eq 0 ]; then bash -c "echo -e '\E[;32m' 64-bit build: OK"; else bash -c "echo -e '\E[;31m' 64-bit build: FAILURE"; fi

