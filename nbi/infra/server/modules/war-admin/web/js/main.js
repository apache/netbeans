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

function show_form_registry() {
    document.forms["Form"].registry.value = "";
    document.getElementById("form-registry").style.display = "block";
}

function close_form_registry() {
    document.getElementById("form-registry").style.display = "none";
    
    update_current_registry();
}

function show_form_codebase() {
    document.forms["Form"].codebase.value = "";
    document.getElementById("form-codebase").style.display = "block";
}

function close_form_registry() {
    document.getElementById("form-codebase").style.display = "none";
}

function show_form_archive() {
    document.getElementById("form-archive").style.display = "block";
}

function close_form_archive() {
    document.getElementById("form-archive").style.display = "none";
}

function add_registry() {
    document.forms["Form"].action = "add-registry";

    show_form_registry();
}

function remove_registry() {
    var select = document.getElementById("registries-select");
    var registry = select.options[select.selectedIndex].value;

    document.forms["Form"].registry.value = registry;
    document.forms["Form"].action = "remove-registry";
    document.forms["Form"].submit();
}

function update_engine() {
    document.forms["Form"].action = "update-engine";

    show_form_archive();
}

function update_current_registry() {
    var platformsSelect  = document.getElementById("platforms-select");
    var registriesSelect = document.getElementById("registries-select");
    
    if ((platformsSelect != null) && (registriesSelect != null)) {
        var platform = platformsSelect.options[platformsSelect.selectedIndex].value;
        var registry = registriesSelect.options[registriesSelect.selectedIndex].value;
        
        document.forms["Form"].registry.value = registry;
        
        for (i = 0; i < registriesSelect.options.length; i++) {
            var value = registriesSelect.options[i].value;
            document.getElementById("registry-" + value).style.display = "none";
        }
        
        document.getElementById("registry-" + registry).style.display = "block";
        
        document.forms["Form"].fallback.value = document.forms["Form"].fallback_base.value + "?registry=" + registry + "&platform=" + platform;
    } else {
        document.forms["Form"].fallback.value = document.forms["Form"].fallback_base.value;
    }
}

function update_target_platform() {
    var platformsSelect  = document.getElementById("platforms-select");
    var registriesSelect = document.getElementById("registries-select");
    
    var platform = platformsSelect.options[platformsSelect.selectedIndex].value;
    var registry = registriesSelect.options[registriesSelect.selectedIndex].value;
    
    window.location = document.forms["Form"].fallback_base.value + "?registry=" + registry + "&platform=" + platform;
}

function remove_component(uid, version, platforms) {
    var action;
    if (version == "null") {
        action = "remove-group";
    } else {
        action = "remove-product";
    }
    
    document.forms["Form"].action          = action;
    document.forms["Form"].uid.value       = uid;
    document.forms["Form"].version.value   = version;
    document.forms["Form"].platforms.value = platforms;
    document.forms["Form"].submit();
}

function add_package(uid, version, platforms) {
    document.forms["Form"].action          = "add-package";
    document.forms["Form"].uid.value       = uid;
    document.forms["Form"].version.value   = version;
    document.forms["Form"].platforms.value = platforms;
    
    show_form_archive();
}

function delete_bundles() {
    var select = document.getElementById("registries-select");
    var registry = select.options[select.selectedIndex].value;
    
    document.forms["Form"].registry.value = registry;
    document.forms["Form"].action = "delete-bundles";
    document.forms["Form"].submit();
}

function generate_bundles() {
    var select = document.getElementById("registries-select");
    var registry = select.options[select.selectedIndex].value;
    
    document.forms["Form"].registry.value = registry;
    document.forms["Form"].action = "generate-bundles";
    document.forms["Form"].submit();
}

function export_registry() {
    var select = document.getElementById("registries-select");
    var registry = select.options[select.selectedIndex].value;
    
    document.forms["Form"].registry.value = registry;
    document.forms["Form"].action = "export-registry";
    
    show_form_codebase();
}

function _expand(id) {
    var row = document.getElementById(id);
    
    if (row.style.display == "none") {
        row.style.display = "table-row";
    } else {
        row.style.display = "none";
    }
}
