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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.websvc.saas.codegen.j2ee;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSPort;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSService;
import org.netbeans.modules.websvc.saas.codegen.Constants;
import org.netbeans.modules.websvc.saas.codegen.java.SoapClientPojoCodeGenerator;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean;
import org.netbeans.modules.websvc.saas.codegen.j2ee.support.J2eeUtil;
import org.netbeans.modules.websvc.saas.codegen.model.SoapClientOperationInfo;
import org.netbeans.modules.websvc.saas.codegen.model.SoapClientSaasBean;
import org.netbeans.modules.websvc.saas.model.SaasMethod;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author nam
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.saas.codegen.spi.SaasClientCodeGenerationProvider.class)
public class SoapClientServletCodeGenerator extends SoapClientPojoCodeGenerator {

    public SoapClientServletCodeGenerator() {
        setDropFileType(Constants.DropFileType.SERVLET);
        setPrecedence(1);
    }

    @Override
    public boolean canAccept(SaasMethod method, Document doc) {
        if (SaasBean.canAccept(method, WsdlSaasMethod.class, getDropFileType()) &&
                J2eeUtil.isServlet(NbEditorUtilities.getDataObject(doc))) {
            return true;
        }
        return false;
    }

    @Override
    protected void preGenerate() throws IOException {
        super.preGenerate();

        SoapClientOperationInfo[] operations = getBean().getOperationInfos();
        List<WSService> services = new ArrayList<WSService>();
        for (SoapClientOperationInfo info : operations) {
            if (info.isRPCEncoded()) {
                WSService service = info.getService();
                if (services.contains(service)) {
                    continue;
                }
                services.add(service);
                preGenerateRpcClient(info, service);
            }
        }
    }

    @Override
    public void init(SaasMethod m, Document doc) throws IOException {
        super.init(m, doc);
        WsdlSaasMethod wsm = (WsdlSaasMethod) m;
        Project p = FileOwnerQuery.getOwner(NbEditorUtilities.getFileObject(doc));
        SaasBean bean = new SoapClientSaasBean(wsm, p, J2eeUtil.toJaxwsOperationInfos(wsm, p));
        setBean(bean);
    }

    private void preGenerateRpcClient(SoapClientOperationInfo info, WSService service) throws IOException {
        WebServicesClientSupport support = WebServicesClientSupport.getWebServicesClientSupport(getTargetFile());
        if (support != null) {
            String wsdlLocation = info.getWsdlLocation();
            File wsdlFile = new File(wsdlLocation);
            FileObject wsdlFO = FileUtil.toFileObject(wsdlFile);  //wsdl file in user directory
            FileObject projectWsdlFolder = support.getWsdlFolder(true); //wsdl Folder in project
            //copy wsdl file to wsdl directory in project
            if (projectWsdlFolder.getFileObject(wsdlFO.getName(), wsdlFO.getExt()) == null) {
                FileUtil.copyFile(wsdlFO, projectWsdlFolder, wsdlFO.getName());
            }

            //look for the mapping file and copy it to project
            FileObject catalogDir = getCatalogDir(info);
            if (catalogDir != null) {
                FileObject mappingFO = catalogDir.getFileObject("mapping.xml");   //NOI18N
                if (mappingFO != null) {
                    if (projectWsdlFolder.getFileObject(mappingFO.getName(), mappingFO.getExt()) == null) {
                        FileUtil.copyFile(mappingFO, projectWsdlFolder, mappingFO.getName());
                    }
                }
            }
            String serviceName = "service/" + service.getName();  //NOI18N
            String fqServiceName = service.getJavaName();

            wsdlLocation = wsdlLocation.substring(wsdlLocation.lastIndexOf(File.separator) + 1);
            wsdlLocation = "WEB-INF/wsdl/" + wsdlLocation;  //NOI18N
            String mappingFile = "WEB-INF/wsdl/mapping.xml";   //NOI18N
            List<? extends WSPort> ports = service.getPorts();
            String[] portClasses = new String[ports.size()];
            for (int i = 0; i < ports.size(); i++) {
                portClasses[i] = ports.get(i).getJavaName();
            }
            support.addServiceClientReference(serviceName, fqServiceName, wsdlLocation, mappingFile, portClasses);
        }
    }

    private FileObject findParentDir(FileObject originDir, String parentDirName) {
        if (originDir == null || originDir.getName().equals(parentDirName)) {
            return originDir;
        } else {
            return findParentDir(originDir.getParent(), parentDirName);
        }

    }

    private FileObject getCatalogDir(SoapClientOperationInfo info) throws IOException {
        FileObject parent = null;
        String wsdlLocation = info.getWsdlLocation();
        File wsdlFile = new File(wsdlLocation);
        if (wsdlFile.exists()) {
            FileObject wsdlFO = FileUtil.toFileObject(wsdlFile);
            parent = findParentDir(wsdlFO.getParent(), "catalog"); //NOI18N
        }
        return parent;
    }
}
