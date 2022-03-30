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

# 
files=$(grep -L 'errors="0".*failures="0"' ./*/*/build/test/*/results/TEST-*.xml 2> /dev/null) || exit 0

echo =================== JUnit Report Summary / failed tests ===================
realfiles=''
for file in $files ; do 
    TEST=$(xmllint --xpath '//testsuite[@errors>0 or @failures>0]/@name' $file 2>/dev/null)
    status=$?

    if [ $status -eq 0 ]; then
        realfiles="$realfiles $file"
        echo
        echo $TEST | cut -f2 -d '=' | tr -d '"'
        xmllint --xpath '//testsuite/testcase[./failure]/@name' $file 2> /dev/null | sed -r 's/name="([^"]+)"/     failed: \1\n/g' 
        xmllint --xpath '//testsuite/testcase[./error]/@name' $file 2> /dev/null | sed -r 's/name="([^"]+)"/     errored: \1\n/g' 
    fi
done

echo
echo ====================== JUnit failure details ===============================
echo
for file in $realfiles ; do 
    classname=$(xmllint --xpath '//testsuite[@errors>0 or @failures>0]/@name' $file 2>/dev/null | cut -f2 -d '=' | tr -d '"')
    echo Suite: $classname 
    
    for err in $(xmllint --xpath "//testsuite/testcase[@classname='${classname}'][./error]/@name" $file 2> /dev/null | sed -r 's/name="([^"]+)"/\1/g') ; do 
        msg=$(xmllint --xpath "//testsuite/testcase[@classname='${classname}' and @name='${err}']/error/@message" $file 2> /dev/null | sed -r 's/message="([^"]+)"/\1/g' )
        echo "      $err ERRORED : $msg"
        xmllint --xpath "//testsuite/testcase[@classname='${classname}' and @name='${err}']/error/text()" $file 2> /dev/null | sed -r 's/^(.*$)/          \1/g'
    done 
    for err in $(xmllint --xpath "//testsuite/testcase[@classname='${classname}'][./failure]/@name" $file 2> /dev/null | sed -r 's/name="([^"]+)"/\1/g') ; do 
        msg=$(xmllint --xpath "//testsuite/testcase[@classname='${classname}' and @name='${err}']/failure/@message" $file 2> /dev/null | sed -r 's/message="([^"]+)"/\1/g' )
        echo "      $err FAILED : $msg"
        xmllint --xpath "//testsuite/testcase[@classname='${classname}' and @name='${err}']/failure/text()" $file 2> /dev/null | sed -r 's/^(.*$)/          \1/g'
    done 
    text=$(xmllint --nocdata --xpath "//testsuite//system-out/text()" $file 2> /dev/null)
    [ -n "$text" ] && { echo "Stdout ----------%<----------%<-------------%<-------------%<---------------" ; echo "$text" ; }
    text=$(xmllint --nocdata --xpath "//testsuite//system-err/text()" $file 2> /dev/null)
    [ -n "$text" ] && { echo "Stderr ----------%<----------%<-------------%<-------------%<---------------" ; echo "$text" ; }
    echo "------------- End suite $classname ------------"
    
done
echo
echo ======================= End of JUnit report ===============================
