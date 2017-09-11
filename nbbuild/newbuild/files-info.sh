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
