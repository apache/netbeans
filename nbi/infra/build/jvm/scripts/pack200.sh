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

ROOT_DIR=`pwd`

for src in `find . -name "*.jar"`
do
    name=`basename "$src"`
    if [ "$name" != "jce.jar" ] && \
       [ "$name" != "local_policy.jar" ] && \
       [ "$name" != "sunjce_provider.jar" ] && \
       [ "$name" != "sunpkcs11.jar" ] && \
       [ "$name" != "US_export_policy.jar" ] && \
       [ "$name" != "sunmscapi.jar" ] ; then
        echo "Packing $src ..."
        dest="$src.pack.gz"
        "$ROOT_DIR"/bin/pack200 -J-Xmx1024m "$dest" "$src"
        if [ 0 -eq $? ] ; then
	   echo "... unpacking $dest..."
           "$ROOT_DIR"/bin/unpack200 "$dest" "$src.tmp.jar"
           if [ 0 -eq $? ] ; then
	       echo "... OK"
           else 
               echo "... failed"
               rm "$dest";
           fi     
           rm -f "$src.tmp.jar"
        else 
           echo "... failed"
           rm -f "$dest"
        fi 
    fi
done

for src in `find . -name "*.jar.pack.gz"`
do
	jar=`echo "$src" | sed "s/\.pack\.gz//"`
	rm -f "$jar"
done