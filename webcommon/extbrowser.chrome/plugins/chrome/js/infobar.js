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

/**
 * Infobar.
 */
var NetBeans_Infobar = {};
// presets
NetBeans_Infobar._presets = null;
// preset container
NetBeans_Infobar._container = null;
// show presets
NetBeans_Infobar.show = function(presets) {
    this._presets = presets;
    this._init();
    this.setSelectionMode(NetBeans.getSelectionMode());
    this._showPresets();
};
// redraw presets
NetBeans_Infobar.redrawPresets = function() {
    this.show(NetBeans_Presets.getPresets());
};
// init
NetBeans_Infobar._init = function() {
    if (this._container !== null) {
        return;
    }
    this._container = document.getElementById('presets');
    this._registerEvents();
};
// register events
NetBeans_Infobar._registerEvents = function() {
    var that = this;
    document.getElementById('autoPresetButton').addEventListener('click', function() {
        NetBeans.resetPageSize();
    }, false);
    document.getElementById('presetCustomizerButton').addEventListener('click', function() {
        NetBeans.showPresetCustomizer();
    }, false);
    document.getElementById('selectionModeCheckBox').addEventListener('click', function() {
        that._updateSelectionMode(false);
    }, false);
    document.getElementById('selectionModeMenu').addEventListener('click', function() {
        that._updateSelectionMode(true);
    }, false);
};
// show presets in the toolbar
NetBeans_Infobar._showPresets = function() {
    // clean
    this._container.innerHTML = '';
    // add buttons
    for (var p in this._presets) {
        var preset = this._presets[p];
        if (!preset.showInToolbar) {
            continue;
        }
        var button = document.createElement('a');
        button.setAttribute('href', '#');
        button.setAttribute('class', 'button');
        button.setAttribute('title', preset.displayName + ' (' + preset.width + ' x ' + preset.height + ')');
        // wrap function to another function so current index is copied (otherwise, the last index will be always used)
        button.addEventListener('click', function(presetIndex) {
            return function() {
                NetBeans.resizePage(presetIndex);
            };
        } (p), false);
        button.appendChild(document.createTextNode(preset.displayName));
        this._container.appendChild(button);
    }
};

NetBeans_Infobar._updateSelectionMode = function(switchCheckBoxValue) {
    var checkbox = document.getElementById('selectionModeCheckBox');
    if (switchCheckBoxValue) {
        checkbox.checked = !checkbox.checked;
    }
    var selectionMode = checkbox.checked;
    NetBeans.setSelectionMode(selectionMode);
};

// Modifies Selection Mode checkbox according to the given value
NetBeans_Infobar.setSelectionMode = function(selectionMode) {
    var checkbox = document.getElementById('selectionModeCheckBox');
    checkbox.checked = selectionMode;
};

// run!
window.addEventListener('load', function() {
    NetBeans_Infobar.show(NetBeans_Presets.getPresets());
}, false);
