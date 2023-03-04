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
nb_dir=$1

if [ -z "$1" ]; then
    echo "usage: $0 nb_dir"
    exit
fi

echo Complete installation $nb_dir

if [ -d "$nb_dir" ]
then
    # remove tmpnb first
    rm -Rf /tmp/tmpnb

    cd "$nb_dir"
    cd Contents/Resources/NetBeans*/bin

    #issue 209263
    #run IDE in headless mode
    echo Run IDE in headless mode
    sh netbeans -J-Dnetbeans.close=true --nosplash -J-Dorg.netbeans.core.WindowSystem.show=false -J-Dorg.netbeans.core.WindowSystem.show=false --userdir /tmp/tmpnb --modules --update-all
    exit_code=$?
    echo Run IDE returns exit code: $exit_code
    if [ ! -d /tmp/tmpnb/var/cache ]; then
        echo Warning: No caches found -> exiting
        exit
    fi

    cd /tmp/tmpnb/var/cache
    # zip -r populate.zip netigso
    zip -r -q populate.zip netigso

    # remove useless files
    # rm -r netigso
    # rm -r lastModified
    # rm splash* # if any
    rm -r netigso lastModified catalogcache splash*

    # copy into nb/var/cache
    mkdir -p "$nb_dir"/Contents/Resources/NetBeans/nb/var/cache/
    cp -v * "$nb_dir"/Contents/Resources/NetBeans/nb/var/cache/

    # copy IDE log to var/log/populate_caches.log
    mkdir -p "$nb_dir"/Contents/Resources/NetBeans/nb/var/log/
    cp -v ../log/messages.log "$nb_dir"/Contents/Resources/NetBeans/nb/var/log/populate_caches.log

    # remove tmpnb
    cd "$nb_dir" 
    rm -Rf /tmp/tmpnb

fi
