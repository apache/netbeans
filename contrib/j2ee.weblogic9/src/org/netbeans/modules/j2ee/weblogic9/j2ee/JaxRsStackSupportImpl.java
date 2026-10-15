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
package org.netbeans.modules.j2ee.weblogic9.j2ee;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.common.ui.BrokenServerLibrarySupport;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryFactory;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.netbeans.modules.j2ee.weblogic9.config.WLServerLibraryManager;
import org.netbeans.modules.j2ee.weblogic9.config.WLServerLibrarySupport;
import org.netbeans.modules.j2ee.weblogic9.config.WLServerLibrarySupport.WLServerLibrary;
import org.netbeans.modules.javaee.specs.support.spi.JaxRsStackSupportImplementation;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Denis Anisimov
 */
class JaxRsStackSupportImpl implements JaxRsStackSupportImplementation {
    private static final String API = "api"; // NOI18N
    private static final String JAX_RS = "jax-rs"; // NOI18N
    private static final String JERSEY = "jersey"; //NOI18N
    private static final String JSON = "json"; //NOI18N
    private static final String JETTISON = "jettison"; //NOI18N
    private static final String ROME = "rome"; //NOI18N
    private static final String WEBLOGIC_JAX_RS = "weblogic jax-rs"; // NOI18N
    
    private static final Logger LOG = Logger.getLogger( JaxRsStackSupportImpl.class.getCanonicalName());

    private final WLJ2eePlatformFactory.J2eePlatformImplImpl platformImpl;

    private final Version serverVersion;
    
    private Library jerseyLibrary;

    JaxRsStackSupportImpl(WLJ2eePlatformFactory.J2eePlatformImplImpl platformImpl,
            Version serverVersion) {
        this.platformImpl = platformImpl;
        this.serverVersion = serverVersion;
    }

    @Override
    public boolean addJsr311Api(Project project) {
        if ( hasJee6Profile() ){
            /*
             *  This method should not be called if JAX-RS API is already in CP, 
             *  so let's add required jar if it had been called even with JEE6 profile
             */
            FileObject core = getJarFile("com.sun.jersey.core_");   // NOI18N
            if (core == null) {
                // 12.1.2, 12.1.3
                core = getJarFile("jersey-core"); // NOI18N
            }
            if (core != null) {
                try {
                    return addJars(project, Collections.singleton( core.getURL() ));
                }
                catch( FileStateInvalidException e ){
                    Logger.getLogger(JaxRsStackSupportImpl.class.getName()).
                        log(Level.WARNING, 
                                "Exception during extending a project classpath", e); //NOI18N
                    return false;
                }
            }
        }
        return addJsr311ServerLibraryApi(project);
    }

    @Override
    public boolean extendsJerseyProjectClasspath(Project project) {
        if ( hasJee6Profile() ){
            try {
                Library lib = getJerseyLibrary();
                if (lib == null) {
                    List<URL> urls = getJerseyJars();
                    return addJars(project,  urls );
                }
                else {
                    FileObject sourceRoot = getSourceRoot(project);
                    if ( sourceRoot == null ){
                        return false;
                    }
                    ProjectClassPathModifier.addLibraries(new Library[]{lib},
                            sourceRoot , JavaClassPathConstants.COMPILE_ONLY);
                    return true;
                }
            }
            catch( FileStateInvalidException e ){
                Logger.getLogger(JaxRsStackSupportImpl.class.getName()).
                log(Level.WARNING, 
                        "Exception during extending a project classpath", e); //NOI18N
                return false;
            }
            catch(IOException e ){
                Logger.getLogger(JaxRsStackSupportImpl.class.getName()).
                log(Level.WARNING, 
                        "Exception during extending a project classpath", e); //NOI18N
                return false;
            }
        }
        return extendsJerseyServerLibraries(project);
    }

