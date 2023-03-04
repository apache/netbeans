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
    echo "usage: $0 jarsDir nbDir"
    echo "jarsDir is the dir with unpacked .jar files"
    echo "nbDir is the dir with NetBeans sources containing update_tracking dirs to be processed"
    exit 1;
fi

jarsDir="$1"
nbDir="$2"

cd "$jarsDir"

for jar in $(find . -name "*.jar")
do
    echo JAR FILE = "$jar"
    #get first number from cksum output
    crc32=`cksum -o 3 "$jar" | sed 's/\([0-9]*\).*/\1/'`
    #get jar's relative path without 'cluster' folder (the same as used in xml)
    jar_subpath=`echo $jar | sed 's/^.\/[a-z0-9\.]*\///'`
    jar_name=`basename $jar 2>&1`
    #find xml file (within update_tracking dirs) in which this jar is mentioned
    update_tracking_xml_file=`ls "$nbDir"/*/update_tracking/*.xml | xargs grep -l "$jar_subpath"`

    #if xml file is found replace the crc32
    if [ ! -z "$update_tracking_xml_file" ] && [ -f "$update_tracking_xml_file" ] ; then
        cp "$update_tracking_xml_file" "$update_tracking_xml_file".back

        jar_subpath_for_sed=`echo $jar_subpath | sed "s/\\\//\\\\\\\\\//g"`
        sed '/'$jar_subpath_for_sed'/s/crc=\"[0-9]*\"/crc=\"'$crc32'\"/' < "$update_tracking_xml_file".back > "$update_tracking_xml_file"

        rm -rf "$update_tracking_xml_file".back
        echo "File '$update_tracking_xml_file' is processed"
       # echo "jar file = $jar"
       # echo "new crc32 = $crc32"
       # echo ""
    fi
done
