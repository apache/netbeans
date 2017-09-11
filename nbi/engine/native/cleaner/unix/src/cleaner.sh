#!/bin/sh
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
#
# Oracle and Java are registered trademarks of Oracle and/or its affiliates.
# Other names may be trademarks of their respective owners.
#
# The contents of this file are subject to the terms of either the GNU General Public
# License Version 2 only ("GPL") or the Common Development and Distribution
# License("CDDL") (collectively, the "License"). You may not use this file except in
# compliance with the License. You can obtain a copy of the License at
# http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
# License for the specific language governing permissions and limitations under the
# License.  When distributing the software, include this License Header Notice in
# each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
# designates this particular file as subject to the "Classpath" exception as provided
# by Oracle in the GPL Version 2 section of the License file that accompanied this code.
# If applicable, add the following below the License Header, with the fields enclosed
# by brackets [] replaced by your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
# 
# Contributor(s):
# 
# The Original Software is NetBeans. The Initial Developer of the Original Software
# is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
# Rights Reserved.
# 
# If you wish your version of this file to be governed by only the CDDL or only the
# GPL Version 2, indicate your decision by adding "[Contributor] elects to include
# this software in this distribution under the [CDDL or GPL Version 2] license." If
# you do not indicate a single choice of license, a recipient has the option to
# distribute your version of this file under either the CDDL, the GPL Version 2 or
# to extend the choice of license to its licensees as provided above. However, if you
# add GPL Version 2 code and therefore, elected the GPL Version 2 license, then the
# option applies only if the new code is made subject to such option by the copyright
# holder.
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
