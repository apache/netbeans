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
import * as path from "path";
import * as fs from "fs";
import * as Handlebars from "handlebars";
import { promisify } from "util";

const readFile = promisify(fs.readFile);

export abstract class GuidePanel {

    readonly webviewsFolder: string = "webviews";
    readonly stylesFolder: string = "styles";
    readonly scriptsFolder: string = "scripts";

    protected properties: any;
    readonly templatePath: string;
    readonly panel: vscode.WebviewPanel;
    disposables: vscode.Disposable[] = [];

    abstract messageHandler(message: any): void;

    constructor(context: vscode.ExtensionContext, viewType: string, templatePath: string, properties: any) {
        this.properties = properties;
        this.templatePath = templatePath;
        this.panel = vscode.window.createWebviewPanel(
            viewType,
            "Oracle Cloud Assets",
            { viewColumn: vscode.ViewColumn.One, preserveFocus: true },
            {
                enableCommandUris: true,
                enableScripts: true,
                localResourceRoots: [vscode.Uri.file(path.join(context.extensionPath, this.webviewsFolder))],
            }
        );

        this.disposables.push(
            this.panel.onDidDispose(
                () => this.dispose()));

        this.panel.onDidChangeViewState(
            () => {
                if (this.panel.visible) {
                    this.setHtml(context);
                }
            },
            null,
            this.disposables
        );

        this.panel.webview.onDidReceiveMessage(
            (e) => this.messageHandler(e),
            undefined,
            this.disposables
        );

        this.setHtml(context);
    }

    protected getSources(context: vscode.ExtensionContext): any {
        return {
            cssUri: this.panel.webview.asWebviewUri(vscode.Uri.file(path.join(context.extensionPath, this.webviewsFolder, this.stylesFolder, "guide.css"))),
            javascriptUri: this.panel.webview.asWebviewUri(
                vscode.Uri.file(path.join(context.extensionPath, this.webviewsFolder, this.scriptsFolder, "guide.js"))
            ),
        }
    }

    private async setHtml(context: vscode.ExtensionContext) {
        let templateFilePath = path.join(context.extensionPath, this.webviewsFolder, this.templatePath);
        const template = await this.getTemplateFromPath(templateFilePath);
        this.panel.webview.html = template({
            cspSource: this.panel.webview.cspSource,
            ...this.getSources(context),
            ...this.properties
        });
    }

    private async getTemplateFromPath(path: string): Promise<HandlebarsTemplateDelegate<any>> {
        const templateFile = await readFile(path, "utf-8");
        return Handlebars.compile(templateFile);
    }

    public dispose() {
        this.panel.dispose();
        this.disposables.forEach(x => x?.dispose());
    }
}
