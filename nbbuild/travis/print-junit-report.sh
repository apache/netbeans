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

echo junit report / failed tests:

ls ./*/*/build/test/*/results/TEST-*.xml | while read file ;
do
    TEST=$(xmllint --xpath '//testsuite[@failures>0]/@name' $file 2>/dev/null)
    status=$?

    if [ $status -eq 0 ]; then
        echo
        echo $TEST | cut -f2 -d '=' | tr -d '"'
        xmllint --xpath '//testsuite/testcase[./failure]/@name' $file | cut -f2 -d '=' | xargs -L1 echo "    failed:"
    fi
done

echo end of report
