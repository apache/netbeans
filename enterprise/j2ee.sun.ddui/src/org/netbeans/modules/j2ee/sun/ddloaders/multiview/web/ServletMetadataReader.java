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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.web;

import org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb.*;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.common.RunAs;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding;

/**
 *
 * @author Peter Williams
 */
public class ServletMetadataReader implements MetadataModelAction<WebAppMetadata, Map<String, Object>> {

    /** Entry point to generate map from standard descriptor
     */
    public static Map<String, Object> readDescriptor(WebApp webApp) {
        return genProperties(webApp);
    }
    
    /** Entry point to generate map from annotation metadata
     */
    public Map<String, Object> run(WebAppMetadata metadata) throws Exception {
        return genProperties(metadata.getRoot());
    }
    
    /** Maps interesting fields from ejb-jar descriptor to a multi-level property map.
     * 
     * @return Map<String, Object> where Object is either a String value or nested map
     *  with the same structure (and thus ad infinitum)
     */
    private static Map<String, Object> genProperties(WebApp webApp) {
        Map<String, Object> data = new HashMap<String, Object>();
        if(webApp != null) {
            Servlet [] servlets = webApp.getServlet();
            if(servlets != null) {
                for(Servlet servlet: servlets) {
                    String servletName = servlet.getServletName();
                    if(Utils.notEmpty(servletName)) {
                        Map<String, Object> servletMap = new HashMap<String, Object>();
                        data.put(servletName, servletMap);
                        servletMap.put(DDBinding.PROP_NAME, servletName);

                        RunAs runAs = servlet.getRunAs();
                        if(runAs != null) {
                            String roleName = runAs.getRoleName();
                            if(Utils.notEmpty(roleName)) {
                                servletMap.put(DDBinding.PROP_RUNAS_ROLE, roleName);
                            }
                        }
                    }
                }
            }
        }
        
        return data.size() > 0 ? data : null;
    }

}
