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
 * Generated interface for LoginConfig element.
 *
 *<p><b><span style="color:red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></span></b>
 *</p>
 */
public interface LoginConfig extends CommonDDBean, CreateCapability {
        /** Setter for auth-method property.
         * @param value property value
         */
	public void setAuthMethod(java.lang.String value);
        /** Getter for auth-method property.
         * @return property value 
         */
	public java.lang.String getAuthMethod();
        /** Setter for realm-name property.
         * @param value property value
         */
	public void setRealmName(java.lang.String value);
        /** Getter for realm-name property.
         * @return property value 
         */
	public java.lang.String getRealmName();
        /** Setter for form-login-config element.
         * @param valueInterface form-login-config element (FormLoginConfig object)
         */
	public void setFormLoginConfig(org.netbeans.modules.j2ee.dd.api.web.FormLoginConfig valueInterface);
        /** Getter for form-login-config element.
         * @return form-login-config element (FormLoginConfig object)
         */
	public org.netbeans.modules.j2ee.dd.api.web.FormLoginConfig getFormLoginConfig();

}
