/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*   https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import Foundation

let netbeansURL = Bundle.main.url(forResource: "netbeans", withExtension: "", subdirectory: "NetBeans/netbeans/bin")

var args = [String]()

// add user's command line arguments
for argument in Array(CommandLine.arguments.dropFirst()) {
    args.append(argument)
}

let launchNetbeans = Process()
launchNetbeans.arguments = args
launchNetbeans.executableURL = netbeansURL
try launchNetbeans.run()

// needed to keep Dock name based on CFBundleName from Info.plist
// does not work if called from command line.
launchNetbeans.waitUntilExit()
