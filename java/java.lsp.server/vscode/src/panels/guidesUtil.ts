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
import * as os from 'os';

export const GUIDE_NOTIFICATION_CONFIGURATION_KEY = 'guides.notified';

type GuideNotification = {
    ocid: string;
    viewType: string;
}

export const sshConfigLocation = path.join(os.homedir(), '.ssh', 'config');
export const dummyKeyPathLocation = path.join(os.homedir(), '<path-to-your-key>', 'ssh-key-yyyy-mm-dd.key');

export function shouldHideGuideFor(viewType: string, ocid?: string): boolean {
    const notifiedGuides: Array<GuideNotification> | undefined = vscode.workspace.getConfiguration('netbeans').get<Array<GuideNotification>>(GUIDE_NOTIFICATION_CONFIGURATION_KEY);
    return notifiedGuides && notifiedGuides.find((notified) => notified.ocid === ocid && notified.viewType === viewType) ? true : false;
}

export async function toggleGuideFor(viewType: string, ocid: string) {
    const notifiedGuides: Array<GuideNotification> = vscode.workspace.getConfiguration('netbeans').get<Array<GuideNotification>>(GUIDE_NOTIFICATION_CONFIGURATION_KEY) || [];
    const notifiedForOcid = notifiedGuides.find((notified) => notified.ocid === ocid && notified.viewType === viewType)

    if (notifiedForOcid) {
        try {
            const updatedNotifiedGuides = notifiedGuides.filter((notified) => notified.ocid !== ocid)
            await vscode.workspace.getConfiguration('netbeans')
                .update(GUIDE_NOTIFICATION_CONFIGURATION_KEY,
                    updatedNotifiedGuides.length > 0 ? updatedNotifiedGuides : undefined,
                    true)
        } catch (err) {
            vscode.window.showErrorMessage(`Failed to update property: netbeans.${GUIDE_NOTIFICATION_CONFIGURATION_KEY}, ${err}`);
        }
    } else {
        notifiedGuides.push({ ocid, viewType });
        try {
            await vscode.workspace.getConfiguration('netbeans').update(GUIDE_NOTIFICATION_CONFIGURATION_KEY, [...notifiedGuides], true)
        } catch (err) {
            vscode.window.showErrorMessage(`Failed to update property: netbeans.${GUIDE_NOTIFICATION_CONFIGURATION_KEY}, ${err}`);
        }
    }
}
