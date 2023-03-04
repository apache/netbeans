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

// references from bg page
var NetBeans = chrome.extension.getBackgroundPage().NetBeans;
var NetBeans_Presets = chrome.extension.getBackgroundPage().NetBeans_Presets;
var NetBeans_Preset = chrome.extension.getBackgroundPage().NetBeans_Preset;
var NetBeans_ViewPort = chrome.extension.getBackgroundPage().NetBeans_ViewPort;

/**
 * Window presets menu.
 */
var NetBeans_PresetMenu = {};
// menu container
NetBeans_PresetMenu._container = null;
// menu presets
NetBeans_PresetMenu._presets = null;
// show the menu
NetBeans_PresetMenu.show = function(presets, activeTab) {
    this._init(activeTab);
    this._initSelectionMode(activeTab);
    this._initDebugInNetBeans(activeTab);
    this._presets = presets;
    this._putPresets(this._presets);
};
NetBeans_PresetMenu.hide = function() {
    window.close();
};
NetBeans_PresetMenu.resetPage = function() {
    var that = this;
    NetBeans.resetPageSize(function() {
        that.hide();
    });
};
NetBeans_PresetMenu.resizePage = function(preset) {
    var that = this;
    NetBeans.resizePage(preset, function() {
        that.hide();
    });
};
NetBeans_PresetMenu.setAutoPresetActive = function() {
    document.getElementById('autoPresetMenu').setAttribute('class', 'active');
    document.getElementById('autoPresetRadio').setAttribute('checked', 'checked');
};
/*** ~Private ***/
// menu init
NetBeans_PresetMenu._init = function(activeTab) {
    if (this._container !== null) {
        return;
    }
    this._container = document.getElementById('presetMenu');
    this._registerEvents(activeTab);
};
// selection mode init
NetBeans_PresetMenu._initSelectionMode = function(activeTab) {
    var selectionMode = document.getElementById('selectionModeCheckBox');
    selectionMode.checked = NetBeans.getSelectionMode();
    var selectionModeMenu = document.getElementById('selectionModeMenu');
    var display = NetBeans.debuggedTab === activeTab.id ? 'block' : 'none';
    selectionModeMenu.style.display = display;
    var selectionModeSeparator = document.getElementById('selectionModeSeparator');
    if (selectionModeSeparator) {
        selectionModeSeparator.style.display = display;
    }
};
// Debug in NetBeans init
NetBeans_PresetMenu._initDebugInNetBeans = function(activeTab) {
    var menu = document.getElementById('debugInNetBeansMenu');
    var display = NetBeans.ideVersion === "7.4" || NetBeans.debuggedTab === activeTab.id ? 'none' : 'block';
    menu.style.display = display;
    var separator = document.getElementById('debugInNetBeansSeparator');
    if (separator) {
        separator.style.display = display;
    }
};
// register events
NetBeans_PresetMenu._registerEvents = function(activeTab) {
    var that = this;
    document.getElementById('autoPresetMenu').addEventListener('click', function() {
        that.resetPage();
    }, false);
    document.getElementById('customizePresetsMenu').addEventListener('click', function() {
        that._showPresetCustomizer();
    }, false);
    document.getElementById('selectionModeMenu').addEventListener('click', function(event) {
        that._updateSelectionMode(event.target.id !== 'selectionModeCheckBox');
    }, false);
    document.getElementById('debugInNetBeansMenu').addEventListener('click', function() {
        that._debugInNetBeans(activeTab);
    }, false);
};
// clean and put presets to the menu
NetBeans_PresetMenu._putPresets = function() {
    var menu = document.getElementById('menuPresets');
    // clean
    menu.innerHTML = '';
    for (var p in this._presets) {
        var preset = this._presets[p];
        var activePreset = NetBeans_ViewPort.width == preset.width && NetBeans_ViewPort.height == preset.height;
        // item
        var item = document.createElement('a');
        item.setAttribute('href', '#');
        item.setAttribute('tabindex', '-1');
        item.setAttribute('title', I18n.message('_PresetTitle', [preset.displayName, preset.width, preset.height]));
        // wrap function to another function so current index is copied (otherwise, the last index will be always used)
        item.addEventListener('click', function(presetIndex) {
            return function() {
                NetBeans_PresetMenu.resizePage(presetIndex);
            };
        } (p), false);
        if (activePreset) {
            item.setAttribute('class', 'active');
        }
        // formitem
        var formItemDiv = document.createElement('div');
        formItemDiv.setAttribute('class', 'form-item');
        var radio = document.createElement('input');
        radio.setAttribute('type', 'radio');
        radio.setAttribute('tabindex', '-1');
        if (activePreset) {
            radio.setAttribute('checked', 'checked');
        }
        formItemDiv.appendChild(radio);
        item.appendChild(formItemDiv);
        // icon
        var presetType = NetBeans_Preset.typeForIdent(preset.type);
        var iconDiv = document.createElement('div');
        iconDiv.setAttribute('class', 'icon');
        var img = document.createElement('img');
        img.setAttribute('src', '../img/presets/' + presetType.ident + '.png');
        img.setAttribute('alt', presetType.title);
        img.setAttribute('title', presetType.title);
        iconDiv.appendChild(img);
        item.appendChild(iconDiv);
        // label
        var labelDiv = document.createElement('div');
        labelDiv.setAttribute('class', 'label');
        // label - main
        var mainLabelDiv = document.createElement('div');
        mainLabelDiv.setAttribute('class', 'main');
        mainLabelDiv.appendChild(document.createTextNode(preset.displayName));
        labelDiv.appendChild(mainLabelDiv);
        // label - info
        var infoLabelDiv = document.createElement('div');
        infoLabelDiv.setAttribute('class', 'info');
        infoLabelDiv.appendChild(document.createTextNode(I18n.message('_PresetWidthHeight', [preset.width, preset.height])));
        labelDiv.appendChild(infoLabelDiv);
        item.appendChild(labelDiv);
        // append item
        menu.appendChild(item);
    }
};
// show preset customizer
NetBeans_PresetMenu._showPresetCustomizer = function() {
    NetBeans.showPresetCustomizer();
    this.hide();
};

NetBeans_PresetMenu._updateSelectionMode = function(switchCheckBoxValue) {
    var checkbox = document.getElementById('selectionModeCheckBox');
    if (switchCheckBoxValue) {
        checkbox.checked = !checkbox.checked;
    }
    var selectionMode = checkbox.checked;
    NetBeans.setSelectionMode(selectionMode);
    this.hide();
};

NetBeans_PresetMenu._debugInNetBeans = function(activeTab) {
    NetBeans.sendMessage({
        message: 'inspect',
        tabId: activeTab.id,
        url: activeTab.url
    });
    this.hide();
};

// run!
window.addEventListener('load', function() {
    NetBeans.detectViewPort(function() {
        NetBeans.getWindowInfo(function(window) {
            var activeTab = null;
            var i;
            for (i=0; i<window.tabs.length; i++) {
                var tab = window.tabs[i];
                if (tab.active) {
                    activeTab = tab;
                    break;
                }
            }
            NetBeans_PresetMenu.show(NetBeans_Presets.getPresets(), activeTab);
            if (window.state === 'maximized') {
                NetBeans_PresetMenu.setAutoPresetActive();
            }
        });
    });
}, false);
