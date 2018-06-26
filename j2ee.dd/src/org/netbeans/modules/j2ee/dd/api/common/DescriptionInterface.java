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
     */
    public void setAllDescriptions(java.util.Map descriptions) throws VersionNotSupportedException;
    
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
     * Returns all description elements in the form of <@link java.util.Map>. 
     *
     * @return map of all descriptions in the form of [locale:description]
     */
    public java.util.Map getAllDescriptions();
    
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
