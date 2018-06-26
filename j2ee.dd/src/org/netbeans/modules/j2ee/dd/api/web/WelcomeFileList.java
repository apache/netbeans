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
/**
 * Generated interface for WelcomeFileList element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface WelcomeFileList extends org.netbeans.modules.j2ee.dd.api.common.CommonDDBean {
        /** Setter for welcome-file property.
         * @param index position in the array of welcome-files
         * @param value property value
         */
	public void setWelcomeFile(int index, java.lang.String value);
        /** Getter for welcome-file property.
         * @param index position in the array of welcome-files
         * @return property value 
         */
	public java.lang.String getWelcomeFile(int index);
        /** Setter for welcome-file property.
         * @param index position in the array of welcome-files
         * @param value array of welcome-file properties
         */
	public void setWelcomeFile(java.lang.String[] value);
        /** Getter for welcome-file property.
         * @return array of welcome-file properties
         */
	public java.lang.String[] getWelcomeFile();
        /** Returns size of welcome-file properties.
         * @return number of welcome-file properties 
         */
	public int sizeWelcomeFile();
        /** Adds welcome-file property.
         * @param value welcome-file property
         * @return index of new welcome-file
         */
	public int addWelcomeFile(java.lang.String value);
        /** Removes welcome-file property.
         * @param value welcome-file property
         * @return index of the removed welcome-file
         */
	public int removeWelcomeFile(java.lang.String value);

}
