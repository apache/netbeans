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

// Initialization/cleanup
NetBeans.cleanup();

// Notify IDE that the extension has been installed/updated
chrome.runtime.onInstalled.addListener(function() {
    var manifest = chrome.runtime.getManifest();
    var version = manifest.version;
    NetBeans.sendReadyMessage(version);
});

// Register reload-callback
NetBeans.browserReloadCallback = function(tabId, newUrl) {
    if (newUrl !== undefined) {
        chrome.tabs.get(tabId, function(tab) {
            if (tab.url === newUrl) {
                chrome.tabs.reload(tabId, {bypassCache: true});
            } else {
                chrome.tabs.update(tabId, {url: newUrl});
            }
        });
    } else {
        chrome.tabs.reload(tabId, {bypassCache: true});
    }
};

NetBeans.browserCloseCallback = function(tabId) {
    chrome.tabs.remove(tabId);
};

NetBeans.debuggedTab = null;
NetBeans.windowWithDebuggedTab = null;
NetBeans.browserAttachDebugger = function(tabId) {
    if (NetBeans.DEBUG) {
        console.log('debugger attach for tab ' + tabId);
    }
    chrome.debugger.attach({tabId : tabId}, "1.0", function(){
        var err = chrome.runtime.lastError;
        if (err) {
            if (err.message) {
                console.log('NetBeans cannot debug this tab because: ' + err.message);
            }
        } else {
            if (NetBeans.debuggedTab !== null && NetBeans.debuggedTab !== tabId) {
                NetBeans.hidePageIcon(NetBeans.debuggedTab);
            }
            NetBeans.debuggedTab = tabId;
            chrome.tabs.get(tabId, function(tab) {
                NetBeans.windowWithDebuggedTab = tab.windowId;
                NetBeans.createContextMenu(tabId, tab.url);
            });
            // detect viewport
            NetBeans.detectViewPort();
        }
    });
};

NetBeans.browserDetachDebugger = function(tabId) {
    if (NetBeans.DEBUG) {
        console.log('debugger detaching from tab ' + tabId);
    }
    chrome.debugger.detach({tabId : tabId});
    chrome.contextMenus.removeAll();
    if (NetBeans.debuggedTab === tabId) {
        NetBeans.hidePageIcon(tabId);
        NetBeans.debuggedTab = null;
        NetBeans.windowWithDebuggedTab = null;
    }
};

// display NB icon in URL bar
NetBeans.showPageIcon = function(tabId) {
    chrome.pageAction.show(tabId);
};
NetBeans.hidePageIcon = function(tabId) {
// Do not hide page icon. The tab is managed still and the page icon
// can be used to reconnect to IDE.
//    chrome.pageAction.hide(tabId);
};

// Creates the Select Mode context menu
NetBeans.createContextMenu = function(tabId, url) {
    if (NetBeans.debuggedTab !== tabId) {
        return; // The tab is managed but not debugged
    }
    var baseUrl = function(url) {
        // Remove anchor
        var index = url.indexOf('#');
        if (index !== -1) {
            url = url.substr(0, index);
        }
        return url;
    };
    NetBeans.contextMenuUrl = baseUrl(url);
    if (NetBeans.contextMenuCreationInProgress) {
        return;
    } else {
        NetBeans.contextMenuCreationInProgress = true;
    }
    // Removing possible orphaned context menus of this extension
    chrome.contextMenus.removeAll(function() {
        chrome.contextMenus.create({
            id: 'selectionMode',
            title: NetBeans.contextMenuName(),
            contexts: ['all'],
            documentUrlPatterns: [NetBeans.contextMenuUrl],
            onclick: function() {
                NetBeans.setSelectionMode(!NetBeans.getSelectionMode());
            }
        },
        function() {
            NetBeans.contextMenuCreationInProgress = false;
        });
    });
};

// Updates the Select Mode context menu
NetBeans.updateContextMenu = function() {
    chrome.contextMenus.update('selectionMode', {
        title: NetBeans.contextMenuName()
    });
};

// Returns the name of 'Select Mode' context menu
NetBeans.contextMenuName = function() {
    return chrome.i18n.getMessage(NetBeans.getSelectionMode() ? '_StopSelectMode' : '_StartSelectMode');
};

