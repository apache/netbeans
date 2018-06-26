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
 * Generated interface for WebResourceCollection element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface WebResourceCollection extends CommonDDBean, DescriptionInterface {
        /** Setter for web-resource-name property.
         * @param value property value
         */
	public void setWebResourceName(java.lang.String value);
        /** Getter for web-resource-name property.
         * @return property value 
         */
	public java.lang.String getWebResourceName();
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
        /** Returns size of url-pattern properties.
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
        /** Setter for http-method property.
         * @param index position in the array of http-methods
         * @param value property value 
         */
	public void setHttpMethod(int index, java.lang.String value);
        /** Getter for http-method property.
         * @param index position in the array of http-methods
         * @return property value 
         */
	public java.lang.String getHttpMethod(int index);
        /** Setter for http-method property.
         * @param index position in the array of http-methods
         * @param value array of http-method properties
         */
	public void setHttpMethod(java.lang.String[] value);
        /** Getter for http-method property.
         * @return array of http-method properties
         */
	public java.lang.String[] getHttpMethod();
        /** Returns size of http-method properties.
         * @return number of http-method properties 
         */
	public int sizeHttpMethod();
        /** Adds http-method property.
         * @param value http-method property
         * @return index of new http-method
         */
	public int addHttpMethod(java.lang.String value);
        /** Removes http-method property.
         * @param value http-method property
         * @return index of the removed http-method
         */
	public int removeHttpMethod(java.lang.String value);

}
