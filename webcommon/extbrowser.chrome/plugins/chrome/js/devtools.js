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
var backgroundPageConnection = chrome.runtime.connect();

chrome.devtools.inspectedWindow.onResourceContentCommitted.addListener(function(resource, content) {
    backgroundPageConnection.postMessage({
        event: 'onResourceContentCommitted',
        resource: resource,
        content: content
    });
});

chrome.devtools.panels.create('NetBeans', '../img/presets/netbeans16.png', '../html/devtools-nb.html');
