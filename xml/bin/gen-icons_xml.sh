#!/bin/sh

#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 1997, 2010 Oracle and/or its affiliates. All rights reserved.
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
#

#set -x

#
# It is used for generating xml/www/*/data/icons.xml files.
# When you set GENERATE to true, default xml/www/*/images/icons/description.properties files are generated.
#
# Run it from xml directory.
#


# 'true' or anything else
GENERATE=true

BASE=`pwd`
WWW=$BASE/www

for dir in api catalog core css tax text-edit tools tree-edit xsl schema; do
    cd $BASE/$dir

    ICONS=$WWW/$dir/images/icons
    mkdir -p $ICONS
    rm -f $ICONS/*.gif

    touch $ICONS/description.properties

    . $ICONS/description.properties

    if [ "$GENERATE" == "true" ]; then
        DESC_FILE=$ICONS/description.properties
        echo "# 
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997, 2010 Oracle and/or its affiliates. All rights reserved.
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
" > $DESC_FILE
    fi

    DATA=$WWW/$dir/data/icons.xml    
        echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<!--
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997, 2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.

The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
particular file as subject to the "Classpath" exception as provided
by Oracle in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):
-->
" > $DATA

    echo "<list module=\"$dir\">" >> $DATA

    echo
    echo "== $dir"

    for file in `find src -name "*.gif" | grep -v www | grep -v testbed | grep -v javahelp`; do
        echo "-- $file"
        NAME=`basename $file`
        KEY=`echo $file | tr \/.- ___`
        CMD="echo \$$KEY"
        DESC=`eval $CMD`

        cp $file $ICONS

        echo "    <icon name=\"$NAME\" file=\"$file\">" >> $DATA
        echo "        <description>$DESC</description>" >> $DATA
#        echo "        <screen-shot name=\"ss-$NAME\"/>" >> $DATA
        echo "        <screen-shot name=\"$NAME\"/>" >> $DATA
        echo "        <usage>" >> $DATA

        ###
        # Full Name with optional extension.
        FULL_NAME=`echo $file | awk '{A=$0; sub("src/", "", A); sub(".gif$", "", A); print A;}'`
        for usage in `grep -r -l "$FULL_NAME[.gif]*\"" ../ | grep -v ".form$" | grep -v CVS | grep -v "~$" | grep -v ".#" | grep -v "/www/" | grep -v "/javahelp/" | grep -v "/testbed/" | grep -v "TODO.xml" | grep -v "build.xml" | grep -v "/test/"`; do
            echo " - $usage"
            REF_FILE=`basename $usage`
            echo "            <source name=\"$REF_FILE\" file=\"$usage\"/>" >> $DATA
        done

        ###
        # Just Name with extension.
        for usage in `grep -r -l "\"$NAME\""    ../ | grep -v ".form$" | grep -v CVS | grep -v "~$" | grep -v ".#" | grep -v "/www/" | grep -v "/javahelp/" | grep -v "/testbed/" | grep -v "TODO.xml" | grep -v "build.xml" | grep -v "/test/"`; do
            echo " - $usage"
            REF_FILE=`basename $usage`
            echo "            <source name=\"$REF_FILE\" file=\"$usage\"/>" >> $DATA
        done

        ###
        # Just Name without extension.
        NO_EXT=`basename $file .gif`
        for usage in `grep -r -l "\"$NO_EXT\""  ../ | grep -v ".form$" | grep -v CVS | grep -v "~$" | grep -v ".#" | grep -v "/www/" | grep -v "/javahelp/" | grep -v "/testbed/" | grep -v "TODO.xml" | grep -v "build.xml" | grep -v "/test/"`; do
            echo " - $usage"
            REF_FILE=`basename $usage`
            echo "            <source name=\"$REF_FILE\" file=\"$usage\"/>" >> $DATA
        done

        echo "        </usage>" >> $DATA
        echo "    </icon>" >> $DATA


        if [ "$GENERATE" == "true" ]; then
            if [ "$DESC" == "" ]; then
                DESC="This icon represents "
            fi
            echo "$KEY=\"$DESC\"" >> $DESC_FILE
        fi

    done

    echo "</list>" >> $DATA

done
