/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
