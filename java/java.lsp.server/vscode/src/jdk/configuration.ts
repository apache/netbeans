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
import * as os from 'os';
import * as path from 'path';
import { MultiStepInput } from '../utils';
import * as settings from './settings';
import * as jdk from './jdk';


const ACTION_NAME = 'JDK Configuration';

export function initialize(context: vscode.ExtensionContext) {
    const COMMAND_JDK_CONFIGURATION = 'nbls.jdk.configuration';
    context.subscriptions.push(vscode.commands.registerCommand(COMMAND_JDK_CONFIGURATION, () => {
        configure();
    }));
}

type FeatureItem = { label: string; description: string; detail: string, setting: settings.Setting };
type JdkItem = { label: string; description: string; detail: string, jdk: jdk.Java };
type ScopeItem = { label: string; description: string; scope: vscode.ConfigurationTarget };

interface State {
    allSettings: FeatureItem[];
    selectedSettings: FeatureItem[];
    allJdks: JdkItem[];
    selectedJdk: JdkItem;
    allScopes: ScopeItem[];
    selectedScope: ScopeItem;
}

async function configure() {
    const state: Partial<State> = {};
    await MultiStepInput.run(input => selectFeatures(input, state));
    if (state.selectedSettings && state.selectedJdk && state.selectedScope) {
        const jdk = state.selectedJdk.jdk;
        for (const selectedSetting of state.selectedSettings) {
            await selectedSetting.setting.setJdk(jdk, state.selectedScope.scope);
        }
        const GRAALVM_EXTENSION_ID = 'oracle-labs-graalvm.graalvm';
        const GRAALVM_SETTINGS_NAME = 'GraalVM Tools for Java';
        if (vscode.extensions.getExtension(GRAALVM_EXTENSION_ID)) {
            vscode.window.showWarningMessage(`The ${GRAALVM_SETTINGS_NAME} extension may conflict with the ${ACTION_NAME} action. Please consider uninstalling it.`, 'Got It');
        }
    }
}

function totalSteps(_state: Partial<State>): number {
    return 3;
}

async function selectFeatures(input: MultiStepInput, state: Partial<State>) {
    if (!state.allSettings) {
        const settings = allSettings();
        state.allSettings = settings[0];
        state.selectedSettings = settings[1];
    }
    const selected: any = await input.showQuickPick({
        title: `${ACTION_NAME}: Settings`,
        step: 1,
        totalSteps: totalSteps(state),
        placeholder: 'Select features and settings for which JDK will be configured',
        items: state.allSettings,
        selectedItems: state.selectedSettings,
        canSelectMany: true,
        validate: () => Promise.resolve(undefined),
        shouldResume: () => Promise.resolve(false)
    });
    if (selected?.length) {
        state.selectedSettings = selected;
        return (input: MultiStepInput) => selectJDK(input, state);
    }
    return;
}

function allSettings(): FeatureItem[][] {
    const allSettings = [];
    const selectedSettings = [];
    const availableSettings = settings.getAvailable();
    for (const setting of availableSettings) {
        const current = setting.getCurrent();
        const item = { label: setting.name, description: setting.getSetting(), detail: `$(coffee) Current: ${current || 'not defined'}`, setting: setting };
        allSettings.push(item);
        if (current || setting.configureIfNotDefined()) {
            selectedSettings.push(item);
        }
    }
    return [ allSettings, selectedSettings ];
}

async function selectJDK(input: MultiStepInput, state: Partial<State>): Promise<any | undefined> {
    if (!state.allJdks) {
        state.allJdks = await allJdks(state);
    }
    const selectCustom = { label: '$(folder-opened) Select Custom JDK...' };
    const selected: any = await input.showQuickPick({
        title: `${ACTION_NAME}: JDK`,
        step: 2,
        totalSteps: totalSteps(state),
        placeholder: 'Select JDK',
        items: [ selectCustom, ...state.allJdks ],
        validate: () => Promise.resolve(undefined),
        shouldResume: () => Promise.resolve(false)
    });
    if (selected?.length) {
        if (selected[0] === selectCustom) {
            const javaHome = state.allJdks?.[1]?.jdk?.javaHome;
            const javaRoot = javaHome ? path.dirname(javaHome) : undefined;
            const customJdk = await selectCustomJdk(javaRoot);
            if (customJdk) {
                await jdk.registerCustom(customJdk);
                state.allJdks = await allJdks(state);
                for (const jdkItem of state.allJdks) {
                    if (jdkItem.jdk?.javaHome === customJdk.javaHome) {
                        state.selectedJdk = jdkItem;
                        break;
                    }
                }
            } else {
                return (input: MultiStepInput) => selectJDK(input, state);
            }
        } else if (selected[0].jdk) {
            state.selectedJdk = selected[0];
        }
    }
    return (input: MultiStepInput) => selectScope(input, state);
}