// show infobar
NetBeans.showInfoBar = function(tabId) {
    chrome.experimental.infobars.show({
        tabId : tabId,
        path: 'html/infobar.html'
    });
};
NetBeans.getWindowInfo = function(callback) {
    chrome.windows.getLastFocused({ populate: true }, callback);
};
NetBeans.detectViewPort = function(callback) {
    if (NetBeans.debuggedTab === null) {
        console.log('No debuggedTab so bypassing the detection');
        if (callback) {
            callback();
        }
        return;
    }
    var script = 'NetBeans_ViewPort = {'
            + '    width: window.innerWidth,'
            + '    height: window.innerHeight,'
            + '    marginWidth: window.outerWidth - window.innerWidth,'
            + '    marginHeight: window.outerHeight - window.innerHeight,'
            + '    isMac: navigator.platform.toUpperCase().indexOf("MAC") !== -1'
            + '};';
    chrome.debugger.sendCommand(
        {tabId : NetBeans.debuggedTab},
        'Runtime.evaluate',
        {expression: script, returnByValue: true},
        function(result) {
            var viewport = result.result.value;
            NetBeans_ViewPort.width = viewport.width;
            NetBeans_ViewPort.height = viewport.height;
            NetBeans_ViewPort.marginWidth = viewport.marginWidth;
            NetBeans_ViewPort.marginHeight = viewport.marginHeight;
            NetBeans_ViewPort.isMac = viewport.isMac;
            if (callback) {
                callback();
            }
        }
    );
};
NetBeans.resetPageSize = function(callback) {
    chrome.windows.getLastFocused(function(win) {
        var opt = {};
        opt.state = 'maximized';
        chrome.windows.update(win.id, opt);
        if (callback) {
            callback();
        }
    });
};
NetBeans.resizePage = function(preset, callback) {
    if (preset === null) {
        this.resetPageSize(callback);
        return;
    }
    var data = NetBeans_Presets.getPreset(preset);
    if (data === null) {
        console.error('Preset [' + preset + '] not found.');
        return;
    }
    this._resizePage(data['width'], data['height'], callback);
};
// resize actual page
NetBeans._resizePage = function(width, height, callback) {
    this.detectViewPort(function() {
        width = parseInt(width);
        height = parseInt(height);
        // resize info
        var opt = {};
        opt.state = 'normal';
        opt.width = width + NetBeans_ViewPort.marginWidth;
        opt.height = height + NetBeans_ViewPort.marginHeight;
        // resize
        chrome.windows.getLastFocused(function(win) {
            chrome.windows.update(win.id, opt);
            if (callback) {
                callback();
            }
            // #218974
            if (NetBeans_ViewPort.isMac && width < 400) {
                NetBeans.openWarning('windowTooSmall', 230);
            }
        });
    });
};
// show preset customizer
NetBeans.showPresetCustomizer = function() {
    chrome.tabs.create({'url': 'html/options.html'});
};

NetBeans.browserSendCommand = function(tabId, id, method, params, callback) {
    if (NetBeans.DEBUG) {
        console.log('send ['+tabId+","+id+","+method+","+JSON.stringify(params));
    }
    chrome.debugger.sendCommand({tabId : tabId}, method, params,
        function(result) {
            if (chrome.runtime.lastError) {
                var error = JSON.stringify(chrome.runtime.lastError);
                console.log('debugger send command result code: ' + error);
                NetBeans.sendDebuggingResponse(tabId, {id : id, error : error});
            } else {
                console.log('debugger send command response: ' + result);
                NetBeans.sendDebuggingResponse(tabId, {id : id, result : result});
            }
        });
};

// "fired" when presets changed
NetBeans_Presets.presetsChanged = function() {
    // no need to refresh popup, refresh only infobar(s)
    var views = chrome.extension.getViews({type: "infobar"});
    console.log('Refreshing ' + views.length +  ' infobars');
    for (var i in views) {
        var view = views[i];
        view.NetBeans_Infobar.redrawPresets();
    }
};

// Updates the context menu and the info-bar according to changes of page-inspection properties
NetBeans.addPageInspectionPropertyListener(function(event) {
    var name = event.name;
    if (name !== 'selectionMode') {
        return;
    }
    NetBeans.updateContextMenu();
    var value = event.value;
    var views = chrome.extension.getViews({type: "infobar"});
    for (var i in views) {
        var view = views[i];
        if (view.NetBeans_Infobar) {
            view.NetBeans_Infobar.setSelectionMode(value);
        }
    }
});

/**
 * Open page with warning about unexpected/incorrect debugger detach.
 * This means that the NetBeans integration will not work.
 * This warning is shown always except these cases:
 * 1. user closes NetBeans IDE
 * 2. the debugged tab is not more visible (tab or window closed)
 */
