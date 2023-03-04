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
 * SunApplication.java
 *
 * Created on November 21, 2004, 12:47 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.app;

import org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping;

public interface SunApplication extends org.netbeans.modules.j2ee.sun.dd.api.RootInterface {

        public static final String VERSION_6_0_1 = "6.01"; //NOI18N
        public static final String VERSION_6_0_0 = "6.00"; //NOI18N
        public static final String VERSION_5_0_0 = "5.00"; //NOI18N
        public static final String VERSION_1_4_0 = "1.40"; //NOI18N
        public static final String VERSION_1_3_0 = "1.30"; //NOI18N
        
        public static final String WEB = "Web";	// NOI18N
	public static final String PASS_BY_REFERENCE = "PassByReference";	// NOI18N
	public static final String UNIQUE_ID = "UniqueId";	// NOI18N
	public static final String SECURITY_ROLE_MAPPING = "SecurityRoleMapping";	// NOI18N
	public static final String REALM = "Realm";	// NOI18N
        
	public void setWeb(int index, Web value);
	public Web getWeb(int index);
	public int sizeWeb();
	public void setWeb(Web[] value);
	public Web[] getWeb();
	public int addWeb(org.netbeans.modules.j2ee.sun.dd.api.app.Web value);
	public int removeWeb(org.netbeans.modules.j2ee.sun.dd.api.app.Web value);
	public Web newWeb();

        /** Setter for pass-by-reference property
        * @param value property value
        */
	public void setPassByReference(String value);
        /** Getter for pass-by-reference property.
        * @return property value
        */
	public String getPassByReference();
        /** Setter for unique-id property
        * @param value property value
        */
	public void setUniqueId(String value);
        /** Getter for unique-id property.
        * @return property value
        */
	public String getUniqueId();

	public void setSecurityRoleMapping(int index, SecurityRoleMapping value);
	public SecurityRoleMapping getSecurityRoleMapping(int index);
	public int sizeSecurityRoleMapping();
	public void setSecurityRoleMapping(SecurityRoleMapping[] value);
	public SecurityRoleMapping[] getSecurityRoleMapping();
	public int addSecurityRoleMapping(SecurityRoleMapping value);
	public int removeSecurityRoleMapping(SecurityRoleMapping value);
	public SecurityRoleMapping newSecurityRoleMapping();

        /** Setter for realm property
        * @param value property value
        */
	public void setRealm(String value);
        /** Getter for realm property.
        * @return property value
        */
	public String getRealm();

}
