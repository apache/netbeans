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

import { commands, window, workspace, ExtensionContext, ProgressLocation, TextEditorDecorationType } from 'vscode';

import {
	LanguageClient,
	LanguageClientOptions,
	ServerOptions,
	StreamInfo
} from 'vscode-languageclient/node';

import {
    CloseAction,
    ErrorAction,
    Message,
    MessageType,
    LogMessageNotification,
    RevealOutputChannelOn,
    DocumentSelector,
    ErrorHandlerResult,
    CloseHandlerResult,
    SymbolInformation,
    TextDocumentFilter,
    TelemetryEventNotification
} from 'vscode-languageclient';

import * as net from 'net';
import * as fs from 'fs';
import * as path from 'path';
import { ChildProcess } from 'child_process';
import * as vscode from 'vscode';
import * as ls from 'vscode-languageserver-protocol';
import * as launcher from './nbcode';
import {NbTestAdapter} from './testAdapter';
import { asRanges, StatusMessageRequest, ShowStatusMessageParams, QuickPickRequest, InputBoxRequest, MutliStepInputRequest, TestProgressNotification, DebugConnector,
         TextEditorDecorationCreateRequest, TextEditorDecorationSetNotification, TextEditorDecorationDisposeNotification, HtmlPageRequest, HtmlPageParams,
         ExecInHtmlPageRequest, SetTextEditorDecorationParams, ProjectActionParams, UpdateConfigurationRequest, QuickPickStep, InputBoxStep, SaveDocumentsRequest, SaveDocumentRequestParams
} from './protocol';
import * as launchConfigurations from './launchConfigurations';
import { createTreeViewService, TreeViewService, TreeItemDecorator, Visualizer, CustomizableTreeDataProvider } from './explorer';
import { initializeRunConfiguration, runConfigurationProvider, runConfigurationNodeProvider, configureRunSettings, runConfigurationUpdateAll } from './runConfiguration';
import { dBConfigurationProvider, onDidTerminateSession } from './dbConfigurationProvider';
import { InputStep, MultiStepInput } from './utils';
import { PropertiesView } from './propertiesView/propertiesView';

const API_VERSION : string = "1.0";
export const COMMAND_PREFIX : string = "nbls";
const DATABASE: string = 'Database';
const listeners = new Map<string, string[]>();
let client: Promise<NbLanguageClient>;
let testAdapter: NbTestAdapter | undefined;
let nbProcess : ChildProcess | null = null;
let debugPort: number = -1;
let consoleLog: boolean = !!process.env['ENABLE_CONSOLE_LOG'];

export class NbLanguageClient extends LanguageClient {
    private _treeViewService: TreeViewService;

    constructor (id : string, name: string, s : ServerOptions, log : vscode.OutputChannel, c : LanguageClientOptions) {
        super(id, name, s, c);
        this._treeViewService = createTreeViewService(log, this);
    }

    findTreeViewService(): TreeViewService {
        return this._treeViewService;
    }

    stop(): Promise<void> {
        // stop will be called even in case of external close & client restart, so OK.
        const r: Promise<void> = super.stop();
        this._treeViewService.dispose();
        return r;
    }

}

function handleLog(log: vscode.OutputChannel, msg: string): void {
    log.appendLine(msg);
    if (consoleLog) {
        console.log(msg);
    }
}

function handleLogNoNL(log: vscode.OutputChannel, msg: string): void {
    log.append(msg);
    if (consoleLog) {
        process.stdout.write(msg);
    }
}

export function enableConsoleLog() {
    consoleLog = true;
    console.log("enableConsoleLog");
}

export function findClusters(myPath : string): string[] {
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

// for tests only !
export function awaitClient() : Promise<NbLanguageClient> {
    const c : Promise<NbLanguageClient> = client;
    if (c && !(c instanceof InitialPromise)) {
        return c;
    }
    let nbcode = vscode.extensions.getExtension('asf.apache-netbeans-java');
    if (!nbcode) {
        return Promise.reject(new Error("Extension not installed."));
    }
    const t : Thenable<NbLanguageClient> = nbcode.activate().then(nc => {
        if (client === undefined || client instanceof InitialPromise) {
            throw new Error("Client not available");
        } else {
            return client;
        }
    });
    return Promise.resolve(t);
}

function findJDK(onChange: (path : string | null) => void): void {
    let nowDark : boolean = isDarkColorTheme();
    let nowJavaEnabled : boolean = isJavaSupportEnabled();
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
        if (timeout) {
            return;
        }
        let interested : boolean = false;
        if (params.affectsConfiguration('netbeans') || params.affectsConfiguration('java')) {
            interested = true;
        } else if (params.affectsConfiguration('workbench.colorTheme')) {
            let d = isDarkColorTheme();
            if (d != nowDark) {
                interested = true;
            }
        }
        if (!interested) {
            return;
        }
        timeout = setTimeout(() => {
            timeout = undefined;
            let newJdk = find();
            let newD = isDarkColorTheme();
            let newJavaEnabled = isJavaSupportEnabled();
            if (newJdk !== currentJdk || newD != nowDark || newJavaEnabled != nowJavaEnabled) {
                nowDark = newD;
                nowJavaEnabled = newJavaEnabled;
                currentJdk = newJdk;
                onChange(currentJdk);
            }
        }, 0);
    });
    onChange(currentJdk);
}

interface VSNetBeansAPI {
    version : string;
    apiVersion: string;
}

function contextUri(ctx : any) : vscode.Uri | undefined {
    if (ctx?.fsPath) {
        return ctx as vscode.Uri;
    } else if (ctx?.resourceUri) {
        return ctx.resourceUri as vscode.Uri;
    } else if (typeof ctx == 'string') {
        try {
            return vscode.Uri.parse(ctx, true);
        } catch (err) {
            return vscode.Uri.file(ctx);
        }
    }
    return vscode.window.activeTextEditor?.document?.uri;
}

/**
 * Executes a project action. It is possible to provide an explicit configuration to use (or undefined), display output from the action etc.
 * Arguments are attempted to parse as file or editor references or Nodes; otherwise they are attempted to be passed to the action as objects.
 *
 * @param action ID of the project action to run
 * @param configuration configuration to use or undefined - use default/active one.
 * @param title Title for the progress displayed in vscode
 * @param log output channel that should be revealed
 * @param showOutput if true, reveals the passed output channel
 * @param args additional arguments
 * @returns Promise for the command's result
 */
function wrapProjectActionWithProgress(action : string, configuration : string | undefined, title : string, log? : vscode.OutputChannel, showOutput? : boolean, ...args : any[]) : Thenable<unknown> {
    let items = [];
    let actionParams = {
        action : action,
        configuration : configuration,
    } as ProjectActionParams;
    for (let item of args) {
        let u : vscode.Uri | undefined;
        if (item?.fsPath) {
            items.push((item.fsPath as vscode.Uri).toString());
        } else if (item?.resourceUri) {
            items.push((item.resourceUri as vscode.Uri).toString());
        } else {
            items.push(item);
        }
    }
    return wrapCommandWithProgress(COMMAND_PREFIX + '.project.run.action', title, log, showOutput, actionParams, ...items);
}

