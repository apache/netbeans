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
    CloseAction,
    ErrorAction,
    StreamInfo,
    Message,
    MessageType,
} from 'vscode-languageclient';

import * as net from 'net';
import * as fs from 'fs';
import * as path from 'path';
import { ChildProcess } from 'child_process';
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

function findJDK(onChange: (path : string | null) => void): void {
    function find(): string | null {
        let nbJdk = workspace.getConfiguration('netbeans').get('jdkhome');
        if (nbJdk) {
            return nbJdk as string;
        }
        let javahome = workspace.getConfiguration('java').get('home');
        if (javahome) {
            return javahome as string;
        }

        let jdkHome: any = process.env.JDK_HOME;
        if (jdkHome) {
            return jdkHome as string;
        }
        let jHome: any = process.env.JAVA_HOME;
        if (jHome) {
            return jHome as string;
        }
        return null;
    }

    let currentJdk = find();
    let timeout: NodeJS.Timeout | undefined = undefined;
    workspace.onDidChangeConfiguration(params => {
        if (timeout || (!params.affectsConfiguration('java') && !params.affectsConfiguration('netbeans'))) {
            return;
        }
        timeout = setTimeout(() => {
            timeout = undefined;
            let newJdk = find();
            if (newJdk !== currentJdk) {
                currentJdk = newJdk;
                onChange(currentJdk);
            }
        }, 0);
    });
    onChange(currentJdk);
}

