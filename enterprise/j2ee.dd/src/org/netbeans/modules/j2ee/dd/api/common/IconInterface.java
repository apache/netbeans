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

package org.netbeans.modules.j2ee.dd.api.common;
/**
 * Super interface for all DD elements having the icon property/properties.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 *
 * @author Milan Kuchtiak
 */
public interface IconInterface {

    /**
     * Sets the small-icon element value for particular locale.<br>
     * If locale=null the method sets the small-icon value for icon element where xml:lang attribute is not specified.<br>
     * If icon=null method removes the small-icon element for a specified locale.<br>
     *
     * @param locale string representing the locale - the value for xml:lang attribute e.g. "fr"
     * @param icon value for small-icon element
     */
    public void setSmallIcon(String locale, String icon) throws VersionNotSupportedException;
    /**
     * Sets the small-icon element value for icon element where xml:lang attribute is not specified.
     *
     * @param icon value for small-icon element
     */
    public void setSmallIcon(String icon);
    /**
     * Sets the large-icon element value for particular locale.<br>
     * If locale=null the method sets the large-icon value for icon element where xml:lang attribute is not specified.<br>
     * If icon=null method removes the large-icon element for a specified locale.<br>
     *
     * @param locale string representing the locale - the value for xml:lang attribute e.g. "fr"
     * @param icon value for large-icon element
     */
    public void setLargeIcon(String locale, String icon) throws VersionNotSupportedException;
    /**
     * Sets the large-icon element value for icon element where xml:lang attribute is not specified.
     *
     * @param icon value for large-icon element
     */
    public void setLargeIcon(String icon);
    /**
     * Sets the multiple icon elements.
     *
     * @param locales array of locales (xml:lang attribute values for icon elements)
     * @param smallIcons array of values for small-icon elements
     * @param largeIcons array of values for large-icon elements
     */
    public void setAllIcons(String[] locales, String[] smallIcons, String[] largeIcons) throws VersionNotSupportedException;
    /**
     * Sets the icon element. Looking for icon element with the same xml:lang attribute.<br>
     * If found the element will be replaced by new icon value.
     *
     * @param icon value for icon element
     */ 
    public void setIcon(org.netbeans.modules.j2ee.dd.api.common.Icon icon);
    
    /**
     * Returns the small-icon element value for particular locale.<br>
     * If locale=null method returns small-icon for default locale.
     *
     * @param locale string representing the locale - the value of xml:lang attribute e.g. "fr".
     * @return small-icon element value or null if not specified for given locale
     */
    public String getSmallIcon(String locale) throws VersionNotSupportedException;
    /**
     * Returns the small-icon element value for default locale. 
     *
     * @return small-icon element value or null if not specified for default locale
     */
    public String getSmallIcon();
    /**
     * Returns the large-icon element value for particular locale.<br>
     * If locale=null method returns large-icon for default locale.
     *
     * @param locale string representing the locale - the value of xml:lang attribute e.g. "fr".
     * @return large-icon element value or null if not specified for given locale
     */
    public String getLargeIcon(String locale) throws VersionNotSupportedException;
    /**
     * Returns the large-icon element value for default locale. 
     *
     * @return large-icon element value or null if not specified for default locale
     */
    public String getLargeIcon();
    /**
     * Returns the icon element value for default locale. 
     *
     * @return icon element value or null if not specified for default locale
     */
    public org.netbeans.modules.j2ee.dd.api.common.Icon getDefaultIcon();
    /**
     * Returns all icon elements in the form of <@link java.util.Map>. 
     *
     * @return map of all icons in the form of [locale:String[]{smallIcon, largeIcon}]
     */
    public java.util.Map getAllIcons();
    
    /**
     * Removes the small-icon element for particular locale.
     * If locale=null the method removes the small-icon element for default locale.
     *
     * @param locale string representing the locale - the value of xml:lang attribute e.g. "fr"
     */
    public void removeSmallIcon(String locale) throws VersionNotSupportedException;
    /**
     * Removes the large-icon element for particular locale.
     * If locale=null the method removes the large-icon element for default locale.
     *
     * @param locale string representing the locale - the value of xml:lang attribute e.g. "fr"
     */
    public void removeLargeIcon(String locale) throws VersionNotSupportedException;
    /**
     * Removes the icon element for particular locale.
     * If locale=null the method removes the icon element for default locale.
     *
     * @param locale string representing the locale - the value of xml:lang attribute e.g. "fr"
     */
    public void removeIcon(String locale) throws VersionNotSupportedException;
    /**
     * Removes small-icon element for default locale.
     */
    public void removeSmallIcon();
    /**
     * Removes large-icon element for default locale.
     */
    public void removeLargeIcon();
    /**
     * Removes icon element for default locale.
     */
    public void removeIcon();
    /**
     * Removes all icon elements from DD element.
     */
    public void removeAllIcons();
}
