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

import { commands, window, workspace, ExtensionContext, ProgressLocation } from 'vscode';

import {
    LanguageClient,
    LanguageClientOptions,
    StreamInfo,
    ShowMessageParams, MessageType,
} from 'vscode-languageclient';

import * as net from 'net';
import * as fs from 'fs';
import * as path from 'path';
import { spawn, ChildProcess, SpawnOptions } from 'child_process';
import * as vscode from 'vscode';
import * as launcher from './nbcode';
import { StatusMessageRequest, ShowStatusMessageParams  } from './protocol';

let client: LanguageClient;
let nbProcess : ChildProcess | null = null;
let debugPort: number = -1;

function findClusters(myPath : string): string[] {
    let clusters = [];
    for (let e of vscode.extensions.all) {
        if (e.extensionPath === myPath) {
            continue;
        }
        const dir = path.join(e.extensionPath, 'nbcode');
        if (!fs.existsSync(dir)) {
            continue;
        }
        const exists = fs.readdirSync(dir);
        for (let clusterName of exists) {
            let clusterPath = path.join(dir, clusterName);
            let clusterModules = path.join(clusterPath, 'config', 'Modules');
            if (!fs.existsSync(clusterModules)) {
                continue;
            }
            let perm = fs.statSync(clusterModules);
            if (perm.isDirectory()) {
                clusters.push(clusterPath);
            }
        }
    }
    return clusters;
}

export function activate(context: ExtensionContext) {
    //verify acceptable JDK is available/set:
    let specifiedJDK = workspace.getConfiguration('netbeans').get('jdkhome');

    let info = {
        clusters : findClusters(context.extensionPath),
        extensionPath: context.extensionPath,
        storagePath : context.globalStoragePath,
        jdkHome : specifiedJDK
    };

    vscode.extensions.all.forEach((e, index) => {
        if (e.extensionPath.indexOf("redhat.java") >= 0) {
            vscode.window.showInformationMessage(`redhat.java found at ${e.extensionPath} - supressing`);
            workspace.getConfiguration().update('java.completion.enabled', false, false).then((ok) => {
                vscode.window.showInformationMessage('Disabling redhat.java code completion');
            }, (reason) => {
                vscode.window.showInformationMessage('Disabling redhat.java code completion failed ' + reason);
            });
        }
    });

    let log = vscode.window.createOutputChannel("Java Language Server");
    log.appendLine("Launching Java Language Server");
    vscode.window.showInformationMessage("Launching Java Language Server");

    let ideRunning = new Promise((resolve, reject) => {
        let collectedText : string | null = '';
        function logAndWaitForEnabled(text: string) {
            log.append(text);
            if (collectedText == null) {
                return;
            }
            collectedText += text;
            if (collectedText.match(/org.netbeans.modules.java.lsp.server.*Enabled/)) {
                resolve(text);
                collectedText = null;
            }
        }
        let p = launcher.launch(info, "--modules", "--list");
        p.stdout.on('data', function(d: any) {
            logAndWaitForEnabled(d.toString());
        });
        p.stderr.on('data', function(d: any) {
            logAndWaitForEnabled(d.toString());
        });
        nbProcess = p;
        nbProcess.on('close', function(code: number) {
            if (code != 0) {
                vscode.window.showWarningMessage("Java Language Server exited with " + code);
            }
            if (collectedText != null) {
                let match = collectedText.match(/org.netbeans.modules.java.lsp.server[^\n]*/)
                if (match?.length == 1) {
                    log.appendLine(match[0]);
                } else {
                    log.appendLine("Cannot find org.netbeans.modules.java.lsp.server in the log!");
                }
                log.show(false);
                reject("Java Language Server not enabled!");
            } else {
                log.appendLine("Exit code " + code);
            }
            nbProcess = null;
        });
    });

    ideRunning.then((value) => {
        const connection = () => new Promise<StreamInfo>((resolve, reject) => {
            const server = net.createServer(socket => {
                server.close();
                resolve({
                    reader: socket,
                    writer: socket
                });
            });
            server.on('error', (err) => {
                reject(err);
            });
            server.listen(() => {
                const address: any = server.address();
                const srv = launcher.launch(info,
                    `--start-java-language-server=connect:${address.port}`,
                    `--start-java-debug-adapter-server=listen:0`
                );
                if (!srv) {
                    reject();
                } else {
                    if (!srv.stdout) {
                        reject(`No stdout to parse!`);
                        srv.disconnect();
                        return;
                    }
                    srv.stdout.on("data", (chunk) => {
                        if (debugPort < 0) {
                            const info = chunk.toString().match(/Debug Server Adapter listening at port (\d*)/);
                            if (info) {
                                debugPort = info[1];
                            }
                        }
                    });
                    srv.once("error", (err) => {
                        reject(err);
                    });
                }
            });
        });

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
            revealOutputChannelOn: 4,
            initializationOptions : {
                'nbcodeCapabilities' : {
                    'statusBarMessageSupport' : true
                }
            }
        }

        // Create the language client and start the client.
        client = new LanguageClient(
                'java',
                'NetBeans Java',
                connection,
                clientOptions
        );

        // Start the client. This will also launch the server
        client.start();
        client.onReady().then((value) => {
            commands.executeCommand('setContext', 'nbJavaLSReady', true);
            client.onNotification(StatusMessageRequest.type, showStatusBarMessage);
        });

        //register debugger:
        let configProvider = new NetBeansConfigurationProvider();
        context.subscriptions.push(vscode.debug.registerDebugConfigurationProvider('java-polyglot', configProvider));

        let debugDescriptionFactory = new NetBeansDebugAdapterDescriptionFactory();
        context.subscriptions.push(vscode.debug.registerDebugAdapterDescriptorFactory('java-polyglot', debugDescriptionFactory));
        window.showInformationMessage('Java Polyglot Debug Adapter ready.');

        // register commands
        context.subscriptions.push(commands.registerCommand('java.workspace.compile', () => {
            return window.withProgress({ location: ProgressLocation.Window }, p => {
                return new Promise(async (resolve, reject) => {
                    const commands = await vscode.commands.getCommands();
                    if (commands.includes('java.build.workspace')) {
                        p.report({ message: 'Compiling workspace...' });
                        client.outputChannel.show(true);
                        const start = new Date().getTime();
                        const res = await vscode.commands.executeCommand('java.build.workspace');
                        const elapsed = new Date().getTime() - start;
                        const humanVisibleDelay = elapsed < 1000 ? 1000 : 0;
                        setTimeout(() => { // set a timeout so user would still see the message when build time is short
                            if (res) {
                                resolve();
                            } else {
                                reject();
                            }
                        }, humanVisibleDelay);
                    } else {
                        reject();
                    }
                });
            });
        }));

    }).catch((reason) => {
        log.append(reason);
        window.showErrorMessage('Error initializing ' + reason);
    });

}

