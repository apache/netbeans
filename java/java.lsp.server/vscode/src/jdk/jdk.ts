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

import * as vscode from 'vscode';
import * as path from 'path';
import * as fs from 'fs';
import * as jdkUtils from 'jdk-utils';


const CUSTOM_JAVA_ROOTS_KEY = 'nbls.jdk.customJavaRoots';

let storage: vscode.Memento;

export function initialize(context: vscode.ExtensionContext) {
    storage = context.globalState;
}

export class Java {

    readonly javaHome: string;

    constructor(javaHome: string) {
        this.javaHome = javaHome;
    }

    name(): string {
        let name = path.basename(this.javaHome);
        if (isMac() && name === 'Home') {
            const javaHome = path.resolve(this.javaHome, '..', '..');
            name = path.basename(javaHome);
        }
        return name;
    }

    isJava(): boolean {
        const java = path.join(this.javaHome, 'bin', isWindows() ? 'java.exe' : 'java');
        return fs.existsSync(java);
    }

    isJdk(): boolean {
        const javac = path.join(this.javaHome, 'bin', isWindows() ? 'javac.exe' : 'javac');
        return this.isJava() && fs.existsSync(javac);
    }

    hasNativeImage(): boolean {
        const ni = path.join(this.javaHome, 'bin', isWindows() ? 'native-image.cmd' : 'native-image');
        return fs.existsSync(ni);
    }

    async getVersion(): Promise<{ java_version: string; major: number; } | undefined> {
        const java = await jdkUtils.getRuntime(this.javaHome, { withVersion: true });
        return java?.version;
    }

}

export async function findAll(knownJavas?: Java[]): Promise<Java[]> {
    const javaRoots: string[] = [];
    function addJavaRoot(javaHome: string) {
        let javaRoot = path.dirname(javaHome);
        if (isMac() && path.basename(javaRoot) === 'Contents') {
            javaHome = path.resolve(javaHome, '..', '..');
            javaRoot = path.dirname(javaHome);
        }
        const normalizedJavaRoot = normalizePath(javaRoot);
        if (!javaRoots.includes(normalizedJavaRoot)) {
            javaRoots.push(normalizedJavaRoot);
        }
    }

    const systemJavas = await jdkUtils.findRuntimes();
    for (const sytemJava of systemJavas) {
        addJavaRoot(sytemJava.homedir);
    }

    if (knownJavas) {
        for (const knownJava of knownJavas) {
            addJavaRoot(knownJava.javaHome);
        }
    }

    const customJavaRootsArr = await customJavaRoots();
    for (const customJavaRoot of customJavaRootsArr) {
        if (!javaRoots.includes(customJavaRoot)) {
            javaRoots.push(customJavaRoot);
        }
    }

    const jdks: Java[] = [];
    for (const javaRoot of javaRoots) {
        const dirents = fs.readdirSync(javaRoot, { withFileTypes: true });
        for (const dirent of dirents) {
            if (dirent.isDirectory()) {
                const javaHome = path.join(javaRoot, dirent.name);
                const java = new Java(javaHome);
                if (java.isJdk()) {
                    jdks.push(java);
                } else if (isMac()) {
                    const macJavaHome = path.join(javaHome, 'Contents', 'Home');
                    const macJava = new Java(macJavaHome);
                    if (macJava.isJdk()) {
                        jdks.push(macJava);
                    }
                }
            }
        }
    }

    return jdks;
}

async function customJavaRoots(): Promise<string[]> {
    const customJavaRoots = storage.get<string>(CUSTOM_JAVA_ROOTS_KEY) || '';
    const customJavaRootsArr = customJavaRoots.split(path.delimiter);
    const newCustomJavaRootsArr = [];
    for (const customJavaRoot of customJavaRootsArr) {
        if (fs.existsSync(customJavaRoot)) {
            newCustomJavaRootsArr.push(customJavaRoot);
        }
    }
    if (customJavaRoots.length !== newCustomJavaRootsArr.length) {
        await storage.update(CUSTOM_JAVA_ROOTS_KEY, newCustomJavaRootsArr.join(path.delimiter));
    }
    return newCustomJavaRootsArr;
}

export async function registerCustom(java: Java) {
    let customJavaRoot = path.dirname(java.javaHome);
    if (isMac() && path.basename(customJavaRoot) === 'Contents') {
        const javaHome = path.resolve(java.javaHome, '..', '..');
        customJavaRoot = path.dirname(javaHome);
    }
    const customJavaRootsArr = await customJavaRoots();
    if (!customJavaRootsArr.includes(customJavaRoot)) {
        customJavaRootsArr.push(customJavaRoot);
        await storage.update(CUSTOM_JAVA_ROOTS_KEY, customJavaRootsArr.join(path.delimiter));
    }
}

function isWindows(): boolean {
    return process.platform === 'win32';
}

function isMac(): boolean {
    return process.platform === 'darwin';
}

function normalizePath(fsPath: string): string {
    return vscode.Uri.file(path.normalize(fsPath)).fsPath;
}
