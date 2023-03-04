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

package org.netbeans.modules.websvc.core.jaxws.projects;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.persistence.api.PersistenceLocation;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.support.SourceGroups;
import org.netbeans.modules.websvc.spi.jaxws.client.ProjectJAXWSClientSupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/** Implementation of JAXWSClientSupportImpl for J2SE Project
 *
 * @author mkuchtiak
 */
public class J2SEProjectJAXWSClientSupport extends ProjectJAXWSClientSupport /*implements JAXWSClientSupportImpl*/ {
    private Project project;
    private static final String WSDL_FOLDER = "wsdl"; //NOI18N
    /** proxy host VM property key */
    private static final String KEY_PROXY_HOST = "http.proxyHost"; // NOI18N
    /** proxy port VM property key */
    private static final String KEY_PROXY_PORT = "http.proxyPort"; // NOI18N
    /** non proxy hosts VM property key */
    private static final String KEY_NON_PROXY_HOSTS = "http.nonProxyHosts"; // NOI18N
    /** https proxy host VM property key */
    private static final String KEY_HTTPS_PROXY_HOST = "https.proxyHost"; // NOI18N
    /** https proxy port VM property key */
    private static final String KEY_HTTPS_PROXY_PORT = "https.proxyPort"; // NOI18N
    /** non proxy hosts VM property key */
    private static final String KEY_HTTPS_NON_PROXY_HOSTS = "https.nonProxyHosts"; // NOI18N
    
    private static final String HTTP_PROXY_HOST_OPTION="-Dhttp.proxyHost"; //NOI18N
    private static final String HTTP_PROXY_PORT_OPTION="-Dhttp.proxyPort"; //NOI18N
    private static final String HTTP_NON_PROXY_HOSTS_OPTION="-Dhttp.nonProxyHosts"; //NOI18N
    private static final String HTTPS_PROXY_HOST_OPTION="-Dhttps.proxyHost"; //NOI18N
    private static final String HTTPS_PROXY_PORT_OPTION="-Dhttps.proxyPort"; //NOI18N
    private static final String HTTPS_NON_PROXY_HOSTS_OPTION="-Dhttps.nonProxyHosts"; //NOI18N
    private static final String RUN_JVM_ARGS = "run.jvmargs"; // NOI18N
    
    /** Creates a new instance of J2SEProjectJAXWSClientSupport */
    public J2SEProjectJAXWSClientSupport(Project project) {
        super(project, null);
        this.project=project;
    }

    public FileObject getWsdlFolder(boolean create) throws IOException {
        if (create) {
            FileObject metaInfDir = PersistenceLocation.createLocation(project);
            if (metaInfDir != null) {
                return FileUtil.createFolder(metaInfDir, WSDL_FOLDER);
            }
        } else {
            FileObject metaInfDir = PersistenceLocation.getLocation(project);
            if (metaInfDir != null) {
                return metaInfDir.getFileObject(WSDL_FOLDER);
            }
        }
        return null;
    }

    @Override
    public String addServiceClient(String clientName, String wsdlUrl, String packageName, boolean isJsr109) {
        
        // call the super.addServiceClient();
        String serviceIdeName = super.addServiceClient(clientName, wsdlUrl, packageName, false);
        
        // add JVM Proxy Options to project's JVM
        if (serviceIdeName!=null) addJVMOptions(clientName);
        
        return serviceIdeName;
    }
    
