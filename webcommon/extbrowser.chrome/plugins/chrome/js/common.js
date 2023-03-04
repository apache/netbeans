/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

NetBeans = new Object();

NetBeans.serverURL = function() {
    var serverProtocol = 'ws';
    var serverHost = '127.0.0.1';
    var serverPort = 8008;
    var serverFile = '/';
    return serverProtocol+'://'+serverHost+':'+serverPort+serverFile;
};

NetBeans.DEBUG = true;
NetBeans.INFOBAR = false;

// Version that the extension reports to the IDE in 'init' message
NetBeans.VERSION = "1.8.1";
// The last version that the IDE reported to the extension
NetBeans.ideVersion = null;

NetBeans.managedTabs = new Object();

NetBeans.STATUS_NEW = 0;
NetBeans.STATUS_UNCONFIRMED = 1;
NetBeans.STATUS_MANAGED = 2;
NetBeans.STATUS_NOT_MANAGED = 3;

NetBeans.selectionMode = false;
NetBeans.synchronizeSelection = false;
NetBeans.pageInspectionListeners = [];

NetBeans.tabStatus = function(tabId) {
    var tabInfo = this.managedTabs[tabId];
    var status;
    if (tabInfo === undefined) {
        status = this.STATUS_NOT_MANAGED;
    } else {
        status = tabInfo.status;
    }
    return status;
};

NetBeans.cleanup = function() {
    this.socket = null;
    this.socketReady = false;
    this.pendingMessages = [];
};

NetBeans.connectIfNeeded = function() {
    if (this.socket === null) {
        var self = this;
        if (typeof(WebSocket) === 'undefined') {
            this.socket = new MozWebSocket(this.serverURL());
        } else {
            this.socket = new WebSocket(this.serverURL());
        }
        this.socket.onerror = function(e) {
            console.log('Socket error!');
            if (e.name && e.message) {
                console.log(e.name + ': ' + e.message);
            }
            self.cleanup();
        };
        this.socket.onclose = function() {
            self.cleanup();
        };
        this.socket.onopen = function() {
            self.socketReady = true;
            self.sendPendingMessages();
        };
        this.socket.onmessage = function(e) {
            if (NetBeans.DEBUG) {
                console.log('Received message: ' + e.data);
            }
            var message;
            try {
                message = JSON.parse(e.data);
            } catch (err) {
                console.log('Message not in JSON format!');
                console.log(err);
                console.log(e.data);
                return;
            }
            self.processMessage(message);
        };
    }
    return this.socketReady;    
};

NetBeans.sendMessage = function(message) {
    if (this.connectIfNeeded()) {
        var messageText = JSON.stringify(message);
        if (this.DEBUG) {
            console.log('Sent message: ' + messageText);
        }
        this.socket.send(messageText);
    } else {
        this.pendingMessages.push(message);
    }
};

NetBeans.sendReadyMessage = function(version) {
    this.sendMessage({
        message: 'ready',
        version: version
    });
};

NetBeans.sendInitMessage = function(tab) {
    this.sendMessage({
        message: 'init',
        url: tab.url,
        tabId: tab.id,
        version: this.VERSION
    });
};

NetBeans.sendCloseMessage = function(tabId) {
    this.sendMessage({
        message: 'close',
        tabId: tabId
    });
};

NetBeans.sendUrlChangeMessage = function(tabId, url) {
    this.sendMessage({
        message: 'urlchange',
        tabId: tabId,
        url: url
    });
};

NetBeans.sendLoadResizeOptionsMessage = function() {
    // XXX message sent more than once
    if (ResizeOptions !== null) {
        return;
    }
    this.sendMessage({
        message: 'load_resize_options'
    });
};

NetBeans.sendSaveResizeOptionsMessage = function(presets) {
    ResizeOptions = presets;
    this.sendMessage({
        message: 'save_resize_options',
        resizeOptions: ResizeOptions
    });
};

NetBeans.sendSelectionModeMessage = function(selectionMode) {
    this.sendMessage({
        message: 'selection_mode',
        selectionMode: selectionMode
    });
};

NetBeans.sendPendingMessages = function() {
    for (var i=0; i<this.pendingMessages.length; i++) {
        this.sendMessage(this.pendingMessages[i]);
    }
    this.pendingMessages = [];
};