function showStatusBarMessage(params : ShowStatusMessageParams) {
    let decorated : string = params.message;
    let defTimeout;
    
    switch (params.type) {
        case MessageType.Error:
            decorated = '$(error) ' + params.message;
            defTimeout = 0;
            break;
        case MessageType.Warning:
            decorated = '$(warning) ' + params.message;
            defTimeout = 0;
            break;
        default:
            defTimeout = 10000;
            break;
    }
    // params.timeout may be defined but 0 -> should be used
    const timeout = params.timeout != undefined ? params.timeout : defTimeout;
    if (timeout > 0) {
        window.setStatusBarMessage(decorated, timeout);
    } else {
        window.setStatusBarMessage(decorated);
    }
}

export function deactivate(): Thenable<void> {
    if (nbProcess != null) {
        nbProcess.kill();
    }
    if (!client) {
        return Promise.resolve();
    }
    return client.stop();
}

class NetBeansDebugAdapterDescriptionFactory implements vscode.DebugAdapterDescriptorFactory {

    createDebugAdapterDescriptor(session: vscode.DebugSession, executable: vscode.DebugAdapterExecutable | undefined): vscode.ProviderResult<vscode.DebugAdapterDescriptor> {
        return new vscode.DebugAdapterServer(debugPort);
    }
}


class NetBeansConfigurationProvider implements vscode.DebugConfigurationProvider {

    resolveDebugConfiguration(folder: vscode.WorkspaceFolder | undefined, config: vscode.DebugConfiguration, token?: vscode.CancellationToken): vscode.ProviderResult<vscode.DebugConfiguration> {
        if (!config.type) {
            config.type = 'java-polyglot';
        }
        if (!config.request) {
            config.request = 'launch';
        }
        if (!config.mainClass) {
            config.mainClass = '${file}';
        }
        if (!config.classPaths) {
            config.classPaths = ['any'];
        }
        if (!config.console) {
            config.console = 'internalConsole';
        }

        return config;
    }
}
