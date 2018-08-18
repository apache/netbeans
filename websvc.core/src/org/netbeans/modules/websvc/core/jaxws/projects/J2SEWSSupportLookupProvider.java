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
 *System.out.println("JAX-WS-MODEL ......... constructor "+fo);
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

package org.netbeans.modules.websvc.core.jaxws.projects;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportFactory;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportImpl;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportFactory;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportImpl;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/** Lookup Provider for WS Support
 *
 * @author mkuchtiak
 */
@ProjectServiceProvider(service=ProjectOpenedHook.class, projectType="org-netbeans-modules-java-j2seproject")
public class J2SEWSSupportLookupProvider extends ProjectOpenedHook {
    private Project project;

    /** Creates a new instance of JaxWSLookupProvider */
    public J2SEWSSupportLookupProvider(Project project) {
        this.project = project;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("DE_MIGHT_IGNORE")
    @Override
    protected void projectOpened() {
        if(WebServicesClientSupport.isBroken(project)) {
            WebServicesClientSupport.showBrokenAlert(project);
        }
        FileObject jaxWsFo = WSUtils.findJaxWsFileObject(project);
        try {
            if (jaxWsFo != null && WSUtils.hasClients(jaxWsFo)) {
                final JAXWSClientSupport jaxWsClientSupport = project.getLookup().lookup(JAXWSClientSupport.class);
                if (jaxWsClientSupport != null) {
                    FileObject wsdlFolder = null;
                    try {
                        wsdlFolder = jaxWsClientSupport.getWsdlFolder(false);
                    } catch (IOException ex) {}
                    if (wsdlFolder == null || wsdlFolder.getParent().getFileObject("jax-ws-catalog.xml") == null) { //NOI18N
                        RequestProcessor.getDefault().post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JaxWsCatalogPanel.generateJaxWsCatalog(project, jaxWsClientSupport);
                                } catch (IOException ex) {
                                    Logger.getLogger(JaxWsCatalogPanel.class.getName()).log(Level.WARNING, "Cannot create jax-ws-catalog.xml", ex);
                                }
                            }
                        });
                    }
                }
            }
        } catch (IOException ex) {
             Logger.getLogger(JaxWsCatalogPanel.class.getName()).log(Level.WARNING, "Cannot read nbproject/jax-ws.xml file", ex);
        }
    }

    @Override
    protected void projectClosed() {
    }

    @ProjectServiceProvider(service=JAXWSClientSupport.class, projectType="org-netbeans-modules-java-j2seproject")
    public static JAXWSClientSupport createJAXWSClientSupport(Project project) {
        JAXWSClientSupportImpl j2seJAXWSClientSupport = new J2SEProjectJAXWSClientSupport(project);
        return JAXWSClientSupportFactory.createJAXWSClientSupport(j2seJAXWSClientSupport);
    }

    @ProjectServiceProvider(service=WebServicesClientSupport.class, projectType="org-netbeans-modules-java-j2seproject")
    public static WebServicesClientSupport createWebServicesClientSupport(Project project) {
        WebServicesClientSupportImpl jaxrpcClientSupport = new J2SEProjectJaxRpcClientSupport(project);
        return WebServicesClientSupportFactory.createWebServicesClientSupport(jaxrpcClientSupport);
    }
}
