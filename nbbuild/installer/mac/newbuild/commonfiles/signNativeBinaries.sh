#!/bin/bash

#
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
#
if [ -z "$1" ] || [ -z "$2" ] ; then
    echo "usage: $0 appSigningIdentity appDir"
    echo "appSigningIdentity is Apple Developer ID Application certificate used for signing"
    echo "appDir is the application directory"
    exit 1;
fi

appSigningIdentity="$1"
appDir="$2"

nativeExecutionBinaries=( "/netbeans/ide/bin/nativeexecution/MacOSX-x86/unbuffer.dylib" "/netbeans/ide/bin/nativeexecution/MacOSX-x86/pty" "/netbeans/ide/bin/nativeexecution/MacOSX-x86/pty_open" "/netbeans/ide/bin/nativeexecution/MacOSX-x86/process_start" "/netbeans/ide/bin/nativeexecution/MacOSX-x86/killall" "/netbeans/ide/bin/nativeexecution/MacOSX-x86/stat" "/netbeans/ide/bin/nativeexecution/MacOSX-x86_64/unbuffer.dylib" "/netbeans/ide/bin/nativeexecution/MacOSX-x86_64/pty" "/netbeans/ide/bin/nativeexecution/MacOSX-x86_64/pty_open" "/netbeans/ide/bin/nativeexecution/MacOSX-x86_64/process_start" "/netbeans/ide/bin/nativeexecution/MacOSX-x86_64/killall" "/netbeans/ide/bin/nativeexecution/MacOSX-x86_64/stat" "/netbeans/ide/modules/lib/aarch64/libjunixsocket-native-2.5.1.dylib" "/netbeans/ide/modules/lib/x86_64/libjunixsocket-native-2.5.1.dylib")
jniBinaries=("/netbeans/platform/modules/lib/aarch64/libjnidispatch-nb.jnilib" "/netbeans/platform/modules/lib/x86_64/libjnidispatch-nb.jnilib")
profilerBinaries=("/netbeans/profiler/lib/deployed/jdk16/mac/libprofilerinterface.jnilib" "/netbeans/profiler/lib/deployed/jdk15/mac/libprofilerinterface.jnilib" )
jansiJar="/netbeans/java/maven/lib/jansi-2.4.0.jar"

function signBinariesFromArray() {
  arr=("$@")
  for file in "${arr[@]}"; do
    echo $appDir$file
    codesign --force --timestamp --options=runtime -s "$appSigningIdentity" -v $appDir$file
  done
}

function signBinaryFromJar() {
  jar tf $appDir$1 | grep '\.so\|\.dylib\|\.jnilib'  > filelist.txt
  while read f
  do
    if [[ "$f" == *native/Mac* ]]; then
      jar xf $appDir$1 $f
      codesign --force --timestamp --options=runtime -s "$appSigningIdentity" -v $f
      jar uf $appDir$1 $f
      rm -rf $f
    fi
  done < filelist.txt
  rm -rf filelist.txt
}

if [[ "$appDir" == *nbide* ]]; then
  signBinariesFromArray "${nativeExecutionBinaries[@]}"
  signBinariesFromArray "${jniBinaries[@]}"
fi

if [[ "$appDir" == *javase* ]]; then
  signBinariesFromArray "${profilerBinaries[@]}"
  signBinaryFromJar $jansiJar
fi