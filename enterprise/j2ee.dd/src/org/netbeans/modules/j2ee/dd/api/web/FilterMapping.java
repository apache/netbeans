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
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
/**
 * Generated interface for FilterMapping element.
 *
 *<p><b><font color="red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></font></b>
 *</p>
 */
public interface FilterMapping extends org.netbeans.modules.j2ee.dd.api.common.CommonDDBean {
        /** Setter for filter-name property.
         * @param value property value
         */
	public void setFilterName(java.lang.String value);
        /** Getter for filter-name property.
         * @return property value 
         */
	public java.lang.String getFilterName();
        /** Setter for url-pattern property.
         * @param value property value
         */
	public void setUrlPattern(java.lang.String value);
	public void setUrlPatterns(java.lang.String[] values) throws VersionNotSupportedException;

        /** Getter for url-pattern property.
         * @return property value 
         */
	public java.lang.String getUrlPattern();
	public java.lang.String[] getUrlPatterns() throws VersionNotSupportedException;

        /** Setter for servlet-name property.
         * @param value property value
         */
	public void setServletName(java.lang.String value);
	public void setServletNames(java.lang.String[] value) throws VersionNotSupportedException;

        /** Getter for servlet-name property.
         * @return property value 
         */
	public java.lang.String getServletName();
	public java.lang.String[] getServletNames() throws VersionNotSupportedException;

        /** Setter for dispatcher property.
         * @param index position in the array of dispatchers
         * @param value property value 
         */
	public void setDispatcher(int index, java.lang.String value) throws VersionNotSupportedException;
        /** Getter for dispatcher property.
         * @param index position in the array of dispatchers
         * @return property value 
         */
	public java.lang.String getDispatcher(int index) throws VersionNotSupportedException;
        /** Setter for dispatcher property.
         * @param value array of dispatcher properties
         */
	public void setDispatcher(java.lang.String[] value) throws VersionNotSupportedException;
        /** Getter for dispatcher property.
         * @return array of dispatcher properties
         */
	public java.lang.String[] getDispatcher() throws VersionNotSupportedException;
        /** Returns size of dispatcher properties.
         * @return number of dispatcher properties 
         */
	public int sizeDispatcher() throws VersionNotSupportedException;
        /** Adds dispatcher property.
         * @param value dispatcher property
         * @return index of new dispatcher
         */
	public int addDispatcher(java.lang.String value) throws VersionNotSupportedException;
        /** Removes dispatcher property.
         * @param value dispatcher property
         * @return index of the removed dispatcher
         */
	public int removeDispatcher(java.lang.String value) throws VersionNotSupportedException;

}
