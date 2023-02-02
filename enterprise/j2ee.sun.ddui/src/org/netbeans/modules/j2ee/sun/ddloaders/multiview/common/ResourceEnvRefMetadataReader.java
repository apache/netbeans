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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.common;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;


/**
 *
 * @author Peter Williams
 */
public class ResourceEnvRefMetadataReader extends CommonBeanReader {

    private String parentName;
    
    public ResourceEnvRefMetadataReader(final String parentName) {
        super(DDBinding.PROP_RESOURCE_ENV_REF);
        this.parentName = parentName;
    }
    
    /** For normalizing data structures within /ejb-jar graph.
     *    /ejb-jar -> -> /ejb-jar/enterprise-beans/session[ejb-name="xxx"]
     * (finds message-driven and entity as well)
     * 
     * TODO This mechanism will probably need optimization and caching to perform
     * for larger files.
     */
    @Override
    protected CommonDDBean normalizeParent(CommonDDBean parent) {
        if(parentName != null && parent instanceof EjbJar) {
            parent = findEjbByName((EjbJar) parent, parentName);
        }
        return parent;
    }
    
    /** Maps interesting fields from resource-env-ref descriptor to a multi-level property map.
     * 
     * @return Map<String, Object> where Object is either a String value or nested map
     *  with the same structure (and thus ad infinitum)
     */
    public Map<String, Object> genProperties(CommonDDBean [] beans) {
        Map<String, Object> result = null;
        if(beans instanceof ResourceEnvRef []) {
            ResourceEnvRef [] resourceEnvRefs = (ResourceEnvRef []) beans;
            for(ResourceEnvRef resourceEnvRef: resourceEnvRefs) {
                String resourceEnvRefName = resourceEnvRef.getResourceEnvRefName();
                if(Utils.notEmpty(resourceEnvRefName)) {
                    if(result == null) {
                        result = new HashMap<>();
                    }
                    Map<String, Object> resourceEnvRefMap = new HashMap<>();
                    result.put(resourceEnvRefName, resourceEnvRefMap);
                    resourceEnvRefMap.put(DDBinding.PROP_NAME, resourceEnvRefName);
                }
            }
        }
        return result;
    }
}
