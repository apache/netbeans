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
/*
 * ConstraintField.java
 *
 * Created on November 15, 2004, 4:26 PM
 */
package org.netbeans.modules.j2ee.sun.dd.api.web;

public interface ConstraintField extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

        public static final String NAME = "Name";	// NOI18N
	public static final String SCOPE = "Scope";	// NOI18N
	public static final String CACHEONMATCH = "CacheOnMatch";	// NOI18N
	public static final String CACHEONMATCHFAILURE = "CacheOnMatchFailure";	// NOI18N
	public static final String CONSTRAINT_FIELD_VALUE = "ConstraintFieldValue";	// NOI18N
	public static final String CONSTRAINTFIELDVALUEMATCHEXPR = "ConstraintFieldValueMatchExpr";	// NOI18N
	public static final String CONSTRAINTFIELDVALUECACHEONMATCH = "ConstraintFieldValueCacheOnMatch";	// NOI18N
	public static final String CONSTRAINTFIELDVALUECACHEONMATCHFAILURE = "ConstraintFieldValueCacheOnMatchFailure";	// NOI18N

        /** Setter for name attribute 
         * @param value attribute value
         */
	public void setName(java.lang.String value);
        /** Getter for name attribute 
         * @return attribute value
         */    
	public java.lang.String getName();
        /** Setter for scope attribute 
         * @param value attribute value
         */
	public void setScope(java.lang.String value);
        /** Getter for scope attribute 
         * @return attribute value
         */    
	public java.lang.String getScope();
        /** Setter for cache-on-match attribute 
         * @param value attribute value
         */
	public void setCacheOnMatch(java.lang.String value);
        /** Getter for cache-on-match attribute 
         * @return attribute value
         */
	public java.lang.String getCacheOnMatch();
        /** Setter for cache-on-match-failure attribute 
         * @param value attribute value
         */
	public void setCacheOnMatchFailure(java.lang.String value);
        /** Getter for cache-on-match-failure attribute 
         * @return attribute value
         */
	public java.lang.String getCacheOnMatchFailure();

	public void setConstraintFieldValue(int index, String value);
	public String getConstraintFieldValue(int index);
	public int sizeConstraintFieldValue();
	public void setConstraintFieldValue(String[] value);
	public String[] getConstraintFieldValue();
	public int addConstraintFieldValue(String value);
	public int removeConstraintFieldValue(String value);

        /** Setter for match-expr attribute of constraint-field-value
         * @param value attribute value
         */
        public void setConstraintFieldValueMatchExpr(int index, java.lang.String value);
        /** Getter for match-expr attribute of constraint-field-value
         * @return attribute value
         */
	public java.lang.String getConstraintFieldValueMatchExpr(int index);

	public int sizeConstraintFieldValueMatchExpr();
        /** Setter for cache-on-match attribute of constraint-field-value
         * @param value attribute value
         */
	public void setConstraintFieldValueCacheOnMatch(int index, java.lang.String value);
        /** Getter for cache-on-match attribute of constraint-field-value
         * @return attribute value
         */
	public java.lang.String getConstraintFieldValueCacheOnMatch(int index);

	public int sizeConstraintFieldValueCacheOnMatch();
        /** Setter for cache-on-match-failure attribute of constraint-field-value
         * @param value attribute value
         */
	public void setConstraintFieldValueCacheOnMatchFailure(int index, java.lang.String value);
        /** Getter for cache-on-match-failure attribute of constraint-field-value
         * @return attribute value
         */
	public java.lang.String getConstraintFieldValueCacheOnMatchFailure(int index);

	public int sizeConstraintFieldValueCacheOnMatchFailure();

}
