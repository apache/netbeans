
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

import * as assert from 'assert';
import * as fs from 'fs';
import * as Mocha from 'mocha';


// You can import and use all API from the 'vscode' module
// as well as import your extension to test it
import * as vscode from 'vscode';
import * as myExtension from '../../extension';
import * as myExplorer from '../../explorer';
import { assertWorkspace, dumpJava, prepareProject } from './testutils';

Mocha.before(async () => {
    vscode.window.showInformationMessage('Cleaning up workspace.');
    let folder: string = assertWorkspace();
    fs.rmdirSync(folder, { recursive: true });
    fs.mkdirSync(folder, { recursive: true });
});

suite('Explorer Test Suite', () => {
    vscode.window.showInformationMessage('Start explorer tests.');
    myExtension.enableConsoleLog();

    test('Explorer can be created', async () => {
        const lvp = await myExplorer.createViewProvider(await myExtension.awaitClient(), 'foundProjects');
        const firstLevelChildren = await (lvp.getChildren() as Thenable<any[]>);
        assert.strictEqual(firstLevelChildren.length, 0, "No child under the root");
    });
});
