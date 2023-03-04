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
        echo "# \
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
" > $DESC_FILE
    fi

    DATA=$WWW/$dir/data/icons.xml    
        echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<!--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
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
