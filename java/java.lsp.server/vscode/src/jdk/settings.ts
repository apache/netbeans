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
import * as process from 'process';
import * as jdk from './jdk';


export abstract class Setting {

    readonly name: string;
    readonly property: string;
    
    constructor(name: string, property: string) {
        this.name = name;
        this.property = property;
    }

    // Name of the property defining this setting
    abstract getSetting(): string;

    // String to describe the current Java configuration for this setting or undefined if not defined
    abstract getCurrent(): string | undefined;

    // All Java installations currently mentioned within this setting (used as pointers for autodiscovery)
    abstract getJavas(): jdk.Java[];

    // True if the setting should be initially selected even if Java configuration is currently not defined
    abstract configureIfNotDefined(): boolean;
    
    // Configure this setting for the specific JDK
    abstract setJdk(jdk: jdk.Java, scope: vscode.ConfigurationTarget): Promise<boolean>;

}

class JavaSetting extends Setting {

    readonly supportsWorkspaceScope: boolean;
    private java: jdk.Java | null | undefined;

    constructor(name: string, property: string, supportsWorkspaceScope: boolean = true) {
        super(name, property);
        this.supportsWorkspaceScope = supportsWorkspaceScope;
    }

    protected getJavaHome(): string | undefined {
        return vscode.workspace.getConfiguration().get<string>(this.property);
    }
    
    protected getJava(): jdk.Java | undefined {
        if (this.java === undefined) {
            const javaHome = this.getJavaHome();
            this.java = javaHome ? new jdk.Java(javaHome) : null;
        }
        return this.java ? this.java : undefined;
    }

    getSetting(): string {
        return this.property;
    }

    getCurrent(): string | undefined {
        return this.getJava()?.javaHome;
    }

    getJavas(): jdk.Java[] {
        const javas = [];
        const java = this.getJava();
        if (java) {
            javas.push(java);
        }
        return javas;
    }

    configureIfNotDefined(): boolean {
        return true;
    }

    protected getJdkSetting(jdk: jdk.Java): any {
        return jdk.javaHome;
    }

    async setJdk(jdk: jdk.Java, scope: vscode.ConfigurationTarget): Promise<boolean> {
        if (!this.supportsWorkspaceScope && scope !== vscode.ConfigurationTarget.Global) {
            const proceedOption = 'Configure It in User Scope';
            const skipOption = 'Skip';
            const selected = await vscode.window.showWarningMessage(`JDK for ${this.name} can only be configured in User scope.`, proceedOption, skipOption);
            if (selected === proceedOption) {
                scope = vscode.ConfigurationTarget.Global;
            } else {
                return false;
            }
        }
        try {
            await vscode.workspace.getConfiguration().update(this.property, this.getJdkSetting(jdk), scope);
            return true;
        } catch (err) {
            vscode.window.showErrorMessage(`Failed to update property ${this.getSetting()}: ${err}`);
            return false;
        }
    }

}

class JavaEnvSetting extends JavaSetting {

    readonly variable: string;
    
    constructor(name: string, property: string, variable: string) {
        super(name, property);
        this.variable = variable;
    }

    protected getEnv(): any | undefined {
        return vscode.workspace.getConfiguration().get<any>(this.property);
    }

    protected getJavaHome(): string | undefined {
        return this.getEnv()?.[this.variable];
    }

    getSetting(): string {
        return `${this.property}.${this.variable}`;
    }

    protected getJdkSetting(jdk: jdk.Java): any {
        const env = this.getEnv() || {};
        env[this.variable] = jdk.javaHome;
        return env;
    }

}

class JavaEnvPathSetting extends JavaEnvSetting {

    private static readonly ENV_PATH_MASK = '${env:PATH}';

    constructor(name: string, property: string, variable: string) {
        super(name, property, variable);
    }

    protected getJavaHome(): string | undefined {
        const currentPath = this.getEnv()?.[this.variable];
        if (currentPath) {
            const expandedPath = `${currentPath}`.replace(/\$\{env\:PATH\}/g, this.expandEnvPath());
            const paths = expandedPath.split(path.delimiter);
            for (const binPath of paths) {
                if (path.basename(binPath).toLowerCase() === 'bin') {
                    const jdkPath = path.dirname(binPath);
                    const java = new jdk.Java(jdkPath);
                    if (java.isJava()) {
                        return java.javaHome;
                    }
                }
            }
        }
        return undefined;
    }