    protected void addJaxWs20Library() {
        ClassPath classPath = null;
        SourceGroup[] sourceGroups = SourceGroups.getJavaSourceGroups(project);
        if (sourceGroups!=null && sourceGroups.length>0) {
            FileObject srcRoot = sourceGroups[0].getRootFolder();
            ClassPath compileClassPath = ClassPath.getClassPath(srcRoot,ClassPath.COMPILE);
            ClassPath bootClassPath = ClassPath.getClassPath(srcRoot,ClassPath.BOOT);
            classPath = ClassPathSupport.createProxyClassPath(new ClassPath[]{compileClassPath, bootClassPath});

            // add JAX-WS Endorsed Classpath
            try {
                addJaxWsApiEndorsed(srcRoot);
            } catch (IOException ex) {
                Logger.getLogger(J2SEProjectJAXWSClientSupport.class.getName()).log(Level.FINE, "Cannot add JAX-WS-ENDORSED classpath", ex);
            }

        }
        FileObject webServiceClass=null;
        if (classPath!=null) {
            webServiceClass = classPath.findResource("javax/xml/ws/WebServiceFeature.class"); // NOI18N
        }
        if (webServiceClass==null) {
            // add Metro library if WebServiceFeature.class
            Library metroLib = LibraryManager.getDefault().getLibrary("metro"); //NOI18N
            if ((sourceGroups.length > 0) && (metroLib != null)) {
                try {
                    ProjectClassPathModifier.addLibraries(
                            new Library[] {metroLib},
                            sourceGroups[0].getRootFolder(),
                            ClassPath.COMPILE);
                } catch (IOException ex) {
                    ErrorManager.getDefault().log(ex.getMessage());
                }
                FileObject wscompileFO=null;
                if (classPath!=null) {
                    wscompileFO = classPath.findResource("com/sun/xml/rpc/tools/ant/Wscompile.class"); // NOI18N
                }
                if (wscompileFO!=null) {
                    NotifyDescriptor desc = new NotifyDescriptor.Message(
                    NbBundle.getMessage(J2SEProjectJAXWSClientSupport.class,"MSG_RemoveJAX-RPC"), // NOI18N
                    NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                }
            }
        }
    }
    
    private void addJVMOptions (final String clientName) {
        ProjectManager.mutex().writeAccess ( new Runnable () {
            public void run() {
                EditableProperties ep = null;
                EditableProperties ep1 = null;
                try {
                    ep = WSUtils.getEditableProperties(project, AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    ep1 = WSUtils.getEditableProperties(project, AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    assert ep != null;
                    assert ep1 != null;
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }

                boolean proxyModif = addJVMProxyOptions(ep);
                
                if (proxyModif)
                try {
                    WSUtils.storeEditableProperties(project, AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);                
                    ProjectManager.getDefault().saveProject(project);
                } catch(IOException ex) {
                    NotifyDescriptor desc = new NotifyDescriptor.Message(
                    NbBundle.getMessage(J2SEProjectJAXWSClientSupport.class,"MSG_ErrorSavingOnWSClientAdd", clientName, ex.getLocalizedMessage()), // NOI18N
                    NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);                  
                }
            }
        });
    }
    
    private boolean addJVMProxyOptions(EditableProperties ep) {
        assert ep != null;
        boolean modif=false;
        final String proxyHost = System.getProperty(KEY_PROXY_HOST);
        if (proxyHost!=null && proxyHost.length()>0) {
            String proxyPort = System.getProperty(KEY_PROXY_PORT);
            if (proxyPort==null || proxyPort.length()==0) proxyPort="8080"; //NOI18N
            String localHosts ="";
            localHosts = getDefaultNonProxyHosts();            
            String jvmOptions = ep.getProperty(RUN_JVM_ARGS);
            if (jvmOptions==null || jvmOptions.length()==0) {
                jvmOptions = HTTP_PROXY_HOST_OPTION+"="+proxyHost+ //NOI18N
                        " "+HTTP_PROXY_PORT_OPTION+"="+proxyPort+ //NOI18N
                        " "+HTTP_NON_PROXY_HOSTS_OPTION+"="+localHosts+ //NOI18N
                        " "+HTTPS_PROXY_HOST_OPTION+"="+proxyHost+ //NOI18N
                        " "+HTTPS_PROXY_PORT_OPTION+"="+proxyPort+ //NOI18N
                        " "+HTTPS_NON_PROXY_HOSTS_OPTION+"="+localHosts; //NOI18N
                modif=true;
            } else {
                if (jvmOptions.indexOf(HTTP_PROXY_HOST_OPTION)<0) {
                    jvmOptions+=" "+HTTP_PROXY_HOST_OPTION+"="+proxyHost; //NOI18N
                    modif=true;
                }
                if (jvmOptions.indexOf(HTTP_PROXY_PORT_OPTION)<0) {
                    jvmOptions+=" "+HTTP_PROXY_PORT_OPTION+"="+proxyPort; //NOI18N
                    modif=true;
                }
                if (jvmOptions.indexOf(HTTP_NON_PROXY_HOSTS_OPTION)<0) {
                    jvmOptions+=" "+HTTP_NON_PROXY_HOSTS_OPTION+"="+localHosts; //NOI18N
                    modif=true;
                }
                if (jvmOptions.indexOf(HTTPS_PROXY_HOST_OPTION)<0) {
                    jvmOptions+=" "+HTTPS_PROXY_HOST_OPTION+"="+proxyHost; //NOI18N
                    modif=true;
                }
                if (jvmOptions.indexOf(HTTPS_PROXY_PORT_OPTION)<0) {
                    jvmOptions+=" "+HTTPS_PROXY_PORT_OPTION+"="+proxyPort; //NOI18N
                    modif=true;
                }
                if (jvmOptions.indexOf(HTTPS_NON_PROXY_HOSTS_OPTION)<0) {
                    jvmOptions+=" "+HTTPS_NON_PROXY_HOSTS_OPTION+"="+localHosts; //NOI18N
                    modif=true;
                }
            }
            if (modif) {
                ep.setProperty(RUN_JVM_ARGS,jvmOptions);
            }
        }
        return modif;
    }

    public void addJaxWsApiEndorsed(FileObject srcRoot) throws IOException {
        WSUtils.addJaxWsApiEndorsed(project, srcRoot);
    }
     
     /** Returns the default value for the http.nonProxyHosts system property. <br>
     *  PENDING: should be a user settable property
     * @return sensible default for non-proxy hosts, including 'localhost'
     */
    private String getDefaultNonProxyHosts() {
        String nonProxy = "localhost|127.0.0.1"; // NOI18N
        String localhost = ""; // NOI18N
        try {
            localhost = InetAddress.getLocalHost().getHostName();
            if (!localhost.equals("localhost")) { // NOI18N
                nonProxy = nonProxy + "|" + localhost; // NOI18N
            } else {
                // Avoid this error when hostname == localhost:
                // Error in http.nonProxyHosts system property:  sun.misc.REException: localhost is a duplicate
            }
        } catch (UnknownHostException e) {
            // OK. Sometimes a hostname is assigned by DNS, but a computer
            // is later pulled off the network. It may then produce a bogus
            // name for itself which can't actually be resolved. Normally
            // "localhost" is aliased to 127.0.0.1 anyway.
        }
        try {
            String localhost2 = InetAddress.getLocalHost().getCanonicalHostName();
            if (!localhost2.equals("localhost") && !localhost2.equals(localhost)) { // NOI18N
                nonProxy = nonProxy + "|" + localhost2; // NOI18N
            } else {
                // Avoid this error when hostname == localhost:
                // Error in http.nonProxyHosts system property:  sun.misc.REException: localhost is a duplicate
            }
        } catch (UnknownHostException e) {
            // OK. Sometimes a hostname is assigned by DNS, but a computer
            // is later pulled off the network. It may then produce a bogus
            // name for itself which can't actually be resolved. Normally
            // "localhost" is aliased to 127.0.0.1 anyway.
        }
        return nonProxy;
    }

    
}
