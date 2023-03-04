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
 * SunEjbJar.java
 *
 * Created on November 17, 2004, 4:26 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;
import org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping;
/**
 *
 * @author  Nitya Doraisamy
 */
public interface SunEjbJar extends org.netbeans.modules.j2ee.sun.dd.api.RootInterface {
    public static final String VERSION_3_1_1 = "3.11"; //NOI18N
    public static final String VERSION_3_1_0 = "3.10"; //NOI18N
    public static final String VERSION_3_0_1 = "3.01"; //NOI18N
    public static final String VERSION_3_0_0 = "3.00"; //NOI18N
    public static final String VERSION_2_1_1 = "2.11"; //NOI18N
    public static final String VERSION_2_1_0 = "2.10"; //NOI18N
    public static final String VERSION_2_0_0 = "2.00"; //NOI18N
        
    public static final String SECURITY_ROLE_MAPPING = "SecurityRoleMapping";	// NOI18N
    public static final String ENTERPRISE_BEANS = "EnterpriseBeans";	// NOI18N
        
    public SecurityRoleMapping[] getSecurityRoleMapping();
    public SecurityRoleMapping getSecurityRoleMapping(int index);
    public void setSecurityRoleMapping(SecurityRoleMapping[] value);
    public void setSecurityRoleMapping(int index, SecurityRoleMapping value);
    public int addSecurityRoleMapping(SecurityRoleMapping value);
    public int removeSecurityRoleMapping(SecurityRoleMapping value);
    public int sizeSecurityRoleMapping();
    public SecurityRoleMapping newSecurityRoleMapping();
    
    public EnterpriseBeans getEnterpriseBeans();
    public void setEnterpriseBeans(EnterpriseBeans value);
    public EnterpriseBeans newEnterpriseBeans();
    
    
}
