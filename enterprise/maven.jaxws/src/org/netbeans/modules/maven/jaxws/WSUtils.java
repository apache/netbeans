/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.maven.jaxws;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping25;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.javaee.specs.support.api.JaxWs;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoint;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoints;
import org.netbeans.modules.websvc.api.jaxws.project.config.EndpointsProvider;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.modules.xml.retriever.RetrieveEntry;
import org.netbeans.modules.xml.retriever.Retriever;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author mkuchtiak
 */
public class WSUtils {

    private static final String GENERATE_NON_JSR109 = "generate_nonjsr109"; //NOI18N
    private static final String SERVLET_CLASS_NAME =
            "com.sun.xml.ws.transport.http.servlet.WSServlet"; //NOI18N
    private static final String SERVLET_LISTENER =
            "com.sun.xml.ws.transport.http.servlet.WSServletContextListener"; //NOI18N
    /** downloads XML resources from source URI to target folder
     * (USAGE : this method can download a wsdl file and all wsdl/XML schemas,
     * that are recursively imported by this wsdl)
     * @param targetFolder A folder inside a NB project (ONLY) to which the retrieved resource will be copied to. 
     * All retrieved imported/included resources will be copied relative to this directory.
     * @param source URI of the XML resource that will be retrieved into the project
     * @return FileObject of the retrieved resource in the local file system
     */
    public static FileObject retrieveResource(FileObject targetFolder, URI catalog, URI source)
            throws java.net.UnknownHostException, java.net.URISyntaxException, IOException{
        try {
            Retriever retriever = Retriever.getDefault();
            FileObject result = retriever.retrieveResource(targetFolder, catalog, source);
            if (result==null) {
                Map<RetrieveEntry,Exception> map = 
                        retriever.getRetrievedResourceExceptionMap();
                if (map!=null) {
                    for(Entry<RetrieveEntry,Exception> entry : map.entrySet()){
                        RetrieveEntry key = entry.getKey();
                        Exception exc = entry.getValue(); 
                        if (exc instanceof IOException) {
                            throw (IOException)exc;
                        } else if (exc instanceof java.net.URISyntaxException) {
                            throw (java.net.URISyntaxException)exc;
                        } else  {
                            IOException ex = new IOException(NbBundle.getMessage(
                                    WSUtils.class,"ERR_retrieveResource",       // NOI18N
                                    key.getCurrentAddress()));
                            ex.initCause(exc);
                            throw ex;
                        }
                    }
                }
            }
            return result;
        } catch (RuntimeException ex) {
            throw (IOException)(new IOException(ex.getLocalizedMessage()).initCause(ex));
        }
    }
    