chrome.extension.onMessage.addListener(
  function(request, sender, sendResponse) {
    if (request.event == "onResourceContentCommitted") {
      console.log('Sending changes from CDT back to NetBeans');
      NetBeans.sendResourceChangedInCDT(request.resource, request.content);
    }
  });

NetBeans.sendResourceChangedInCDT = function(url, content) {
    this.sendMessage({
        message: 'resource_changed',
        resource: url,
        content: content
    });
};

NetBeans.processMessage = function(message) {
    var type = message.message;
    if (type === 'init') {
        this.processInitMessage(message);
    } else if (type === 'reload') {
        this.processReloadMessage(message);
    } else if (type === 'close') {
        this.processCloseMessage(message);
    } else if (type === 'attach_debugger') {
        this.selectionMode = false;
        this.processAttachDebuggerMessage(message);
    } else if (type === 'detach_debugger') {
        this.processDetachDebuggerMessage(message);
    } else if (type === 'debugger_command') {
        this.processDebuggerCommandMessage(message);
    } else if (type === 'load_resize_options') {
        this.processLoadResizeOptionsMessage(message);
    } else if (type === 'save_resize_options') {
        this.processSaveResizeOptionsMessage(message);
    } else if (type === 'pageInspectionPropertyChange') {
        this.processPageInspectionPropertyChange(message);
    } else {
        console.log('Unsupported message!');
        console.log(message);
    }
};

NetBeans.tabIdFromMessage = function(message) {
    var tabIdValue = message.tabId;
    var tabId;
    if (typeof(tabIdValue) === 'number') {
        tabId = tabIdValue;
    } else if (typeof(tabIdValue) === 'string') {
        tabId = parseInt(tabIdValue);
    } else {
        console.log('Missing/incorrect tabId attribute!');
        console.log(message);
    }
    return tabId;
};

NetBeans.processInitMessage = function(message) {
    var tabId = this.tabIdFromMessage(message);
    if (tabId !== undefined) {
        var tabInfo = this.managedTabs[tabId];
        if (tabInfo === undefined) {
            console.log('Ignoring init message for an unknown tab: '+tabId);
        } else if (tabInfo.status === this.STATUS_UNCONFIRMED) {
            if (message.status === 'accepted') {
                // Tab should be managed
                NetBeans.ideVersion = message.version;
                if (tabInfo.closed) {
                    // Delayed confirmation request for already closed tab;
                    // for a tab whose URL changed
                    this.sendCloseMessage(tabId);
                    delete this.managedTabs[tabId];
                } else {
                    tabInfo.status = this.STATUS_MANAGED;
                    this.showPageIcon(tabId);
                    this.createContextMenu(tabId, tabInfo.url);
                }
            } else {
                // Tab shouldn't be managed
                delete this.managedTabs[tabId];
            }
        } else {
            console.log('Ignoring init message for a tab for which such message was not requested: '+tabId);
        }
    }
};

NetBeans.processReloadMessage = function(message) {
    var tabId = this.tabIdFromMessage(message);
    if (tabId !== undefined) {
        var status = this.tabStatus(tabId);
        if (status === this.STATUS_MANAGED) {
            this.browserReloadCallback(tabId, message.url);
        } else {
            console.log('Refusing to reload tab that is not managed: '+tabId);
        }
    }
};

NetBeans.processCloseMessage = function(message) {
    var tabId = this.tabIdFromMessage(message);
    if (tabId !== undefined) {
        var status = this.tabStatus(tabId);
        if (status === this.STATUS_MANAGED) {
            this.browserCloseCallback(tabId);
        } else {
            console.log('Refusing to close tab that is not managed: '+tabId);
        }
    }
};

NetBeans.processAttachDebuggerMessage = function(message) {
    var tabId = this.tabIdFromMessage(message);
    if (tabId !== undefined) {
        var status = this.tabStatus(tabId);
        if (status === this.STATUS_MANAGED) {
            this.browserAttachDebugger(tabId);
        } else {
            console.log('Refusing to attach debugger to tab that is not managed: '+tabId);
        }
    }
};

NetBeans.processDetachDebuggerMessage = function(message) {
    var tabId = this.tabIdFromMessage(message);
    if (tabId !== undefined) {
        var status = this.tabStatus(tabId);
        if (status === this.STATUS_MANAGED) {
            this.browserDetachDebugger(tabId);
        } else {
            console.log('Refusing to dettach debugger from tab that is not managed: '+tabId);
        }
    }
};

