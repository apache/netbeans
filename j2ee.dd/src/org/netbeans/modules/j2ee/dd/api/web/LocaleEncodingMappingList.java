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

package org.netbeans.modules.j2ee.dd.api.web;
import org.netbeans.modules.j2ee.dd.api.common.*;
/**
 * Generated interface for LocaleEncodingMappingList element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface LocaleEncodingMappingList extends CommonDDBean, CreateCapability, FindCapability {
        /** Setter for locale-encoding-mapping element.
         * @param index position in the array of elements
         * @param valueInterface locale-encoding-mapping element (LocaleEncodingMapping object)
         */
	public void setLocaleEncodingMapping(int index, org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping valueInterface);
        /** Getter for locale-encoding-mapping element.
         * @param index position in the array of elements
         * @return locale-encoding-mapping element (LocaleEncodingMapping object)
         */
	public org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping getLocaleEncodingMapping(int index);
        /** Setter for locale-encoding-mapping elements.
         * @param value array of locale-encoding-mapping elements (LocaleEncodingMapping objects)
         */
	public void setLocaleEncodingMapping(org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping[] value);
        /** Getter for locale-encoding-mapping elements.
         * @return array of locale-encoding-mapping elements (LocaleEncodingMapping objects)
         */
	public org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping[] getLocaleEncodingMapping();
        /** Returns size of locale-encoding-mapping elements.
         * @return number of locale-encoding-mapping elements 
         */
	public int sizeLocaleEncodingMapping();
        /** Adds locale-encoding-mapping element.
         * @param valueInterface locale-encoding-mapping element (LocaleEncodingMapping object)
         * @return index of new locale-encoding-mapping
         */
	public int addLocaleEncodingMapping(org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping valueInterface);
        /** Removes locale-encoding-mapping element.
         * @param valueInterface locale-encoding-mapping element (LocaleEncodingMapping object)
         * @return index of the removed locale-encoding-mapping
         */
	public int removeLocaleEncodingMapping(org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping valueInterface);

}
