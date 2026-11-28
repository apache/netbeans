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

importScripts('../i18n/i18n.js', 'preset.js', 'common.js', 'chrome.js');

chrome.runtime.onMessage.addListener((message, sender, respond) => {
    if (sender.id !== chrome.runtime.id || !message?.action) {
        return;
    }
    handleAction(message).then(respond, error => {
        console.warn('NetBeans action failed:', error);
        respond(false);
    });
    return true;
});

async function handleAction({action, tabId, value, ident, resource, content}) {
    const managed = NetBeans.socketReady && NetBeans.tabStatus(tabId) === NetBeans.STATUS_MANAGED;
    switch (action) {
        case 'popup':
            if (tabId === NetBeans.debuggedTab) {
                await new Promise(resolve => NetBeans.detectViewPort(resolve));
            }
            return {
                presets: NetBeans_Presets.getPresets(),
                debuggedTab: NetBeans.debuggedTab,
                selectionMode: NetBeans.getSelectionMode(),
                ideVersion: NetBeans.ideVersion,
                viewport: NetBeans_ViewPort
            };
        case 'resize':
            if (managed) {
                NetBeans.resizePage(value);
            }
            return managed;
        case 'inspect':
            if (managed) {
                const tab = await chrome.tabs.get(tabId);
                return NetBeans.sendMessage({message: 'inspect', tabId, url: tab.url});
            }
            return false;
        case 'selection':
            if (managed && tabId === NetBeans.debuggedTab && typeof value === 'boolean') {
                NetBeans.setSelectionMode(value);
                return true;
            }
            return false;
        case 'presets':
            return NetBeans_Presets.getPresets(true);
        case 'savePresets':
            if (NetBeans.socketReady && Array.isArray(value)) {
                NetBeans_Presets.setPresets(value);
                return true;
            }
            return false;
        case 'resetWarnings':
            NetBeans_Warnings.reset();
            return true;
        case 'setWarning':
            NetBeans_Warnings.enable(ident, value);
            return true;
        case 'getWriteBack':
            return new Promise(resolve => NetBeans_DevTools.areChangesPropagated(resolve));
        case 'setWriteBack':
            NetBeans_DevTools.setChangesPropagated(value === true);
            return true;
        case 'resourceCommitted':
            if (managed && typeof resource?.url === 'string' && typeof content === 'string') {
                const enabled = await new Promise(resolve => NetBeans_DevTools.areChangesPropagated(resolve));
                if (enabled && NetBeans.socketReady
                        && NetBeans.tabStatus(tabId) === NetBeans.STATUS_MANAGED) {
                    NetBeans.sendResourceChangedInCDT(resource, content);
                    return true;
                }
            }
            return false;
        default:
            return false;
    }
}

chrome.alarms.onAlarm.addListener(alarm => {
    if (alarm.name === 'netbeans-reconnect') {
        NetBeans.connectIfNeeded();
    }
});
chrome.runtime.onInstalled.addListener(() => {
    chrome.alarms.create('netbeans-reconnect', {periodInMinutes: 1});
});
chrome.runtime.onStartup.addListener(() => NetBeans.connectIfNeeded());
NetBeans.connectIfNeeded();
