/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