    public static void generateSunJaxwsFile(final FileObject targetDir) throws IOException {
        final String sunJaxwsContent =
                readResource(WsdlModel.class.getResourceAsStream("/org/netbeans/modules/websvc/jaxwsmodel/resources/sun-jaxws.xml")); //NOI18N
        FileSystem fs = targetDir.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
                FileObject sunJaxwsFo = FileUtil.createData(targetDir, "sun-jaxws.xml");//NOI18N
                try (FileLock lock = sunJaxwsFo.lock(); OutputStream os = sunJaxwsFo.getOutputStream(lock);
                        OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                        BufferedWriter bw = new BufferedWriter(osw)) {
                    bw.write(sunJaxwsContent);
                }
            }
        });
    }
    
    static final J2eeModuleProvider getModuleProvider(Project project){
        return project.getLookup().lookup(J2eeModuleProvider.class);
    }
    
    private static String readResource(InputStream is) throws IOException {
        // read the config from resource first
        StringBuffer sb = new StringBuffer();
        String lineSep = System.getProperty("line.separator");//NOI18N
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(lineSep);
                line = br.readLine();
            }
        }
        return sb.toString();
    }
    
    public static void removeImplClass(Project project, String implClass) {
        Sources sources = project.getLookup().lookup(Sources.class);
        String resource = implClass.replace('.','/')+".java"; //NOI18N
        if (sources!=null) {
            SourceGroup[] srcGroup = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (int i=0;i<srcGroup.length;i++) {
                final FileObject srcRoot = srcGroup[i].getRootFolder();
                final FileObject implClassFo = srcRoot.getFileObject(resource);
                if (implClassFo!=null) {
                    try {
                        FileSystem fs = implClassFo.getFileSystem();
                        fs.runAtomicAction(new AtomicAction() {
                            @Override
                            public void run() {
                                deleteFile(implClassFo);
                            }
                        });
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                    return;
                }
            }
        }
    }
    
    private static void deleteFile(FileObject f) {
        try {
            DataObject dObj = DataObject.find(f);
            if (dObj != null) {
                SaveCookie save = dObj.getCookie(SaveCookie.class);
                if (save!=null) {
                    save.save();
                }
            }
            try (FileLock lock = f.lock()) {
                f.delete(lock);
            }
        } catch(java.io.IOException e) {
            NotifyDescriptor ndd =
                    new NotifyDescriptor.Message(NbBundle.getMessage(WSUtils.class, "MSG_Unable_Delete_File", f.getNameExt()),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(ndd);
        }
    }
    
    /** Copy files from source folder to target folder recursively */
    public static void copyFiles(FileObject sourceFolder, FileObject targetFolder) throws IOException {
        FileObject[] children = sourceFolder.getChildren();
        for (int i=0;i<children.length;i++) {
            if (children[i].isFolder()) {
                FileObject folder = targetFolder.createFolder(children[i].getNameExt());
                copyFiles(children[i],folder);
            } else {
                children[i].copy(targetFolder, children[i].getName(), children[i].getExt());
            }
        }
    }
    
    public static FileObject findJaxWsFileObject(Project project) {
        return project.getProjectDirectory().getFileObject("nbproject/jax-ws.xml");
    }
    
    private static final String DEFAULT_PACKAGE_NAME="org.netbeans.ws"; //NOI18N
    
    private static String getPackageNameFromNamespace(String ns) {
        String base = ns;
        int doubleSlashIndex = ns.indexOf("//"); //NOI18N
        if (doubleSlashIndex >=0) {
            base = ns.substring(doubleSlashIndex+2);
        } else {
            int colonIndex = ns.indexOf(":");
            if (colonIndex >=0) base = ns.substring(colonIndex+1);
        }
        StringTokenizer tokens = new StringTokenizer(base,"/"); //NOI18N
        if (tokens.countTokens() > 0) {
            List<String> packageParts = new ArrayList<>();
            List<String> nsParts = new ArrayList<>();
            while (tokens.hasMoreTokens()) {
                String part = tokens.nextToken();
                if (part.length() >= 0) {
                    nsParts.add(part);
                }
            }
            if (nsParts.size() > 0) {
                StringTokenizer tokens1 = new StringTokenizer(nsParts.get(0),"."); //NOI18N
                int countTokens = tokens1.countTokens();
                if (countTokens > 0) {
                    List<String> list = new ArrayList<>();
                    while(tokens1.hasMoreTokens()) {
                        list.add(tokens1.nextToken());
                    }
                    for (int i=countTokens-1; i>=0; i--) {
                        String part = list.get(i);
                        if (i > 0 || !"www".equals(part)) { //NOI18N
                            packageParts.add(list.get(i).toLowerCase());
                        }
                    }
                } else {
                    return DEFAULT_PACKAGE_NAME;
                }
                for (int i=1; i<nsParts.size(); i++) {
                    packageParts.add(nsParts.get(i).toLowerCase());
                }
                StringBuffer buf = new StringBuffer(packageParts.get(0));
                for (int i=1;i<packageParts.size();i++) {
                    buf.append("."+packageParts.get(i));
                }
                return buf.toString();
            }
        }
        return DEFAULT_PACKAGE_NAME;
        
    }

    public static boolean isProjectReferenceable(Project sourceProject, Project targetProject) {
        if (sourceProject == targetProject) {
            return true;
        } else {
            NbMavenProject mavenProject = sourceProject.getLookup().lookup(NbMavenProject.class);
            if (mavenProject != null && NbMavenProject.TYPE_JAR.equals(mavenProject.getPackagingType())) {
                return true;
            }
            return false;
        }
    }

    public static boolean isEJB(Project project) {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            J2eeModule.Type moduleType = j2eeModuleProvider.getJ2eeModule().getType();
            if (J2eeModule.Type.EJB.equals(moduleType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWeb(Project project) {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            J2eeModule.Type moduleType = j2eeModuleProvider.getJ2eeModule().getType();
            if (J2eeModule.Type.WAR.equals(moduleType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if this project is at least Jakarta EE 9 and will use the 
     * {@code jakarta.*} namespace.
     * @param project 
     * @return True if this project use jakarta namespace {@code false} otherwise
     */
    public static boolean isJakartaEENameSpace(Project project) {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            J2eeModule.Type moduleType = j2eeModuleProvider.getJ2eeModule().getType();
            FileObject projectDirectory = project.getProjectDirectory();
            if (J2eeModule.Type.WAR.equals(moduleType)) {
                WebModule wm = WebModule.getWebModule(projectDirectory);
                Profile profile = wm.getJ2eeProfile();
                boolean isJakarta = profile.isAtLeast(Profile.JAKARTA_EE_9_WEB);
                return isJakarta;
            } else if (J2eeModule.Type.WAR.equals(moduleType)) {
                EjbJar ejbm = EjbJar.getEjbJar(projectDirectory);
                Profile profile = ejbm.getJ2eeProfile();
                boolean isJakarta = profile.isAtLeast(Profile.JAKARTA_EE_9_WEB);
                return isJakarta;
            }
        }
        return false;
    }
    
    public static void updateClients(final Project prj, final JAXWSLightSupport jaxWsSupport) {
        Runnable runnable = new Runnable() {
            
            @Override
            public void run() {
                doUpdateClients(prj, jaxWsSupport);
            }
        };
        jaxWsSupport.runAtomic(runnable);
    }

    public static void detectWsdlClients(final Project prj, final JAXWSLightSupport jaxWsSupport)  {
        final List<WsimportPomInfo> candidates = MavenModelUtils.getWsdlFiles(prj);
        if (candidates.size() > 0) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    for (WsimportPomInfo candidate : candidates) {             
                        if (isClient(prj, candidate)) {
                            String wsdlPath = candidate.getWsdlPath();
                            JaxWsService client = new JaxWsService(wsdlPath, false);
                            if (candidate.getHandlerFile() != null) {
                                client.setHandlerBindingFile(candidate.getHandlerFile());
                            }
                            client.setId(candidate.getId());
                            client.setWsdlUrl(getOriginalWsdlUrl(prj, client.getId(), false));
                            jaxWsSupport.addService(client);
                        }
                    }                    
                }
            };
            jaxWsSupport.runAtomic(runnable);
        } else {
            // look for wsdl in wsdl folder
        }
    }
    
    private static void doUpdateClients(Project prj, JAXWSLightSupport jaxWsSupport) {
        // get old clients
        List<JaxWsService> oldClients = new ArrayList<>();
        Set<String> oldNames = new HashSet<>();
        for (JaxWsService s : jaxWsSupport.getServices()) {
            if (!s.isServiceProvider()) {
                oldClients.add(s);
                oldNames.add(s.getId());
            }
        }
        FileObject wsdlFolder = jaxWsSupport.getWsdlFolder(false);
        if (wsdlFolder != null) {
            List<JaxWsService> newClients = getJaxWsClients(prj);
            Set<String> commonNames = new HashSet<>();
            for (JaxWsService client : newClients) {
                String id = client.getId();
                if (oldNames.contains(id)) {
                    commonNames.add(id);
                }
            }
            // removing old clients
            for (JaxWsService oldClient : oldClients) {
                if (!commonNames.contains(oldClient.getId())) {
                    jaxWsSupport.removeService(oldClient);
                }
            }
            // add new clients
            for (JaxWsService newClient : newClients) {
                if (!commonNames.contains(newClient.getId())) {
                    newClient.setWsdlUrl(getOriginalWsdlUrl(prj, newClient.getId(), false));
                    jaxWsSupport.addService(newClient);
                }
            }
        } else {
            // removing ald clients
            for (JaxWsService client : oldClients) {
                jaxWsSupport.removeService(client);
            }
        }
        
    }

    private static List<JaxWsService> getJaxWsClients(Project prj) {
        List<WsimportPomInfo> candidates = MavenModelUtils.getWsdlFiles(prj);
        List<JaxWsService> clients = new ArrayList<>();
        for (WsimportPomInfo candidate : candidates) {           
            if (isClient(prj, candidate)) {
                String wsdlPath = candidate.getWsdlPath();
                JaxWsService client = new JaxWsService(wsdlPath, false);
                client.setId(candidate.getId());
                if (candidate.getHandlerFile() != null) {
                    client.setHandlerBindingFile(candidate.getHandlerFile());
                }
                clients.add(client);
            }
        }
        return clients;
    }

    private static boolean isClient(Project prj, WsimportPomInfo candidate) {
        Preferences prefs = ProjectUtils.getPreferences(prj, MavenWebService.class,true);
        if (prefs != null) {
            // if client exists return true
            if (prefs.get(MavenWebService.SERVICE_PREFIX+candidate.getId(), null) != null) {
                return false;
            }
        }
        return true;
    }

    static FileObject getLocalWsdl(JAXWSLightSupport jaxWsSupport, String localWsdlPath) {
        FileObject wsdlFolder = jaxWsSupport.getWsdlFolder(false);
        if (wsdlFolder!=null) {
            return wsdlFolder.getFileObject(localWsdlPath);
        }
        return null;
    }

    public static String getOriginalWsdlUrl(Project prj, String id, boolean forService) {
        Preferences prefs = ProjectUtils.getPreferences(prj, MavenWebService.class, true);
        if (prefs != null) {
            // remember original WSDL URL for service
            if (forService) {
                return prefs.get(MavenWebService.SERVICE_PREFIX+id, null);
            } else {
                return prefs.get(MavenWebService.CLIENT_PREFIX+id, null);
            }
        }
        return null;
    }
    private static boolean webAppHasListener(WebApp webApp, String listenerClass){
        Listener[] listeners = webApp.getListener();
        for(int i = 0; i < listeners.length; i++){
            Listener listener = listeners[i];
            if(listenerClass.equals(listener.getListenerClass())){
                return true;
            }
        }
        return false;
    }

    // useful methods to work with Deployment Descriptor 
    
    private static WebApp getWebApp(Project prj) {
        try {
            FileObject deploymentDescriptor = getDeploymentDescriptor(prj);
            if(deploymentDescriptor != null) {
                return DDProvider.getDefault().getDDRoot(deploymentDescriptor);
            }
        } catch (java.io.IOException e) {
            Logger.getLogger("global").log(Level.INFO, e.getLocalizedMessage());
        }
        return null;
    }

    private static FileObject getDeploymentDescriptor(Project prj) {
        J2eeModuleProvider provider = prj.getLookup().lookup(J2eeModuleProvider.class);
        if (provider != null) {
            File dd = provider.getJ2eeModule().getDeploymentConfigurationFile(
                    "WEB-INF/web.xml");                                // NOI18N
            if (dd != null && dd.exists()) {
                return FileUtil.toFileObject(dd);
            }
            else {
                WebModule wm = WebModule.getWebModule(prj.getProjectDirectory());
                if ( wm ==null ){
                    return null;
                }
                FileObject webInf = wm.getWebInf();
                try {
                    if (webInf == null) {
                        FileObject docBase = wm.getDocumentBase();
                        if (docBase != null) {
                            webInf = docBase.createFolder("WEB-INF"); // NOI18N
                        }
                    }
                    if (webInf == null) {
                        return null;
                    }
                    return DDHelper.createWebXml(wm.getJ2eeProfile(), webInf);
                }
                catch (IOException e) {
                    Logger.getLogger(WSUtils.class.getName()).log(Level.INFO, null, e );
                    return null;
                }
            }
        }
        return null;
    }

    // methods that handle sun-jaxws.xml file

    public static Endpoint addSunJaxWsEntry(FileObject ddFolder, JaxWsService service)
            throws IOException {
        FileObject sunjaxwsFile = ddFolder.getFileObject("sun-jaxws.xml"); //NOI18N
        if(sunjaxwsFile == null){
            generateSunJaxwsFile(ddFolder);
        }
        sunjaxwsFile = ddFolder.getFileObject("sun-jaxws.xml"); //NOI18N
        Endpoints endpoints = EndpointsProvider.getDefault().getEndpoints(sunjaxwsFile);
        Endpoint oldEndpoint =
                endpoints.findEndpointByImplementation(service.getImplementationClass());
        if (oldEndpoint == null) {
            Endpoint newEndpoint = addService(endpoints, service);
            synchronized (sunjaxwsFile) {
                try (FileLock lock = sunjaxwsFile.lock(); 
                        OutputStream os = sunjaxwsFile.getOutputStream(lock);) {
                    endpoints.write(os);
                }
            }
            return newEndpoint;
        } else {
            return oldEndpoint;
        }
    }

    private static void addJaxWsEntries(FileObject ddFolder, JAXWSLightSupport jaxWsSupport)
            throws IOException {

        generateSunJaxwsFile(ddFolder);
        FileObject sunjaxwsFile = ddFolder.getFileObject("sun-jaxws.xml"); //NOI18N
        Endpoints endpoints = EndpointsProvider.getDefault().getEndpoints(sunjaxwsFile);
        for (JaxWsService service: jaxWsSupport.getServices()) {
            if (service.isServiceProvider()) {
                addService(endpoints, service);
            }
        }
        synchronized (sunjaxwsFile) {
            try (FileLock lock = sunjaxwsFile.lock();
                    OutputStream os = sunjaxwsFile.getOutputStream(lock)) {
                endpoints.write(os);
            }
        }
    }

    private static Endpoint addService(Endpoints endpoints, JaxWsService service) {
        Endpoint endpoint = endpoints.newEndpoint();
        endpoint.setEndpointName(service.getServiceName());
        endpoint.setImplementation(service.getImplementationClass());
        endpoint.setUrlPattern("/" + service.getServiceName());
        endpoints.addEnpoint(endpoint);
        return endpoint;
    }

    public static void removeSunJaxWsEntry(FileObject ddFolder, JaxWsService service)
            throws IOException {
        FileObject sunjaxwsFile = ddFolder.getFileObject("sun-jaxws.xml"); //NOI18N
        if (sunjaxwsFile != null) {
            Endpoints endpoints = EndpointsProvider.getDefault().getEndpoints(sunjaxwsFile);
            Endpoint endpoint = endpoints.findEndpointByName(service.getServiceName());
            if (endpoint != null) {
                endpoints.removeEndpoint(endpoint);
                synchronized (sunjaxwsFile) {
                    try (FileLock lock = sunjaxwsFile.lock();
                            OutputStream os = sunjaxwsFile.getOutputStream(lock)) {
                        endpoints.write(os);
                    }
                }
            }
        }
    }

    private static void removeSunJaxWs(FileObject ddFolder)
            throws IOException {
        FileObject sunjaxwsFile = ddFolder.getFileObject("sun-jaxws.xml"); //NOI18N
        if (sunjaxwsFile != null) {
            sunjaxwsFile.delete();
        }
    }

    public static void replaceSunJaxWsEntries(FileObject ddFolder, String oldServiceName, String newServiceName)
            throws IOException {

        FileObject sunjaxwsFile = ddFolder.getFileObject("sun-jaxws.xml"); //NOI18N
        if (sunjaxwsFile != null) {
            Endpoints endpoints = EndpointsProvider.getDefault().getEndpoints(sunjaxwsFile);
            Endpoint endpoint = endpoints.findEndpointByName(oldServiceName);
            if (endpoint != null) {
                endpoint.setEndpointName(newServiceName);
                endpoint.setUrlPattern("/" + newServiceName);
                synchronized (sunjaxwsFile) {
                    try (FileLock lock = sunjaxwsFile.lock();
                            OutputStream os = sunjaxwsFile.getOutputStream(lock)) {
                        endpoints.write(os);
                    }
                }
            }
        }
    }
    
    public static boolean needNonJsr109Artifacts(Project prj){
        FileObject ddFolder = getDeploymentDescriptorFolder(prj);
        return WSUtils.generateNonJsr109Artifacts(prj);
    }

    private static boolean generateNonJsr109Artifacts(Project prj) {
        Preferences prefs = ProjectUtils.getPreferences(prj, WSUtils.class, true);
        if (prefs == null) {
            return false;
        }
        if (prefs.get(GENERATE_NON_JSR109 , null) == null) {
            ConfirmationPanel panel =
                new ConfirmationPanel(NbBundle.getMessage(WSUtils.class,"MSG_GenerateDDEntries", prj.getProjectDirectory().getName()));
            DialogDescriptor dd = new DialogDescriptor(
                    panel,
                    NbBundle.getMessage(WSUtils.class,"TTL_GenerateDDEntries"),
                    true,
                    DialogDescriptor.YES_NO_OPTION,
                    null,null);
            Object result = DialogDisplayer.getDefault().notify(dd);
            if (panel.notAskAgain()) {
                if (prefs != null) {
                    prefs.put(GENERATE_NON_JSR109, (NotifyDescriptor.YES_OPTION.equals(result) ? "true" : "false")); //NOI18N
                }
            }
            return NotifyDescriptor.YES_OPTION.equals(result);
        } else {
            return "true".equals(prefs.get(GENERATE_NON_JSR109, null));
        }
    }

    private static boolean removeNonJsr109Artifacts(Project prj) {
        Preferences prefs = ProjectUtils.getPreferences(prj, WSUtils.class, true);
        if (prefs == null) {
            return false;
        }
        if (prefs.get(GENERATE_NON_JSR109 , null) == null) {
            ConfirmationPanel panel =
                new ConfirmationPanel(NbBundle.getMessage(WSUtils.class,"MSG_RemoveDDEntries"));
            DialogDescriptor dd = new DialogDescriptor(
                    panel,
                    NbBundle.getMessage(WSUtils.class,"TTL_RemoveDDEntries"),
                    true,
                    DialogDescriptor.YES_NO_OPTION,
                    null,null);
            Object result = DialogDisplayer.getDefault().notify(dd);
            if (panel.notAskAgain()) {
                if (prefs != null) {
                    prefs.put(GENERATE_NON_JSR109 , (NotifyDescriptor.YES_OPTION.equals(result) ? "true" : "false")); //NOI18N
                }
            }
            return NotifyDescriptor.YES_OPTION.equals(result);
        } else {
            return "true".equals(prefs.get(GENERATE_NON_JSR109, null));
        }
    }
    
    public static FileObject getDeploymentDescriptorFolder(Project project) {
        JAXWSLightSupport jaxWsSupport = JAXWSLightSupport.
                getJAXWSLightSupport(project.getProjectDirectory());
        return jaxWsSupport.getDeploymentDescriptorFolder();
    }

    public static boolean isJsr109Supported(Project project) {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider == null) {
            // set to true by default
            return true;
        } else {
            WSStackUtils stackUtils = new WSStackUtils(project);
            return stackUtils.isJsr109Supported();
        }
    }

    /** Add service entries to deployment descriptor.
     *
     * @param prj
     * @param service
     * @throws java.io.IOException
     */
    public static void addServiceToDD(Project prj, JaxWsService service, Endpoint endpoint)
        throws IOException {
        //add servlet entry to web.xml
        WebApp webApp = getWebApp(prj);
        if (webApp != null) {
            try{
                addServlet(webApp, service, endpoint);
                if (!webAppHasListener(webApp, SERVLET_LISTENER)){
                    webApp.addBean("Listener", new String[]{"ListenerClass"}, //NOI18N
                            new Object[]{SERVLET_LISTENER}, "ListenerClass"); //NOI18N
                }
                // This also saves server specific configuration, if necessary.
                webApp.write(getDeploymentDescriptor(prj));
            } catch (ClassNotFoundException exc) {
                Logger.getLogger("global").log(Level.INFO, exc.getLocalizedMessage()); //NOI18N
            } catch (NameAlreadyUsedException exc) {
                Logger.getLogger(WSUtils.class.getName()).log(Level.INFO, exc.getLocalizedMessage()); //NOI18N
            }
        }
    }

    private static void addServicesToDD(Project prj, JAXWSLightSupport jaxWsSupport)
        throws IOException {
        WebApp webApp = getWebApp(prj);
        if (webApp != null) {
            try {
                if (!webAppHasListener(webApp, SERVLET_LISTENER)) {
                    webApp.addBean("Listener", new String[]{"ListenerClass"}, //NOI18N
                            new Object[]{SERVLET_LISTENER}, "ListenerClass"); //NOI18N
                }
                for (JaxWsService service : jaxWsSupport.getServices()) {
                    if (service.isServiceProvider()) {
                        addServlet(webApp, service);
                    }
                }
            } catch (NameAlreadyUsedException exc) {
                Logger.getLogger(WSUtils.class.getName()).log(Level.INFO, exc.getLocalizedMessage()); //NOI18N
            } catch (ClassNotFoundException exc) {
                Logger.getLogger(WSUtils.class.getName()).log(Level.INFO, exc.getLocalizedMessage()); //NOI18N
            }
            webApp.write(getDeploymentDescriptor(prj));
        }
    }

    private static void addServlet(WebApp webApp, JaxWsService service) throws ClassNotFoundException, NameAlreadyUsedException {
        String servletName = service.getServiceName();
        Servlet servlet = (Servlet)webApp.addBean("Servlet", new String[]{"ServletName","ServletClass"}, //NOI18N
                new Object[]{servletName, SERVLET_CLASS_NAME}, "ServletName"); //NOI18N
        servlet.setLoadOnStartup(new java.math.BigInteger("1")); //NOI18N
        webApp.addBean("ServletMapping", new String[] {"ServletName", "UrlPattern"}, //NOI18N
                new Object[]{servletName, "/" + servletName}, "ServletName"); //NOI18N
    }

    private static void addServlet(WebApp webApp, JaxWsService service, Endpoint endpoint) throws ClassNotFoundException, NameAlreadyUsedException {
        String endpointName = endpoint.getEndpointName();
        if (endpointName == null) {
            return;
        }
        // compare existing servlet mappings with endpoint mapping, whether they match
        if (servletMappingExistsFor(webApp, endpoint)) {
            return;
        }
        
        Servlet servlet = (Servlet)webApp.addBean("Servlet", new String[]{"ServletName","ServletClass"}, //NOI18N
                new Object[]{endpointName, SERVLET_CLASS_NAME}, "ServletName"); //NOI18N
        if (servlet != null) {
            servlet.setLoadOnStartup(new java.math.BigInteger("1")); //NOI18N
        }
        webApp.addBean("ServletMapping", new String[] {"ServletName", "UrlPattern"}, //NOI18N
                new Object[]{endpointName, "/" + endpointName}, "ServletName"); //NOI18N
    }
    
    private static boolean servletMappingExistsFor(WebApp webApp, Endpoint endpoint) {
        for (Servlet servlet : webApp.getServlet()) {
            String servletName = servlet.getServletName();
            if (endpoint.getEndpointName().equals(servletName)) {
                return true;
            }
            if (SERVLET_CLASS_NAME.equals(servlet.getServletClass())) {
                for (ServletMapping servletMapping : webApp.getServletMapping()) {
                    if (servletName != null && servletName.equals(servletMapping.getServletName())) {
                        String endpoindPattern = cutEndingWildcard(endpoint.getUrlPattern());
                        String servletMappingPattern = cutEndingWildcard(((ServletMapping25)servletMapping).getUrlPatterns()[0]);
                        if (endpoindPattern.startsWith(servletMappingPattern)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private static String cutEndingWildcard(String urlPattern) {
        if (urlPattern == null) {
            return "";
        }
        if (urlPattern.endsWith("/*")) {
            return urlPattern.substring(0,urlPattern.length()-2);
        }
        return urlPattern;
    }
    
    /**
     * Remove the service entries from deployment descriptor.
     *
     * @param serviceName Name of the web service to be removed
     */
    public static void removeServiceFromDD(Project prj, JaxWsService service)
        throws IOException {
        WebApp webApp = getWebApp(prj);
        if (webApp != null) {
            boolean changed = removeServiceFromDD(webApp, service.getServiceName());

            //determine if there are other web services in the project
            //if none, remove the listener
            boolean hasMoreWebServices = false;
            Servlet[] remainingServlets = webApp.getServlet();
            for(int i = 0; i < remainingServlets.length; i++) {
                if (SERVLET_CLASS_NAME.equals(remainingServlets[i].getServletClass())) {
                    hasMoreWebServices = true;
                    break;
                }
            }
            if(!hasMoreWebServices){
                Listener[] listeners = webApp.getListener();
                for (int i = 0; i < listeners.length; i++) {
                    Listener listener = listeners[i];
                    if (SERVLET_LISTENER.equals(listener.getListenerClass())) {
                        webApp.removeListener(listener);
                        changed = true;
                        break;
                    }
                }
            }
            if (changed) {
                webApp.write(getDeploymentDescriptor(prj));
            }
        }
    }

    private static void removeServicesFromDD(Project prj, JAXWSLightSupport jaxWsSupport)
        throws IOException {
        WebApp webApp = getWebApp(prj);
        if (webApp != null) {
            boolean changed = false;
            // remove all services
            for (JaxWsService service : jaxWsSupport.getServices()) {
                changed = removeServiceFromDD(webApp, service.getServiceName());
            }
            // remove servlet listener
            Listener[] listeners = webApp.getListener();
            for(int i = 0; i < listeners.length; i++){
                Listener listener = listeners[i];
                if (SERVLET_LISTENER.equals(listener.getListenerClass())) {
                    webApp.removeListener(listener);
                    changed = true;
                    break;
                }
            }
            if (changed) {
                webApp.write(getDeploymentDescriptor(prj));
            }
        }
    }

    /**
     * Remove the web.xml servlets for the non-JSR 109 web service.
     *
     * @param serviceName Name of the web service to be removed
     */
    private static boolean removeServiceFromDD(WebApp webApp, String serviceName) {
        boolean changed = false;
        //first remove the servlet
        Servlet[] servlets = webApp.getServlet();
        for(int i = 0; i < servlets.length; i++){
            Servlet servlet = servlets[i];
            if(servlet.getServletName().equals(serviceName)){
                webApp.removeServlet(servlet);
                changed = true;
                break;
            }
        }
        //remove the servlet mapping
        ServletMapping[] mappings = webApp.getServletMapping();
        for(int i = 0; i < mappings.length; i++){
            ServletMapping mapping = mappings[i];
            if(mapping.getServletName().equals(serviceName)){
                webApp.removeServletMapping(mapping);
                changed = true;
                break;
            }
        }
        return changed;
    }

    /**
     * Remove the web.xml entries for the non-JSR 109 web service.
     *
     * @param serviceName Name of the web service to be removed
     */
    public static void replaceServiceEntriesFromDD(Project prj, String oldServiceName, String newServiceName)
        throws IOException {
        WebApp webApp = getWebApp(prj);
        if (webApp != null) {
            boolean changed = replaceServiceInDD(webApp, oldServiceName, newServiceName);
            if (changed) {
                webApp.write(getDeploymentDescriptor(prj));
            }
        }
    }

    /**
     * Remove the web.xml servlets for the non-JSR 109 web service.
     *
     * @param serviceName Name of the web service to be removed
     */
    private static boolean replaceServiceInDD(WebApp webApp, String oldServiceName, String newServiceName) {
        boolean changed = false;
        //first remove the servlet
        Servlet[] servlets = webApp.getServlet();
        for(int i = 0; i < servlets.length; i++){
            Servlet servlet = servlets[i];
            if(servlet.getServletName().equals(oldServiceName)){
                servlet.setServletName(newServiceName);
                changed = true;
                break;
            }
        }
        //remove the servlet mapping
        ServletMapping[] mappings = webApp.getServletMapping();
        for(int i = 0; i < mappings.length; i++){
            ServletMapping25 mapping = (ServletMapping25)mappings[i];
            if(mapping.getServletName().equals(oldServiceName)){
                mapping.setServletName(newServiceName);
                mapping.setUrlPatterns(new String[]{"/"+newServiceName});
                break;
            }
        }
        return changed;
    }

    public static void checkNonJSR109Entries(Project prj) {
        JAXWSLightSupport jaxWsSupport = JAXWSLightSupport.getJAXWSLightSupport(prj.getProjectDirectory());
        if (jaxWsSupport != null) {
            WSStack<JaxWs> wsStack = new WSStackUtils(prj).getWsStack(JaxWs.class);
            if (wsStack != null) {
                FileObject ddFolder = jaxWsSupport.getDeploymentDescriptorFolder();
                if (wsStack.isFeatureSupported(JaxWs.Feature.JSR109)) {
                    if (ddFolder != null && ddFolder.getFileObject("sun-jaxws.xml") != null) {
                        // remove non JSR109 artifacts
                        if (removeNonJsr109Artifacts(prj)) {
                            try {
                                removeSunJaxWs(ddFolder);
                            } catch (IOException ex) {
                                Logger.getLogger(WSUtils.class.getName()).log(Level.WARNING,
                                        "Cannot remove sun-jaxws.xml file.", ex); //NOI18N
                            }
                            try {
                                removeServicesFromDD(prj, jaxWsSupport);
                            } catch (IOException ex) {
                                Logger.getLogger(WSUtils.class.getName()).log(Level.WARNING,
                                        "Cannot remove services from web.xml.", ex); //NOI18N
                            }
                        }
                    }
                } else {
                    if (ddFolder == null || ddFolder.getFileObject("sun-jaxws.xml") == null) { // NOI18N
                    // generate non JSR109 artifacts
                    if (generateNonJsr109Artifacts(prj)) {
                        if (ddFolder != null) {
                            try {
                                addJaxWsEntries(ddFolder, jaxWsSupport);
                            }
                            catch (IOException ex) {
                                Logger.getLogger(WSUtils.class.getName()).log(
                                        Level.WARNING,
                                        "Cannot modify sun-jaxws.xml file", ex); // NOI18N
                            }
                            try {
                                addServicesToDD(prj, jaxWsSupport);
                            }
                            catch (IOException ex) {
                                Logger.getLogger(WSUtils.class.getName()).log(
                                        Level.WARNING,
                                        "Cannot modify web.xml file", ex); // NOI18N
                            }
                        }
                        else {
                            String mes = NbBundle.getMessage(
                                    MavenJAXWSSupportImpl.class,
                                    "MSG_CannotFindWEB-INF"); // NOI18N
                            NotifyDescriptor desc = new NotifyDescriptor.Message(
                                    mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(desc);
                        }
                    }
                }
            }
        }
    }
    }

    public static String getUniqueId(String id, List<JaxWsService> services) {
        String result = id;
        Set<String> serviceIdSet = new HashSet<>();
        for (JaxWsService s : services) {
            String serviceId = s.getId();
            if (serviceId != null) {
                serviceIdSet.add(serviceId);
            }
        }

        int i=1;
        while (serviceIdSet.contains(result)) {
            result = id+"_"+String.valueOf(i++); //NOI18N
        }
        return result;
    }

    static boolean isInSourceGroup(Project prj, String serviceClass) {
        if ( serviceClass == null ){
            return false;
        }
        String resource = serviceClass.replace('.', '/') + ".java"; //NOI18N
        SourceGroup[] sourceGroups = ProjectUtils.getSources(prj).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup group : sourceGroups) {
            if (group.getRootFolder() != null && group.getRootFolder().
                    getFileObject(resource) != null) 
            {
                return true;
            }

        }
        return false;
    }
    
}
