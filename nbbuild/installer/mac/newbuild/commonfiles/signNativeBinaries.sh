#!/bin/sh

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
if [ -z "$1" ] || [ -z "$2" ] ; then
    echo "usage: $0 appSigningIdentity appDir"
    echo "appSigningIdentity is Apple Developer ID Application certificate used for signing"
    echo "appDir is the application directory"
    exit 1;
fi

nativeExecutionBinaries=( "/netbeans/ide/bin/nativeexecution/MacOSX-x86/unbuffer.dylib" "/netbeans/ide/bin/nativeexecution/MacOSX-x86/pty" "/netbeans/ide/bin/nativeexecution/MacOSX-x86/pty_open" "/netbeans/ide/bin/nativeexecution/MacOSX-x86/process_start" "/netbeans/ide/bin/nativeexecution/MacOSX-x86/killall" "/netbeans/ide/bin/nativeexecution/MacOSX-x86/stat" "/netbeans/ide/bin/nativeexecution/MacOSX-x86_64/unbuffer.dylib" "/netbeans/ide/bin/nativeexecution/MacOSX-x86_64/pty" "/netbeans/ide/bin/nativeexecution/MacOSX-x86_64/pty_open" "/netbeans/ide/bin/nativeexecution/MacOSX-x86_64/process_start" "/netbeans/ide/bin/nativeexecution/MacOSX-x86_64/killall" "/netbeans/ide/bin/nativeexecution/MacOSX-x86_64/stat")

appSigningIdentity="$1"
appDir="$2"

if [[ "$appDir" == *nbide* ]]; then
  for file in ${nativeExecutionBinaries[@]}; do
    echo $appDir$file
    codesign --force --timestamp --options=runtime -s "$appSigningIdentity" -v $appDir$file
  done
fi

