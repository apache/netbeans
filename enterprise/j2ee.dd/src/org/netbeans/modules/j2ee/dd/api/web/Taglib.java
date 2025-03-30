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
 * Generated interface for Taglib element.
 *
 *<p><b><span style="color:red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></span></b>
 *</p>
 */
public interface Taglib extends org.netbeans.modules.j2ee.dd.api.common.CommonDDBean {
        /** Setter for taglib-uri property.
         * @param value property value
         */
	public void setTaglibUri(java.lang.String value);
        /** Getter for taglib-uri property.
         * @return property value 
         */
	public java.lang.String getTaglibUri();
        /** Setter for talib-location property.
         * @param value property value
         */
	public void setTaglibLocation(java.lang.String value);
        /** Getter for talib-location property.
         * @return property value 
         */
	public java.lang.String getTaglibLocation();

}
