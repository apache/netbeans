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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.common;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;


/**
 *
 * @author Peter Williams
 */
public class SecurityRoleMetadataReader extends CommonBeanReader {

    public SecurityRoleMetadataReader() {
        super(DDBinding.PROP_SECURITY_ROLE);
    }
    
    /** For normalizing data structures within /ejb-jar graph.
     *    /ejb-jar -> /ejb-jar/assembly-descriptor
     */
    @Override
    protected CommonDDBean normalizeParent(CommonDDBean parent) {
        if(parent instanceof EjbJar) {
            parent = ((EjbJar) parent).getSingleAssemblyDescriptor();
        }
        return parent;
    }
    
    /** Maps interesting fields from security-role descriptor to a multi-level property map.
     * 
     * @return Map<String, Object> where Object is either a String value or nested map
     *  with the same structure (and thus ad infinitum)
     */
    public Map<String, Object> genProperties(CommonDDBean [] beans) {
        Map<String, Object> result = null;
        if(beans instanceof SecurityRole []) {
            SecurityRole [] roles = (SecurityRole []) beans;
            for(SecurityRole securityRole: roles) {
                String securityRoleName = securityRole.getRoleName();
                if(Utils.notEmpty(securityRoleName)) {
                    if(result == null) {
                        result = new HashMap<String, Object>();
                    }
                    Map<String, Object> securityRoleMap = new HashMap<String, Object>();
                    result.put(securityRoleName, securityRoleMap);
                    securityRoleMap.put(DDBinding.PROP_NAME, securityRoleName);
                }
            }
        }
        return result;
    }
}
