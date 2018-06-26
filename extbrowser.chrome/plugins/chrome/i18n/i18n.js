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

var I18n = {

    /**
     * Get message for the given key (and optional substitutions).
     */
    message: function(messageKey, substitutions) {
        return chrome.i18n.getMessage(messageKey, substitutions);
    },

    /**
     * Translate page title.
     */
    pageTitle: function() {
        document.title = this.message(document.title);
    },
    /**
     * Translate text (content) of the element with the given identifier.
     */
    element: function(elementId) {
        var element = document.getElementById(elementId);
        this._checkElement(element, elementId);
        element.innerHTML = this.message(element.innerHTML);
    },
    /**
     * Translate text (content) of the given elements.
     */
    elements: function(elements) {
        for (var index = 0; index < elements.length; ++index) {
            var element = elements[index];
            this._checkElement(element, index);
            element.innerHTML = this.message(element.innerHTML);
        }
    },
    /**
     * Translate given attribute of the element with the given identifier.
     */
    attribute: function(elementId, attrname) {
        var element = document.getElementById(elementId);
        this._checkElement(element, elementId);
        element.setAttribute(attrname, this.message(element.getAttribute(attrname)));
    },
    /**
     * Translate text (content) and given attribute of the element with the given identifier.
     */
    elementAttribute: function(elementId, attrname) {
        var element = document.getElementById(elementId);
        this._checkElement(element, elementId);
        element.innerHTML = this.message(element.innerHTML);
        element.setAttribute(attrname, this.message(element.getAttribute(attrname)));
    },
    /**
     * Translate text (content) of the elements with the given className.
     */
    className: function(className) {
        this.elements(document.getElementsByClassName(className));
    },

    /**
     * Check element and warn to console if element does not exist.
     */
    _checkElement: function(element, elementId) {
        if (typeof element === 'undefined') {
            console.error('Element not found for ID (or index): ' + elementId);
        }
    }

};
