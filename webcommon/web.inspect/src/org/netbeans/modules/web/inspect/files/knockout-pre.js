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

// This code will be placed on one line (to ensure that line breakpoints
// will work). Any occurrence of // is interpreted as the beginning
// of a line comment (that will be removed during this process).

(function() {

if (typeof(NetBeans) !== 'object') {
    NetBeans = new Object();
}

// Returns Knockout (if available)
NetBeans.getKnockout = function() {
    var ko = window.ko;
    if (!ko) {
        if (typeof (window.require) === 'function') {
            try {
                ko = require('ko');
            } catch(ex) {}
            if (!ko) {
                try {
                    ko = require('knockout');
                } catch(e) {}
            }
        }
    }
    return ko;
};

// Initializes the detection of unused bindings
NetBeans.detectUnusedBindings = function() {
    if (NetBeans.knockoutMarkers) {
        return;
    }
    var ko = NetBeans.getKnockout();
    var delegate = (typeof(ko) === 'object') ? ko.bindingProvider.instance.getBindingAccessors : null;
    if (delegate) {
        NetBeans.knockoutMarkers = [];
        var createMarker = function(node, bindingName) {
            var marker = {
                invoked: false,
                binding: bindingName,
                node: node
            };
            NetBeans.knockoutMarkers.push(marker);
            return marker;
        };
        ko.bindingProvider.instance.getBindingAccessors = function(node) {
            var accessors = delegate.apply(this, arguments);
            if (accessors) {
                for (var accessorName in accessors) {
                    if (accessors.hasOwnProperty(accessorName)) {
                        var originalAccessor = accessors[accessorName];
                        accessors[accessorName] = (function(marker, accessor) {
                            return function() {
                                marker.invoked = true;
                                return accessor();
                            };
                        })(createMarker(node, accessorName), originalAccessor);
                    }
                }
            }
            return accessors;
        };
    } // else not Knockout 3+ script
};

// Initializes the detection of unused bindings when Knockout is initialized
// through a global define function
if (typeof define === 'function' && define['amd']) {
    var originalDefine = define;
    // Overwrite define temporarily, it will be invoked from Knockout script
    define = function() {
        var i;
        for (i=0; i<arguments.length; i++) {
            if (typeof(arguments[i]) === 'function') {
                break;
            }
        }
        var koDefinition = arguments[i];
        var wrapper = function() {
            var result = koDefinition.apply(this, arguments);
            NetBeans.detectUnusedBindings();
            define = originalDefine;
            return result;
        };
        arguments[i] = wrapper;
	originalDefine.apply(this, arguments);
    };
    define.amd = originalDefine.amd;
}

})();
