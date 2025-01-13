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
 * Super interface for all DD elements having the description property/properties.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 *
 * @author Milan Kuchtiak
 */
public interface DescriptionInterface {

    /**
     * Sets the description element for particular locale.<br>
     * If locale=null the method sets the description element without xml:lang attribute.<br>
     * If description=null method removes the description element for a specified locale.<br>
     *
     * @param locale string representing the locale - the value for xml:lang attribute e.g. "fr"
     * @param description value for description element
     */
    public void setDescription(String locale, String description) throws VersionNotSupportedException;
    
    /**
     * Sets the description element without xml:lang attribute.
     *
     * @param description value for description element
     */
    public void setDescription(String description);

    /**
     * Sets the multiple description elements.
     *
     * @param descriptions Map of descriptions in the form of [locale,description]
     * @throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
     */
    public void setAllDescriptions(Map<String, String> descriptions) throws VersionNotSupportedException;
    
    /**
     * Returns the description element value for particular locale.<br>
     * If locale=null method returns description for default locale.
     *
     * @param locale string representing the locale - the value of xml:lang attribute e.g. "fr".
     * @return description element value or null if not specified for given locale
     */
    public String getDescription(String locale) throws VersionNotSupportedException;

    /**
     * Returns the description element value for default locale. 
     *
     * @return description element value or null if not specified for default locale
     */
    public String getDefaultDescription();

    /**
     * Returns all description elements in the form of {@link java.util.Map}. 
     *
     * @return map of all descriptions in the form of [locale:description]
     */
    public Map<String, String> getAllDescriptions();
    
    /**
     * Removes the description element for particular locale.
     * If locale=null the method removes the description element for default locale.
     *
     * @param locale string representing the locale - the value of xml:lang attribute e.g. "fr"
     */
    public void removeDescriptionForLocale(String locale) throws VersionNotSupportedException;
    
    /**
     * Removes description element for default locale.
     */
    public void removeDescription();
    
    /**
     * Removes all description elements from DD element.
     */
    public void removeAllDescriptions();

}