async function allJdks(state: Partial<State>): Promise<JdkItem[]> {
    const knownJavas: jdk.Java[] = [];
    for (const setting of state.allSettings || []) {
        knownJavas.push(...setting.setting.getJavas());
    }
    const jdks = await jdk.findAll(knownJavas);
    jdks.sort((jdk1, jdk2) => jdk1 > jdk2 ? -1 : 1);

    const jdkItems = [];
    const jdkItemsNi: JdkItem | { jdk?: jdk.Java, kind?: vscode.QuickPickItemKind }[] = [];
    const jdkItemsWoNi: JdkItem | { jdk?: jdk.Java, kind?: vscode.QuickPickItemKind }[] = [];
    for (const jdk of jdks) {
        const jdkItem = { label: `$(coffee) ${jdk.name()}`, description: `${jdk.javaHome}`, jdk: jdk, kind: vscode.QuickPickItemKind.Default };
        if (jdk.hasNativeImage()) {
            jdkItemsNi.push(jdkItem);
        } else {
            jdkItemsWoNi.push(jdkItem);
        }
    }

    if (jdkItemsNi.length) {
        jdkItems.push({ label: 'With Native Image', kind: vscode.QuickPickItemKind.Separator });
        jdkItems.push(...jdkItemsNi);
    }

    if (jdkItemsWoNi.length) {
        jdkItems.push({ label: 'Without Native Image', kind: vscode.QuickPickItemKind.Separator });
        jdkItems.push(...jdkItemsWoNi);
    }
    return jdkItems as JdkItem[];
}

async function selectCustomJdk(javaRoot?: string): Promise<jdk.Java | null | undefined> {
    const selected = await vscode.window.showOpenDialog({
        title: 'Select Custom JDK',
        canSelectFiles: false,
        canSelectFolders: true,
        canSelectMany: false,
        defaultUri: vscode.Uri.file(javaRoot || os.homedir()),
        openLabel: 'Select'
    });
    if (selected?.length === 1) {
        const selectedJavaHome = selected[0].fsPath;
        const selectedJdk = new jdk.Java(selectedJavaHome);
        if (selectedJdk.isJdk()) {
            return selectedJdk;
        } else {
            vscode.window.showWarningMessage(`Not a valid JDK installation: ${selectedJavaHome}`);
            return null;
        }
    }
    return undefined;
}

async function selectScope(input: MultiStepInput, state: Partial<State>) {
    if (!state.allScopes) {
        state.allScopes = allScopes();
    }
    const selected: any = await input.showQuickPick({
        title: `${ACTION_NAME}: Scope`,
        step: 3,
        totalSteps: totalSteps(state),
        placeholder: 'Select configuration scope',
        items: state.allScopes,
        validate: () => Promise.resolve(undefined),
        shouldResume: () => Promise.resolve(false)
    });
    if (selected?.length && selected[0].scope) {
        state.selectedScope = selected[0];
    }
}

function allScopes(): ScopeItem[] {
    const allScopes = [];
    allScopes.push({ label: 'User', description: 'JDK will be configured for all workspaces, may be overriden by Workspace configuration', scope: vscode.ConfigurationTarget.Global });
    if (vscode.workspace.workspaceFolders) {
        allScopes.push({ label: 'Workspace', description: 'JDK will be configured for the current workspace, overrides User configuration', scope: vscode.ConfigurationTarget.Workspace });
    }
    return allScopes;
}
