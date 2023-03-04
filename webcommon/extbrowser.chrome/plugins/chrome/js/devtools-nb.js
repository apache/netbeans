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

var NetBeans_Panel = {};

NetBeans_Panel._backgroundPageConnection = null;
NetBeans_Panel._propagateChangesCheckbox = null;

NetBeans_Panel.init = function() {
    if (NetBeans_Panel._backgroundPageConnection !== null) {
        return;
    }
    this._backgroundPageConnection = chrome.runtime.connect();
    this._propagateChangesCheckbox = document.getElementById('propagateChangesCheckbox');
    this._registerEvents();
    this._load();
};
// register events
NetBeans_Panel._registerEvents = function() {
    var that = this;
    this._backgroundPageConnection.onMessage.addListener(function(message) {
        if (message.enabled !== undefined) {
            that._propagateChangesCheckbox.checked = message.enabled;
        }
    });
    this._propagateChangesCheckbox.addEventListener('change', function() {
        that._backgroundPageConnection.postMessage({
            event: 'setChangesPropagated',
            enabled: that._propagateChangesCheckbox.checked
        });
    }, false);
};
// load initial state
NetBeans_Panel._load = function() {
    this._backgroundPageConnection.postMessage({
        event: 'areChangesPropagated'
    });
};

// run!
window.addEventListener('load', function() {
    NetBeans_Panel.init();
}, false);