    protected getJdkSetting(newJdk: jdk.Java): any {
        const env = this.getEnv() || {};
        const binPath = path.join(newJdk.javaHome, 'bin');
        const currentPath = env[this.variable];
        if (!currentPath) {
            env[this.variable] = `${binPath}${path.delimiter}${JavaEnvPathSetting.ENV_PATH_MASK}`;
        } else {
            const newPaths: string[] = [ binPath ];
            const currentPaths = currentPath.split(path.delimiter);
            for (const currentPath of currentPaths) {
                const pathName = path.basename(currentPath).toLowerCase();
                const parentPath = pathName === 'bin' ? path.dirname(currentPath) : undefined;
                const java = parentPath ? new jdk.Java(parentPath) : undefined;
                if (!java || !java.isJava()) {
                    newPaths.push(currentPath);
                }
            }
            env[this.variable] = newPaths.join(path.delimiter);
        }
        return env;
    }

    private expandEnvPath(): string {
        const expandedPath = process.env[this.variable] || JavaEnvPathSetting.ENV_PATH_MASK;
        return expandedPath.endsWith(path.delimiter) ? expandedPath.slice(0, -path.delimiter.length) : expandedPath;
    }

}

class JavaEnvArrSetting extends JavaEnvSetting {

    constructor(name: string, property: string, variable: string) {
        super(name, property, variable);
    }

    protected getJavaHome(): string | undefined {
        const env = this.getEnv();
        if (Array.isArray(env)) {
            for (const item of env) {
                if (item.environmentVariable === this.variable) {
                    return item.value;
                }
            }
        }
        return undefined;
    }

    protected getJdkSetting(jdk: jdk.Java): any {
        const env = this.getEnv() || [];
        let updated = false;
        for (const item of env) {
            if (item.environmentVariable === this.variable) {
                item.value = jdk.javaHome;
                updated = true;
                break;
            }
        }
        if (!updated) {
            env.push({
                environmentVariable: this.variable,
                value: jdk.javaHome
            });
        }
        return env;
    }

}

class ProjectJavaSettings extends Setting {

    constructor(name: string, property: string) {
        super(name, property);
    }

    getSetting(): string {
        return this.property;
    }

    private getDefinitions(): { name: string, path: string }[] | undefined {
        return vscode.workspace.getConfiguration().get<any>(this.property);
    }

    getCurrent(): string | undefined {
        const current = [];
        const definitions = this.getDefinitions();
        if (Array.isArray(definitions)) {
            for (const definition of definitions) {
                if (definition.name?.length && definition.path?.length) {
                    current.push(`${definition.name}: ${definition.path}`);
                }
            }
        }
        return current.length ? current.join(', ') : undefined;
    }

    getJavas(): jdk.Java[] {
        const javas = [];
        const definitions = this.getDefinitions();
        if (Array.isArray(definitions)) {
            for (const definition of definitions) {
                if (definition.path?.length) {
                    javas.push(new jdk.Java(definition.path));
                }
            }
        }
        return javas;
    }

    configureIfNotDefined(): boolean {
        return false;
    }

    async setJdk(jdk: jdk.Java, scope: vscode.ConfigurationTarget): Promise<boolean> {
        const details = await jdk.getVersion();
        if (details?.major) {
            const major = `${details.major <= 8 ? '1.' : ''}${details.major}`;
            const version = `JavaSE-${major}`;
            const definitions = this.getDefinitions() || []; // NOTE: merges User & Workspace definitions
            if (Array.isArray(definitions)) {
                let updated = false;
                for (const definition of definitions) {
                    if (definition.name === version) {
                        definition.path = jdk.javaHome;
                        updated = true;
                        break;
                    }
                }
                if (!updated) {
                    definitions.push({
                        name: version,
                        path: jdk.javaHome
                    });
                    }
            }
            try {
                await vscode.workspace.getConfiguration().update(this.property, definitions, scope);
                return true;
            } catch (err) {
                vscode.window.showErrorMessage(`Failed to update property ${this.getSetting()}: ${err}`);
            }
        }
        return false;
    }

}

