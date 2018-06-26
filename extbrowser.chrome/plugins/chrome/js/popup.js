/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
