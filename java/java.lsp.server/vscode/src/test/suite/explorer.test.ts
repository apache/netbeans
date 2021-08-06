import * as assert from 'assert';
import * as fs from 'fs';
import * as path from 'path';
import * as ps from 'ps-node';
import { spawn, ChildProcessByStdio, spawnSync, SpawnSyncReturns } from 'child_process';
import { Readable } from 'stream';

// You can import and use all API from the 'vscode' module
// as well as import your extension to test it
import * as vscode from 'vscode';
import * as myExtension from '../../extension';
import * as myExplorer from '../../explorer';
import { TextDocument, TextEditor, Uri } from 'vscode';
import { assertWorkspace, dumpJava } from './testutils';

suite('Explorer Test Suite', () => {
    vscode.window.showInformationMessage('Cleaning up workspace.');
    let folder: string = assertWorkspace();
    fs.rmdirSync(folder, { recursive: true });
    fs.mkdirSync(folder, { recursive: true });
    vscode.window.showInformationMessage('Start all tests.');
    myExtension.enableConsoleLog();

    test('Explorer can be created', async () => {
        const lvp = myExplorer.foundProjects(await myExtension.findLanguageClient());
        const firstLevelChildren = await (lvp.getChildren(null) as Thenable<any[]>);
        assert.equal(firstLevelChildren.length, 0, "No child under the root");
    });
});
