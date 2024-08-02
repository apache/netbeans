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

import * as fs from 'fs';
import * as path from 'path';
import * as cp from 'child_process';

const JAVA_VERSION_REGEX = /version\s+"(\S+)"/;

function findExecutable(program: string, home: string): string | undefined {
    if (home) {
        let executablePath = path.join(home, 'bin', program);
        if (process.platform === 'win32') {
            if (fs.existsSync(executablePath + '.cmd')) {
                return executablePath + '.cmd';
            }
            if (fs.existsSync(executablePath + '.exe')) {
                return executablePath + '.exe';
            }
        } else if (fs.existsSync(executablePath)) {
            return executablePath;
        }
    }
    return undefined;
}

export function normalizeJavaVersion(version: string): string {
    return version.startsWith("1.") ? version.substring(2) : version;
}

export async function getJavaVersion(homeFolder: string): Promise<string | undefined> {
    return new Promise<string | undefined>(resolve => {
        if (homeFolder && fs.existsSync(homeFolder)) {
            const executable: string | undefined = findExecutable('java', homeFolder);
            if (executable) {
                cp.execFile(executable, ['-version'], { encoding: 'utf8' }, (_error, _stdout, stderr) => {
                    if (stderr) {
                        let javaVersion: string | undefined;
                        stderr.split('\n').forEach((line: string) => {
                            const javaInfo: string[] | null = line.match(JAVA_VERSION_REGEX);
                            if (javaInfo && javaInfo.length > 1) {
                                javaVersion = javaInfo[1];
                            }
                        });
                        if (javaVersion) {
                            let majorVersion = normalizeJavaVersion(javaVersion);
                            let i = majorVersion.indexOf('.');
                            if (i > -1) {
                                majorVersion = majorVersion.slice(0, i);
                            }
                            resolve(majorVersion);
                            return;
                        }
                    }
                    resolve(undefined);
                });
            }
        } else {
            resolve(undefined);
        }
    });
}