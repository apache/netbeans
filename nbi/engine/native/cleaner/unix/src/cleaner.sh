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

deleteFiles() {
        testSymlinkErr=`test -L / > /dev/null`
        if [ -z "$testSymlinkErr" ] ; then
            isSymlink=-L
        else
            isSymlink=-h
        fi

	#wait for main thread to finish...
	sleep 3
	waitOnError=1
	tryTimes=3
	list="$1"
	if [ -n "$list" ] && [ -f "$list" ] ; then
		#echo "Using list file : $list"
		itemsNumber=`wc -l $list | sed "s/^\ *//;s/\ .*//" 2>/dev/null`
		#echo "Total items : $itemsNumber"
		counter=1
		try=$tryTimes
		allitems=`cat "$list" 2>/dev/null`
		if [ -f "$list" ] ; then 
			#echo "... remove cleaner list $list"
			rm -f "$list"
		fi
		while [ $counter -le $itemsNumber ] ; do			
			file=`echo "$allitems" | sed -n "${counter}p" 2>/dev/null`
			#echo "entry : $file"
			result=1
			if [ -n "$file" ] ; then
				#echo "... file not zero"
				if [ $isSymlink "$file" ] || [ -f "$file" ] ; then
					# file or symlink
					#echo "deleting [F] $file"
					rm -f "$file" 2>/dev/null 1>&2
					if [ $? -ne 0 ] ; then
						#echo "... can't delete $file"
						result=0
					fi 
				elif [ -d "$file" ] ; then
					# directory
					#echo "deleting [D] $file"
					rmdir "$file" 2>/dev/null 1>&2
					if [ $? -ne 0 ] ; then 
						result=0
						#echo "... can't delete $file"
					fi
				fi
			fi
			if [ 0 -eq $result ] ; then
				# try to remove it again after a short wait
				if [ $try -gt 0 ] ; then	
					try=`expr "$try" - 1`
					sleep $waitOnError
				else
					#can`t delete.. skip it
					result=1
				fi
				
			fi
			if [ 1 -eq $result ] ; then
				counter=`expr "$counter" + 1`
				try=$tryTimes
			fi		
		done				
	fi
	if [ -f "$0" ] ; then 
		#echo "... remove cleaner itself $0"
		rm -f "$0"
	fi
}

deleteFiles "$@"
