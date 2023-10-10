/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
