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

counter=0;
output_file=./js/files.js
type digest >> /dev/null 2>&1
if [ 0 -eq $? ] ; then
    alg=`type -p digest`
    alg="$alg -a md5"
else
    type sha256sum >> /dev/null 2>&1
    if [ 0 -eq $? ] ; then
        alg=`type -p sha256sum`
    else 
        type gmd5sum >> /dev/null 2>&1
        if [ 0 -eq $? ] ; then
            alg=`type -p gmd5sum`
        else
            type md5 >> /dev/null 2>&1
            if [ 0 -eq $? ] ; then
                alg=`type -p md5`
                alg="$alg -q"
            fi
        fi
    fi
fi
if [ -z "$alg" ] ; then
	echo "Cannot find MD5 calculating programm"
	exit 1
else 
	echo "...getting MD5 with the help of $alg"
fi


while [ $# != 0 ] ; do	
	echo "Target directory : $1"		
	for nextfile in `ls -1 "$1"` ; do
		nextfile="$1"/"$nextfile"
		if [  -f "$nextfile" ] ; then			
			if [ 0 -eq $counter ] ; then
				mkdir -p `dirname "$output_file"`
				rm -f "$output_file"
				#echo "file_names = new Array();" >> "$output_file"
				#echo "file_sizes = new Array();" >> "$output_file"
				#echo "file_md5s  = new Array();" >> "$output_file"
			fi
			name=`basename "$nextfile"`
			echo 
			echo "... file : `basename $nextfile`"
			size=`stat -Lc %s $nextfile 2>/dev/null`
			echo "... size : $size"
			md5=`$alg "$nextfile" | sed "s/ .*//g"`
			echo "...  md5 : $md5"
			#echo "file_names["$counter"]=\"$1/$name\";" >> "$output_file"
			#echo "file_sizes["$counter"]=$size;" >> "$output_file"
			#echo "file_md5s["$counter"]=\"$md5\";" >> "$output_file"
			echo "add_file(\"$1/$name\", $size, \"$md5\", \"en,$LOCALES\");" >> "$output_file"
			counter=`expr $counter + 1`
		fi
        done
	shift
done
