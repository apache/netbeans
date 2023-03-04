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
# It is used for generating xml/*/jar.list files.
#
# Run it from xml/.. directory.
# It is necessary to call "ant netbeans" before -- list files
# are generated from content of netbeans directories.
#

# reset old list
JAR_LIST="xml/jar.list"
JAR_LIST_TEMP="${JAR_LIST}.temp"
echo -n > $JAR_LIST_TEMP

for module in api catalog core css tax text-edit tools tree-edit xsl schema; do
    MODULE_HOME="xml/${module}"
    MODULE_JAR_LIST="jar.list"
    MODULE_JAR_LIST_TEMP="${MODULE_JAR_LIST}.temp"

    cd xml/${module}
    rm -f ${MODULE_JAR_LIST_TEMP}

    ## netbeans
    find netbeans -type f -name "*.jar" | grep -v "_ja\." >> ${MODULE_JAR_LIST_TEMP}

    ## sort
    cat ${MODULE_JAR_LIST_TEMP} | sort > ${MODULE_JAR_LIST}
    rm ${MODULE_JAR_LIST_TEMP}
    cd ../..

    cat ${MODULE_HOME}/${MODULE_JAR_LIST} >> $JAR_LIST_TEMP
done

cat ${JAR_LIST_TEMP} | sort > ${JAR_LIST}
rm ${JAR_LIST_TEMP}