NetBeans.processDebuggerCommandMessage = function(message) {
    var tabId = this.tabIdFromMessage(message);
    if (tabId !== undefined) {
        var status = this.tabStatus(tabId);
        if (status === this.STATUS_MANAGED) {
            var command = message.command;
            this.browserSendCommand(tabId, command.id, command.method, command.params);
        } else {
            console.log('Refusing to send debugger command to tab that is not managed: '+tabId);
        }
    }
};

NetBeans.processLoadResizeOptionsMessage = function(message) {
    ResizeOptions = JSON.parse(message.resizeOptions);
};

NetBeans.processSaveResizeOptionsMessage = function(message) {
    this.sendMessage({
        message: 'save_resize_options',
        resizeOptions: message.resizeOptions
    });
};

NetBeans.processPageInspectionPropertyChange = function(message) {
    var name = message.propertyName;
    var value = message.propertyValue;
    if (name === 'selectionMode') {
        this.selectionMode = value;
    } else if (name === 'synchronizeSelection') {
        this.synchronizeSelection = value;
    }
    for (var i=0; i<this.pageInspectionListeners.length; i++) {
        this.pageInspectionListeners[i]({
            name: name,
            value: value
        });
    }
};

NetBeans.addPageInspectionPropertyListener = function(listener) {
    this.pageInspectionListeners.push(listener);
};

NetBeans.sendDebuggingResponse = function(tabId, response) {
    this.sendMessage({
        message: 'debugger_command_response',
        tabId: tabId,
        response : response
    });
};

NetBeans.sendDebuggerDetached = function(tabId) {
    this.sendMessage({
        message: 'debugger_detached',
        tabId: tabId
    });
};


NetBeans.tabCreated = function(tabId) {
    this.managedTabs[tabId] = {status: this.STATUS_NEW};
};

NetBeans.tabUpdated = function(tab) {
    var status = this.tabStatus(tab.id);
    var tabInfo = this.managedTabs[tab.id];
    if (status === this.STATUS_NEW) {
        tabInfo.status = this.STATUS_UNCONFIRMED;
        tabInfo.url = tab.url;
        // Send URL to IDE - ask if the tab is managed
        this.sendInitMessage(tab);
        this.sendLoadResizeOptionsMessage();
    } else if (tabInfo !== undefined) {
        // URL change should not mean that tab was closed; it may notify
        // IDE that different page is opened in the browser pane if such knowledge
        // of such state is desirable.
        if (status === this.STATUS_UNCONFIRMED) {
            // Navigation in an unconfirmed tab
            // Confirmation may be delayed; do nothing for now
        } else if (status === this.STATUS_MANAGED) {
            // Navigation in a managed tab => send "urlchange" message
            if (tabInfo.url !== tab.url) {
                this.sendUrlChangeMessage(tab.id, tab.url);
                tabInfo.url = tab.url;
            }
            this.showPageIcon(tab.id);
            this.createContextMenu(tab.id, tab.url);
            if (this.INFOBAR) {
                this.showInfoBar(tab.id);
            }
        }
    }
};

NetBeans.tabRemoved = function(tabId) {
    var status = this.tabStatus(tabId);
    if (status === this.STATUS_UNCONFIRMED) {
        // Unconfirmed tab was closed
        // Confirmation may be delayed; Mark it such that we know that
        // "close" message should be sent if such delayed confirmation arrives
        this.managedTabs[tabId].closed = true;
    } else if (status === this.STATUS_MANAGED) {
        // Managed tab was closed => send "closed" message
        this.sendCloseMessage(tabId);
    }
    if (status !== this.STATUS_UNCONFIRMED) {
        // Remove the tab from the set of managed tabs (if it was there)
        delete this.managedTabs[tabId];
    }
};

NetBeans.setSelectionMode = function(selectionMode) {
    this.selectionMode = selectionMode;
    this.sendSelectionModeMessage(selectionMode);
};

NetBeans.getSelectionMode = function() {
    return this.selectionMode;
};

NetBeans.getSynchronizeSelection = function() {
    return this.synchronizeSelection;
};

/**
 * Class representing window preset.
 *
 * Internal presets cannot be removed.
 */
