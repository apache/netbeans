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
