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
 * CacheHelper.java
 *
 * Created on November 15, 2004, 4:26 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.web;

public interface CacheHelper extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

        public static final String NAME = "Name";	// NOI18N
	public static final String CLASSNAME = "ClassName";	// NOI18N
	public static final String PROPERTY = "WebProperty";	// NOI18N

        /** Setter for name attribute.
         * @param value attribute value
         */
	public void setName(java.lang.String value);
         /** Getter for name attribute.
         * @return attribute value
         */
	public java.lang.String getName();
        /** Setter for class-name attribute.
         * @param value attribute value
         */
	public void setClassName(java.lang.String value);
         /** Getter for class-name attribute.
         * @return attribute value
         */
	public java.lang.String getClassName();

	public void setWebProperty(int index, WebProperty value);
	public WebProperty getWebProperty(int index);
	public int sizeWebProperty();
	public void setWebProperty(WebProperty[] value);
	public WebProperty[] getWebProperty();
	public int addWebProperty(WebProperty value);
	public int removeWebProperty(WebProperty value);
	public WebProperty newWebProperty();

}
