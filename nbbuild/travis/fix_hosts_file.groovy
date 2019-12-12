#!/usr/bin/env groovy
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/* 
 * Fixes Travis /etc/hosts to allow run platform/core.network tests.
 * Proxy tests fails if they resolves that hostname ip is equals to localhost ip.
 */

def hostname = "hostname".execute().text.trim()

def fixedHostsFileContent = new StringBuilder("## File modified by nbbuild/travis/fix_hosts_file.groovy\n")
def hostsFile = new File('/etc/hosts')
hostsFile.eachLine { line ->
    if (line =~ /127.0.0.1/) {
        fixedHostsFileContent.append("# ").append(line).append("\n")
        fixedHostsFileContent.append(line - hostname).append("\n")
    } else {
        fixedHostsFileContent.append(line).append("\n")
    }
}

hostsFile.write(fixedHostsFileContent.toString())
