#!/bin/bash
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

function get_public_packages() {
    prj=$1

    nbp="${prj}/nbproject"
    if [ ! -d $nbp ]; then	
        echo "Not a directory: ${nbp}"
    fi

    xml=${prj}/nbproject/project.xml
    if [ ! -r ${xml} ]; then	
        echo "Can not read ${xml}"
    fi

    inside="n"
    while read -r line || [[ -n "$line" ]]; do
        if [ "${line}" = "<public-packages>" ]; then
            inside="y"
        elif [ "${line}" = "</public-packages>" ]; then
            inside="n"
        elif [ "${inside}" = "y" ]; then
            # if you need just package name, then use 2 lines below
            #package="${line//<package>/}"
            #package="${package//<\/package>/}"
            #echo "${package}"
            echo "${line}"
        fi
    done < ${xml}
}

jars=`ls *.jar | sort`

echo "----- Below is the text to insert into project properties -----"

echo ""; echo ""; echo ""; echo ""; 
echo "#IMPORTANT: we got messages per *.jar like:"
echo "#WARNING [org.netbeans.core.startup.InstalledFileLocatorImpl]: module org.netbeans.libs.clank in /opt/netbeans/cnd does not own modules/ext/org-clang-lex.jar at org.netbeans.LocaleVariants.findLogicalPath(LocaleVariants.java:271)"

echo "#release.external/*.jar used for code assistance"
for j in $jars; do 
    echo "release.external/$j=modules/ext/$j"
done

echo ""; echo ""
echo "#properties below are used to provide code assistance for clank built from sputnik"
for j in $jars; do 
    echo "file.reference.$j=external/$j"
done

echo ""; echo ""
echo "#properties below are used for javadoc"
for j in $jars; do 
    just_name=`echo $j |  cut -d'.' -f1`
    with_dots="${just_name//-/.}"
    echo "javadoc.reference.$j=\${sputnik}/modules/${with_dots}/src"
done

echo ""; echo ""
echo "#properties below are used to go into clank sources"
for j in $jars; do 
    just_name=`echo $j |  cut -d'.' -f1`
    with_dots="${just_name//-/.}"
    echo "source.reference.$j=\${sputnik}/modules/${with_dots}/src"
done


echo ""; echo ""
echo "----- Below is the list of all public packages to insert into <public-packages> section in project.xml -----"
for j in $jars; do 
    just_name=`echo $j |  cut -d'.' -f1`
    with_dots="${just_name//-/.}"
    dir="${SPUTNIK}/modules/${with_dots}"
    get_public_packages ${dir}
done


echo ""; echo ""
echo "----- Below is the list of classpaths to insert into as <class-path-extension> into project.xml -----"

for j in $jars; do 
    echo "<class-path-extension>"
    echo "    <runtime-relative-path>ext/${j}</runtime-relative-path>"
    echo "    <binary-origin>external/${j}</binary-origin>"
    echo "</class-path-extension>"
done
