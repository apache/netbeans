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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.webservice;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;


/**
 *
 * @author Peter Williams
 */
public class PortComponentMetadataReader extends CommonBeanReader {

    private String parentName;
    
    public PortComponentMetadataReader(final String parentName) {
        super(DDBinding.PROP_PORTCOMPONENT);
        if(parentName != null && parentName.startsWith("WSServlet_")) {
            this.parentName = parentName.substring(10);
        } else {
            this.parentName = parentName;
        }
    }
    
    /** For normalizing data structures
     *    /webservices -> -> /webservices/webservice-description[webservice-description-name="xxx"]
     * 
     * TODO This mechanism will probably need optimization and caching to perform
     * for larger files.
     */
    @Override
    protected CommonDDBean normalizeParent(CommonDDBean parent) {
        if(parentName != null && parent instanceof Webservices) {
            parent = findWebServiceDescByName((Webservices) parent, parentName);
        }
        return parent;
    }
    
    private CommonDDBean findWebServiceDescByName(Webservices webservices, String parentName) {
        return findWebServiceDescByName(webservices.getWebserviceDescription(), parentName);
    }
    
    private CommonDDBean findWebServiceDescByName(WebserviceDescription [] descs, String wsDescName) {
        CommonDDBean match = null;
        if(descs != null) {
            for(WebserviceDescription ws: descs) {
                if(wsDescName.equals(ws.getWebserviceDescriptionName())) {
                    match = ws;
                    break;
                }
            }
        }
        return match;
    }
    
    @Override
    public Map<String, Object> readAnnotations(DataObject dObj) {
        Map<String, Object> result = null;
        try {
            File key = FileUtil.toFile(dObj.getPrimaryFile());
            GlassfishConfiguration dc = GlassfishConfiguration.getConfiguration(key);
            if(dc != null) {
                J2eeModule module = dc.getJ2eeModule();
                if(module != null) {
                    if(J2eeModule.Type.WAR.equals(module.getType()) || J2eeModule.Type.EJB.equals(module.getType())) {
                        result = readWebservicesMetadata(module.getMetadataModel(WebservicesMetadata.class));
                    }
                }
            }
        } catch(MetadataModelException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch(IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return result;
    }
    
    /** Maps interesting fields from port-component descriptor to a multi-level property map.
     * 
     * @return Map<String, Object> where Object is either a String value or nested map
     *  with the same structure (and thus ad infinitum)
     */
    public Map<String, Object> genProperties(CommonDDBean [] beans) {
        Map<String, Object> result = null;
        if(beans instanceof PortComponent []) {
            PortComponent [] ports = (PortComponent []) beans;
            for(PortComponent port: ports) {
                String portName = port.getPortComponentName();
                if(Utils.notEmpty(portName)) {
                    if(result == null) {
                        result = new HashMap<String, Object>();
                    }
                    Map<String, Object> portMap = new HashMap<String, Object>();
                    result.put(portName, portMap);
                    portMap.put(DDBinding.PROP_NAME, portName);
                    
                    addMapString(portMap, DDBinding.PROP_SEI, port.getServiceEndpointInterface());

                    // Wsdl port is actually 3 fields wrapped in a QName.  Do we really need it?
//                    port.getWsdlPort();

                    ServiceImplBean serviceBean = port.getServiceImplBean();
                    if(serviceBean != null) {
                        addMapString(portMap, DDBinding.PROP_SERVLET_LINK, serviceBean.getServletLink());
                        addMapString(portMap, DDBinding.PROP_EJB_LINK, serviceBean.getEjbLink());
                    }
                }
            }
        }
        return result;
    }
}
