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
 * Generated interface for JspPropertyGroup element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface JspPropertyGroup extends CommonDDBean, ComponentInterface {
        /** Setter for url-pattern property.
         * @param index position in the array of url-patterns
         * @param value property value
         */
	public void setUrlPattern(int index, java.lang.String value);
        /** Getter for url-pattern property.
         * @param index position in the array of url-patterns
         * @return property value 
         */
	public java.lang.String getUrlPattern(int index);
        /** Setter for url-pattern property.
         * @param index position in the array of url-patterns
         * @param value array of url-pattern properties
         */
	public void setUrlPattern(java.lang.String[] value);
        /** Getter for url-pattern property.
         * @return array of url-pattern properties
         */
	public java.lang.String[] getUrlPattern();
        /** Returns number of url-pattern properties.
         * @return number of url-pattern properties 
         */
	public int sizeUrlPattern();
        /** Adds url-pattern property.
         * @param value url-pattern property
         * @return index of new url-pattern
         */
	public int addUrlPattern(java.lang.String value);
        /** Removes url-pattern property.
         * @param value url-pattern property
         * @return index of the removed url-pattern
         */
	public int removeUrlPattern(java.lang.String value);
        /** Setter for el-ignored property.
         * @param value property value
         */
	public void setElIgnored(boolean value);
        /** Getter for el-ignored property.
         * @return property value 
         */
	public boolean isElIgnored();
        /** Setter for page-encoding property.
         * @param value property value
         */
	public void setPageEncoding(java.lang.String value);
        /** Getter for page-encoding property.
         * @return property value 
         */
	public java.lang.String getPageEncoding();
        /** Setter for scripting-invalid property.
         * @param value property value
         */
	public void setScriptingInvalid(boolean value);
        /** Getter for scripting-invalid property.
         * @return property value 
         */
	public boolean isScriptingInvalid();
        /** Setter for is-xml property.
         * @param value property value
         */
	public void setIsXml(boolean value);
        /** Getter for is-xml property.
         * @return property value 
         */
	public boolean isIsXml();
        /** Setter for include-prelude property.
         * @param index position in the array of include-preludes
         * @param value property value 
         */
	public void setIncludePrelude(int index, java.lang.String value);
        /** Getter for include-prelude property.
         * @param index position in the array of include-preludes
         * @return property value 
         */
	public java.lang.String getIncludePrelude(int index);
        /** Setter for include-prelude property.
         * @param index position in the array of include-preludes
         * @param value array of include-prelude properties
         */
	public void setIncludePrelude(java.lang.String[] value);
        /** Getter for include-prelude property.
         * @return array of include-prelude properties
         */
	public java.lang.String[] getIncludePrelude();
        /** Returns number of include-prelude properties.
         * @return number of include-prelude properties 
         */
	public int sizeIncludePrelude();
        /** Adds include-prelude property.
         * @param value include-prelude property
         * @return index of new include-prelude
         */
	public int addIncludePrelude(java.lang.String value);
        /** Removes include-prelude property.
         * @param value include-prelude property
         * @return index of the removed include-prelude
         */
	public int removeIncludePrelude(java.lang.String value);
        /** Setter for include-coda property.
         * @param index position in the array of include-codas
         * @param value property value 
         */
	public void setIncludeCoda(int index, java.lang.String value);
        /** Getter for include-coda property.
         * @param index position in the array of include-codas
         * @return property value 
         */
	public java.lang.String getIncludeCoda(int index);
        /** Setter for include-coda property.
         * @param index position in the array of include-codas
         * @param value array of include-coda properties
         */
	public void setIncludeCoda(java.lang.String[] value);
        /** Getter for include-coda property.
         * @return array of include-coda properties
         */
	public java.lang.String[] getIncludeCoda();
        /** Returns number of include-coda properties.
         * @return number of include-coda properties 
         */
	public int sizeIncludeCoda();
        /** Adds include-coda property.
         * @param value include-coda property
         * @return index of new include-coda
         */
	public int addIncludeCoda(java.lang.String value);
        /** Removes include-coda property.
         * @param value include-coda property
         * @return index of the removed include-coda
         */
	public int removeIncludeCoda(java.lang.String value);

}
