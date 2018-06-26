/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
