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

package org.netbeans.modules.j2ee.dd.api.common;

import java.util.Map;

/**
 * Super interface for all DD elements having the display-name property/properties.
 *
 *<p><b><span style="color:red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></span></b>
 *</p>
 *
 * @author Milan Kuchtiak
 */
public interface DisplayNameInterface {

    /**
     * Sets the display-name element value for particular locale.<br>
     * If locale=null the method sets the display-name element without xml:lang attribute.<br>
     * If displayName=null method removes the display-name element for a specified locale.<br>
     *
     * @param locale string representing the locale - the value for xml:lang attribute e.g. "fr"
     * @param displayName value for display-name element
     */
    public void setDisplayName(String locale, String displayName) throws VersionNotSupportedException;
    
    /**
     * Sets the display-name element without xml:lang attribute.
     *
     * @param displayName value for display-name element
     */
    public void setDisplayName(String displayName);
    
    /**
     * Sets the multiple display-name elements.
     *
     * @param displayNames Map of display names in the form of [locale,display-name]
     */
    public void setAllDisplayNames(Map displayNames) throws VersionNotSupportedException;
    
    /**
     * Returns the display-name element value for particular locale.<br>
     * If locale=null method returns display-name for default locale.
     *
     * @param locale string representing the locale - the value of xml:lang attribute e.g. "fr".
     * @return display-name element value or null if not specified for given locale
     */
    public String getDisplayName(String locale) throws VersionNotSupportedException;
    
    /**
     * Returns the display-name element value for default locale. 
     *
     * @return display-name element value or null if not specified for default locale
     */
    public String getDefaultDisplayName();
    
    /**
     * Returns all display-name elements in the form of {@link java.util.Map}. 
     *
     * @return map of all display-names in the form of [locale:display-name]
     */
    public Map getAllDisplayNames();
    
    /**
     * Removes the display-name element for particular locale.
     * If locale=null the method removes the display-name element for default locale.
     *
     * @param locale string representing the locale - the value of xml:lang attribute e.g. "fr"
     */
    public void removeDisplayNameForLocale(String locale) throws VersionNotSupportedException;
    
    /**
     * Removes display-name element for default locale.
     */
    public void removeDisplayName();
    
    /**
     * Removes all display-name elements from DD element.
     */
    public void removeAllDisplayNames();
}