    @Override
    public void removeJaxRsLibraries(Project project) {
        if ( hasJee6Profile() ){
            Library lib = getJerseyLibrary();
            try {
                if (lib != null) {
                    FileObject sourceRoot = getSourceRoot(project);
                    if (sourceRoot == null) {
                        return;
                    }
                    ProjectClassPathModifier.removeLibraries(
                            new Library[] { lib }, sourceRoot,
                            JavaClassPathConstants.COMPILE_ONLY);
                }
                else {
                    List<URL> urls = getJerseyJars();
                    FileObject core = getJarFile("com.sun.jersey.core_"); // NOI18N
                    if (core == null) {
                        // 12.1.2, 12.1.3
                        core = getJarFile("jersey-core"); // NOI18N
                    }
                    if (core != null) {
                        urls.add(core.getURL());
                    }
                    removeLibraries(project, urls);
                }
            }
            catch (FileStateInvalidException e) {
                Logger.getLogger(JaxRsStackSupportImpl.class.getName()).log(
                        Level.WARNING,
                        "Exception during extending a project classpath", e); // NOI18N
            }
            catch (IOException e) {
                Logger.getLogger(JaxRsStackSupportImpl.class.getName()).log(
                        Level.WARNING,
                        "Exception during extending a project classpath", e); // NOI18N
            }
            
            J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
            if ( provider == null ){
                return;
            }
            J2eeModule j2eeModule = provider.getJ2eeModule();
            if ( j2eeModule == null ){
                return;
            }
            File weblogicXml = j2eeModule.getDeploymentConfigurationFile(
                    "WEB-INF/weblogic.xml"); // NOI18N
            if ( weblogicXml == null ){
                return;
            }
            FileObject config = FileUtil.toFileObject( FileUtil.
                    normalizeFile( weblogicXml));
            
            /*
             *  TODO : all subsequent code should be rewritten to use OM 
             *  instead of direct file read and DOM modification.  
             */
            Document document = readDocument( config );
            
            NodeList nodeList = document.getElementsByTagName( 
                    "container-descriptor");     // NOI18N
            if ( nodeList.getLength() == 0 ){
                return;
            }
            Element containerDescriptor = (Element)nodeList.item(0);
            nodeList = containerDescriptor.getElementsByTagName( 
                "prefer-application-packages");        // NOI18N
            
            if ( nodeList.getLength() ==0  ){
                return;
            }
            Element appPackages = (Element)nodeList.item(0);
            containerDescriptor.removeChild( appPackages );
        }
    }
    
