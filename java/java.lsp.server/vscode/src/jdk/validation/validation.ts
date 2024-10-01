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
import * as jdkUtils from 'jdk-utils';

import { isRHExtensionActive, waitForNblsCommandToBeAvailable, currentClientJDK } from './extensionUtils';
import { getJavaVersion } from './javaUtil';
import { getProjectFrom } from './project';

const CONFIGURE_JDK_COMMAND = 'nbls.jdk.configuration'
const CONFIGURE_JDK = 'Configure JDK';

export async function validateJDKCompatibility(projectPath: string | null) {
    // In this case RH will try it's best to validate Java versions
    if (isRHExtensionActive()) return;

    const projectJavaVersion = await getProjectJavaVersion();
    // at this point, the NBLS client should be running, so clientRuntimeJDK should be set.
    const ideJavaVersion = await parseJavaVersion(await currentClientJDK());
    const ideProjectJavaVersion = await parseJavaVersion(projectPath);

    let conflictingVersion : number = 0;

    if (projectJavaVersion) {
        // project settings is preferred, if defined.
        if (ideProjectJavaVersion) {
            if (ideProjectJavaVersion < projectJavaVersion) {
                conflictingVersion = ideProjectJavaVersion;
            } 
        } else if (ideJavaVersion && ideJavaVersion < projectJavaVersion) {
            conflictingVersion = ideJavaVersion;
        }
    }
    if (conflictingVersion) {
        const value = await vscode.window.showWarningMessage(`Source level (JDK ${projectJavaVersion}) not compatible with current JDK installation (JDK ${conflictingVersion})`, CONFIGURE_JDK);
        if (value === CONFIGURE_JDK) {
            vscode.commands.executeCommand(CONFIGURE_JDK_COMMAND);
        }
    }
}

export async function parseJavaVersion(javaPath: string | null): Promise<number | undefined> {
    if (!javaPath) return undefined;

    const javaRuntime = await jdkUtils.getRuntime(javaPath, { checkJavac: true });
    if (!javaRuntime?.hasJavac) {
        return undefined;
    }
    const version = await getJavaVersion(javaRuntime.homedir);
    return version ? Number(version) : undefined;
}

async function getProjectJavaVersion(): Promise<number | undefined> {
    const folder = vscode.workspace.workspaceFolders?.[0];
    if (!folder) return undefined;

    await waitForNblsCommandToBeAvailable();

    const project = await getProjectFrom(folder.uri);
    const javaVersion = await project?.getJavaVersion();

    return javaVersion;
}
