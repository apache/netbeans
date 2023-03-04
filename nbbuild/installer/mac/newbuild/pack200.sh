#!/bin/sh -x
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

verifyClassName=
verifyClassPath=
doVerify=0

#it slows down building
#if [ -n "$2" ] && [ -n "$3" ] ; then
#   verifyClassName="$2"
#   verifyClassPath="$3"
#   doVerify=1
#fi

javaPath=`/usr/libexec/java_home --version 1.8`
#javaPath="/Library/Java/JavaVirtualMachines/1.6.0_65-b14-462.jdk/Contents/Home"
unpackCommand="$javaPath/bin/unpack200"
javaCommand="$javaPath/bin/java"
packCommand="$javaPath/bin/pack200"

verify(){
  filenamePacked="$1"
  filenameSource="$2"
  tmpFile="$2.tmp"
  $unpackCommand "$1" "$tmpFile"
  result=1
  if [ 0 -eq $? ] ; then
	$javaCommand -cp "$verifyClassPath" "$verifyClassName" "$tmpFile" >/dev/null
	result=$?
  fi

  if [ -f "$tmpFile" ] ; then
      rm "$tmpFile"
  fi

  return $result
}

for f in `find $1 -name "*.jar"`
do
  bn=`basename $f`
  if  [ "$bn" != "jhall.jar" ] && [ "$bn" != "derby.jar" ] && [ "$bn" != "derbyclient.jar" ]
  then
    if [ ! -z "$dont_pack_anything" ] && [ "$dont_pack_anything" == "y" ] ; then
        echo "Skipping packing of $f"
        continue
    fi
    if [ ! -z "$dont_pack_localization_jars" ] && [ "$dont_pack_localization_jars" == "y" ]; then
        if [ ! -z `echo $f | grep "/locale/"` ]; then
            echo "Skipping packing of localization jars: $f"
            continue
        fi
    fi
    if [ -f "$f.pack" ] || [ -f "$f.pack.gz" ] ; then 
        echo "Packed file $f.pack(.gz) exists, skipping packing of the original file $f"
        continue
    fi
    if [ -f `echo $f | sed 's/.jar/.jad/'` ] ; then
        echo "Jar Descriptor (.jad) exists, skipping packing of the original file $f"
        continue
    fi
    if [ 2 -eq `unzip -l "$f" 2>/dev/null | grep "META-INF/" | sed "s/.*META-INF\///g" | grep "\.SF\|\.RSA\|\.DSA"| wc -l` ] ; then
        echo "Jar file $f is signed, skipping packing"
        continue
    fi

    echo Packing $f
    $packCommand -J-Xmx256m -g $f.pack $f
    if [ 0 -eq $? ] ; then
        res=0
        if [ 1 -eq $doVerify ] ; then
	    verify $f.pack $f
            res=$?
	fi

        if [ 0 -eq $res ] ; then
            chmod `stat -f %Lp $f` $f.pack && touch -r $f $f.pack
            rm $f
        else
            echo Error verification packed jar : $f
	    rm $f.pack
        fi
    else
	if [ -f $f.pack ] ; then
	    echo Error packing jar : $f
	    rm $f.pack
	fi
    fi
  fi
done

