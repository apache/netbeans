/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
