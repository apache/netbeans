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
package org.netbeans.modules.websvc.core.jaxws.actions;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.core.jaxws.nodes.JaxWsNode;
import org.netbeans.modules.websvc.core.jaxws.saas.RestResourceGenerator;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.ErrorManager;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author rico
 */
public class ConvertToRestCookieImpl implements ConvertToRestCookie {

    private JaxWsNode node;

    public ConvertToRestCookieImpl(JaxWsNode node) {
        this.node = node;
    }

    public void convertToRest() {

        FileObject fo = node.getLookup().lookup(FileObject.class);
        JAXWSSupport support = JAXWSSupport.getJAXWSSupport(fo);
        Service service = node.getLookup().lookup(Service.class);
        String packageName = getPackageName(service.getImplementationClass());
        String wsdlFileName = service.getLocalWsdlFile();
        URI wsdlURL = null;
        if (wsdlFileName != null) {  //fromWsdl

            FileObject fileObject = support.getLocalWsdlFolderForService(
                    service.getName(), false) ;
            File dir = FileUtil.toFile(fileObject);
            File urlFile = new File( dir,  wsdlFileName);
            wsdlURL = urlFile.toURI();
        } else {  
            try {
                //fromJava
                wsdlURL = new URI(node.getWsdlURL());
            } catch (URISyntaxException ex) {
                ErrorManager.getDefault().notify(ex);
            }

        }
        RestResourceGenerator generator = new RestResourceGenerator(fo.getParent(), wsdlURL, packageName);
        generator.generate();

    }

    private String getPackageName(String implementationClass) {
        int index = implementationClass.lastIndexOf(".");
        return implementationClass.substring(0, index);
    }

    private void invokeWsGen(String serviceName, Project project) {
        FileObject buildImplFo = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        try {
            ExecutorTask wsgentTask =
                    ActionUtils.runTarget(buildImplFo,
                    new String[]{"wsgen-" + serviceName}, null); //NOI18N

            wsgentTask.waitFinished();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
