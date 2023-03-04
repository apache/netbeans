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
 * SecurityMap.java
 *
 * Created on November 21, 2004, 2:33 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.serverresources;
/**
 * @author Nitya Doraisamy
 */
public interface SecurityMap {

        public static final String NAME = "Name";	// NOI18N
	public static final String PRINCIPAL = "Principal";	// NOI18N
	public static final String USER_GROUP = "UserGroup";	// NOI18N
	public static final String BACKEND_PRINCIPAL = "BackendPrincipal";	// NOI18N
	public static final String BACKENDPRINCIPALUSERNAME = "BackendPrincipalUserName";	// NOI18N
	public static final String BACKENDPRINCIPALPASSWORD = "BackendPrincipalPassword";	// NOI18N
        
	public void setName(java.lang.String value);

	public java.lang.String getName();

	public void setPrincipal(int index, String value);

	public String getPrincipal(int index);

	public int sizePrincipal();

	public void setPrincipal(String[] value);

	public String[] getPrincipal();

	public int addPrincipal(String value);

	public int removePrincipal(String value);

	public void setUserGroup(int index, String value);

	public String getUserGroup(int index);

	public int sizeUserGroup();

	public void setUserGroup(String[] value);

	public String[] getUserGroup();

	public int addUserGroup(String value);

	public int removeUserGroup(String value);

	public void setBackendPrincipal(boolean value);

	public boolean isBackendPrincipal();

	public void setBackendPrincipalUserName(java.lang.String value);

	public java.lang.String getBackendPrincipalUserName();

	public void setBackendPrincipalPassword(java.lang.String value);

	public java.lang.String getBackendPrincipalPassword();

}