function wrapCommandWithProgress(lsCommand : string, title : string, log? : vscode.OutputChannel, showOutput? : boolean, ...args : any[]) : Thenable<unknown> {
    return window.withProgress({ location: ProgressLocation.Window }, p => {
        return new Promise(async (resolve, reject) => {
            let c : LanguageClient = await client;
            const commands = await vscode.commands.getCommands();
            if (commands.includes(lsCommand)) {
                p.report({ message: title });
                c.outputChannel.show(true);
                const start = new Date().getTime();
                try {
                    if (log) {
                        handleLog(log, `starting ${lsCommand}`);
                    }
                    const res = await vscode.commands.executeCommand(lsCommand, ...args)
                    const elapsed = new Date().getTime() - start;
                    if (log) {
                        handleLog(log, `finished ${lsCommand} in ${elapsed} ms with result ${res}`);
                    }
                    const humanVisibleDelay = elapsed < 1000 ? 1000 : 0;
                    setTimeout(() => { // set a timeout so user would still see the message when build time is short
                        if (res) {
                            resolve(res);
                        } else {
                            if (log) {
                                handleLog(log, `Command ${lsCommand} takes too long to start`);
                            }
                            reject(res);
                        }
                    }, humanVisibleDelay);
                } catch (err : any) {
                    if (log) {
                        handleLog(log, `command ${lsCommand} executed with error: ${JSON.stringify(err)}`);
                    }
                }
            } else {
                reject(`cannot run ${lsCommand}; client is ${c}`);
            }
        });
    });
}

/**
 * Just a simple promise subclass, so I can test for the 'initial promise' value:
 * unlike all other promises, that must be fullfilled in order to e.g. properly stop the server or otherwise communicate with it,
 * the initial one needs to be largely ignored in the launching/mgmt code, BUT should be available to normal commands / features.
 */
class InitialPromise extends Promise<NbLanguageClient> {
    constructor(f : (resolve: (value: NbLanguageClient | PromiseLike<NbLanguageClient>) => void, reject: (reason?: any) => void) => void) {
        super(f);
    }
}

/**
 * Determines the outcome, if there's a conflict betwee RH Java and us: disable java, enable java, ask the user.
 * @returns false, if java should be disablde; true, if enabled. Undefined if no config is present, ask the user
 */
function shouldEnableConflictingJavaSupport() : boolean | undefined {
    // backwards compatibility; remove in NBLS 19
    if (vscode.extensions.getExtension('oracle-labs-graalvm.gcn')) {
        return false;
    }
    let r = undefined;
    for (const ext of vscode.extensions.all) {
        const services = ext.packageJSON?.contributes && ext.packageJSON?.contributes['netbeans.options'];
        if (!services) {
            continue;
        }
        if (services['javaSupport.conflict'] !== undefined) {
            const v = !!services['javaSupport.conflict'];
            if (!v) {
                // request to disable wins.
                return false;
            }
            r = v;
        }
    }
    return r;
}

