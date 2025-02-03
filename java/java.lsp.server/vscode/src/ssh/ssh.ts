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

import * as vscode from "vscode";
import * as fs from 'fs';
import { runCommandInTerminal } from "./terminalRunner";
import { COMMAND_PREFIX } from "../extension";
import path = require("path");
import { promisify } from "util";
import * as Handlebars from "handlebars";

interface ConfigFiles {
    applicationProperties : string | null;
    bootstrapProperties: string | null;
}

const readFile = promisify(fs.readFile);

export class SSHSession {

    private readonly username: string;
    private readonly host: string;

    constructor(username: string, host: string) {
        this.username = username;
        this.host = host;
    }

    open(sessionName?: string) {
        let terminalName = sessionName || `${this.username}@${this.host}`;
        const sshCommand = `ssh ${this.username}@${this.host}`;
    
        runCommandInTerminal(sshCommand, `SSH: ${terminalName}`);
    }

    async runDocker(context: vscode.ExtensionContext, dockerImage: string, isRepositoryPrivate: boolean) {
        const configFiles: ConfigFiles = await vscode.commands.executeCommand(COMMAND_PREFIX + '.config.file.path') as ConfigFiles;
        const { applicationProperties, bootstrapProperties } = configFiles;
        let bearerTokenFile: string | undefined;

        const applicationPropertiesRemotePath = `/home/${this.username}/application.properties`;
        const bootstrapPropertiesRemotePath = `/home/${this.username}/bootstrap.properties`;
        const bearerTokenRemotePath = `/home/${this.username}/token.txt`;
        const applicationPropertiesContainerPath = "/home/app/application.properties";
        const bootstrapPropertiesContainerPath = "/home/app/bootstrap.properties";
        const ocirServer = dockerImage.split('/')[0];
        const remotePathToCopyTo = `/home/${this.username}/`;

        let sshCommand = "";
        let mountVolume = "";
        let micronautConfigFilesEnv = "";
        let filesToCopy = "";
        let renameFilesCommand = "";
        const removeOldFilesCommand = `rm -f ${bootstrapPropertiesRemotePath} ${applicationPropertiesRemotePath} ${bearerTokenRemotePath}`;

        if (isRepositoryPrivate) {
            bearerTokenFile = await vscode.commands.executeCommand(COMMAND_PREFIX + '.cloud.assets.createBearerToken', ocirServer);
            if (bearerTokenFile) {
                filesToCopy = bearerTokenFile;
                renameFilesCommand = `mv ${remotePathToCopyTo}${path.basename(bearerTokenFile)} ${bearerTokenRemotePath} && `;
            }
        }

        if (bootstrapProperties) {
            filesToCopy += ` ${bootstrapProperties}`;
            renameFilesCommand += ` mv ${remotePathToCopyTo}${path.basename(bootstrapProperties)} ${bootstrapPropertiesRemotePath} && `;
            mountVolume = `-v ${bootstrapPropertiesRemotePath}:${bootstrapPropertiesContainerPath}:Z `;
            micronautConfigFilesEnv = `${bootstrapPropertiesContainerPath}`;
        }

        if (applicationProperties) {
            filesToCopy += ` ${applicationProperties}`;
            renameFilesCommand += ` mv ${remotePathToCopyTo}${path.basename(applicationProperties)} ${applicationPropertiesRemotePath} && `;
            mountVolume += ` -v ${applicationPropertiesRemotePath}:${applicationPropertiesContainerPath}:Z`;
            micronautConfigFilesEnv += `${bootstrapProperties ? "," : ""}${applicationPropertiesContainerPath}`;
        }

        let templateFilePath = path.join(context.extensionPath, "templates", "run-container.sh.handlebars");
        const template = await this.getTemplateFromPath(templateFilePath);
        const script = template({
            username: this.username,
            isRepositoryPrivate,
            bearerTokenRemotePath,
            ocirServer,
            dockerImage,
            mountVolume,
            micronautConfigFilesEnv
        });

        const tempDir = process.env.TEMP || process.env.TMP || '/tmp';
        const scriptName = `run-container-${Date.now()}.sh`;
        const runContainerScript = path.join(tempDir, scriptName);
        fs.writeFileSync(runContainerScript, script);
        renameFilesCommand += ` mv -f ${scriptName} run-container.sh `;

        sshCommand = `scp ${filesToCopy} ${runContainerScript} ${this.username}@${this.host}:${remotePathToCopyTo} && `    
        sshCommand += `ssh ${this.username}@${this.host} "${removeOldFilesCommand} && ${renameFilesCommand} && rm -f ${scriptName} && chmod +x run-container.sh && ./run-container.sh" `

        runCommandInTerminal(sshCommand, `Container: ${this.username}@${this.host}`)
    }


    private async getTemplateFromPath(path: string): Promise<HandlebarsTemplateDelegate<any>> {
        const templateFile = await readFile(path, "utf-8");
        return Handlebars.compile(templateFile);
    }
}