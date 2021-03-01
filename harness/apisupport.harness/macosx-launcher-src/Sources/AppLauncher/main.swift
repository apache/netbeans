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

import Foundation

func getDefaultDir(for directory: FileManager.SearchPathDirectory) -> String {
    let urls = FileManager.default.urls(for: directory, in: .userDomainMask)
    let defaultDir = URL(string: brandingToken!, relativeTo: urls[0])

    return defaultDir!.path
}

func processClusters(file: String) -> String {
    let appDir = Bundle.main.path(forResource: brandingToken, ofType: "")
    var clusters = String()
    do {
        let contents = try String(contentsOfFile: file)
        let lines = contents.components(separatedBy: .newlines)
        for line in lines {
            if (line.starts(with: "#")) {
                continue;
            }
            clusters = clusters + appDir! + "/" + line + ":"
        }
    } catch {
        print("error reading" + brandingToken! + ".clusters")
    }

    return clusters
}

func processConf(confFile: String, confDict: inout Dictionary<String, String> )  {
    //set default userdir and cachedir
    let userdir_root = getDefaultDir(for: .applicationSupportDirectory)
    let cachedir_root = getDefaultDir(for: .cachesDirectory)
    
    do {
        let contents = try String(contentsOfFile: confFile)
        let lines = contents.components(separatedBy: .newlines)
        for line in lines {
            if (line.starts(with: "#")) {
                continue;
            }
        
            if let idx = line.firstIndex(of: "=") {
                let varRange = line.startIndex..<idx
                let idx2 = line.index(after: idx)
                let valRange = idx2..<line.endIndex
                let nbvar = String(line[varRange])
                let nbval = String(line[valRange])
                
                // strip quotes
                let start = nbval.index(after: nbval.startIndex)
                let end = nbval.index(before: nbval.endIndex)
                let val = nbval[start..<end]
                
                switch nbvar {
                case "default_userdir":
                    confDict["default_userdir"] = val.replacingOccurrences(of: "${DEFAULT_USERDIR_ROOT}", with: userdir_root)
                case "default_cachedir":
                    confDict["default_cachedir"] = val.replacingOccurrences(of: "${DEFAULT_CACHEDIR_ROOT}", with: cachedir_root)
                case "default_options":
                    confDict["default_options"] = String(val)
                case "jdkhome":
                    confDict["jdkhome"] = String(val)
                case "extra_clusters":
                    confDict["extra_clusters"] = String(val)
                default:
                    print("Unknown " + confFile + " variable: " + nbvar + "=" + nbval)
                }
            }
        }
    } catch {
        print("error reading " + confFile)
    }
}

let brandingToken = Bundle.main.object(forInfoDictionaryKey: "CFBundleExecutable") as? String
let appName = Bundle.main.object(forInfoDictionaryKey: "CFBundleName") as? String
let confFile = Bundle.main.path(forResource: brandingToken, ofType: "conf", inDirectory: brandingToken! + "/etc")
let clustersFile = Bundle.main.path(forResource: brandingToken, ofType: "clusters", inDirectory: brandingToken! + "/etc")
let iconFile = Bundle.main.path(forResource: brandingToken, ofType: "icns")
let nbexecURL = Bundle.main.url(forResource: "nbexec", withExtension: "", subdirectory: brandingToken! + "/platform/lib")

var confDict = Dictionary<String, String>()
var clusters = processClusters(file: clustersFile!)
var foundUserDir = false
var foundCacheDir = false

// process app.conf file
processConf(confFile: confFile!, confDict: &confDict)

// process user's app.conf
if let userDir = confDict["default_userdir"] {
    let userConfFile = userDir + "/etc/" + brandingToken! + ".conf"
    if FileManager.default.fileExists(atPath: userConfFile) {
        processConf(confFile: userConfFile, confDict: &confDict)
    }
}

// check command line arguments for userdir or cachedir
for argument in CommandLine.arguments {
    switch argument {
    case "--userdir":
        foundUserDir = true
    case "--cachedir":
        foundCacheDir = true
    default:
        break
    }
}

var args = [String]()
args.append("-J-Xdock:icon=" + iconFile!)
args.append("-J-Xdock:name=" + appName!)


// convert conf variables to command line arguments
for (key, val) in confDict {
    switch key {
    case "default_userdir":
        if (!foundUserDir) {
            args.append("--userdir")
            args.append(val)
        }
    case "default_cachedir":
        if (!foundUserDir) {
            args.append("--cachedir")
            args.append(val)
        }
    case "default_options":
        let nbargs = val.components(separatedBy: " ")
        for arg in nbargs {
            if !arg.trimmingCharacters(in: .whitespaces).isEmpty {
                args.append(arg)
            }
        }
    case "jdkhome":
        args.append("--jdkhome")
        args.append(val)
    case "extra_clusters":
        clusters = clusters + val
    default:
        print("Unknown netbeans.conf variable: " + key + "=" + val)
    }
}
        
args.append("--clusters")
args.append(clusters)

// add user's command line arguments
for argument in Array(CommandLine.arguments.dropFirst()) {
    args.append(argument)
}


let launchNbexec = Process()
var env = ProcessInfo.processInfo.environment
env["DEFAULT_USERDIR_ROOT"] = getDefaultDir(for: .applicationSupportDirectory)
launchNbexec.environment = env
launchNbexec.arguments = args
launchNbexec.executableURL = nbexecURL
try launchNbexec.run()

// needed to keep Dock name based on CFBundleName from Info.plist
// does not work if called from command line.
launchNbexec.waitUntilExit()