export function activate(context: ExtensionContext): VSNetBeansAPI {
    let log = vscode.window.createOutputChannel("Apache NetBeans Language Server");

    var clientResolve : (x : NbLanguageClient) => void;
    var clientReject : (err : any) => void;

    // establish a waitable Promise, export the callbacks so they can be called after activation.
    client = new InitialPromise((resolve, reject) => {
        clientResolve = resolve;
        clientReject = reject;
    });

    function checkConflict(): void {
        let conf = workspace.getConfiguration();
        if (conf.get("netbeans.conflict.check")) {
            if (conf.get("netbeans.javaSupport.enabled")) {
                const e : boolean | undefined = shouldEnableConflictingJavaSupport();
                if (!e && vscode.extensions.getExtension('redhat.java')) {
                    if (e === false) {
                        // do not ask, an extension wants us to disable on conflict
                        conf.update("netbeans.javaSupport.enabled", false, true);
                    } else {
                        const DISABLE_EXTENSION = `Manually disable extension`;
                        const DISABLE_JAVA = `Disable Java in Apache NetBeans Language Server`;
                        vscode.window.showInformationMessage(`Another Java support extension is already installed. It is recommended to use only one Java support per workspace.`, DISABLE_EXTENSION, DISABLE_JAVA).then((selected) => {
                            if (DISABLE_EXTENSION === selected) {
                                vscode.commands.executeCommand('workbench.extensions.action.showInstalledExtensions');
                            } else if (DISABLE_JAVA === selected) {
                                conf.update("netbeans.javaSupport.enabled", false, true);
                            }
                        });
                    }
                }
            } else if (!vscode.extensions.getExtension('redhat.java')) {
                workspace.findFiles(`**/*.java`, undefined, 1).then(files => {
                    if (files.length) {
                        const ENABLE_JAVA = `Enable Java in Apache NetBeans Language Server`;
                        vscode.window.showInformationMessage(`Java in Apache NetBeans Language Server is disabled and no other Java support extension is currently installed.`, ENABLE_JAVA).then((selected) => {
                            if (ENABLE_JAVA === selected) {
                                conf.update("netbeans.javaSupport.enabled", true, true);
                            }
                        });
                    }
                });
            }
        }
    }
    checkConflict();

    // find acceptable JDK and launch the Java part
    findJDK((specifiedJDK) => {
        let currentClusters = findClusters(context.extensionPath).sort();
        const dsSorter = (a: TextDocumentFilter, b: TextDocumentFilter) => {
            return (a.language || '').localeCompare(b.language || '')
                || (a.pattern || '').localeCompare(b.pattern || '')
                || (a.scheme || '').localeCompare(b.scheme || '');
        };
        let currentDocumentSelectors = collectDocumentSelectors().sort(dsSorter);
        context.subscriptions.push(vscode.extensions.onDidChange(() => {
            checkConflict();
            const newClusters = findClusters(context.extensionPath).sort();
            const newDocumentSelectors = collectDocumentSelectors().sort(dsSorter);
            if (newClusters.length !== currentClusters.length || newDocumentSelectors.length !== currentDocumentSelectors.length
                || newClusters.find((value, index) => value !== currentClusters[index]) || newDocumentSelectors.find((value, index) => value !== currentDocumentSelectors[index])) {
                currentClusters = newClusters;
                currentDocumentSelectors = newDocumentSelectors;
                activateWithJDK(specifiedJDK, context, log, true, clientResolve, clientReject);
            }
        }));
        activateWithJDK(specifiedJDK, context, log, true, clientResolve, clientReject);
    });

    //register debugger:
    let debugTrackerFactory =new NetBeansDebugAdapterTrackerFactory();
    context.subscriptions.push(vscode.debug.registerDebugAdapterTrackerFactory('java+', debugTrackerFactory));
    let configInitialProvider = new NetBeansConfigurationInitialProvider();
    context.subscriptions.push(vscode.debug.registerDebugConfigurationProvider('java+', configInitialProvider, vscode.DebugConfigurationProviderTriggerKind.Initial));
    let configDynamicProvider = new NetBeansConfigurationDynamicProvider(context);
    context.subscriptions.push(vscode.debug.registerDebugConfigurationProvider('java+', configDynamicProvider, vscode.DebugConfigurationProviderTriggerKind.Dynamic));
    let configResolver = new NetBeansConfigurationResolver();
    context.subscriptions.push(vscode.debug.registerDebugConfigurationProvider('java+', configResolver));
    let configNativeResolver = new NetBeansConfigurationNativeResolver();
    context.subscriptions.push(vscode.debug.registerDebugConfigurationProvider('nativeimage', configNativeResolver));
    context.subscriptions.push(vscode.debug.onDidTerminateDebugSession(((session) => onDidTerminateSession(session))));

    let debugDescriptionFactory = new NetBeansDebugAdapterDescriptionFactory();
    context.subscriptions.push(vscode.debug.registerDebugAdapterDescriptorFactory('java+', debugDescriptionFactory));
    context.subscriptions.push(vscode.debug.registerDebugAdapterDescriptorFactory('nativeimage', debugDescriptionFactory));

    // initialize Run Configuration
    initializeRunConfiguration().then(initialized => {
		if (initialized) {
            context.subscriptions.push(vscode.debug.registerDebugConfigurationProvider('java+', dBConfigurationProvider));
            context.subscriptions.push(vscode.debug.registerDebugConfigurationProvider('java', dBConfigurationProvider));
			context.subscriptions.push(vscode.debug.registerDebugConfigurationProvider('java+', runConfigurationProvider));
			context.subscriptions.push(vscode.debug.registerDebugConfigurationProvider('java', runConfigurationProvider));
			context.subscriptions.push(vscode.window.registerTreeDataProvider('run-config', runConfigurationNodeProvider));
			context.subscriptions.push(vscode.commands.registerCommand(COMMAND_PREFIX + '.workspace.configureRunSettings', (...params: any[]) => {
				configureRunSettings(context, params);
			}));
			vscode.commands.executeCommand('setContext', 'runConfigurationInitialized', true);
		}
	});

    // register commands
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.workspace.new', async (ctx) => {
        let c : LanguageClient = await client;
        const commands = await vscode.commands.getCommands();
        if (commands.includes(COMMAND_PREFIX + '.new.from.template')) {
            // first give the context, then the open-file hint in the case the context is not specific enough
            const res = await vscode.commands.executeCommand(COMMAND_PREFIX + '.new.from.template', contextUri(ctx)?.toString(), vscode.window.activeTextEditor?.document?.uri?.toString());

            if (typeof res === 'string') {
                let newFile = vscode.Uri.parse(res as string);
                await vscode.window.showTextDocument(newFile, { preview: false });
            } else if (Array.isArray(res)) {
                for(let r of res) {
                    if (typeof r === 'string') {
                        let newFile = vscode.Uri.parse(r as string);
                        await vscode.window.showTextDocument(newFile, { preview: false });
                    }
                }
            }
        } else {
            throw `Client ${c} doesn't support new from template`;
        }
    }));
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.workspace.newproject', async (ctx) => {
        let c : LanguageClient = await client;
        const commands = await vscode.commands.getCommands();
        if (commands.includes(COMMAND_PREFIX + '.new.project')) {
            const res = await vscode.commands.executeCommand(COMMAND_PREFIX + '.new.project', contextUri(ctx)?.toString());
            if (typeof res === 'string') {
                let newProject = vscode.Uri.parse(res as string);

                const OPEN_IN_NEW_WINDOW = 'Open in new window';
                const ADD_TO_CURRENT_WORKSPACE = 'Add to current workspace';

                const value = await vscode.window.showInformationMessage('New project created', OPEN_IN_NEW_WINDOW, ADD_TO_CURRENT_WORKSPACE);
                if (value === OPEN_IN_NEW_WINDOW) {
                    await vscode.commands.executeCommand('vscode.openFolder', newProject, true);
                } else if (value === ADD_TO_CURRENT_WORKSPACE) {
                    vscode.workspace.updateWorkspaceFolders(vscode.workspace.workspaceFolders ? vscode.workspace.workspaceFolders.length : 0, undefined, { uri: newProject });
                }
            }
        } else {
            throw `Client ${c} doesn't support new project`;
        }
    }));
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.workspace.compile', () =>
        wrapCommandWithProgress(COMMAND_PREFIX + '.build.workspace', 'Compiling workspace...', log, true)
    ));
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.workspace.clean', () =>
        wrapCommandWithProgress(COMMAND_PREFIX + '.clean.workspace', 'Cleaning workspace...', log, true)
    ));
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.project.compile', (args) => {
        wrapProjectActionWithProgress('build', undefined, 'Compiling...', log, true, args);
    }));
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.project.clean', (args) => {
        wrapProjectActionWithProgress('clean', undefined, 'Cleaning...', log, true, args);
    }));
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.open.type', () => {
        wrapCommandWithProgress(COMMAND_PREFIX + '.quick.open', 'Opening type...', log, true).then(() => {
            commands.executeCommand('workbench.action.focusActiveEditorGroup');
        });
    }));
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.java.goto.super.implementation', async () => {
        if (window.activeTextEditor?.document.languageId !== "java") {
            return;
        }
        const uri = window.activeTextEditor.document.uri;
        const position = window.activeTextEditor.selection.active;
        const locations: any[] = await vscode.commands.executeCommand(COMMAND_PREFIX + '.java.super.implementation', uri.toString(), position) || [];
        return vscode.commands.executeCommand('editor.action.goToLocations', window.activeTextEditor.document.uri, position,
            locations.map(location => new vscode.Location(vscode.Uri.parse(location.uri), new vscode.Range(location.range.start.line, location.range.start.character, location.range.end.line, location.range.end.character))),
            'peek', 'No super implementation found');
    }));
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.rename.element.at', async (offset) => {
        const editor = window.activeTextEditor;
        if (editor) {
            await commands.executeCommand('editor.action.rename', [
                editor.document.uri,
                editor.document.positionAt(offset),
            ]);
        }
    }));
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.surround.with', async (items) => {
        const selected: any = await window.showQuickPick(items, { placeHolder: 'Surround with ...' });
        if (selected) {
            if (selected.userData.edit) {
                const edit = await (await client).protocol2CodeConverter.asWorkspaceEdit(selected.userData.edit as ls.WorkspaceEdit);
                await workspace.applyEdit(edit);
                await commands.executeCommand('workbench.action.focusActiveEditorGroup');
            }
            await commands.executeCommand(selected.userData.command.command, ...(selected.userData.command.arguments || []));
        }
    }));
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.db.add.all.connection', async () => {
        const ADD_JDBC = 'Add Database Connection';
        const ADD_ADB = 'Add Oracle Autonomous DB';
        const selected: any = await window.showQuickPick([ADD_JDBC, ADD_ADB], { placeHolder: 'Select type...' });
        if (selected == ADD_JDBC) {
            await commands.executeCommand('nbls.db.add.connection');
        } else if (selected == ADD_ADB) {
            await commands.executeCommand('nbls:Tools:org.netbeans.modules.cloud.oracle.actions.AddADBAction');
        }
    }));

    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.generate.code', async (command, data) => {
        const edit: any = await commands.executeCommand(command, data);
        if (edit) {
            const wsEdit = await (await client).protocol2CodeConverter.asWorkspaceEdit(edit as ls.WorkspaceEdit);
            await workspace.applyEdit(wsEdit);
            await commands.executeCommand('workbench.action.focusActiveEditorGroup');
        }
    }));

    async function findRunConfiguration(uri : vscode.Uri) : Promise<vscode.DebugConfiguration|undefined> {
        // do not invoke debug start with no (java+) configurations, as it would probably create an user prompt
        let cfg = vscode.workspace.getConfiguration("launch");
        let c = cfg.get('configurations');
        if (!Array.isArray(c)) {
            return undefined;
        }
        let f = c.filter((v) => v['type'] === 'java+');
        if (!f.length) {
            return undefined;
        }
        class P implements vscode.DebugConfigurationProvider {
            config : vscode.DebugConfiguration | undefined;
            
            resolveDebugConfigurationWithSubstitutedVariables(folder: vscode.WorkspaceFolder | undefined, debugConfiguration: vscode.DebugConfiguration, token?: vscode.CancellationToken): vscode.ProviderResult<vscode.DebugConfiguration> {
                this.config = debugConfiguration;
                return undefined;
            }
        }
        let provider = new P();
        let d = vscode.debug.registerDebugConfigurationProvider('java+', provider);
        // let vscode to select a debug config
        return await vscode.commands.executeCommand('workbench.action.debug.start', { config: {
            type: 'java+',
            mainClass: uri.toString()
        }, noDebug: true}).then((v) => {
            d.dispose();
            return provider.config;
        }, (err) => {
            d.dispose();
            return undefined;
        });
    }

    const runDebug = async (noDebug: boolean, testRun: boolean, uri: any, methodName?: string, launchConfiguration?: string, project : boolean = false, ) => {
    const docUri = contextUri(uri);
        if (docUri) {
            // attempt to find the active configuration in the vsode launch settings; undefined if no config is there.
            let debugConfig : vscode.DebugConfiguration = await findRunConfiguration(docUri) || {
                type: "java+",
                name: "Java Single Debug",
                request: "launch"
            };
            if (!methodName) {
                debugConfig['methodName'] = methodName;
            }
            if (launchConfiguration == '') {
                if (debugConfig['launchConfiguration']) {
                    delete debugConfig['launchConfiguration'];
                }
            } else {
                debugConfig['launchConfiguration'] = launchConfiguration;
            }
            debugConfig['testRun'] = testRun;

            const workspaceFolder = vscode.workspace.getWorkspaceFolder(docUri);
            if (project) {
                debugConfig['projectFile'] = docUri.toString();
                debugConfig['project'] = true;
            } else {
                debugConfig['mainClass'] =  docUri.toString();
            }
            
            const debugOptions : vscode.DebugSessionOptions = {
                noDebug: noDebug,
            }
            
            
            const ret = await vscode.debug.startDebugging(workspaceFolder, debugConfig, debugOptions);
            return ret ? new Promise((resolve) => {
                const listener = vscode.debug.onDidTerminateDebugSession(() => {
                    listener.dispose();
                    resolve(true);
                });
            }) : ret;
        }
    };
    
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.run.test', async (uri, methodName?, launchConfiguration?) => {
        await runDebug(true, true, uri, methodName, launchConfiguration);
    }));
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.debug.test', async (uri, methodName?, launchConfiguration?) => {
        await runDebug(false, true, uri, methodName, launchConfiguration);
    }));
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.run.single', async (uri, methodName?, launchConfiguration?) => {
        await runDebug(true, false, uri, methodName, launchConfiguration);
    }));
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.debug.single', async (uri, methodName?, launchConfiguration?) => {
        await runDebug(false, false, uri, methodName, launchConfiguration);
    }));
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.project.run', async (node, launchConfiguration?) => {
        return runDebug(true, false, contextUri(node)?.toString() || '',  undefined, launchConfiguration, true);
    }));
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.project.debug', async (node, launchConfiguration?) => {
        return runDebug(false, false, contextUri(node)?.toString() || '',  undefined, launchConfiguration, true);
    }));
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.project.test', async (node, launchConfiguration?) => {
        return runDebug(true, true, contextUri(node)?.toString() || '',  undefined, launchConfiguration, true);
    }));
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.package.test', async (uri, launchConfiguration?) => {
        await runDebug(true, true, uri, undefined, launchConfiguration);
    }));
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.open.stacktrace', async (uri, methodName, fileName, line) => {
        const location: string | undefined = uri ? await commands.executeCommand(COMMAND_PREFIX + '.resolve.stacktrace.location', uri, methodName, fileName) : undefined;
        if (location) {
            const lNum = line - 1;
            window.showTextDocument(vscode.Uri.parse(location), { selection: new vscode.Range(new vscode.Position(lNum, 0), new vscode.Position(lNum, 0)) });
        } else {
            if (methodName) {
                const fqn: string = methodName.substring(0, methodName.lastIndexOf('.'));
                commands.executeCommand('workbench.action.quickOpen', '#' + fqn.substring(fqn.lastIndexOf('.') + 1));
            }
        }
    }));
    context.subscriptions.push(commands.registerCommand('nbls.workspace.symbols', async (query) => {
        const c = await client;
        return (await c.sendRequest<SymbolInformation[]>("workspace/symbol", { "query": query })) ?? [];
    }));
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.java.complete.abstract.methods', async () => {
        const active = vscode.window.activeTextEditor;
        if (active) {
            const position = new vscode.Position(active.selection.start.line, active.selection.start.character);
            await commands.executeCommand(COMMAND_PREFIX + '.java.implement.all.abstract.methods', active.document.uri.toString(), position);
        }
    }));
    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.startup.condition', async () => {
        return client;
    }));
    context.subscriptions.push(commands.registerCommand('nbls.addEventListener', (eventName, listener) => {
        let ls = listeners.get(eventName);
        if (!ls) {
            ls = [];
            listeners.set(eventName, ls);
        }
        ls.push(listener);
    }));
    context.subscriptions.push(commands.registerCommand('nbls.node.properties.edit',
        async (node) => await PropertiesView.createOrShow(context, node, (await client).findTreeViewService())));

    context.subscriptions.push(commands.registerCommand(COMMAND_PREFIX + '.cloud.ocid.copy',
        async (node) => {
            const ocid : string = await commands.executeCommand(COMMAND_PREFIX + '.cloud.ocid.get', node.id);
            vscode.env.clipboard.writeText(ocid);
        }
    ));

    const archiveFileProvider = <vscode.TextDocumentContentProvider> {
        provideTextDocumentContent: async (uri: vscode.Uri, token: vscode.CancellationToken): Promise<string> => {
            return await commands.executeCommand('nbls.get.archive.file.content', uri.toString());
        }
    };
    context.subscriptions.push(workspace.registerTextDocumentContentProvider('jar', archiveFileProvider));
    context.subscriptions.push(workspace.registerTextDocumentContentProvider('nbjrt', archiveFileProvider));

    launchConfigurations.updateLaunchConfig();

    // register completions:
    launchConfigurations.registerCompletion(context);
    return Object.freeze({
        version : API_VERSION,
        apiVersion : API_VERSION
    });
}

