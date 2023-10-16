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
import { homedir } from 'os';

export async function initializeRunConfiguration(): Promise<boolean> {
	const java = await vscode.workspace.findFiles('**/*.java', '**/node_modules/**', 1);
	if (java?.length > 0) {
		const maven = await vscode.workspace.findFiles('pom.xml', '**/node_modules/**', 1);
		if (maven?.length > 0) {
			return true;
		}
		const gradle = await vscode.workspace.findFiles('build.gradle', '**/node_modules/**', 1);
		if (gradle?.length > 0) {
			return true;
		}
	}
	return false;
}

class DBConfigurationProvider implements vscode.DebugConfigurationProvider {
	resolveDebugConfiguration(_folder: vscode.WorkspaceFolder | undefined, config: vscode.DebugConfiguration, _token?: vscode.CancellationToken): vscode.ProviderResult<vscode.DebugConfiguration> {
		return new Promise<vscode.DebugConfiguration>(resolve => {
			resolve(config);
		});
	}

	resolveDebugConfigurationWithSubstitutedVariables?(_folder: vscode.WorkspaceFolder | undefined, config: vscode.DebugConfiguration, _token?: vscode.CancellationToken): vscode.ProviderResult<vscode.DebugConfiguration> {
        return new Promise<vscode.DebugConfiguration>(async resolve => {
			let o: Object = await vscode.commands.executeCommand('nbls.db.connection');
			if (config === undefined) {
				config = {} as vscode.DebugConfiguration;
			}
			if (config.env === undefined) {
				config.env = {};
			}
			for (let val of Object.keys(o) as (keyof typeof o)[]) {
				let value = o[val];
				config.env[val as string] = value;
			}
			resolve(config);
		});
	}
}

export function onDidTerminateSession(session: vscode.DebugSession): any {
    const config = session.configuration;
    if (config.env) {
        const file = config.env["MICRONAUT_CONFIG_FILES"];
        if (file) {
            vscode.workspace.fs.delete(vscode.Uri.file(file));
        }
    }
}

export const dBConfigurationProvider = new DBConfigurationProvider();
