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
var NetBeans_Warnings = chrome.extension.getBackgroundPage().NetBeans_Warnings;

/**
 * Warning - the content is set by the URL ident (accessible via <code>window.location.hash</code>).
 */
var NetBeans_Warning = {};

NetBeans_Warning._ident = null;
NetBeans_Warning._okButton = null;
NetBeans_Warning._doNotShowAgainButton = null;

NetBeans_Warning.init = function() {
    if (NetBeans_Warning._ident !== null) {
        return;
    }
    this._ident = window.location.hash.substring(1);
    this._okButton = document.getElementById('okButton');
    this._doNotShowAgainButton = document.getElementById('doNotShowAgainCheck');
    this._showContent();
    this._registerEvents();
};
// show proper content of the page
NetBeans_Warning._showContent = function() {
    document.getElementById(this._ident).style.display = 'block';
};
// register events
NetBeans_Warning._registerEvents = function() {
    var that = this;
    this._okButton.addEventListener('click', function() {
        that._close();
    }, false);
};
NetBeans_Warning._close = function() {
    this._doNotShowAgain();
    window.close();
};
NetBeans_Warning._doNotShowAgain = function() {
    NetBeans_Warnings.enable(this._ident, !this._doNotShowAgainButton.checked);
};

// run!
window.addEventListener('load', function() {
    NetBeans_Warning.init();
}, false);