    @Override
    public void configureCustomJersey( Project project ){
        if ( hasJee6Profile() ){
            J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
            if ( provider == null ){
                return;
            }
            J2eeModule j2eeModule = provider.getJ2eeModule();
            if ( j2eeModule == null ){
                return;
            }
            File weblogicXml = j2eeModule.getDeploymentConfigurationFile(
                    "WEB-INF/weblogic.xml"); // NOI18N
            if ( weblogicXml == null ){
                return;
            }
            FileObject config = FileUtil.toFileObject( FileUtil.
                    normalizeFile( weblogicXml));
            
            /*
             *  TODO : all subsequent code should be rewritten to use OM 
             *  instead of direct file read and DOM modification.  
             */
            Document document = readDocument( config );
            Element root = document.getDocumentElement();
            
            NodeList nodeList = document.getElementsByTagName( 
                    "container-descriptor");     // NOI18N
            Element containerDescriptor = null;
            if ( nodeList.getLength() == 0 ){
                containerDescriptor = document.createElement(
                        "container-descriptor");     // NOI18N
                root.appendChild( containerDescriptor );
            }
            else {
                containerDescriptor = (Element)nodeList.item(0);
            }
            nodeList = containerDescriptor.getElementsByTagName( 
                    "prefer-application-packages");        // NOI18N
            Element appPackages = null;
            if ( nodeList.getLength() == 0 ){
                appPackages = document.createElement( 
                        "prefer-application-packages");     // NOI18N
                containerDescriptor.appendChild( appPackages );
            }
            else {
                appPackages = (Element)nodeList.item(0);
            }
            addPackage(document, appPackages, "com.sun.jersey.*");  // NOI18N  
            addPackage(document, appPackages, "com.sun.research.ws.wadl.*");  // NOI18N
            addPackage(document, appPackages, "com.sun.ws.rs.ext.*");  // NOI18N  
            addPackage(document, appPackages, "org.objectweb.asm.*");  // NOI18N 
            addPackage(document, appPackages, "org.codehaus.jackson.*");  // NOI18N 
            addPackage(document, appPackages, "org.codehaus.jettison.*");  // NOI18N 
            addPackage(document, appPackages, "javax.ws.rs.*");  // NOI18N 
            save(document, config);
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.javaee.specs.support.spi.JaxRsStackSupportImplementation#isBundled(java.lang.String)
     */
    @Override
    public boolean isBundled( String classFqn ) {
        try {
            List<URL> urls = getJerseyJars();
            for (URL url : urls) {
                FileObject root = URLMapper.findFileObject(url);
                if (FileUtil.isArchiveFile(root)) {
                    root = FileUtil.getArchiveRoot(root);
                }
                String path = classFqn.replace('.', '/') + ".class";
                if (root.getFileObject(path) != null) {
                    return true;
                }
            }
            return false;
        }
        catch (FileStateInvalidException e) {
            return false;
        }
    }
    
//====REMOVE THIS ALONG WITH CHANGE CODE FOR DIRECT WEBLOGIC.XML MODIFICATION========

    private void addPackage( Document document,
            Element appPackages , String packageName )
    {
        Element packageElement = document.createElement( 
                    "package-name");                                    // NOI18N
        Text text = document.createTextNode( packageName );    
        packageElement.appendChild(text);
        appPackages.appendChild( packageElement );
    }
    
    public void save( final Document document, final FileObject fileObject ) {
        RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                FileLock lock = null;
                OutputStream os = null;

                try {
                    DocumentType docType = document.getDoctype();
                    TransformerFactory factory = TransformerFactory.newInstance();
                    Transformer transformer = factory.newTransformer();
                    DOMSource source = new DOMSource(document);

                    lock = fileObject.lock();
                    os = fileObject.getOutputStream(lock);
                    StreamResult result = new StreamResult(os);

                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");        //NOI18N

                    transformer.setOutputProperty(OutputKeys.METHOD, "xml");        //NOI18N

                    transformer.transform(source, result);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } 
                        catch (IOException ex) {
                            LOG.log(Level.WARNING, null, ex);  
                        }
                    }

                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        });
    }
    
    private Document readDocument( FileObject xml ){
        DocumentBuilder builder = getDocumentBuilder();
        Document document = null;
        if (builder == null) {
            LOG.log(Level.INFO, "Cannot get XML parser for "+xml);  // NOI18N
            return null;
        }
        FileLock lock = null;
        InputStream is = null;

        try {
            lock = xml.lock();
            is = xml.getInputStream();
            document = builder.parse(is);
        } catch (SAXParseException ex) {
            LOG.log(Level.INFO, "Cannot parse "+xml, ex);       // NOI18N
        } catch (SAXException ex) {
            LOG.log(Level.INFO, "Cannot parse "+xml, ex);       // NOI18N
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Cannot parse "+xml, ex);       // NOI18N
        } finally {
            if (is != null) {
                try {
                    is.close();
                } 
                catch (IOException ex) {
                    LOG.log(Level.WARNING, null, ex);  
                }
            }

            if (lock != null) {
                lock.releaseLock();
            }
        }
        return document;
    }
    
    private DocumentBuilder getDocumentBuilder() {
        DocumentBuilder builder = null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setIgnoringComments(false);
        factory.setIgnoringElementContentWhitespace(false);
        factory.setCoalescing(false);
        factory.setExpandEntityReferences(false);
        factory.setValidating(false);

        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }

        return builder;
    }
    
