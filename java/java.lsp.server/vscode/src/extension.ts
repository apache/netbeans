/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
/*Heavily influenced by the extension for Kotlin Language Server which is:
 * Copyright (c) 2016 George Fraser
 * Copyright (c) 2018 fwcd
 */
'use strict';

import { window, workspace, ExtensionContext } from 'vscode';

import {
	LanguageClient,
	LanguageClientOptions,
	ServerOptions
} from 'vscode-languageclient';

import * as path from 'path';
import { execSync } from 'child_process';

let client: LanguageClient;

export function activate(context: ExtensionContext) {
    //verify acceptable JDK is available/set:
    let specifiedJDK: string = workspace.getConfiguration('netbeans').get('jdkhome');

    try {
        let targetJava = specifiedJDK != null ? specifiedJDK + '/bin/java' : 'java';
        execSync(targetJava + ' ' + context.extensionPath + '/src/VerifyJDK11.java');
    } catch (e) {
        window.showErrorMessage('The Java language server needs a JDK 11 to run, but none found. Please configure it under File/Preferences/Settings/Extensions/Java and restart VS Code.');
        return ;
    }
    let serverPath = path.resolve(context.extensionPath, "nb-java-lsp-server", "bin", "nb-java-lsp-server");

    let serverOptions: ServerOptions;
    let args: string[] = [];
    if (specifiedJDK != null) {
        args = ['--jdkhome', specifiedJDK];
    }
    serverOptions = {
        command: serverPath,
        args: args,
        options: { cwd: workspace.rootPath }
    }

    // Options to control the language client
    let clientOptions: LanguageClientOptions = {
        // Register the server for java documents
        documentSelector: ['java'],
        synchronize: {
            configurationSection: 'java',
            fileEvents: [
                workspace.createFileSystemWatcher('**/*.java')
            ]
        },
        outputChannelName: 'Java',
        revealOutputChannelOn: 4 // never
    }

	// Create the language client and start the client.
	client = new LanguageClient(
		'java',
		'NetBeans Java',
		serverOptions,
		clientOptions
	);

	// Start the client. This will also launch the server
	client.start();
}

export function deactivate(): Thenable<void> {
	if (!client) {
		return undefined;
	}
	return client.stop();
}