export function activate(context: ExtensionContext) {
    let log = vscode.window.createOutputChannel("Apache NetBeans Language Server");

    let conf = workspace.getConfiguration();
    if (conf.get("netbeans.conflict.check")) {
        let e = vscode.extensions.getExtension('redhat.java');
        function disablingFailed(reason: any) {
            log.appendLine('Disabling some services failed ' + reason);
        }
        if (e && workspace.name) {
            vscode.window.showInformationMessage(`redhat.java found at ${e.extensionPath} - Suppressing some services to not clash with Apache NetBeans Language Server.`);
            conf.update('java.completion.enabled', false, false).then(() => {
                vscode.window.showInformationMessage('Usage of only one Java extension is recommended. Certain services of redhat.java have been disabled. ');
                conf.update('java.debug.settings.enableRunDebugCodeLens', false, false).then(() => {}, disablingFailed);
                conf.update('java.test.editor.enableShortcuts', false, false).then(() => {}, disablingFailed);
            }, disablingFailed);
        }
    }

    // find acceptable JDK and launch the Java part
    findJDK((specifiedJDK) => {
        activateWithJDK(specifiedJDK, context, log, true);
    });

    //register debugger:
    let configProvider = new NetBeansConfigurationProvider();
    context.subscriptions.push(vscode.debug.registerDebugConfigurationProvider('java8+', configProvider));

    let debugDescriptionFactory = new NetBeansDebugAdapterDescriptionFactory();
    context.subscriptions.push(vscode.debug.registerDebugAdapterDescriptorFactory('java8+', debugDescriptionFactory));

    // register commands
    context.subscriptions.push(commands.registerCommand('java.workspace.compile', () => {
        return window.withProgress({ location: ProgressLocation.Window }, p => {
            return new Promise(async (resolve, reject) => {
                const commands = await vscode.commands.getCommands();
                if (commands.includes('java.build.workspace')) {
                    p.report({ message: 'Compiling workspace...' });
                    if (client != null) {
                        client.outputChannel.show(true);
                    }
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
}

/**
 * Pending maintenance (install) task, activations should be chained after it.
 */
let maintenance : Promise<void> | null;

/**
 * Pending activation flag. Will be cleared when the process produces some message or fails.
 */
let activationPending : boolean = false;

function activateWithJDK(specifiedJDK: string | null, context: ExtensionContext, log : vscode.OutputChannel, notifyKill: boolean): void {
    const a : Promise<void> | null = maintenance;
    if (activationPending) {
        // do not activate more than once in parallel.
        console.log("Server activation requested repeatedly, ignoring...");
        return;
    }
    activationPending = true;
    // chain the restart after termination of the former process.
    if (a != null) {
        console.log("Server activation initiated while in maintenance mode, scheduling after maintenance");
        a.then(() => killNbProcess(notifyKill, log)).
            then(() => doActivateWithJDK(specifiedJDK, context, log, notifyKill));
    } else {
        console.log("Initiating server activation");
        killNbProcess(notifyKill, log).then(
            () => doActivateWithJDK(specifiedJDK, context, log, notifyKill)
        );
    }
}

function killNbProcess(notifyKill : boolean, log : vscode.OutputChannel, specProcess?: ChildProcess) : Promise<void> {
    const p = nbProcess;
    console.log("Request to kill LSP server.");
    if (p && (!specProcess || specProcess == p)) {
        if (notifyKill) {
            vscode.window.setStatusBarMessage("Restarting Apache NetBeans Language Server.", 2000);
        }
        return new Promise((resolve, reject) => {
            nbProcess = null;
            p.on('close', function(code: number) {
                console.log("LSP server closed: " + p.pid)
                resolve();
            });
            console.log("Killing LSP server " + p.pid);
            if (!p.kill()) {
                reject("Cannot kill");
            }
        });
    } else {
        let msg = "Cannot kill: ";
        if (specProcess) {
            msg += "Requested kill on " + specProcess.pid + ", ";
        }
        console.log(msg + "current process is " + (p ? p.pid : "None"));
        return new Promise((res, rej) => { res(); });
    }
}

function doActivateWithJDK(specifiedJDK: string | null, context: ExtensionContext, log : vscode.OutputChannel, notifyKill: boolean): void {
    maintenance = null;
    let restartWithJDKLater : ((time: number, n: boolean) => void) = function restartLater(time: number, n : boolean) {
        log.appendLine(`Restart of Apache Language Server requested in ${(time / 1000)} s.`);
        setTimeout(() => {
            activateWithJDK(specifiedJDK, context, log, n);
        }, time);
    };

    const beVerbose : boolean = workspace.getConfiguration('netbeans').get('verbose', false);
    let info = {
        clusters : findClusters(context.extensionPath),
        extensionPath: context.extensionPath,
        storagePath : context.globalStoragePath,
        jdkHome : specifiedJDK,
        verbose: beVerbose
    };
    let launchMsg = `Launching Apache NetBeans Language Server with ${specifiedJDK ? specifiedJDK : 'default system JDK'}`;
    log.appendLine(launchMsg);
    vscode.window.setStatusBarMessage(launchMsg, 2000);

    let ideRunning = new Promise((resolve, reject) => {
        let collectedText : string | null = '';
        function logAndWaitForEnabled(text: string) {
            if (p == nbProcess) {
                activationPending = false;
            }
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
        console.log("LSP server launching: " + p.pid);
        p.stdout.on('data', function(d: any) {
            logAndWaitForEnabled(d.toString());
        });
        p.stderr.on('data', function(d: any) {
            logAndWaitForEnabled(d.toString());
        });
        nbProcess = p;
        p.on('close', function(code: number) {
            if (p == nbProcess && code != 0 && code) {
                vscode.window.showWarningMessage("Apache NetBeans Language Server exited with " + code);
            }
            if (collectedText != null) {
                let match = collectedText.match(/org.netbeans.modules.java.lsp.server[^\n]*/)
                if (match?.length == 1) {
                    log.appendLine(match[0]);
                } else {
                    log.appendLine("Cannot find org.netbeans.modules.java.lsp.server in the log!");
                }
                log.show(false);
                killNbProcess(false, log, p);
                reject("Apache NetBeans Language Server not enabled!");
            } else {
                console.log("LSP server " + p.pid + " terminated with " + code);
                log.appendLine("Exit code " + code);
            }
        });
    });

    ideRunning.then(() => {
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
                    debugPort = -1;
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
            outputChannel: log,
            revealOutputChannelOn: 3, // error
            initializationOptions : {
                'nbcodeCapabilities' : {
                    'statusBarMessageSupport' : true
                }
            },
            errorHandler: {
                error : function(_error: Error, _message: Message, count: number): ErrorAction {
                    return ErrorAction.Continue;
                },
                closed : function(): CloseAction {
                    log.appendLine("Connection to Apache NetBeans Language Server closed.");
                    if (!activationPending) {
                        restartWithJDKLater(10000, false);
                    }
                    return CloseAction.DoNotRestart;
                }
            }
        }

        if (client) {
            client.stop();
        }
        client = new LanguageClient(
                'java',
                'NetBeans Java',
                connection,
                clientOptions
        );
        client.start();
        client.onReady().then(() => {
            commands.executeCommand('setContext', 'nbJavaLSReady', true);
            client.onNotification(StatusMessageRequest.type, showStatusBarMessage);
        });
    }).catch((reason) => {
        activationPending = false;
        log.append(reason);
        window.showErrorMessage('Error initializing ' + reason);
    });

    function showStatusBarMessage(params : ShowStatusMessageParams) {
        let decorated : string = params.message;
        let defTimeout;

        switch (params.type) {
            case MessageType.Error:
                decorated = '$(error) ' + params.message;
                defTimeout = 0;
                checkInstallNbJavac(params.message);
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

    function checkInstallNbJavac(msg : string) {
        const NO_JAVA_SUPPORT = "Cannot initialize Java support";
        if (msg.startsWith(NO_JAVA_SUPPORT)) {
            const yes = "Install GPLv2+CPEx code";
            window.showErrorMessage("Additional Java Support is needed", yes).then(reply => {
                if (yes === reply) {
                    vscode.window.setStatusBarMessage("Preparing Apache NetBeans Language Server for additional installation", 2000);
                    restartWithJDKLater = function() {
                        console.log("Ignoring request for restart of Apache NetBeans Language Server");
                    };
                    maintenance = new Promise((resolve, reject) => {
                        const kill : Promise<void> = killNbProcess(false, log);
                        kill.then(() => {
                            let installProcess = launcher.launch(info, "-J-Dnetbeans.close=true", "--modules", "--install", ".*nbjavac.*");
                            console.log("Launching installation process: " + installProcess.pid);
                            let logData = function(d: any) {
                                log.append(d.toString());
                            };
                            installProcess.stdout.on('data', logData);
                            installProcess.stderr.on('data', logData);
                            installProcess.addListener("error", reject);
                            // MUST wait on 'close', since stdout is inherited by children. The installProcess dies but
                            // the inherited stream will be closed by the last child dying.
                            installProcess.on('close', function(code: number) {
                                console.log("Installation completed: " + installProcess.pid);
                                log.appendLine("Additional Java Support installed with exit code " + code);
                                // will be actually run after maintenance is resolve()d.
                                activateWithJDK(specifiedJDK, context, log, notifyKill)
                                resolve();
                            });
                            return installProcess;
                        });
                    });
                }
            });
        }
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

    createDebugAdapterDescriptor(_session: vscode.DebugSession, _executable: vscode.DebugAdapterExecutable | undefined): vscode.ProviderResult<vscode.DebugAdapterDescriptor> {
        return new Promise<vscode.DebugAdapterDescriptor>((resolve, reject) => {
            let cnt = 10;
            const fnc = () => {
                if (debugPort < 0) {
                    if (cnt-- > 0) {
                        setTimeout(fnc, 1000);
                    } else {
                        reject(new Error('Apache NetBeans Debug Server Adapter not yet initialized. Please wait for a while and try again.'));
                    }
                } else {
                    resolve(new vscode.DebugAdapterServer(debugPort));
                }
            }
            fnc();
        });
    }
}


class NetBeansConfigurationProvider implements vscode.DebugConfigurationProvider {

    resolveDebugConfiguration(_folder: vscode.WorkspaceFolder | undefined, config: vscode.DebugConfiguration, _token?: vscode.CancellationToken): vscode.ProviderResult<vscode.DebugConfiguration> {
        if (!config.type) {
            config.type = 'java8+';
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
