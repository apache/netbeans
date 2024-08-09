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
import { GuidePanel } from "./GuidePanel";
import { dummyKeyPathLocation, shouldHideGuideFor, sshConfigLocation, toggleGuideFor } from "./guidesUtil";

export type RunImageGuidePanelProps = {
    publicIp: string;
    ocid: string;
    isRepositoryPrivate: boolean;
    registryUrl: string;
}

export const viewType: string = "runImageGuide";
const templatePath: string = "run-image-guide.handlebars";

export class RunImageGuidePanel extends GuidePanel {
    public static currentPanel: RunImageGuidePanel | undefined;

    public static createOrShow(context: vscode.ExtensionContext, props: RunImageGuidePanelProps) {
        if (RunImageGuidePanel.currentPanel) {
            if (!this.propertiesUpdated(props)) {
                RunImageGuidePanel.currentPanel.panel.reveal();
                return;
            } 
            RunImageGuidePanel.currentPanel.panel.dispose();
        }

        RunImageGuidePanel.currentPanel = new RunImageGuidePanel(context, props);
    }

    private static propertiesUpdated(props: RunImageGuidePanelProps): boolean {
        const { ocid, publicIp } = props;
        return RunImageGuidePanel.currentPanel?.properties.ocid !== ocid || RunImageGuidePanel.currentPanel.properties.publicIp !== publicIp;
    }

    constructor(context: vscode.ExtensionContext, props: RunImageGuidePanelProps) {
        const { ocid, publicIp, isRepositoryPrivate, registryUrl } = props;
        super(context, viewType, templatePath, {
            ocid,
            publicIp,
            isRepositoryPrivate,
            registryUrl,
            dummyKeyPathLocation,
            sshConfigLocation,
            showGuide: "",
        });
    }

    async messageHandler(message: any): Promise<void> {
        if (message.command === "showGuide") {
            await toggleGuideFor(viewType, this.properties.ocid);
            this.properties.showGuide =  this.properties.showGuide === "checked" ? "" : "checked"
        } else if (message.command === "openSSHConfig") {
            vscode.workspace.openTextDocument(sshConfigLocation).then(document => vscode.window.showTextDocument(document));
        }
    }

    public dispose() {
        RunImageGuidePanel.currentPanel = undefined;
        super.dispose();
    }

}
