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

window.addEventListener('load', function() {
    I18n.pageTitle();
    // texts
    I18n.className('i18n');
    // table
    I18n.elements(document.getElementById('presetCustomizerTable').getElementsByTagName('thead')[0].getElementsByTagName('th'));
    // preset buttons
    I18n.elementAttribute('addPreset', 'title');
    I18n.elementAttribute('removePreset', 'title');
    I18n.elementAttribute('moveUpPreset', 'title');
    I18n.elementAttribute('moveDownPreset', 'title');
    // main buttons
    I18n.elementAttribute('presetCustomizerOk', 'title');
    I18n.elementAttribute('presetCustomizerCancel', 'title');
    I18n.elementAttribute('resetWarnings', 'title');
}, false);
