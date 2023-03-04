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