NetBeans._checkUnexpectedDetach = function(tabId, reason) {
    if (reason === 'replaced_with_devtools') {
        // this is ok, do not warn user
        return;
    }
    var debuggedTab = NetBeans.debuggedTab;
    if (debuggedTab != tabId) {
        // not "NetBeans" tab
        return;
    }
    // 1. user closes NetBeans IDE
    //   -> this case already works out-of-the-box
    // delay the check since detach is called before tabClosed
    setTimeout(function() {
        // 2. the debugged tab is not more visible (tab or window closed)
        chrome.tabs.get(debuggedTab, function(tab) {
            if (tab !== undefined) {
                // the tab still exists
                NetBeans.openWarning('disconnectedDebugger', 390);
            }
        });
    }, 100);
};

NetBeans.openWarning = function(ident, height) {
    NetBeans_Warnings.runIfEnabled(ident, function() {
        NetBeans.detectViewPort(function() {
            var windowTitleHeight = NetBeans_ViewPort.marginHeight - 60; // try to remove the height of the location bar
            NetBeans.openPopup('html/warning.html#' + ident, 550, height + Math.max(windowTitleHeight, 0));
        });
    });
};

/**
 * Open popup window.
 * @param {string} url url to be opened
 * @param {int} width popup width, can be omitted
 * @param {type} height popup height, can be omitted
 * @returns {void}
 */
NetBeans.openPopup = function(url, width, height) {
    var options = {
        url: url,
        type: 'popup'
    };
    if (width !== undefined) {
        options['width'] = width;
    }
    if (height !== undefined) {
        options['height'] = height;
    }
    chrome.windows.create(options);
};

chrome.debugger.onEvent.addListener(function(source, method, params) {
    NetBeans.sendDebuggingResponse(source.tabId, {method : method, params : params});
});

chrome.debugger.onDetach.addListener(function(source, reason) {
    NetBeans._checkUnexpectedDetach(source.tabId, reason);
    chrome.contextMenus.removeAll();
    if (source.tabId === NetBeans.debuggedTab) {
        NetBeans.debuggedTab = null;
        NetBeans.windowWithDebuggedTab = null;
    }
    NetBeans.sendDebuggerDetached(source.tabId);
});

// Register tab listeners
chrome.tabs.onCreated.addListener(function(tab) {
    NetBeans.tabCreated(tab.id);
});
chrome.tabs.onUpdated.addListener(function(tabId, changeInfo, tab) {
    NetBeans.tabUpdated(tab);
});
chrome.tabs.onRemoved.addListener(function(tabId) {
    NetBeans.tabRemoved(tabId);
});

// onCreated event is not delivered for the first tab;
// As a workaround, we go through all existing tabs and consider them as new.
// onUpdated event is not delivered sometimes as well for the first tab;
// Hence, we consider also tab urls that are known already.
chrome.windows.getAll({populate: true}, function(windows) {
    for (var i=0; i<windows.length; i++) {
        var window = windows[i];
        for (var j=0; j<window.tabs.length; j++) {
            var tab = window.tabs[j];
            NetBeans.tabCreated(tab.id);
            var url = tab.url;
            if (url !== undefined && url !== null && url.length !== 0) {
                // URL of the tab is known already
                NetBeans.tabUpdated(tab);
            }
        }
    }
});

NetBeans.windowFocused = function(windowId) {
// Disabled because of issue 244689 (caused by bugs in chrome.windows.onFocusChanged)
//    if (NetBeans.debuggedTab !== null) {
//        var active = (windowId === NetBeans.windowWithDebuggedTab);
//        var script = 'if (typeof(NetBeans) === "object") { NetBeans.setWindowActive('+active+'); }';
//        chrome.debugger.sendCommand(
//            {tabId : NetBeans.debuggedTab},
//            'Runtime.evaluate',
//            {expression: script});
//    }
};

chrome.windows.onFocusChanged.addListener(NetBeans.windowFocused);

chrome.tabs.onAttached.addListener(function(tabId, attachInfo) {
    if (NetBeans.debuggedTab === tabId) {
        // Debugger tab moved into a different window
        var windowId = attachInfo.newWindowId;
        NetBeans.windowWithDebuggedTab = windowId;
        NetBeans.windowFocused(windowId);
    }
});

/**
 * Warnings manager.
 */
