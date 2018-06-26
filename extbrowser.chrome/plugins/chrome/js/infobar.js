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