//=======================================================================================
    
    private boolean extendsJerseyServerLibraries( Project project ) {
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        Collection<ServerLibrary> serverLibraries = getServerJerseyLibraries();
        if (provider != null && serverLibraries.size() > 0) {
            try {
                for (ServerLibrary serverLibrary : serverLibraries) {
                    provider.getConfigSupport().configureLibrary(ServerLibraryDependency.minimalVersion(serverLibrary.getName(), serverLibrary.getSpecificationVersion(), serverLibrary.getImplementationVersion()));
                }
                Preferences prefs = ProjectUtils.getPreferences(project, ProjectUtils.class, true);
                prefs.put(BrokenServerLibrarySupport.OFFER_LIBRARY_DEPLOYMENT, Boolean.TRUE.toString());
                return true;
            } catch (org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException ex) {
                Logger.getLogger(JaxRsStackSupportImpl.class.getName()).log(Level.INFO, 
                        "Exception during extending a project classpath", ex); //NOI18N
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean hasJee6Profile(){
        Set<Profile> profiles = platformImpl.getSupportedProfiles();
        return profiles.contains(Profile.JAVA_EE_6_FULL) || 
                profiles.contains(Profile.JAVA_EE_6_WEB) ;
    }
    
    private Library getJerseyLibrary() {
        if (jerseyLibrary == null) {
            File modulesFolder = WLJ2eePlatformFactory.getMiddlewareModules(platformImpl.getMiddlewareHome());
            jerseyLibrary = JerseyLibraryHelper
                    .getJerseyLibrary(serverVersion, FileUtil.toFileObject(modulesFolder));
        }
        return jerseyLibrary;
    }
    
    private List<URL> getJerseyJars() throws FileStateInvalidException {
        if (WLDeploymentFactory.VERSION_1212.isBelowOrEqual(serverVersion)) {
            return getJerseyJars1212();
        }
        return getJerseyJars12();
    }
    
    private List<URL> getJerseyJars12() throws FileStateInvalidException {
        FileObject client = getJarFile("com.sun.jersey.client_");   // NOI18N
        List<URL> urls = new LinkedList<URL>();
        if ( client != null){
            urls.add( client.toURL());
        }
        FileObject json = getJarFile("com.sun.jersey.json_");       // NOI18N
        if ( json != null){
            urls.add( json.toURL());
        }
        FileObject multipart = getJarFile("com.sun.jersey.multipart_");// NOI18N
        if ( multipart != null){
            urls.add( multipart.toURL());
        }
        FileObject server = getJarFile("com.sun.jersey.server_");       // NOI18N
        if ( server != null){
            urls.add( server.toURL());
        }
        FileObject asl = getJarFile("org.codehaus.jackson.core.asl_");  // NOI18N
        if ( asl != null){
            urls.add( asl.toURL());
        }
        FileObject jacksonJaxRs = getJarFile("org.codehaus.jackson.jaxrs_");// NOI18N
        if ( jacksonJaxRs != null){
            urls.add( jacksonJaxRs.toURL());
        }
        FileObject jacksonMapper = getJarFile("org.codehaus.jackson.mapper.asl_");// NOI18N
        if ( jacksonMapper != null){
            urls.add( jacksonMapper.toURL());
        }
        FileObject jacksonXc = getJarFile("org.codehaus.jackson.xc_");// NOI18N
        if ( jacksonXc != null){
            urls.add( jacksonXc.toURL());
        }
        FileObject jettison = getJarFile("org.codehaus.jettison_");// NOI18N
        if ( jettison != null){
            urls.add( jettison.toURL());
        }
        return urls;
    }
    
    private List<URL> getJerseyJars1212() throws FileStateInvalidException {
        FileObject client = getJarFile("jersey-client-");   // NOI18N
        List<URL> urls = new LinkedList<URL>();
        if ( client != null){
            urls.add( client.toURL());
        }
        FileObject json = getJarFile("jersey-json-");       // NOI18N
        if ( json != null){
            urls.add( json.toURL());
        }
        FileObject multipart = getJarFile("jersey-json-multipart");// NOI18N
        if ( multipart != null){
            urls.add( multipart.toURL());
        }
        FileObject server = getJarFile("jersey-server-");       // NOI18N
        if ( server != null){
            urls.add( server.toURL());
        }
        FileObject asl = getJarFile("jackson-core-asl-");  // NOI18N
        if ( asl != null){
            urls.add( asl.toURL());
        }
        FileObject jacksonJaxRs = getJarFile("jackson-jaxrs-");// NOI18N
        if ( jacksonJaxRs != null){
            urls.add( jacksonJaxRs.toURL());
        }
        FileObject jacksonMapper = getJarFile("jackson-mapper-asl");// NOI18N
        if ( jacksonMapper != null){
            urls.add( jacksonMapper.toURL());
        }
        FileObject jacksonXc = getJarFile("jackson-xc-");// NOI18N
        if ( jacksonXc != null){
            urls.add( jacksonXc.toURL());
        }
        FileObject jettison = getJarFile("jettison-");// NOI18N
        if ( jettison != null){
            urls.add( jettison.toURL());
        }
        return urls;
    }
   
    private boolean addJsr311ServerLibraryApi( Project project ) {
        /*
         *  WL has a deployable JSR311 war. But it will appear in the project's
         *  classpath only after specific user action. This is unacceptable
         *  because generated source code requires classes independently
         *  of additional explicit user actions.
         *
         *  So the following code returns true only if there is already deployed
         *  JSR311 library on the server
         */
        WLServerLibrarySupport support = getLibrarySupport();
        if (support == null) {
            return false;
        }
        Set<WLServerLibrary> libraries = support.getDeployedLibraries();
        for (WLServerLibrary library : libraries) {
            String title = library.getImplementationTitle();
            if (title != null && title.toLowerCase(Locale.ENGLISH).contains(JAX_RS) && title.toLowerCase(Locale.ENGLISH).contains(API)) {
                ServerLibrary apiLib = ServerLibraryFactory.createServerLibrary(library);
                J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
                try {
                    provider.getConfigSupport().configureLibrary(ServerLibraryDependency.minimalVersion(apiLib.getName(), apiLib.getSpecificationVersion(), apiLib.getImplementationVersion()));
                } catch (org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException ex) {
                    Logger.getLogger(JaxRsStackSupportImpl.class.getName()).log(Level.INFO, null, ex);
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    private Collection<ServerLibrary> getServerJerseyLibraries() {
        if (platformImpl.getDeploymentManager().isRemote()) {
            return Collections.emptySet();
        }

        WLServerLibraryManager manager = getLibraryManager();
        Collection<ServerLibrary> libraries = new LinkedList<ServerLibrary>();
        libraries.addAll(findJerseyLibraries(manager.getDeployableLibraries()));
        libraries.addAll(findJerseyLibraries(manager.getDeployedLibraries()));
        return libraries;
    }

    private Collection<ServerLibrary> findJerseyLibraries(Collection<ServerLibrary> collection) {
        Collection<ServerLibrary> result = new ArrayList<ServerLibrary>(collection.size());
        for (Iterator<ServerLibrary> iterator = collection.iterator(); iterator.hasNext();) {
            ServerLibrary library = iterator.next();
            String title = library.getImplementationTitle();
            if (title == null) {
                continue;
            }
            title = title.toLowerCase(Locale.ENGLISH);
            if (title.contains(JERSEY) || title.contains(JSON) || title.contains(ROME) 
                    || title.contains(JETTISON) || title.contains(WEBLOGIC_JAX_RS)) {
                result.add(library);
            }
        }
        return result;
    }

    private WLServerLibraryManager getLibraryManager() {
        return new WLServerLibraryManager(platformImpl.getDeploymentManager());
    }

    private WLServerLibrarySupport getLibrarySupport() {
        if (platformImpl.getDeploymentManager().isRemote()) {
            return null;
        }
        return new WLServerLibrarySupport(platformImpl.getDeploymentManager());
    }
    
    private FileObject getJarFile(final String startName) {
        File modulesFolder = WLJ2eePlatformFactory.getMiddlewareModules(platformImpl.getMiddlewareHome());
        if (!modulesFolder.isDirectory()){
            return null;
        }
        File[] children = modulesFolder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(startName) && name.endsWith(".jar");
            }
        });
        if (children != null) {
            for (File fileChild : children) {
                FileObject child = FileUtil.toFileObject(fileChild);
                if (child != null) {
                    return child;
                }
            }
        }
        return null;
    }
    
    private boolean addJars( Project project, Collection<URL> jars ){
        List<URL> urls = new ArrayList<URL>();
        for (URL url : jars) {
            if ( FileUtil.isArchiveFile( url)){
                urls.add(FileUtil.getArchiveRoot(url));
            }
        }
        FileObject sourceRoot = getSourceRoot(project);
        if ( sourceRoot == null ){
            return false;
        }
        String classPathType;
        if ( hasJee6Profile() ){
            classPathType = JavaClassPathConstants.COMPILE_ONLY;
        }
        else {
            classPathType = ClassPath.COMPILE;
        }
        try {
            ProjectClassPathModifier.addRoots(urls.toArray(new URL[0]), sourceRoot, classPathType);
        } 
        catch(UnsupportedOperationException ex) {
            return false;
        }
        catch ( IOException e ){
            return false;
        }
        return true;
    }
    
    private FileObject getSourceRoot(Project project){
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroups == null || sourceGroups.length < 1) {
            return null;
        }
        return sourceGroups[0].getRootFolder();
    }
    
    private void removeLibraries(Project project, Collection<URL> urls) {
        if ( urls.size() >0 ){
            SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
            if (sourceGroups == null || sourceGroups.length < 1) {
                return;
            }
            FileObject sourceRoot = sourceGroups[0].getRootFolder();
            String[] classPathTypes = new String[]{ ClassPath.COMPILE , ClassPath.EXECUTE };
            for (String type : classPathTypes) {
                try {
                    ProjectClassPathModifier.removeRoots(urls.toArray(new URL[0]), sourceRoot, type);
                }    
                catch(UnsupportedOperationException ex) {
                    Logger.getLogger( JaxRsStackSupportImpl.class.getName() ).
                            log (Level.INFO, null , ex );
                }
                catch( IOException e ){
                    Logger.getLogger( JaxRsStackSupportImpl.class.getName() ).
                            log(Level.INFO, null , e );
                }
            }     
        }
    }

}
