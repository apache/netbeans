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
/**
 * Generated interface for ErrorPage element.
 *
 *<p><b><span style="color:red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></span></b>
 *</p>
 */
public interface ErrorPage extends org.netbeans.modules.j2ee.dd.api.common.CommonDDBean {
        /** Setter for error-code property.
         * @param value property value
         */
	public void setErrorCode(java.lang.Integer value);
        /** Getter for error-code property.
         * @return property value 
         */
	public java.lang.Integer getErrorCode();
        /** Setter for exception-type property.
         * @param value property value
         */
	public void setExceptionType(java.lang.String value);
        /** Getter for exception-type property.
         * @return property value 
         */
	public java.lang.String getExceptionType();
        /** Setter for location property.
         * @param value property value
         */
	public void setLocation(java.lang.String value);
        /** Getter for location property.
         * @return property value 
         */
	public java.lang.String getLocation();

}
