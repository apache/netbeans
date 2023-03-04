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
if [ -z "$jdkhome" ] ; then
    # try to find JDK

    # read Java Preferences
    if [ -x "/usr/libexec/java_home" ]; then
        jdkhome=`/usr/libexec/java_home`
    fi

    # JRE
    if [ -z "$jdkhome" ] && [ -d "/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home" ]; then
        jdkhome="/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home"
    fi

fi

echo $jdkhome
