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
 * Generated interface for JspConfig element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface JspConfig extends CommonDDBean, FindCapability, CreateCapability {
        /** Setter for taglib element.
         * @param index position in the array of elements
         * @param valueInterface taglib element (Taglib object)
         */
	public void setTaglib(int index, org.netbeans.modules.j2ee.dd.api.web.Taglib valueInterface);
        /** Getter for taglib element.
         * @param index position in the array of elements
         * @return taglib element (Taglib object)
         */
	public org.netbeans.modules.j2ee.dd.api.web.Taglib getTaglib(int index);
        /** Setter for taglib elements.
         * @param value array of taglib elements (Taglib objects)
         */
	public void setTaglib(org.netbeans.modules.j2ee.dd.api.web.Taglib[] value);
        /** Getter for taglib elements.
         * @return array of taglib elements (Taglib objects)
         */
	public org.netbeans.modules.j2ee.dd.api.web.Taglib[] getTaglib();
        /** Returns number of taglib elements.
         * @return number of taglib elements 
         */
	public int sizeTaglib();
        /** Adds taglib element.
         * @param valueInterface taglib element (Taglib object)
         * @return index of new taglib
         */
	public int addTaglib(org.netbeans.modules.j2ee.dd.api.web.Taglib valueInterface);
        /** Removes taglib element.
         * @param valueInterface taglib element (Taglib object)
         * @return index of the removed taglib
         */
	public int removeTaglib(org.netbeans.modules.j2ee.dd.api.web.Taglib valueInterface);
        /** Setter for jsp-property-group element.
         * @param index position in the array of elements
         * @param valueInterface jsp-property-group element (JspPropertyGroup object)
         */
	public void setJspPropertyGroup(int index, org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup valueInterface);
        /** Getter for jsp-property-group element.
         * @param index position in the array of elements
         * @return jsp-property-group element (JspPropertyGroup object)
         */
	public org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup getJspPropertyGroup(int index);
        /** Setter for jsp-property-group elements.
         * @param value array of jsp-property-group elements (JspPropertyGroup objects)
         */
	public void setJspPropertyGroup(org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup[] value);
        /** Getter for jsp-property-group elements.
         * @return array of jsp-property-group elements (JspPropertyGroup objects)
         */
	public org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup[] getJspPropertyGroup();
        /** Returns number of jsp-property-group elements.
         * @return number of jsp-property-group elements 
         */
	public int sizeJspPropertyGroup();
        /** Adds jsp-property-group element.
         * @param valueInterface jsp-property-group element (JspPropertyGroup object)
         * @return index of new jsp-property-group
         */
	public int addJspPropertyGroup(org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup valueInterface);
        /** Removes jsp-property-group element.
         * @param valueInterface jsp-property-group element (JspPropertyGroup object)
         * @return index of the removed jsp-property-group
         */
	public int removeJspPropertyGroup(org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup valueInterface);

}
