#!/bin/sh
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2009, 2016 Oracle and/or its affiliates. All rights reserved.
#
# Oracle and Java are registered trademarks of Oracle and/or its affiliates.
# Other names may be trademarks of their respective owners.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common
# Development and Distribution License("CDDL") (collectively, the
# "License"). You may not use this file except in compliance with the
# License. You can obtain a copy of the License at
# http://www.netbeans.org/cddl-gplv2.html
# or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
# specific language governing permissions and limitations under the
# License.  When distributing the software, include this License Header
# Notice in each file and include the License file at
# nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
# particular file as subject to the "Classpath" exception as provided
# by Oracle in the GPL Version 2 section of the License file that
# accompanied this code. If applicable, add the following below the
# License Header, with the fields enclosed by brackets [] replaced by
# your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# If you wish your version of this file to be governed by only the CDDL
# or only the GPL Version 2, indicate your decision by adding
# "[Contributor] elects to include this software in this distribution
# under the [CDDL or GPL Version 2] license." If you do not indicate a
# single choice of license, a recipient has the option to distribute
# your version of this file under either the CDDL, the GPL Version 2 or
# to extend the choice of license to its licensees as provided above.
# However, if you add GPL Version 2 code and therefore, elected the GPL
# Version 2 license, then the option applies only if the new code is
# made subject to such option by the copyright holder.
#
# Contributor(s):


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
