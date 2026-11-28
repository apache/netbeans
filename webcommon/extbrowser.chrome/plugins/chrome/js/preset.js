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
