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

package org.netbeans.modules.websvc.wsitconf.projects;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.javaee.specs.support.api.JaxWs;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.wsitconf.spi.WsitProvider;
import org.netbeans.modules.websvc.wsitconf.util.ServerUtils;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Grebac
 */
public class WebWsitProvider extends WsitProvider {

    private static final Logger logger = Logger.getLogger(WebWsitProvider.class.getName());

    protected static final String SERVLET_NAME = "ServletName";                     // NOI18N
    protected static final String SERVLET_CLASS = "ServletClass";                   // NOI18N
    protected static final String URL_PATTERN = "UrlPattern";                       // NOI18N
    
    private static final String WS_SERVLET = "com.sun.xml.ws.transport.http.servlet.WSServlet";// NOI18N
    
    private static final String WS_LISTENER = 
        "com.sun.xml.ws.transport.http.servlet.WSServletContextListener";           // NOI18N
    
    private static final String LISTENER = "Listener";                              // NOI18N
    
    private static final String LISTENER_CLASS= "ListenerClass";                    // NOI18N

    public WebWsitProvider(Project p) {
        this.project = p;
    }

    @Override
    public boolean isJsr109Project() {
        J2eePlatform j2eePlatform = ServerUtils.getJ2eePlatform(project);
        if (j2eePlatform != null){
            Collection<WSStack> wsStacks = (Collection<WSStack>)
                    j2eePlatform.getLookup().lookupAll(WSStack.class);
            for (WSStack stack : wsStacks) {
                if (stack.isFeatureSupported(JaxWs.Feature.JSR109)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public FileObject getConfigFilesFolder(boolean client, boolean create ) {
        if (!client) {
            WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
            return wm.getWebInf();
        } else {
            return super.getConfigFilesFolder(client, create );
        }
    }

    @Override
    public void addServiceDDEntry(String serviceImplPath, String mexUrl, String targetName) {

        boolean isGlassFish = ServerUtils.isGlassfish(project);
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            try {
                WebApp wApp = DDProvider.getDefault ().getDDRoot(wm.getDeploymentDescriptor());
                Servlet servlet = Util.getServlet(wApp, serviceImplPath);
                if (servlet == null) {
                    try {
                        /*if (isGlassFish) {
                            servlet = (Servlet)wApp.addBean("Servlet",              //NOI18N
                                    new String[]{SERVLET_NAME,SERVLET_CLASS},
                                    new Object[]{serviceImplPath, serviceImplPath},SERVLET_NAME);
                            servlet.setLoadOnStartup(new java.math.BigInteger("0"));               //NOI18N
                            wApp.addBean("ServletMapping", new String[]{SERVLET_NAME,URL_PATTERN}, //NOI18N
                                    new Object[]{serviceImplPath, "/" + targetName + "Service"},SERVLET_NAME);      //NOI18N
                            try {
                                servlet = (Servlet)wApp.addBean("Servlet",              //NOI18N
                                        new String[]{SERVLET_NAME,SERVLET_CLASS},
                                        new Object[]{Util.MEX_CLASS_NAME,Util.MEX_CLASS_NAME},SERVLET_NAME);
                                servlet.setLoadOnStartup(new java.math.BigInteger("0"));     //NOI18N
                            } catch (NameAlreadyUsedException ex) {
                                // do nothing, this is ok - there should be only one instance of this
                            }
                            wApp.addBean("ServletMapping", new String[]{SERVLET_NAME,URL_PATTERN}, //NOI18N
                                    new Object[]{Util.MEX_CLASS_NAME, mexUrl},URL_PATTERN);  //NOI18N
                            wApp.write(wm.getDeploymentDescriptor());
                        } else {
                            try {
                                servlet = (Servlet)wApp.addBean("Servlet",              //NOI18N
                                        new String[]{SERVLET_NAME,SERVLET_CLASS},
                                        new Object[]{Util.MEX_NAME, Util.MEX_CLASS_NAME},SERVLET_NAME);
                                servlet.setLoadOnStartup(new java.math.BigInteger("0"));     //NOI18N
                            } catch (NameAlreadyUsedException ex) {
                                // do nothing, this is ok - there should be only one instance of this
                            }
                            wApp.addBean("ServletMapping", new String[]{SERVLET_NAME,URL_PATTERN}, //NOI18N
                                    new Object[]{Util.MEX_NAME, mexUrl},URL_PATTERN);  //NOI18N
                            wApp.write(wm.getDeploymentDescriptor());
                        }*/
                        wApp.addBean(LISTENER,              //NOI18N
                                new String[]{LISTENER_CLASS},
                                new Object[]{WS_LISTENER},
                                LISTENER_CLASS);
                        servlet = (Servlet)wApp.addBean("Servlet",              //NOI18N
                                new String[]{SERVLET_NAME,SERVLET_CLASS},
                                new Object[]{serviceImplPath, WS_SERVLET},
                                SERVLET_NAME);
                        servlet.setLoadOnStartup(new java.math.BigInteger("1"));               //NOI18N
                        wApp.addBean("ServletMapping", new String[]{
                                SERVLET_NAME,URL_PATTERN}, //NOI18N
                                new Object[]{serviceImplPath, "/" + targetName + 
                                    "Service"},URL_PATTERN);      //NOI18N
                        wApp.addBean("ServletMapping", new String[]{
                                SERVLET_NAME,URL_PATTERN}, //NOI18N
                                new Object[]{serviceImplPath, "/" + 
                                    targetName + "Service/mex"},URL_PATTERN);      //NOI18N
                        wApp.write(wm.getDeploymentDescriptor());
                    } catch (NameAlreadyUsedException ex) {
                        ex.printStackTrace();
                    } catch (ClassNotFoundException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    servlet.setLoadOnStartup(new java.math.BigInteger("1"));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    @Override
    public boolean addMetroRtLibrary() {
        Library metroLib = LibraryManager.getDefault().getLibrary("metro"); //NOI18N
        if (metroLib != null) {
            try {
                SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                if ((sourceGroups != null) && (sourceGroups.length > 0)) {
                    return ProjectClassPathModifier.addLibraries(new Library[] {metroLib}, sourceGroups[0].getRootFolder(),
                        JavaClassPathConstants.COMPILE_ONLY);
                }
            } catch (IOException e) {
                //NOOP
            }
        }
        return false;
    }

}
