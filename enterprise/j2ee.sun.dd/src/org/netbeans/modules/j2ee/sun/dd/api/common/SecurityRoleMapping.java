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
 * SecurityRoleMapping.java
 *
 * Created on November 17, 2004, 4:29 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface SecurityRoleMapping extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    public static final String VERSION_SERVER_8_0 = "Server 8.0"; 
    
    public static final String ROLE_NAME = "RoleName";	// NOI18N
    public static final String PRINCIPAL_NAME = "PrincipalName";	// NOI18N
    public static final String PRINCIPALNAMECLASSNAME = "PrincipalNameClassName";	// NOI18N
    public static final String GROUP_NAME = "GroupName";	// NOI18N

    public void setRoleName(java.lang.String value);
    public java.lang.String getRoleName();

    public String[] getPrincipalName();
    public String getPrincipalName(int index);
    public void setPrincipalName(String[] value);
    public void setPrincipalName(int index, String value);
    public int addPrincipalName(String value);
    public int removePrincipalName(String value);
    public int sizePrincipalName();
    
	public void setPrincipalNameClassName(int index, String value) throws VersionNotSupportedException;
	public String getPrincipalNameClassName(int index) throws VersionNotSupportedException;
	public int sizePrincipalNameClassName() throws VersionNotSupportedException;
    
    public String[] getGroupName();
    public String getGroupName(int index);
    public void setGroupName(String[] value);
    public void setGroupName(int index, String value);
    public int addGroupName(String value);
    public int removeGroupName(String value);
    public int sizeGroupName();
    
}