const NBLS_EXTENSION_ID = 'asf.apache-netbeans-java';
const NBLS_SETTINGS_NAME = 'Language Server by Apache NetBeans';
const NBLS_SETTINGS_PROPERTY = 'netbeans.jdkhome';
function nblsSetting(): Setting {
    return new JavaSetting(NBLS_SETTINGS_NAME, NBLS_SETTINGS_PROPERTY, true);
}

const NBLS_SETTINGS_PROJECT_NAME = 'Language Server by Apache NetBeans - Java Runtime for Projects';
const NBLS_SETTINGS_PROJECT_PROPERTY = 'netbeans.project.jdkhome';
function nblsProjectSetting(): Setting {
    return new JavaSetting(NBLS_SETTINGS_PROJECT_NAME, NBLS_SETTINGS_PROJECT_PROPERTY, true);
}


const JDTLS_EXTENSION_ID = 'redhat.java';
const JDTLS_SETTINGS_NAME = 'Language Server by RedHat';
const JDTLS_SETTINGS_PROPERTY = 'java.jdt.ls.java.home';
function jdtlsSetting(): Setting {
    return new JavaSetting(JDTLS_SETTINGS_NAME, JDTLS_SETTINGS_PROPERTY);
}

const PROJECTS_SETTINGS_NAME = 'Language Server by RedHat - Java Runtime for Projects';
const PROJECTS_SETTINGS_PROPERTY = 'java.configuration.runtimes';
export function projectsSettings(): Setting {
    return new ProjectJavaSettings(PROJECTS_SETTINGS_NAME, PROJECTS_SETTINGS_PROPERTY);
}

const TERMINAL_JAVAHOME_SETTINGS_NAME = 'Integrated Terminal JAVA_HOME';
const TERMINAL_JAVAHOME_SETTINGS_PROPERTY = `terminal.integrated.env.${platformProperty()}`;
function terminalJavaHomeSetting(): Setting {
    return new JavaEnvSetting(TERMINAL_JAVAHOME_SETTINGS_NAME, TERMINAL_JAVAHOME_SETTINGS_PROPERTY, 'JAVA_HOME');
}

const TERMINAL_PATH_SETTINGS_NAME = 'Integrated Terminal PATH';
const TERMINAL_PATH_SETTINGS_PROPERTY = `terminal.integrated.env.${platformProperty()}`;
function terminalPathSetting(): Setting {
    return new JavaEnvPathSetting(TERMINAL_PATH_SETTINGS_NAME, TERMINAL_PATH_SETTINGS_PROPERTY, 'PATH');
}

const MAVEN_EXTENSION_ID = 'vscjava.vscode-maven';
const MAVEN_JAVAHOME_SETTINGS_NAME = 'Maven Runtime JAVA_HOME';
const MAVEN_JAVAHOME_SETTINGS_PROPERTY = 'maven.terminal.customEnv';
function mavenJavaHomeSetting(): Setting {
    return new JavaEnvArrSetting(MAVEN_JAVAHOME_SETTINGS_NAME, MAVEN_JAVAHOME_SETTINGS_PROPERTY, 'JAVA_HOME');
}

export function getAvailable(): Setting[] {
    const settings = [];

    if (vscode.extensions.getExtension(NBLS_EXTENSION_ID)) {
        settings.push(nblsSetting());
        settings.push(nblsProjectSetting());
    }

    if (vscode.extensions.getExtension(JDTLS_EXTENSION_ID)) {
        settings.push(jdtlsSetting());
        settings.push(projectsSettings());
    }

    settings.push(terminalJavaHomeSetting());
    settings.push(terminalPathSetting());

    if (vscode.extensions.getExtension(MAVEN_EXTENSION_ID)) {
        settings.push(mavenJavaHomeSetting());
    }

    return settings;
}


function platformProperty(): string {
    const platform = process.platform;
    if (platform === 'win32') {
        return 'windows';
    } else if (platform === 'darwin') {
        return 'osx';
    }
    return platform;
}