NetBeans_Warnings = {};
/**
 * Runs the given task if the warning identified by the given ident is enabled.
 * @param {String} ident warning identifier
 * @param {function} task task to be run
 * @returns {void}
 */
NetBeans_Warnings.runIfEnabled = function(ident, task) {
    var key = NetBeans_Warnings._getKeyFor(ident, 'enabled');
    chrome.storage.sync.get(key, function(items) {
        NetBeans_Storage._logError('get', key);
        if (items[key] !== undefined && items[key] === 'false') {
            // warning disabled
            return;
        }
        task();
    });
};
/**
 * Enable/disable the given warning.
 * @param {String} ident warning identifier
 * @param {boolean} true for enable, false to disable
 * @returns {void}
 */
NetBeans_Warnings.enable = function(ident, enabled) {
    var key = NetBeans_Warnings._getKeyFor(ident, 'enabled');
    if (enabled) {
        NetBeans_Warnings._remove(key);
    } else {
        // disable
        var data = {};
        data[key] = 'false';
        chrome.storage.sync.set(data, function() {
            NetBeans_Storage._logError('set', key);
        });
    }
};
/**
 * Reset all warnings (all warnings dialogs will be shown again).
 * @returns {void}
 */
NetBeans_Warnings.reset = function() {
    chrome.storage.sync.get(function(items) {
        NetBeans_Storage._logError('reset', 'none');
        var warningPrefix = NetBeans_Warnings._getKeyFor();
        for (var key in items) {
            if (key.indexOf(warningPrefix) === 0) {
                NetBeans_Warnings._remove(key);
            }
        }
    });
};
NetBeans_Warnings._getKeyFor = function(ident, key) {
    var keyName = 'warning.';
    if (ident !== undefined) {
        keyName += ident;
        if (key !== undefined) {
            keyName += '.' + key;
        }
    }
    return keyName;
};
NetBeans_Warnings._remove = function(key) {
    // remove from local storage
    chrome.storage.sync.remove(key, function() {
        NetBeans_Storage._logError('remove', key);
    });
};

/**
 * DevTools manager.
 */
NetBeans_DevTools = {};
NetBeans_DevTools.PROPAGATE_CHANGES_KEY = 'devtools.changes.propagate';
NetBeans_DevTools.areChangesPropagated = function(task) {
    chrome.storage.sync.get(NetBeans_DevTools.PROPAGATE_CHANGES_KEY, function(items) {
        NetBeans_Storage._logError('get', NetBeans_DevTools.PROPAGATE_CHANGES_KEY);
        var enabled = true;
        if (items[NetBeans_DevTools.PROPAGATE_CHANGES_KEY] !== undefined) {
            enabled = items[NetBeans_DevTools.PROPAGATE_CHANGES_KEY];
        }
        task(enabled);
    });
};
NetBeans_DevTools.setChangesPropagated = function(enabled) {
    var data = {};
    data[NetBeans_DevTools.PROPAGATE_CHANGES_KEY] = enabled;
    chrome.storage.sync.set(data, function() {
        NetBeans_Storage._logError('set', NetBeans_DevTools.PROPAGATE_CHANGES_KEY);
    });
};
/**
 * Connect dev tools page.
 */
chrome.runtime.onConnect.addListener(function(devToolsConnection) {
    var devToolsListener = function(message) {
        var event = message.event;
        if (event === 'onResourceContentCommitted') {
            NetBeans_DevTools.areChangesPropagated(function(enabled) {
                if (enabled) {
                    // propagate change to NB
                    chrome.extension.sendMessage({
                        event: "onResourceContentCommitted",
                        resource : message.resource,
                        content: message.content
                    });
                }
            });
        } else if (event === 'areChangesPropagated') {
            NetBeans_DevTools.areChangesPropagated(function(enabled) {
                devToolsConnection.postMessage({
                    enabled: enabled
                });
            });
        } else if (event === 'setChangesPropagated') {
            NetBeans_DevTools.setChangesPropagated(message.enabled);
        }
    };
    devToolsConnection.onMessage.addListener(devToolsListener);
    devToolsConnection.onDisconnect.addListener(function() {
        devToolsConnection.onMessage.removeListener(devToolsListener);
    });
});

/**
 * Storage logging
 */
NetBeans_Storage = {};
NetBeans_Storage._logError = function(operation, key) {
    if (chrome.runtime
            && chrome.runtime.lastError) {
        console.error('Local storage error ("' + operation + '" operation for "' + key + '"): ' + chrome.runtime.lastError.message);
    }
};