/**
 * Pending maintenance (install) task, activations should be chained after it.
 */
let maintenance : Promise<void> | null;

/**
 * Pending activation flag. Will be cleared when the process produces some message or fails.
 */
let activationPending : boolean = false;

function activateWithJDK(specifiedJDK: string | null, context: ExtensionContext, log : vscode.OutputChannel, notifyKill: boolean, 
    clientResolve? : (x : NbLanguageClient) => void, clientReject? : (x : any) => void): void {
    if (activationPending) {
        // do not activate more than once in parallel.
        handleLog(log, "Server activation requested repeatedly, ignoring...");
        return;
    }
    let oldClient = client;
    let setClient : [(c : NbLanguageClient) => void, (err : any) => void];
    client = new Promise<NbLanguageClient>((clientOK, clientErr) => {
        setClient = [
            function (c : NbLanguageClient) {
                clientOK(c);
                if (clientResolve) {
                    clientResolve(c);
                }
            }, function (err) {
                clientErr(err);
                if (clientReject) {
                    clientReject(err);
                }
            }
        ]
        //setClient = [ clientOK, clientErr ];
    });
    const a : Promise<void> | null = maintenance;

    commands.executeCommand('setContext', 'nbJavaLSReady', false);
    commands.executeCommand('setContext', 'dbAddConnectionPresent', true);
    activationPending = true;
    // chain the restart after termination of the former process.
    if (a != null) {
        handleLog(log, "Server activation initiated while in maintenance mode, scheduling after maintenance");
        a.then(() => stopClient(oldClient)).then(() => killNbProcess(notifyKill, log)).then(() => {
            doActivateWithJDK(specifiedJDK, context, log, notifyKill, setClient);
        });
    } else {
        handleLog(log, "Initiating server activation");
        stopClient(oldClient).then(() => killNbProcess(notifyKill, log)).then(() => {
            doActivateWithJDK(specifiedJDK, context, log, notifyKill, setClient);
        });
    }
}


