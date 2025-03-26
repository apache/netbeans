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
import { dummyKeyPathLocation, sshConfigLocation, toggleGuideFor } from "./guidesUtil";

export type SshGuidePanelProps = {
    publicIp: string;
    ocid: string;
}

export const viewType: string = "sshGuide";
const templatePath: string = "ssh-guide.handlebars";

export class SshGuidePanel extends GuidePanel {

    public static currentPanel: SshGuidePanel | undefined;

    public static createOrShow(context: vscode.ExtensionContext, props: SshGuidePanelProps) {
        if (SshGuidePanel.currentPanel) {
            if (!this.propertiesUpdated(props)) {
                SshGuidePanel.currentPanel.panel.reveal();
                return;
            } 
            SshGuidePanel.currentPanel.panel.dispose();
        }

        SshGuidePanel.currentPanel = new SshGuidePanel(context, props);
    }

    private static propertiesUpdated(props: SshGuidePanelProps): boolean {
        const { ocid, publicIp } = props;
        return SshGuidePanel.currentPanel?.properties.ocid !== ocid || SshGuidePanel.currentPanel.properties.publicIp !== publicIp;
    }

    constructor(context: vscode.ExtensionContext, props: SshGuidePanelProps) {
        const { ocid, publicIp } = props;
        super(context, viewType, templatePath, {
            ocid,
            publicIp,
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
        SshGuidePanel.currentPanel = undefined;
        super.dispose();
    }
}
