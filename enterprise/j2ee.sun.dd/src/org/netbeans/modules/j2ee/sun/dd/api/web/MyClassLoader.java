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
 * MyClassLoader.java
 *
 * Created on November 15, 2004, 4:26 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.web;

import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

public interface MyClassLoader extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

        public static final String EXTRACLASSPATH = "ExtraClassPath";	// NOI18N
	public static final String DELEGATE = "Delegate";	// NOI18N
	public static final String DYNAMICRELOADINTERVAL = "DynamicReloadInterval";	// NOI18N
	public static final String PROPERTY = "WebProperty";	// NOI18N

         /** Setter for extra-class-path attribute
         * @param value attribute value
         */
	public void setExtraClassPath(java.lang.String value);
        /** Getter for extra-class-path attribute.
         * @return attribute value
         */
	public java.lang.String getExtraClassPath();
         /** Setter for delegate attribute
         * @param value attribute value
         */
	public void setDelegate(java.lang.String value);
        /** Getter for delegate attribute.
         * @return attribute value
         */
	public java.lang.String getDelegate();
         /** Setter for dynamic-reload-interval attribute
         * @param value attribute value
         */
	public void setDynamicReloadInterval(java.lang.String value) throws VersionNotSupportedException;
        /** Getter for dynamic-reload-interval attribute.
         * @return attribute value
         */
	public java.lang.String getDynamicReloadInterval() throws VersionNotSupportedException;

	public void setWebProperty(int index, WebProperty value) throws VersionNotSupportedException;
	public WebProperty getWebProperty(int index) throws VersionNotSupportedException;
	public int sizeWebProperty() throws VersionNotSupportedException;
	public void setWebProperty(WebProperty[] value) throws VersionNotSupportedException;
	public WebProperty[] getWebProperty() throws VersionNotSupportedException;
	public int addWebProperty(WebProperty value) throws VersionNotSupportedException;
	public int removeWebProperty(WebProperty value) throws VersionNotSupportedException;
	public WebProperty newWebProperty() throws VersionNotSupportedException;

}