function killNbProcess(notifyKill : boolean, log : vscode.OutputChannel, specProcess?: ChildProcess) : Promise<void> {
    const p = nbProcess;
    handleLog(log, "Request to kill LSP server.");
    if (p && (!specProcess || specProcess == p)) {
        if (notifyKill) {
            vscode.window.setStatusBarMessage("Restarting Apache NetBeans Language Server.", 2000);
        }
        return new Promise((resolve, reject) => {
            nbProcess = null;
            p.on('close', function(code: number) {
                handleLog(log, "LSP server closed: " + p.pid)
                resolve();
            });
            handleLog(log, "Killing LSP server " + p.pid);
            if (!p.kill()) {
                reject("Cannot kill");
            }
        });
    } else {
        let msg = "Cannot kill: ";
        if (specProcess) {
            msg += "Requested kill on " + specProcess.pid + ", ";
        }
        handleLog(log, msg + "current process is " + (p ? p.pid : "None"));
        return new Promise((res, rej) => { res(); });
    }
}

/**
 * Attempts to determine if the Workbench is using dark-style color theme, so that NBLS
 * starts with some dark L&F for icon resource selection.
 */
function isDarkColorTheme() : boolean {
    const themeName = workspace.getConfiguration('workbench')?.get('colorTheme');
    if (!themeName) {
        return false;
    }
    for (const ext of vscode.extensions.all) {
        const themeList : object[] =  ext.packageJSON?.contributes && ext.packageJSON?.contributes['themes'];
        if (!themeList) {
            continue;
        }
        let t : any;
        for (t of themeList) {
            if (t.id !== themeName) {
                continue;
            }
            const uiTheme = t.uiTheme;
            if (typeof(uiTheme) == 'string') {
                if (uiTheme.includes('-dark') || uiTheme.includes('-black')) {
                    return true;
                }
            }
        }
    }
    return false;
}

function isJavaSupportEnabled() : boolean {
    return workspace.getConfiguration('netbeans')?.get('javaSupport.enabled') as boolean;
}

