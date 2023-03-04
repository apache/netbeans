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
 * CacheMapping.java
 *
 * Created on November 15, 2004, 4:26 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.web;

import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

public interface CacheMapping extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

        public static final String SERVLET_NAME = "ServletName";	// NOI18N
	public static final String URL_PATTERN = "UrlPattern";	// NOI18N
	public static final String CACHE_HELPER_REF = "CacheHelperRef";	// NOI18N
	public static final String DISPATCHER = "Dispatcher";	// NOI18N
	public static final String TIMEOUT = "Timeout";	// NOI18N
	public static final String TIMEOUTNAME = "TimeoutName";	// NOI18N
	public static final String TIMEOUTSCOPE = "TimeoutScope";	// NOI18N
	public static final String REFRESH_FIELD = "RefreshField";	// NOI18N
	public static final String REFRESHFIELDNAME = "RefreshFieldName";	// NOI18N
	public static final String REFRESHFIELDSCOPE = "RefreshFieldScope";	// NOI18N
	public static final String HTTP_METHOD = "HttpMethod";	// NOI18N
	public static final String KEY_FIELD = "KeyField";	// NOI18N
	public static final String KEYFIELDNAME = "KeyFieldName";	// NOI18N
	public static final String KEYFIELDSCOPE = "KeyFieldScope";	// NOI18N
	public static final String CONSTRAINT_FIELD = "ConstraintField";	// NOI18N

    
        /** Setter for servlet-name property
         * @param value property value
         */
	public void setServletName(String value);
        /** Getter for servlet-name property.
         * @return property value
         */
	public String getServletName();
        /** Setter for url-pattern property
         * @param value property value
         */
	public void setUrlPattern(String value);
        /** Getter for url-pattern property.
         * @return property value
         */
	public String getUrlPattern();
        /** Setter for cache-helper-ref property
         * @param value property value
         */
	public void setCacheHelperRef(String value);
        /** Getter for cache-helper-ref property.
         * @return property value
         */
	public String getCacheHelperRef();
        
	public void setDispatcher(int index, String value) throws VersionNotSupportedException;
	public String getDispatcher(int index) throws VersionNotSupportedException;
	public int sizeDispatcher() throws VersionNotSupportedException;
	public void setDispatcher(String[] value) throws VersionNotSupportedException;
	public String[] getDispatcher() throws VersionNotSupportedException;
	public int addDispatcher(String value) throws VersionNotSupportedException;
	public int removeDispatcher(String value) throws VersionNotSupportedException;

        /** Setter for timeout property
         * @param value property value
         */
	public void setTimeout(String value);
        /** Getter for timeout property.
         * @return property value
         */
	public String getTimeout();
        /** Setter for name attribute of timeout
         * @param value attribute value
         */
	public void setTimeoutName(java.lang.String value);
        /** Getter for name attribute of timeout
         * @return attribute value
         */
	public java.lang.String getTimeoutName();
        /** Setter for scope attribute of timeout
         * @param value attribute value
         */
	public void setTimeoutScope(java.lang.String value);
        /** Getter for scope attribute of timeout
         * @return attribute value
         */
	public java.lang.String getTimeoutScope();
        /** Setter for refresh-field property
         * @param value property value
         */
	public void setRefreshField(boolean value);
        /** Check for refresh-field property
         * @return boolean value
         */
	public boolean isRefreshField();
        /** Setter for name attribute of refresh-field
         * @param value attribute value
         */
	public void setRefreshFieldName(java.lang.String value);
        /** Getter for name attribute of refresh-field
         * @return attribute value
         */
	public java.lang.String getRefreshFieldName();
        /** Setter for scope attribute of refresh-field
         * @param value attribute value
         */
	public void setRefreshFieldScope(java.lang.String value);
        /** Getter for scope attribute of refresh-field
         * @return attribute value
         */
	public java.lang.String getRefreshFieldScope();
        
        public void setHttpMethod(int index, String value);
	public String getHttpMethod(int index);     
	public int sizeHttpMethod();
	public void setHttpMethod(String[] value);
	public String[] getHttpMethod();
	public int addHttpMethod(String value);
	public int removeHttpMethod(String value);

        public void setKeyField(int index, boolean value);
	public boolean isKeyField(int index);
	public int sizeKeyField();
	public void setKeyField(boolean[] value);
	public boolean[] getKeyField();
	public int addKeyField(boolean value);
	public int removeKeyField(boolean value);
	public void removeKeyField(int index);

        /** Setter for name attribute of key-field
         * @param value attribute value
         */
        public void setKeyFieldName(int index, java.lang.String value);
        /** Getter for name attribute of key-field
         * @return attribute value
         */
	public java.lang.String getKeyFieldName(int index);

	public int sizeKeyFieldName();
        /** Setter for scope attribute of key-field
         * @param value attribute value
         */
	public void setKeyFieldScope(int index, java.lang.String value);
        /** Getter for scope attribute of key-field
         * @return attribute value
         */
	public java.lang.String getKeyFieldScope(int index);

	public int sizeKeyFieldScope();
        
	public void setConstraintField(int index, ConstraintField value); 
	public ConstraintField getConstraintField(int index);
	public int sizeConstraintField();
	public void setConstraintField(ConstraintField[] value);
	public ConstraintField[] getConstraintField();
	public int addConstraintField(ConstraintField value);
	public int removeConstraintField(ConstraintField value);
	public ConstraintField newConstraintField();

}
