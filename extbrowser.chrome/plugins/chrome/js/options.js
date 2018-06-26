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
var INFOBAR = chrome.extension.getBackgroundPage().NetBeans.INFOBAR;
var NetBeans_Presets = chrome.extension.getBackgroundPage().NetBeans_Presets;
var NetBeans_Preset = chrome.extension.getBackgroundPage().NetBeans_Preset;
var NetBeans_Warnings = chrome.extension.getBackgroundPage().NetBeans_Warnings;

/**
 * Preset customizer.
 */
var NetBeans_PresetCustomizer = {};
// customizer container
NetBeans_PresetCustomizer._container = null;
// help button
NetBeans_PresetCustomizer._moreHelpButton = null;
// presets container
NetBeans_PresetCustomizer._rowContainer = null;
// add button
NetBeans_PresetCustomizer._addPresetButton = null;
// remove button
NetBeans_PresetCustomizer._removePresetButton = null;
// move up button
NetBeans_PresetCustomizer._moveUpPresetButton = null;
// move down button
NetBeans_PresetCustomizer._moveDownPresetButton = null;
// OK button
NetBeans_PresetCustomizer._okButton = null;
// reset warnings button
NetBeans_PresetCustomizer._resetWarningsButton = null;
// presets
NetBeans_PresetCustomizer._presets = null;
// active/selected preset
NetBeans_PresetCustomizer._activePreset = null;
// show customizer
NetBeans_PresetCustomizer.show = function(presets) {
    this._init();
    this._presets = presets;
    this._putPresets(this._presets);
};
/*** ~Private ***/
// customizer init
NetBeans_PresetCustomizer._init = function() {
    if (this._rowContainer !== null) {
        return;
    }
    if (!INFOBAR) {
        document.getElementById('toolbarHeader').style.display = 'none';
    }
    this._moreHelpButton = document.getElementById('moreHelpButton');
    this._rowContainer = document.getElementById('presetCustomizerTable').getElementsByTagName('tbody')[0];
    this._addPresetButton = document.getElementById('addPreset');
    this._removePresetButton = document.getElementById('removePreset');
    this._moveUpPresetButton = document.getElementById('moveUpPreset');
    this._moveDownPresetButton = document.getElementById('moveDownPreset');
    this._okButton = document.getElementById('presetCustomizerOk');
    this._resetWarningsButton = document.getElementById('resetWarnings');
    this._registerEvents();
};
// hide customizer
NetBeans_PresetCustomizer._hide = function() {
    window.close();
};
// register events
NetBeans_PresetCustomizer._registerEvents = function() {
    var that = this;
    this._moreHelpButton.addEventListener('click', function() {
        that._switchHelp();
    }, false);
    this._addPresetButton.addEventListener('click', function() {
        that._addPreset();
    }, false);
    this._removePresetButton.addEventListener('click', function() {
        that._removePreset();
    }, false);
    this._moveUpPresetButton.addEventListener('click', function() {
        that._moveUpPreset();
    }, false);
    this._moveDownPresetButton.addEventListener('click', function() {
        that._moveDownPreset();
    }, false);
    this._okButton.addEventListener('click', function() {
        that._save();
    }, false);
    document.getElementById('presetCustomizerCancel').addEventListener('click', function() {
        that._cancel();
    }, false);
    this._resetWarningsButton.addEventListener('click', function() {
        that._resetWarnings();
    }, false);
};
// put presets to the customizer?
NetBeans_PresetCustomizer._putPresets = function(presets) {
    if (this._presets === null) {
        this._putNoPresets();
        this._enableButtons();
    } else {
        this._putPresetsInternal(presets);
    }
};
// no presets available (netbeans not running)
NetBeans_PresetCustomizer._putNoPresets = function() {
    var row = document.createElement('tr');
    var info = document.createElement('td');
    info.setAttribute('colspan', '5');
    info.setAttribute('class', 'info');
    info.appendChild(document.createTextNode(I18n.message('_WindowSettingsNotAvailable')));
    row.appendChild(info);
    this._rowContainer.appendChild(row);
};
// put presets to the table
NetBeans_PresetCustomizer._putPresetsInternal = function(presets) {
    var that = this;
    var allPresetTypes = NetBeans_Preset.allTypes();
    for (p in presets) {
        var preset = presets[p];
        // row
        var row = document.createElement('tr');
        row.addEventListener('click', function() {
            that._rowSelected(this);
        }, true);
        // type
        var type = document.createElement('td');
        var typeSelect = document.createElement('select');
        for (i in allPresetTypes) {
            var presetType = allPresetTypes[i];
            var option = document.createElement('option');
            option.setAttribute('value', presetType.ident);
            if (preset.type === presetType.ident) {
                option.setAttribute('selected', 'selected');
            }
            option.appendChild(document.createTextNode(presetType.title));
            typeSelect.appendChild(option);
        }
        typeSelect.addEventListener('change', function() {
            that._typeChanged(this);
        }, false);
        type.appendChild(typeSelect);
        row.appendChild(type);
        // name
        var title = document.createElement('td');
        var titleInput = document.createElement('input');
        titleInput.setAttribute('value', preset.displayName);
        titleInput.addEventListener('keyup', function() {
            that._titleChanged(this);
        }, false);
        title.appendChild(titleInput);
        row.appendChild(title);
        // width
        var witdh = document.createElement('td');
        var widthInput = document.createElement('input');
        widthInput.setAttribute('value', preset.width);
        widthInput.className = 'number';
        widthInput.addEventListener('keyup', function() {
            that._widthChanged(this);
        }, false);
        witdh.appendChild(widthInput);
        row.appendChild(witdh);
        // height
        var height = document.createElement('td');
        var heightInput = document.createElement('input');
        heightInput.setAttribute('value', preset.height);
        heightInput.className = 'number';
        heightInput.addEventListener('keyup', function() {
            that._heightChanged(this);
        }, false);
        height.appendChild(heightInput);
        row.appendChild(height);
        // toolbar
        if (INFOBAR) {
            var toolbar = document.createElement('td');
            toolbar.setAttribute('class', 'toolbar');
            var toolbarCheckbox = document.createElement('input');
            toolbarCheckbox.setAttribute('type', 'checkbox');
            if (preset.showInToolbar) {
                toolbarCheckbox.setAttribute('checked', 'checked');
            }
            toolbarCheckbox.addEventListener('click', function() {
                that._toolbarChanged(this);
            }, false);
            toolbar.appendChild(toolbarCheckbox);
            row.appendChild(toolbar);
        }
        // append row
        this._rowContainer.appendChild(row);
        preset['_row'] = row;
        preset['_errors'] = [];
    }
};
// cleanup (remove presets from customizer)
NetBeans_PresetCustomizer._cleanUp = function() {
    this._presets = null;
    while (this._rowContainer.hasChildNodes()) {
        this._rowContainer.removeChild(this._rowContainer.firstChild);
    }
    this._activePreset = null;
    this._enableButtons();
};
// add a new preset
NetBeans_PresetCustomizer._addPreset = function() {
    var preset = new NetBeans_Preset(NetBeans_Preset.DESKTOP.ident, I18n.message('_New_hellip'), '800', '600', true, false);
    this._presets.push(preset);
    this._putPresets([preset]);
    this._enableButtons();
};
// remove the active preset
NetBeans_PresetCustomizer._removePreset = function() {
    // presets
    this._presets.splice(this._presets.indexOf(this._activePreset), 1);
    // ui
    this._rowContainer.removeChild(this._activePreset['_row']);
    this._activePreset = null;
    this._enableButtons();
};
// move the active preset up
NetBeans_PresetCustomizer._moveUpPreset = function() {
    // presets
    this._movePreset(this._activePreset, -1);
    // ui
    var row = this._activePreset['_row'];
    var before = row.previousSibling;
    this._rowContainer.removeChild(row);
    this._rowContainer.insertBefore(row, before);
    this._enableButtons();
};
// move the active preset down
NetBeans_PresetCustomizer._moveDownPreset = function() {
    // presets
    this._movePreset(this._activePreset, +1);
    // ui
    var row = this._activePreset['_row'];
    var after = row.nextSibling;
    this._rowContainer.removeChild(row);
    nbInsertAfter(row, after);
    this._enableButtons();
};
// move the preset up or down
NetBeans_PresetCustomizer._movePreset = function(preset, shift) {
    var index = this._presets.indexOf(preset);
    var tmp = this._presets[index];
    this._presets[index] = this._presets[index + shift];
    this._presets[index + shift] = tmp;
};
// save presets to the central storage and redraw them
NetBeans_PresetCustomizer._save = function() {
    for (i in this._presets) {
        var preset = this._presets[i];
        delete preset['_row'];
        delete preset['_errors'];
    }
    NetBeans_Presets.setPresets(this._presets);
    this._cleanUp();
    this._hide();
};
// cancel customizer
NetBeans_PresetCustomizer._cancel = function() {
    this._cleanUp();
    this._hide();
};
// switch help
NetBeans_PresetCustomizer._switchHelp = function() {
    var help = document.getElementById('help');
    var displayed = help.style.display == 'block';
    help.style.display = displayed ? 'none' : 'block';
    this._moreHelpButton.innerHTML = I18n.message(displayed ? '_More_hellip' : '_Less_hellip');
};
// reset warnings
NetBeans_PresetCustomizer._resetWarnings = function() {
    NetBeans_Warnings.reset();
    this._resetWarningsButton.innerHTML = I18n.message('_Done');
    this._resetWarningsButton.setAttribute('disabled', 'disabled');
};
// callback when row is selected
NetBeans_PresetCustomizer._rowSelected = function(row) {
    if (this._activePreset !== null) {
        if (this._activePreset['_row'] === row) {
            // repeated click => ignore
            return;
        }
        this._activePreset['_row'].className = '';
    }
    // select
    var that = this;
    for (var i in this._presets) {
        var preset = this._presets[i];
        if (preset['_row'] === row) {
            that._activePreset = preset;
            that._activePreset['_row'].className = 'active';
        }
    }
    this._enableButtons();
};
// enable/disable action buttons (based on the active preset)
NetBeans_PresetCustomizer._enableButtons = function() {
    this._enablePresetButtons();
    this._enableMainButtons();
};
// enable/disable preset buttons (based on the active preset)
NetBeans_PresetCustomizer._enablePresetButtons = function() {
    if (this._activePreset !== null) {
        // any preset selected
        if (this._activePreset.isDefault) {
            this._removePresetButton.setAttribute('disabled', 'disabled');
        } else {
            this._removePresetButton.removeAttribute('disabled');
        }
        if (this._activePreset['_row'] !== this._rowContainer.firstChild) {
            this._moveUpPresetButton.removeAttribute('disabled');
        } else {
            this._moveUpPresetButton.setAttribute('disabled', 'disabled');
        }
        if (this._activePreset['_row'] !== this._rowContainer.lastChild) {
            this._moveDownPresetButton.removeAttribute('disabled');
        } else {
            this._moveDownPresetButton.setAttribute('disabled', 'disabled');
        }
    } else {
        if (this._presets === null) {
            // nb not running
            this._addPresetButton.setAttribute('disabled', 'disabled');
        }
        this._removePresetButton.setAttribute('disabled', 'disabled');
        this._moveUpPresetButton.setAttribute('disabled', 'disabled');
        this._moveDownPresetButton.setAttribute('disabled', 'disabled');
    }
};
// enable/disable customizer buttons
NetBeans_PresetCustomizer._enableMainButtons = function() {
    var anyError = false;
    if (this._presets === null) {
        anyError = true;
    } else {
        for (i in this._presets) {
            if (this._presets[i]['_errors'].length) {
                anyError = true;
                break;
            }
        }
    }
    if (anyError) {
        this._okButton.setAttribute('disabled', 'disabled');
    } else {
        this._okButton.removeAttribute('disabled');
    }
};
// callback when preset type changes
NetBeans_PresetCustomizer._typeChanged = function(input) {
    if (this._activePreset === null) {
        // select change event fired before row click event => select the closest row
        var row = input;
        while (true) {
            row = row.parentNode;
            if (row.tagName.toLowerCase() === 'tr') {
                break;
            }
        }
        this._rowSelected(row);
    }
    this._activePreset.type = NetBeans_Preset.typeForIdent(input.value).ident;
};
// callback when preset title changes
NetBeans_PresetCustomizer._titleChanged = function(input) {
    var that = this;
    this._checkField(input, 'displayName', function(value) {
        return that._validateNotEmpty(value);
    });
};
// callback when preset width changes
NetBeans_PresetCustomizer._widthChanged = function(input) {
    var that = this;
    this._checkField(input, 'width', function(value) {
        return that._validateNumber(value);
    });
};
// callback when preset height changes
NetBeans_PresetCustomizer._heightChanged = function(input) {
    var that = this;
    this._checkField(input, 'height', function(value) {
        return that._validateNumber(value);
    });
};
// callback when preset toolbar changes
NetBeans_PresetCustomizer._toolbarChanged = function(input) {
    this._activePreset.showInToolbar = input.checked;
};
// check whether the value is not empty
NetBeans_PresetCustomizer._validateNotEmpty = function(value) {
    return value != null && value.trim().length > 0;
};
// check whether the value is a number
NetBeans_PresetCustomizer._validateNumber = function(value) {
    return value != null && value.search(/^[1-9][0-9]*$/) != -1;
};
// check the given input, for the given key with the given validation callback
NetBeans_PresetCustomizer._checkField = function(input, key, validation) {
    var value = input.value;
    var index = this._activePreset['_errors'].indexOf(key);
    if (validation(value)) {
        nbRemoveCssClass(input, 'error');
        if (index !== -1) {
            this._activePreset['_errors'].splice(index, 1);
        }
    } else {
        nbAddCssClass(input, 'error');
        if (index === -1) {
            this._activePreset['_errors'].push(key);
        }
    }
    this._activePreset[key] = value;
    this._enableMainButtons();
};

/*** ~Helpers ***/
// mirror function to element.insertBefore()
function nbInsertAfter(newElement, targetElement) {
	var parent = targetElement.parentNode;
	if (parent.lastchild === targetElement) {
		parent.appendChild(newElement);
    } else {
		parent.insertBefore(newElement, targetElement.nextSibling);
    }
}
// add CSS class to the given element
function nbAddCssClass(element, cssClass) {
    var className = element.className;
    if (className.indexOf(cssClass) !== -1) {
        // already has this class
        return;
    }
    element.className = (element.className.trim() + ' ' + cssClass);
}
// remove CSS class to the given element
function nbRemoveCssClass(element, cssClass) {
    element.className = element.className.replace(cssClass, '').trim();
}

// run!
window.addEventListener('load', function() {
    NetBeans_PresetCustomizer.show(NetBeans_Presets.getPresets(true));
}, false);