function NetBeans_Preset(type, displayName, width, height, showInToolbar, isDefault) {
    // type (its ident)
    this.type = type;
    // display name
    this.displayName = displayName;
    // width (in px)
    this.width = width;
    // height (in px)
    this.height = height;
    // show in toolbar
    this.showInToolbar = showInToolbar;
    // default or not?
    this.isDefault = isDefault;
}
// preset type for Desktops
NetBeans_Preset.DESKTOP = {
    ident: 'DESKTOP',
    title: I18n.message('_Desktop')
};
// preset type for Netbooks
NetBeans_Preset.NETBOOK = {
    ident: 'NETBOOK',
    title: I18n.message('_Netbook')
};
NetBeans_Preset.WIDESCREEN = {
    ident: 'WIDESCREEN',
    title: I18n.message('_Widescreen')
};
// preset type for Tablets (Landscape)
NetBeans_Preset.TABLET_LANDSCAPE = {
    ident: 'TABLET_LANDSCAPE',
    title: I18n.message('_TabletLandscape')
};
// preset type for Tablets (Portrait)
NetBeans_Preset.TABLET_PORTRAIT = {
    ident: 'TABLET_PORTRAIT',
    title: I18n.message('_TabletPortrait')
};
// preset type for Smartphones  (Landscape)
NetBeans_Preset.SMARTPHONE_LANDSCAPE = {
    ident: 'SMARTPHONE_LANDSCAPE',
    title: I18n.message('_SmartphoneLandscape')
};
// preset type for Smartphones  (Portrait)
NetBeans_Preset.SMARTPHONE_PORTRAIT = {
    ident: 'SMARTPHONE_PORTRAIT',
    title: I18n.message('_SmartphonePortrait')
};
// get a list of all preset types
NetBeans_Preset.allTypes = function() {
    return [
        NetBeans_Preset.DESKTOP,
        NetBeans_Preset.NETBOOK,
        NetBeans_Preset.WIDESCREEN,
        NetBeans_Preset.TABLET_LANDSCAPE,
        NetBeans_Preset.TABLET_PORTRAIT,
        NetBeans_Preset.SMARTPHONE_LANDSCAPE,
        NetBeans_Preset.SMARTPHONE_PORTRAIT
    ];
};
// get preset type for the given ident, or null if not found
NetBeans_Preset.typeForIdent = function(ident) {
    var allTypes = NetBeans_Preset.allTypes();
    for (i in allTypes) {
        if (allTypes[i].ident === ident) {
            return allTypes[i];
        }
    }
    console.error('Type not found for ident: ' + ident);
    // fallback, avoid NPE
    return allTypes[0];
};

/**
 * Window presets manager.
 */
var NetBeans_Presets = {};
// all presets
NetBeans_Presets._presets = null;
// active/current preset
NetBeans_Presets._preset = null;
NetBeans_Presets.getPreset = function(preset) {
    if (preset === undefined) {
        return this._preset;
    }
    var tmp = this.getPresets()[preset];
    if (tmp === undefined) {
        return null;
    }
    this._preset = tmp;
    return this._preset;
};
// get all presets
NetBeans_Presets.getPresets = function(copy) {
    if (copy) {
        return this._loadPresets();
    }
    if (this._presets === null) {
        this._presets = this._loadPresets();
    }
    return this._presets;
};
// set (and save) new presets
NetBeans_Presets.setPresets = function(presets) {
    this._presets = presets;
    this._savePresets();
    this.presetsChanged();
};
// load presets from the central storage
NetBeans_Presets._loadPresets = function() {
    if (ResizeOptions === null) {
        // netbeans not running?
        return null;
    }
    console.log('Mapping window presets from NetBeans');
    var presets = [];
    for (var i in ResizeOptions) {
        var option = ResizeOptions[i];
        presets.push(new NetBeans_Preset(
            option.type,
            option.displayName,
            option.width,
            option.height,
            option.showInToolbar,
            option.isDefault
        ));
    }
    return presets;
};
// save presets to the central storage
NetBeans_Presets._savePresets = function() {
    console.log('Saving window presets back to NetBeans');
    NetBeans.sendSaveResizeOptionsMessage(this._presets);
};

/**
 * Viewport (so browser window can be correctly resized).
 */
NetBeans_ViewPort = {
    width: -1,
    height: -1,
    marginWidth: 0,
    marginHeight: 0,
    isMac: false
};

/**
 * Resize options (a.k.a. Windows Presets from NetBeans).
 */
var ResizeOptions = null;
