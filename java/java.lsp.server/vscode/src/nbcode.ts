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
'use strict';

import * as fs from 'fs';
import * as os from 'os';
import * as path from 'path';
import { spawn, ChildProcessByStdio } from 'child_process';
import { Readable } from 'stream';
import { env } from 'process';

export interface LaunchInfo {
    clusters: string[];
    extensionPath: string;
    storagePath: string;
    jdkHome: string | unknown;
    verbose? : boolean;
}

function find(info: LaunchInfo): string {
    let nbcode = os.platform() === 'win32' ? 
        os.arch() === 'x64' ? 'nbcode64.exe' : 'nbcode.exe' 
        : 'nbcode';
    let nbcodePath = path.join(info.extensionPath, "nbcode", "bin", nbcode);

    let nbcodePerm = fs.statSync(nbcodePath);
    if (!nbcodePerm.isFile()) {
        throw `Cannot execute ${nbcodePath}`;
    }
    return nbcodePath;
}

export function launch(
    info: LaunchInfo,
    ...extraArgs : string[]
): ChildProcessByStdio<null, Readable, Readable> {
    let nbcodePath = find(info);

    const userDir = path.join(info.storagePath, "userdir");
    fs.mkdirSync(userDir, {recursive: true});
    let userDirPerm = fs.statSync(userDir);
    if (!userDirPerm.isDirectory()) {
        throw `Cannot create ${userDir}`;
    }

    let clusterPath = info.clusters.join(path.delimiter);
    let ideArgs: string[] = [
        '--userdir', userDir
    ];
    if (info.jdkHome) {
        ideArgs.push('--jdkhome', info.jdkHome as string);
    }
    if (info.verbose) {
        ideArgs.push('-J-Dnetbeans.logger.console=true');
    }
    ideArgs.push(`-J-Dnetbeans.extra.dirs=${clusterPath}`)
    if (env['netbeans_extra_options']) {
        ideArgs.push(...env['netbeans_extra_options'].split(' '));
    }
    ideArgs.push(...extraArgs);
    if (env['netbeans_debug'] && extraArgs && extraArgs.find(s => s.includes("--list"))) {
        ideArgs.push(...['-J-Dnetbeans.logger.console=true', '-J-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000']);
    }

    console.log(`Launching NBLS with arguments: ` + ideArgs);

    let process: ChildProcessByStdio<any, Readable, Readable> = spawn(nbcodePath, ideArgs, {
        cwd : userDir,
        stdio : ["ignore", "pipe", "pipe"],
    });
    return process;
}

if (typeof process === 'object' && typeof process.argv0 ==='string' && process.argv0.startsWith('node')) {
    let extension = path.join(process.argv[1], '..', '..');
    let nbcode = path.join(extension, 'nbcode');
    if (!fs.existsSync(nbcode)) {
        throw `Cannot find ${nbcode}. Try npm run compile first!`;
    }
    let clusters = fs.readdirSync(nbcode).filter(c => c !== 'bin' && c !== 'etc').map(c => path.join(nbcode, c));
    let args = process.argv.slice(2);
    let json = JSON.parse("" + fs.readFileSync(path.join(extension, 'package.json')));
    let storage;
    
    if (!env.nbcode_userdir || env.nbcode_userdir == 'global') {
        storage = path.join(os.platform() === 'darwin' ? 
                path.join(os.homedir(), 'Library', 'Application Support') :
                path.join(os.homedir(), '.config'),
                'Code', 'User', 'globalStorage', json.publisher + '.' + json.name);
    } else {
        storage = env.nbcode_userdir;
    }
    console.log('Launching NBLS with user directory: ' + storage)
    let info = {
        clusters : clusters,
        extensionPath: extension,
        storagePath : storage,
        jdkHome : null
    };
    let p = launch(info, ...args);
    p.stdout.on('data', function(data) {
        console.log(data.toString());
    });
    p.stderr.on('data', function(data) {
        console.log(data.toString());
    });
    p.on('close', (code) => {
        console.log(`nbcode finished with status ${code}`);
    });
}
