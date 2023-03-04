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