function doActivateWithJDK(specifiedJDK: string | null, context: ExtensionContext, log : vscode.OutputChannel, notifyKill: boolean,
    setClient : [(c : NbLanguageClient) => void, (err : any) => void]
): void {
    maintenance = null;
    let restartWithJDKLater : ((time: number, n: boolean) => void) = function restartLater(time: number, n : boolean) {
        handleLog(log, `Restart of Apache Language Server requested in ${(time / 1000)} s.`);
        setTimeout(() => {
            activateWithJDK(specifiedJDK, context, log, n);
        }, time);
    };

    const netbeansConfig = workspace.getConfiguration('netbeans');
    const beVerbose : boolean = netbeansConfig.get('verbose', false);
    let userdir = process.env['nbcode_userdir'] || netbeansConfig.get('userdir', 'local');
    switch (userdir) {
        case 'local':
            if (context.storagePath) {
                userdir = context.storagePath;
                break;
            }
            // fallthru
        case 'global':
            userdir = context.globalStoragePath;
            break;
        default:
            // assume storage is path on disk
    }

    let info = {
        clusters : findClusters(context.extensionPath),
        extensionPath: context.extensionPath,
        storagePath : userdir,
        jdkHome : specifiedJDK,
        verbose: beVerbose
    };
    let launchMsg = `Launching Apache NetBeans Language Server with ${specifiedJDK ? specifiedJDK : 'default system JDK'} and userdir ${userdir}`;
    handleLog(log, launchMsg);
    vscode.window.setStatusBarMessage(launchMsg, 2000);

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
            let extras : string[] = ["--modules", "-J-XX:PerfMaxStringConstLength=10240"];
            if (isDarkColorTheme()) {
                extras.push('--laf', 'com.formdev.flatlaf.FlatDarkLaf');
            }
            if (isJavaSupportEnabled()) {
                extras.push('--direct-disable', 'org.netbeans.modules.nbcode.integration.java');
            } else {
                extras.push('--enable', 'org.netbeans.modules.nbcode.integration.java');
            }
            extras.push(`--start-java-language-server=connect:${address.port}`);
            extras.push(`--start-java-debug-adapter-server=listen:0`);
            const srv = launcher.launch(info,...extras);
            var p = srv;
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
            let stdOut : string | null = '';
            function logAndWaitForEnabled(text: string, isOut: boolean) {
                if (p == nbProcess) {
                    activationPending = false;
                }
                handleLogNoNL(log, text);
                if (stdOut == null) {
                    return;
                }
                if (isOut) {
                    stdOut += text;
                }
                if (stdOut.match(/org.netbeans.modules.java.lsp.server/)) {
                    stdOut = null;
                }
            }
            handleLog(log, "LSP server launching: " + p.pid);
            handleLog(log, "LSP server user directory: " + userdir);
            p.stdout.on('data', function(d: any) {
                logAndWaitForEnabled(d.toString(), true);
            });
            p.stderr.on('data', function(d: any) {
                logAndWaitForEnabled(d.toString(), false);
            });
            nbProcess = p;
            p.on('close', function(code: number) {
                if (p == nbProcess) {
                    nbProcess = null;
                }
                if (p == nbProcess && code != 0 && code) {
                    vscode.window.showWarningMessage("Apache NetBeans Language Server exited with " + code);
                }
                if (stdOut != null) {
                    let match = stdOut.match(/org.netbeans.modules.java.lsp.server[^\n]*/)
                    if (match?.length == 1) {
                        handleLog(log, match[0]);
                    } else {
                        handleLog(log, "Cannot find org.netbeans.modules.java.lsp.server in the log!");
                    }
                    log.show(false);
                    killNbProcess(false, log, p);
                    reject("Apache NetBeans Language Server not enabled!");
                } else {
                    handleLog(log, "LSP server " + p.pid + " terminated with " + code);
                    handleLog(log, "Exit code " + code);
                }
            });

        });
    });
    const conf = workspace.getConfiguration();
    let documentSelectors : DocumentSelector = [
            { language: 'java' },
            { language: 'yaml', pattern: '**/{application,bootstrap}*.yml' },
            { language: 'properties', pattern: '**/{application,bootstrap}*.properties' },
            { language: 'jackpot-hint' },
            { language: 'xml', pattern: '**/pom.xml' },
            { pattern: '**/build.gradle'}
    ];
    documentSelectors.push(...collectDocumentSelectors());
    const enableJava = isJavaSupportEnabled();
    const enableGroovy : boolean = conf.get("netbeans.groovySupport.enabled") as boolean;
    if (enableGroovy) {
        documentSelectors.push({ language: 'groovy'});
    }
    // Options to control the language client
    let clientOptions: LanguageClientOptions = {
        // Register the server for java documents
        documentSelector: documentSelectors,
        synchronize: {
            configurationSection: [
                'netbeans.format',
                'netbeans.java.imports',
                'java+.runConfig.vmOptions'
            ],
            fileEvents: [
                workspace.createFileSystemWatcher('**/*.java')
            ]
        },
        outputChannel: log,
        revealOutputChannelOn: RevealOutputChannelOn.Never,
        progressOnInitialization: true,
        initializationOptions : {
            'nbcodeCapabilities' : {
                'statusBarMessageSupport' : true,
                'testResultsSupport' : true,
                'showHtmlPageSupport' : true,
                'wantsJavaSupport' : enableJava,
                'wantsGroovySupport' : enableGroovy
            }
        },
        errorHandler: {
            error : function(error: Error, _message: Message, count: number): ErrorHandlerResult {
                return { action: ErrorAction.Continue, message: error.message };
            },
            closed : function(): CloseHandlerResult {
                handleLog(log, "Connection to Apache NetBeans Language Server closed.");
                if (!activationPending) {
                    restartWithJDKLater(10000, false);
                }
                return { action: CloseAction.DoNotRestart };
            }
        }
    }

    let c = new NbLanguageClient(
            'java',
            'NetBeans Java',
            connection,
            log,
            clientOptions
    );
    handleLog(log, 'Language Client: Starting');
    c.start().then(() => {
        if (isJavaSupportEnabled()) {
            testAdapter = new NbTestAdapter();
        }
        c.onNotification(StatusMessageRequest.type, showStatusBarMessage);
        c.onRequest(HtmlPageRequest.type, showHtmlPage);
        c.onRequest(ExecInHtmlPageRequest.type, execInHtmlPage);
        c.onNotification(LogMessageNotification.type, (param) => handleLog(log, param.message));
        c.onRequest(QuickPickRequest.type, async param => {
            const selected = await window.showQuickPick(param.items, { title: param.title, placeHolder: param.placeHolder, canPickMany: param.canPickMany, ignoreFocusOut: true });
            return selected ? Array.isArray(selected) ? selected : [selected] : undefined;
        });
        c.onRequest(UpdateConfigurationRequest.type, async (param) => {
            await vscode.workspace.getConfiguration(param.section).update(param.key, param.value);
            runConfigurationUpdateAll();
        });
        c.onRequest(SaveDocumentsRequest.type, async (request : SaveDocumentRequestParams) => {
            const uriList = request.documents.map(s => {
                let re = /^file:\/(?:\/\/)?([A-Za-z]):\/(.*)$/.exec(s);
                if (!re) {
                    return s;
                }
                // don't ask why vscode mangles URIs this way; in addition, it uses lowercase drive letter ???
                return `file:///${re[1].toLowerCase()}%3A/${re[2]}`;
            });
            for (let ed of workspace.textDocuments) {
                if (uriList.includes(ed.uri.toString())) {
                    return ed.save();
                }
            }
            return false;
        });
        c.onRequest(InputBoxRequest.type, async param => {
            return await window.showInputBox({ title: param.title, prompt: param.prompt, value: param.value, password: param.password });
        });
        c.onRequest(MutliStepInputRequest.type, async param => {
            const data: { [name: string]: readonly vscode.QuickPickItem[] | string } = {};
            async function nextStep(input: MultiStepInput, step: number, state: { [name: string]: readonly vscode.QuickPickItem[] | string }): Promise<InputStep | void> {
                const inputStep = await c.sendRequest(MutliStepInputRequest.step, { inputId: param.id, step, data: state });
                if (inputStep && inputStep.hasOwnProperty('items')) {
                    const quickPickStep = inputStep as QuickPickStep;
                    state[inputStep.stepId] = await input.showQuickPick({
                        title: param.title,
                        step,
                        totalSteps: quickPickStep.totalSteps,
                        placeholder: quickPickStep.placeHolder,
                        items: quickPickStep.items,
                        canSelectMany: quickPickStep.canPickMany,
                        selectedItems: quickPickStep.items.filter(item => item.picked)
                    });
                    return (input: MultiStepInput) => nextStep(input, step + 1, state);
                } else if (inputStep && inputStep.hasOwnProperty('value')) {
                    const inputBoxStep = inputStep as InputBoxStep;
                    state[inputStep.stepId] = await input.showInputBox({
                        title: param.title,
                        step,
                        totalSteps: inputBoxStep.totalSteps,
                        value: state[inputStep.stepId] as string || inputBoxStep.value,
                        prompt: inputBoxStep.prompt,
                        password: inputBoxStep.password,
                        validate: (val) => {
                            const d = { ...state };
                            d[inputStep.stepId] = val;
                            return c.sendRequest(MutliStepInputRequest.validate, { inputId: param.id, step, data: d });
                        }
                    });
                    return (input: MultiStepInput) => nextStep(input, step + 1, state);
                }
            }
            await MultiStepInput.run(input => nextStep(input, 1, data));
            return data;
        });
        c.onNotification(TestProgressNotification.type, param => {
            if (testAdapter) {
                testAdapter.testProgress(param.suite);
            }
        });
        let decorations = new Map<string, TextEditorDecorationType>();
        let decorationParamsByUri = new Map<vscode.Uri, SetTextEditorDecorationParams>();
        c.onRequest(TextEditorDecorationCreateRequest.type, param => {
            let decorationType = vscode.window.createTextEditorDecorationType(param);
            decorations.set(decorationType.key, decorationType);
            return decorationType.key;
        });
        c.onNotification(TextEditorDecorationSetNotification.type, param => {
            let decorationType = decorations.get(param.key);
            if (decorationType) {
                let editorsWithUri = vscode.window.visibleTextEditors.filter(
                    editor => editor.document.uri.toString() == param.uri
                );
                if (editorsWithUri.length > 0) {
                    editorsWithUri[0].setDecorations(decorationType, asRanges(param.ranges));
                    decorationParamsByUri.set(editorsWithUri[0].document.uri, param);
                }
            }
        });
        let disposableListener = vscode.window.onDidChangeVisibleTextEditors(editors => {
            editors.forEach(editor => {
                let decorationParams = decorationParamsByUri.get(editor.document.uri);
                if (decorationParams) {
                    let decorationType = decorations.get(decorationParams.key);
                    if (decorationType) {
                        editor.setDecorations(decorationType, asRanges(decorationParams.ranges));
                    }
                }
            });
        });
        context.subscriptions.push(disposableListener);
        c.onNotification(TextEditorDecorationDisposeNotification.type, param => {
            let decorationType = decorations.get(param);
            if (decorationType) {
                decorations.delete(param);
                decorationType.dispose();
                decorationParamsByUri.forEach((value, key, map) => {
                    if (value.key == param) {
                        map.delete(key);
                    }
                });
            }
        });
        c.onNotification(TelemetryEventNotification.type, (param) => {
            const ls = listeners.get(param);
            if (ls) {
                for (const listener of ls) {
                    commands.executeCommand(listener);
                }
            }
        });
        handleLog(log, 'Language Client: Ready');
        setClient[0](c);
        commands.executeCommand('setContext', 'nbJavaLSReady', true);

        if (enableJava) {
            // create project explorer:
            //c.findTreeViewService().createView('foundProjects', 'Projects', { canSelectMany : false });
            createProjectView(context, c);
        }

        createDatabaseView(c);
        if (enableJava) {
            c.findTreeViewService().createView('cloud.resources', undefined, { canSelectMany : false });
        }
    }).catch(setClient[1]);

    class Decorator implements TreeItemDecorator<Visualizer> {
        private provider : CustomizableTreeDataProvider<Visualizer>;
        private setCommand : vscode.Disposable;
        private initialized: boolean = false;

        constructor(provider : CustomizableTreeDataProvider<Visualizer>, client : NbLanguageClient) {
            this.provider = provider;
            this.setCommand = vscode.commands.registerCommand(COMMAND_PREFIX + '.local.db.set.preferred.connection', (n) => this.setPreferred(n));
        }

        decorateChildren(element: Visualizer, children: Visualizer[]): Visualizer[] {
            if (element.id == this.provider.getRoot().id) {
                vscode.commands.executeCommand('setContext', 'nb.database.view.active', children.length == 0);
            }
            return children;
        }

        async decorateTreeItem(vis : Visualizer, item : vscode.TreeItem) : Promise<vscode.TreeItem> {
            if (!(item.contextValue && item.contextValue.match(/class:org.netbeans.api.db.explorer.DatabaseConnection/))) {
                return item;
            }
            return vscode.commands.executeCommand(COMMAND_PREFIX + '.db.preferred.connection').then((id) => {
                if (id == vis.id) {
                    item.description = '(default)';
                }
                return item;
            });
        }

        setPreferred(...args : any[]) {
            const id : number = args[0]?.id || -1;
            vscode.commands.executeCommand(COMMAND_PREFIX + ':Database:netbeans.db.explorer.action.makepreferred', ...args);
            // refresh all
            this.provider.fireItemChange();
        }

        dispose() {
            this.setCommand?.dispose();
        }
    }

    function createDatabaseView(c : NbLanguageClient) {
        let decoRegister : CustomizableTreeDataProvider<Visualizer>;
        c.findTreeViewService().createView('database.connections', undefined , {
            canSelectMany : true,

            providerInitializer : (customizable) =>
                customizable.addItemDecorator(new Decorator(customizable, c))
        });

    }

    async function createProjectView(ctx : ExtensionContext, client : NbLanguageClient) {
        const ts : TreeViewService = client.findTreeViewService();
        let tv : vscode.TreeView<Visualizer> = await ts.createView('foundProjects', 'Projects', { canSelectMany : false });

        async function revealActiveEditor(ed? : vscode.TextEditor) {
            const uri = window.activeTextEditor?.document?.uri;
            if (!uri || uri.scheme.toLowerCase() !== 'file') {
                return;
            }
            if (!tv.visible) {
                return;
            }
            let vis : Visualizer | undefined = await ts.findPath(tv, uri.toString());
            if (!vis) {
                return;
            }
            tv.reveal(vis, { select : true, focus : false, expand : false });
        }

        ctx.subscriptions.push(window.onDidChangeActiveTextEditor(ed => {
            const netbeansConfig = workspace.getConfiguration('netbeans');
            if (netbeansConfig.get("revealActiveInProjects")) {
                revealActiveEditor(ed);
            }
        }));
        ctx.subscriptions.push(vscode.commands.registerCommand(COMMAND_PREFIX + ".select.editor.projects", () => revealActiveEditor()));

        // attempt to reveal NOW:
        if (netbeansConfig.get("revealActiveInProjects")) {
            revealActiveEditor();
        }
    }

    const webviews = new Map<string, vscode.Webview>();

    async function showHtmlPage(params : HtmlPageParams): Promise<void> {
        return new Promise(resolve => {
            let data = params.text;
            const match = /<title>(.*)<\/title>/i.exec(data);
            const name = match && match.length > 1 ? match[1] : '';
            const resourceDir = vscode.Uri.joinPath(context.globalStorageUri, params.id);
            workspace.fs.createDirectory(resourceDir);
            let view = vscode.window.createWebviewPanel('htmlView', name, vscode.ViewColumn.Beside, {
                enableScripts: true,
                localResourceRoots: [resourceDir, vscode.Uri.joinPath(context.extensionUri, 'node_modules', '@vscode/codicons', 'dist')]
            });
            webviews.set(params.id, view.webview);
            const resources = params.resources;
            if (resources) {
                for (const resourceName in resources) {
                    const resourceText = resources[resourceName];
                    const resourceUri = vscode.Uri.joinPath(resourceDir, resourceName);
                    workspace.fs.writeFile(resourceUri, Buffer.from(resourceText, 'utf8'));
                    data = data.replace('href="' + resourceName + '"', 'href="' + view.webview.asWebviewUri(resourceUri) + '"');
                }
            }
            const codiconsUri = view.webview.asWebviewUri(vscode.Uri.joinPath(context.extensionUri, 'node_modules', '@vscode/codicons', 'dist', 'codicon.css'));
            view.webview.html = data.replace('href="codicon.css"', 'href="' + codiconsUri + '"');
            view.webview.onDidReceiveMessage(message => {
                switch (message.command) {
                    case 'dispose':
                        webviews.delete(params.id);
                        view.dispose();
                        break;
                    case 'command':
                        vscode.commands.executeCommand(COMMAND_PREFIX + '.htmlui.process.command', message.data);
                        break;
                }
            });
            view.onDidDispose(() => {
                resolve();
                workspace.fs.delete(resourceDir, {recursive: true});
            });
        });
    }

    async function execInHtmlPage(params : HtmlPageParams): Promise<boolean> {
        return new Promise(resolve => {
            const webview = webviews.get(params.id);
            if (webview) {
                webview.postMessage({
                    execScript: params.text,
                    pause: params.pause
                }).then(ret => {
                    resolve(ret);
                });
            }
            resolve(false);
        });
    }

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
                        handleLog(log, "Ignoring request for restart of Apache NetBeans Language Server");
                    };
                    maintenance = new Promise((resolve, reject) => {
                        const kill : Promise<void> = killNbProcess(false, log);
                        kill.then(() => {
                            let installProcess = launcher.launch(info, "-J-Dnetbeans.close=true", "--modules", "--install", ".*nbjavac.*");
                            handleLog(log, "Launching installation process: " + installProcess.pid);
                            let logData = function(d: any) {
                                handleLogNoNL(log, d.toString());
                            };
                            installProcess.stdout.on('data', logData);
                            installProcess.stderr.on('data', logData);
                            installProcess.addListener("error", reject);
                            // MUST wait on 'close', since stdout is inherited by children. The installProcess dies but
                            // the inherited stream will be closed by the last child dying.
                            installProcess.on('close', function(code: number) {
                                handleLog(log, "Installation completed: " + installProcess.pid);
                                handleLog(log, "Additional Java Support installed with exit code " + code);
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

function stopClient(clientPromise: Promise<LanguageClient>): Thenable<void> {
    if (testAdapter) {
        testAdapter.dispose();
        testAdapter = undefined;
    }
    return clientPromise && !(clientPromise instanceof InitialPromise) ? clientPromise.then(c => c.stop()) : Promise.resolve();
}

export function deactivate(): Thenable<void> {
    if (nbProcess != null) {
        nbProcess.kill();
    }
    return stopClient(client);
}

function collectDocumentSelectors(): TextDocumentFilter[] {
    const selectors = [];
    for (const extension of vscode.extensions.all) {
        const contributesSection = extension.packageJSON['contributes'];
        if (contributesSection) {
            const documentSelectors = contributesSection['netbeans.documentSelectors'];
            if (Array.isArray(documentSelectors) && documentSelectors.length) {
                selectors.push(...documentSelectors);
            }
        }
    }
    return selectors;
}

class NetBeansDebugAdapterTrackerFactory implements vscode.DebugAdapterTrackerFactory {

    createDebugAdapterTracker(_session: vscode.DebugSession): vscode.ProviderResult<vscode.DebugAdapterTracker> {
        return {
            onDidSendMessage(message: any): void {
                if (testAdapter && message.type === 'event' && message.event === 'output') {
                    testAdapter.testOutput(message.body.output);
                }
            }
        }
    }
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


class NetBeansConfigurationInitialProvider implements vscode.DebugConfigurationProvider {

    provideDebugConfigurations(folder: vscode.WorkspaceFolder | undefined, token?: vscode.CancellationToken): vscode.ProviderResult<vscode.DebugConfiguration[]> {
       return this.doProvideDebugConfigurations(folder, token);
    }

    async doProvideDebugConfigurations(folder: vscode.WorkspaceFolder | undefined, _token?:  vscode.CancellationToken):  Promise<vscode.DebugConfiguration[]> {
        let c : LanguageClient = await client;
        if (!folder) {
            return [];
        }
        var u : vscode.Uri | undefined;
        if (folder && folder.uri) {
            u = folder.uri;
        } else {
            u = vscode.window.activeTextEditor?.document?.uri
        }
        let result : vscode.DebugConfiguration[] = [];
        const configNames : string[] | null | undefined = await vscode.commands.executeCommand(COMMAND_PREFIX + '.project.configurations', u?.toString());
        if (configNames) {
            let first : boolean = true;
            for (let cn of configNames) {
                let cname : string;

                if (first) {
                    // ignore the default config, comes first.
                    first = false;
                    continue;
                } else {
                    cname = "Launch Java: " + cn;
                }
                const debugConfig : vscode.DebugConfiguration = {
                    name: cname,
                    type: "java+",
                    request: "launch",
                    launchConfiguration: cn,
                };
                result.push(debugConfig);
            }
        }
        return result;
    }
}

class NetBeansConfigurationDynamicProvider implements vscode.DebugConfigurationProvider {

    context: ExtensionContext;
    commandValues = new Map<string, string>();

    constructor(context: ExtensionContext) {
        this.context = context;
    }

    provideDebugConfigurations(folder: vscode.WorkspaceFolder | undefined, token?: vscode.CancellationToken): vscode.ProviderResult<vscode.DebugConfiguration[]> {
       return this.doProvideDebugConfigurations(folder, this.context, this.commandValues, token);
    }

    async doProvideDebugConfigurations(folder: vscode.WorkspaceFolder | undefined, context: ExtensionContext, commandValues: Map<string, string>, _token?:  vscode.CancellationToken):  Promise<vscode.DebugConfiguration[]> {
        let c : LanguageClient = await client;
        if (!folder) {
            return [];
        }
        let result : vscode.DebugConfiguration[] = [];
        const attachConnectors : DebugConnector[] | null | undefined = await vscode.commands.executeCommand(COMMAND_PREFIX + '.java.attachDebugger.configurations');
        if (attachConnectors) {
            for (let ac of attachConnectors) {
                const debugConfig : vscode.DebugConfiguration = {
                    name: ac.name,
                    type: ac.type,
                    request: "attach",
                };
                for (let i = 0; i < ac.arguments.length; i++) {
                    let defaultValue: string = ac.defaultValues[i];
                    if (!defaultValue.startsWith("${command:")) {
                        // Create a command that asks for the argument value:
                        let cmd: string = COMMAND_PREFIX + ".java.attachDebugger.connector." + ac.id + "." + ac.arguments[i];
                        debugConfig[ac.arguments[i]] = "${command:" + cmd + "}";
                        if (!commandValues.has(cmd)) {
                            commandValues.set(cmd, ac.defaultValues[i]);
                            let description: string = ac.descriptions[i];
                            context.subscriptions.push(commands.registerCommand(cmd, async (ctx) => {
                                return vscode.window.showInputBox({
                                    prompt: description,
                                    value: commandValues.get(cmd),
                                }).then((value) => {
                                    if (value) {
                                        commandValues.set(cmd, value);
                                    }
                                    return value;
                                });
                            }));
                        }
                    } else {
                        debugConfig[ac.arguments[i]] = defaultValue;
                    }
                }
                result.push(debugConfig);
            }
        }
        return result;
    }
}

class NetBeansConfigurationResolver implements vscode.DebugConfigurationProvider {

    resolveDebugConfiguration(_folder: vscode.WorkspaceFolder | undefined, config: vscode.DebugConfiguration, _token?: vscode.CancellationToken): vscode.ProviderResult<vscode.DebugConfiguration> {
        if (!config.type) {
            config.type = 'java+';
        }
        if (!config.request) {
            config.request = 'launch';
        }
        if (vscode.window.activeTextEditor) {
            config.file = '${file}';
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

class NetBeansConfigurationNativeResolver implements vscode.DebugConfigurationProvider {

    resolveDebugConfiguration(_folder: vscode.WorkspaceFolder | undefined, config: vscode.DebugConfiguration, _token?: vscode.CancellationToken): vscode.ProviderResult<vscode.DebugConfiguration> {
        if (!config.type) {
            config.type = 'nativeimage';
        }
        if (!config.request) {
            config.request = 'launch';
        }
        if ('launch' == config.request && !config.nativeImagePath) {
            config.nativeImagePath = '${workspaceFolder}/build/native-image/application';
        }
        if (!config.miDebugger) {
            config.miDebugger = 'gdb';
        }
        if (!config.console) {
            config.console = 'internalConsole';
        }

        return config;
    }
}
