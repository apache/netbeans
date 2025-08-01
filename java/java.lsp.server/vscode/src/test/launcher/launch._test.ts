
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
import * as Mocha from 'mocha';
import * as path from 'path';

import * as vscode from 'vscode';
import * as myExtension from '../../extension';
import { assertWorkspace, waitProjectRecognized } from '../suite/testutils';
import { copyDirSync, projectFileUri } from './fileUtil';

Mocha.before(async () => {
    vscode.window.showInformationMessage('Cleaning up workspace.');
    let workspaceFolder: string = assertWorkspace();
    fs.rmdirSync(workspaceFolder, { recursive: true });
    fs.mkdirSync(workspaceFolder, { recursive: true });

    const sourcePath = path.resolve(__dirname, '..' , '..', '..', 'test-projects', 'test-app');
    copyDirSync(sourcePath, workspaceFolder);
});

Mocha.afterEach(() => {
    myExtension.clearDebugConsoleListeners(); 
})

const MAVEN_COMMAND_REGEX = /\/netbeans\/java\/java\.lsp\.server\/vscode\/nbcode\/java\/maven\/bin\/mvn/;
const MAVEN_PLUGIN_RUN_REGEX = /io\.micronaut\.maven:micronaut-maven-plugin:run/;

function isMavenCommand(input: string) {
    return MAVEN_COMMAND_REGEX.test(input);
}

function createDebugConsoleEventCallback(verifyConditionCallback: CallableFunction, errorMessageCallback: CallableFunction, done: Mocha.Done): (value: string) => void {
    return (value: string) => {
        if (isMavenCommand(value)) {
            vscode.commands.executeCommand("workbench.action.debug.stop");
            if (verifyConditionCallback(value)) {
                done();
            } else {
                done(new Error(errorMessageCallback(value)))
            }
        }
    }
}

suite('Micronaut Launcher Test Suite', () => {
    vscode.window.showInformationMessage('Starting Micronaut launcher tests.');
    myExtension.enableConsoleLog();

    test('Micronaut run', (done) => {
        let folder: string = assertWorkspace();
        const verifyConditionCallback = (value: string) => new RegExp(/.*/.source + MAVEN_COMMAND_REGEX.source + /.*/.source + MAVEN_PLUGIN_RUN_REGEX.source).test(value);
        const errorMessageCallback = (value: string) => `Output: ${value} doesn't contain exec-maven-plugin:exec command`;

        myExtension.debugConsoleListeners.push({
            callback: createDebugConsoleEventCallback(verifyConditionCallback, errorMessageCallback, done)
        });
        const entrypointPath = projectFileUri(folder, 'src', 'main', 'java', 'com', 'example', 'Application.java');
        waitProjectRecognized(entrypointPath).then(() => {
            vscode.commands.executeCommand(`${myExtension.COMMAND_PREFIX}.run.single`, entrypointPath, null, '');
        });
    });

    test('Micronaut debug', (done) => {
        let folder: string = assertWorkspace();
        const verifyConditionCallback = (value: string) => new RegExp(/.*/.source + MAVEN_COMMAND_REGEX.source + /.*jpda\.listen=true.*jpda\.address=.*/.source + MAVEN_PLUGIN_RUN_REGEX.source).test(value);
        const errorMessageCallback = (value: string) => `Output: ${value} doesn't contain flags that starts debug mode`;
        
        myExtension.debugConsoleListeners.push({
            callback: createDebugConsoleEventCallback(verifyConditionCallback, errorMessageCallback, done)
        });
        const entrypointPath = projectFileUri(folder, 'src', 'main', 'java', 'com', 'example', 'Application.java');
        waitProjectRecognized(entrypointPath).then(() => {
            vscode.commands.executeCommand(`${myExtension.COMMAND_PREFIX}.debug.single`, entrypointPath, null, '');
        });
    });

    test('Micronaut dev mode working', (done) => {
        let folder: string = assertWorkspace();       
        const verifyConditionCallback = (value: string) => new RegExp(/.*/.source + MAVEN_COMMAND_REGEX.source + /.*mn:run/.source).test(value);
        const errorMessageCallback = (value: string) => `Output: ${value} doesn't contain mn:run command`;
        
        myExtension.debugConsoleListeners.push({
            callback: createDebugConsoleEventCallback(verifyConditionCallback, errorMessageCallback, done)
        });
        const entrypointPath = projectFileUri(folder, 'src', 'main', 'java', 'com', 'example', 'Application.java');
        waitProjectRecognized(entrypointPath).then(() => {
            vscode.commands.executeCommand(`${myExtension.COMMAND_PREFIX}.run.single`, entrypointPath, null, 'Micronaut: dev mode');
        });
    });
});