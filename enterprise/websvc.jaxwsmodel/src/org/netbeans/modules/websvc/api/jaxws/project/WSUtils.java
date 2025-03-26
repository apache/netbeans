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

package org.netbeans.modules.websvc.api.jaxws.project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.jaxwsmodel.project.TransformerUtils;
import org.netbeans.modules.websvc.jaxwsmodel.project.WsdlNamespaceHandler;
import org.netbeans.modules.xml.retriever.RetrieveEntry;
import org.netbeans.modules.xml.retriever.Retriever;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
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
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;


/**
 *
 * @author mkuchtiak
 */
public class WSUtils {
    
    private static final String WSIMPORT_BAD_VERSION = "wsimport.bad.version";  // NOI18N
    private static String SUN_DOMAIN_12_DTD_SUFFIX =
            "lib" + File.separator + "dtds" + File.separator + "sun-domain_1_2.dtd";
    private static String SUN_DOMAIN_13_DTD_SUFFIX =
            "lib" + File.separator + "dtds" + File.separator + "sun-domain_1_3.dtd";
    
    private static final String JAX_WS_XML_PATH = "nbproject/jax-ws.xml"; // NOI18N

    public static final String JAX_WS_ENDORSED="JAX-WS-ENDORSED"; //NOI18N
    
    /** downloads XML resources from source URI to target folder
     * (USAGE : this method can download a wsdl file and all wsdl/XML schemas,
     * that are recursively imported by this wsdl)
     * @param targetFolder A folder inside a NB project (ONLY) to which the retrieved resource will be copied to. All retrieved imported/included resources will be copied relative to this directory.
     * @param source URI of the XML resource that will be retrieved into the project
     * @return FileObject of the retrieved resource in the local file system
     */
    public static FileObject retrieveResource(FileObject targetFolder, URI source)
            throws java.net.UnknownHostException, java.net.URISyntaxException, IOException{
        try {
            Retriever retriever = Retriever.getDefault();
            FileObject result = retriever.retrieveResource(targetFolder, source);
            if (result==null) {
                Map<RetrieveEntry,Exception> map = retriever.getRetrievedResourceExceptionMap();
                if (map!=null) {
                    for(Entry<RetrieveEntry,Exception> entry: map.entrySet()){
                        RetrieveEntry key = entry.getKey();
                        Exception exc = entry.getValue();
                        if (exc instanceof IOException) {
                            throw (IOException)exc;
                        } else if (exc instanceof java.net.URISyntaxException) {
                            throw (java.net.URISyntaxException)exc;
                        } else  {
                            IOException ex = new IOException(NbBundle.getMessage(
                                    WSUtils.class,"ERR_retrieveResource",
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
    
    public static String findProperServiceName(String name, JaxWsModel jaxWsModel) {
        if (jaxWsModel.findServiceByName(name)==null) return name;
        for (int i = 1;; i++) {
            String destName = name + "_" + i; // NOI18N
            if (jaxWsModel.findServiceByName(destName)==null)
                return destName;
        }
    }
    
    public static void retrieveJaxWsFromResource(FileObject projectDir) throws IOException {
        final String jaxWsContent =
                readResource(WSUtils.class.getResourceAsStream("/org/netbeans/modules/websvc/jaxwsmodel/resources/jax-ws.xml")); //NOI18N
        final FileObject nbprojFo = projectDir.getFileObject("nbproject"); //NOI18N
        assert nbprojFo != null : "Cannot find nbproject directory"; //NOI18N
        FileSystem fs = nbprojFo.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject jaxWsFo = FileUtil.createData(nbprojFo, "jax-ws.xml");//NOI18N
                FileLock lock = jaxWsFo.lock();
                BufferedWriter bw = null;
                try {
                    bw = new BufferedWriter(new OutputStreamWriter(
                            jaxWsFo.getOutputStream(lock), StandardCharsets.UTF_8));
                    bw.write(jaxWsContent);
                } finally {
                    lock.releaseLock();
                    if( bw!= null ){
                        bw.close();
                    }
                }
            }
        });
    }
    
    public static void retrieveHandlerConfigFromResource(final FileObject targetDir, final String handlerConfigName) throws IOException {
        final String handlerContent =
                readResource(WSUtils.class.getResourceAsStream("/org/netbeans/modules/websvc/jaxwsmodel/resources/handler.xml")); //NOI18N
        FileSystem fs = targetDir.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject handlerFo = FileUtil.createData(targetDir, handlerConfigName);//NOI18N
                FileLock lock = handlerFo.lock();
                BufferedWriter bw = null;
                OutputStream os = null;
                try {
                    os = handlerFo.getOutputStream(lock);
                    bw = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                    bw.write(handlerContent);
                    bw.close();
                } finally {
                    if(bw != null)
                        bw.close();
                    if(os != null)
                        os.close();
                    if(lock != null)
                        lock.releaseLock();
                }
            }
        });
    }
    
    public static String getJAXWSVersion(File appSvrRoot){
        if(appSvrRoot == null) return JAXWSVersionProvider.JAXWS20;
        
        File dtdFile_12 = new File(appSvrRoot, SUN_DOMAIN_12_DTD_SUFFIX);        
        File dtdFile_13 = new File(appSvrRoot, SUN_DOMAIN_13_DTD_SUFFIX);
                        
        //if there is a sun-domain_1_2.dtd AND there is no sun-domain_1_3.dtd in
        //the lib/dtds directory, then it is AppServer 9.0 which uses JAXWS 2.0
        if(dtdFile_12.exists() && !dtdFile_13.exists()){
            return JAXWSVersionProvider.JAXWS20;
        }
        return JAXWSVersionProvider.JAXWS21;
    }
    
    public static void generateSunJaxwsFile(final FileObject targetDir) throws IOException {
        final String sunJaxwsContent =
                readResource(WSUtils.class.getResourceAsStream("/org/netbeans/modules/websvc/jaxwsmodel/resources/sun-jaxws.xml")); //NOI18N
        FileSystem fs = targetDir.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject sunJaxwsFo = FileUtil.createData(targetDir, "sun-jaxws.xml");//NOI18N
                FileLock lock = sunJaxwsFo.lock();
                BufferedWriter bw = null;
                OutputStream os = null;
                OutputStreamWriter osw = null;
                try {
                    os = sunJaxwsFo.getOutputStream(lock);
                    osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                    bw = new BufferedWriter(osw);
                    bw.write(sunJaxwsContent);
                } finally {
                    if(bw != null)
                        bw.close();
                    if(os != null)
                        os.close();
                    if(osw != null)
                        osw.close();
                    if(lock != null)
                        lock.releaseLock();
                }
            }
        });
    }
    
    private static String readResource(InputStream is) throws IOException {
        // read the config from resource first
        StringBuffer sb = new StringBuffer();
        String lineSep = System.getProperty("line.separator");//NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append(lineSep);
            line = br.readLine();
        }
        br.close();
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
        FileLock lock = null;
        try {
            DataObject dObj = DataObject.find(f);
            if (dObj != null) {
                SaveCookie save = dObj.getLookup().lookup(SaveCookie.class);
                if (save!=null) save.save();
            }
            lock = f.lock();
            f.delete(lock);
        } catch(java.io.IOException e) {
            NotifyDescriptor ndd =
                    new NotifyDescriptor.Message(NbBundle.getMessage(WSUtils.class, "MSG_Unable_Delete_File", f.getNameExt()),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(ndd);
        } finally {
            if(lock != null) {
                lock.releaseLock();
            }
        }
    }
    
    /** Copy files from source folder to target folder recursively */
    public static void copyFiles(FileObject sourceFolder, FileObject targetFolder) throws IOException {
        FileObject[] children = sourceFolder.getChildren();
        for (int i=0;i<children.length;i++) {
            if (children[i].isFolder()) {
                String folderName = children[i].getNameExt();
                // don't copy system/VCS files
                if (!folderName.startsWith(".")) { //NOI18N
                    FileObject folder = targetFolder.getFileObject(children[i].getNameExt());
                    if (folder == null) {
                        folder = targetFolder.createFolder(children[i].getNameExt());
                    }
                    // recursive call
                    copyFiles(children[i], folder);
                }
            } else {
                String fileName = children[i].getName();
                // don't copy system/VCS files
                if (!fileName.startsWith(".")) {
                    String fileExt = children[i].getExt();
                    FileObject oldFile = targetFolder.getFileObject(fileName, fileExt);
                    if (oldFile != null) {
                        oldFile.delete();
                    }
                    children[i].copy(targetFolder, fileName, fileExt);
                }
            }
        }
    }
    
    public static FileObject backupAndGenerateJaxWs(FileObject projectDir, FileObject oldJaxWs, RuntimeException reason) throws IOException {
        DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(NbBundle.getMessage(WSUtils.class,"ERR_corruptedJaxWs",oldJaxWs.getPath(),reason.getMessage()),NotifyDescriptor.ERROR_MESSAGE));
        FileObject parent = oldJaxWs.getParent();
        FileObject oldBackup = parent.getFileObject("jax-ws.xml.old"); //NOI18N
        FileLock lock = null;
        if (oldBackup!=null) {
            // remove old backup
            try {
                lock = oldBackup.lock();
                oldBackup.delete(lock);
            } finally {
                if (lock!=null) lock.releaseLock();
            }
        }
        // renaming old jax-ws.xml;
        try {
            lock = oldJaxWs.lock();
            oldJaxWs.rename(lock, "jax-ws.xml","old"); //NOI18N
        } finally {
            if (lock!=null) lock.releaseLock();
        }
        retrieveJaxWsFromResource(projectDir);
        return projectDir.getFileObject(JAX_WS_XML_PATH);
    }
    
    private static String getJaxWsApiDir() {
        File jaxwsApi = InstalledFileLocator.getDefault().locate("modules/ext/jaxws22/api/jakarta.xml.ws-api.jar", null, false); // NOI18N
        if (jaxwsApi!=null) {
            File jaxbApi =  InstalledFileLocator.getDefault().locate("modules/ext/jaxb/api/jaxb-api.jar", null, false); // NOI18N
            return jaxwsApi.getParent()+(jaxbApi != null? ":"+jaxbApi.getParent() : ""); //NOI18N
        }
        return null;
    }

    private  static final String ENDORSED = "classpath/endorsed"; //NOI18N

    public static void addJaxWsApiEndorsed(Project project, FileObject srcRoot) throws IOException {
        Library jaxWsApiLib = LibraryManager.getDefault().getLibrary(JAX_WS_ENDORSED);
        if (!isJaxWs22InJDK(srcRoot)) {
            if (jaxWsApiLib == null) {
                jaxWsApiLib = createJaxWsApiLibrary();
            }
            ClassPath classPath = ClassPath.getClassPath(srcRoot, ENDORSED);
            if (classPath == null || classPath.findResource("javax/xml/ws/EndpointContext.class") == null) { //NOI18N
                ProjectClassPathModifier.addLibraries(new Library[]{jaxWsApiLib}, srcRoot, ENDORSED);
            }
        } else if (jaxWsApiLib != null) {
            ClassPath classPath = ClassPath.getClassPath(srcRoot, ENDORSED);
            if (classPath != null && classPath.findResource("javax/xml/ws/EndpointContext.class") != null) {
                try {
                    ProjectClassPathModifier.removeLibraries(new Library[]{jaxWsApiLib}, srcRoot, ENDORSED);
                } catch (UnsupportedOperationException ex) {
                    Logger.getLogger(WSUtils.class.getName()).log(Level.INFO, null, ex);
                }
            }
        }        
    }
    
    private static boolean isJaxWs22InJDK(FileObject srcRoot) {
        ClassPath cp = ClassPath.getClassPath(srcRoot, ClassPath.BOOT);
        return cp != null && cp.findResource("javax/xml/ws/EndpointContext.class") != null;
    }

    public static Library createJaxWsApiLibrary() throws IOException {
        List<URL> apiJars = getJaxWsApiJars();
        if (apiJars.size() > 0) {
            Map<String, List<URL>> map = Collections.<String, List<URL>>singletonMap("classpath", apiJars); //NOI18N
            return LibraryManager.getDefault().createLibrary("j2se", JAX_WS_ENDORSED, map); //NOI18N
        }
        return null;
    }

    private static List<URL> getJaxWsApiJars() throws IOException {
        List<URL> urls = new ArrayList<URL>();
        File apiJar = InstalledFileLocator.getDefault().locate("modules/ext/jaxws22/api/jakarta.xml.ws-api.jar", null, false); // NOI18N
        if (apiJar != null) {
            URL url = new URL("jar:nbinst://org.netbeans.modules.websvc.jaxws21api/modules/ext/jaxws22/api/jakarta.xml.ws-api.jar!/");
            /*URL url = apiJar.toURI().toURL();
            if (FileUtil.isArchiveFile(url)) {
                urls.add(FileUtil.getArchiveRoot(url));
            }*/
            urls.add(url);
        }
        apiJar = InstalledFileLocator.getDefault().locate("modules/ext/jaxb/api/jaxb-api.jar", null, false); // NOI18N
        if (apiJar != null) {
            URL url = new URL("jar:nbinst://org.netbeans.libs.jaxb/modules/ext/jaxb/api/jaxb-api.jar!/");
            /*URL url = apiJar.toURI().toURL();
            if (FileUtil.isArchiveFile(url)) {
                urls.add(FileUtil.getArchiveRoot(url));
            }*/
            urls.add(url);
        }
        return urls;
    }
    
    public static FileObject findJaxWsFileObject(Project project) {
        return project.getProjectDirectory().getFileObject(TransformerUtils.JAX_WS_XML_PATH);
    }
    
    /** copy jax-ws.xml from default filesystem to nbproject directory,
     *  generate JaxWsModel,
     *  add FileChangeListener to jax-ws.xml file object
     */
    public static FileObject createJaxWsFileObject(final Project project) throws IOException {
        
        try {
            return ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<FileObject>() {
                public FileObject run() throws IOException {
                    retrieveJaxWsFromResource(project.getProjectDirectory());
                    FileObject jaxWsFo = findJaxWsFileObject(project);
                    assert jaxWsFo != null : "Cannot find jax-ws.xml in project's nbproject directory"; //NOI18N
                    if (jaxWsFo!=null) {
                        JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
                        if (jaxWsModel!=null) jaxWsModel.setJaxWsFile(jaxWsFo);
                    }
                    return jaxWsFo;
                }

        

            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }
    
    public static EditableProperties getEditableProperties(final Project prj,final  String propertiesPath) 
        throws IOException {        
        try {
            return
            ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<EditableProperties>() {
                public EditableProperties run() throws IOException {                                             
                    FileObject propertiesFo = prj.getProjectDirectory().getFileObject(propertiesPath);
                    EditableProperties ep = null;
                    if (propertiesFo!=null) {
                        InputStream is = null; 
                        ep = new EditableProperties();
                        try {
                            is = propertiesFo.getInputStream();
                            ep.load(is);
                        } finally {
                            if (is!=null) is.close();
                        }
                    }
                    return ep;
                }
            });
        } catch (MutexException ex) {
            return null;
        }
    }
    
    public static void storeEditableProperties(final Project prj, final  String propertiesPath, final EditableProperties ep) 
        throws IOException {        
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {                                             
                    FileObject propertiesFo = prj.getProjectDirectory().getFileObject(propertiesPath);
                    if (propertiesFo!=null) {
                        OutputStream os = null;
                        try {
                            os = propertiesFo.getOutputStream();
                            ep.store(os);
                        } finally {
                            if (os!=null) os.close();
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException ex) {
        }
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
            List<String> packageParts = new ArrayList<String>();
            List<String> nsParts = new ArrayList<String>();
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
                    List<String> list = new ArrayList<String>();
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
    
    public static String getPackageNameForWsdl(File wsdl) {
        WsdlNamespaceHandler handler = new WsdlNamespaceHandler();
        try {
            handler.parse(wsdl);
        } catch (ParserConfigurationException ex) {
            ErrorManager.getDefault().log(ex.getLocalizedMessage());
        } catch (IOException ex) {
            ErrorManager.getDefault().log(ex.getLocalizedMessage());
        } catch (SAXException ex) {
            if (WsdlNamespaceHandler.SAX_PARSER_FINISHED_OK.equals(ex.getMessage())) {
                // THIS IS OK, parser finished correctly
            } else {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            }
        }
        String targetNamespace = handler.getTargetNamespace();
        if (targetNamespace != null) {
            return getPackageNameFromNamespace(targetNamespace);
        } else {
            return null;
        }
    }

    public static FileObject retrieveJaxWsCatalogFromResource(final FileObject webInf) throws IOException {
        assert  webInf != null : "WEB-INF (META-INF) directory"; //NOI18N
        final String jaxWsContent =
                readResource(WSUtils.class.getResourceAsStream(
                        "/org/netbeans/modules/websvc/jaxwsmodel/resources/jax-ws-catalog.xml")); //NOI18N
        FileSystem fs = webInf.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject jaxWsCatalog = FileUtil.createData(webInf, "jax-ws-catalog.xml");//NOI18N
                FileLock lock = jaxWsCatalog.lock();
                BufferedWriter bw =null;
                try {
                    bw = new BufferedWriter(new OutputStreamWriter(
                            jaxWsCatalog.getOutputStream(lock), StandardCharsets.UTF_8));
                    bw.write(jaxWsContent);
                } finally {
                    lock.releaseLock();
                    if ( bw!= null) {
                        bw.close();
                    }
                }
            }
        });
        return webInf.getFileObject("jax-ws-catalog.xml");
    }


    public static boolean hasClients(FileObject jaxWsFo) throws IOException {
        BufferedReader br = null;
        boolean found = false;
        try {
            br = new BufferedReader(new InputStreamReader( 
                    new FileInputStream(FileUtil.toFile(jaxWsFo)), StandardCharsets.UTF_8));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.contains("<client ")) { //NOI18N
                    found = true;
                    break;
                }
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }
        return found;
    }

    public static boolean hasServiceOrClient(FileObject jaxWsFo) throws IOException {
        BufferedReader br = null;
        boolean found = false;
        try {
            br = new BufferedReader(new InputStreamReader( 
                    new FileInputStream(FileUtil.toFile(jaxWsFo)), StandardCharsets.UTF_8));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.contains("<client ") || line.contains("<service ")) { //NOI18N
                    found = true;
                    break;
                }
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }
        return found;
    }
    
    public static Properties identifyWsimport( final AntProjectHelper helper ){
        if ( helper == null ){
            return null;
        }
        EditableProperties props = helper.getProperties(
                AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String wsImportCp = props.getProperty("j2ee.platform.wsimport.classpath");  // NOI18N
        if ( wsImportCp ==null || wsImportCp.length() == 0 ){
            return null;
        }
        PropertyEvaluator evaluator = helper.getStandardPropertyEvaluator();
        String[] roots = wsImportCp.split(":");
        List<FileObject> cpItems = new ArrayList<FileObject>(roots.length);
        for (String root : roots) {
            String wsImportCpItem = evaluator.evaluate(root);
            FileObject fileObject = FileUtil.toFileObject( 
                    FileUtil.normalizeFile( new File(wsImportCpItem)));
            if ( fileObject == null ){
                continue;
            }
            if ( fileObject.isFolder() ){
                cpItems.add( fileObject);
            }
            else if ( FileUtil.isArchiveFile(fileObject)){
                cpItems.add( FileUtil.getArchiveRoot(fileObject));
            }
        }
        
        ClassPath classPath = ClassPathSupport.createClassPath(cpItems.toArray(new FileObject[0]));
        FileObject wsImport = classPath.findResource(
                    "com/sun/tools/ws/ant/WsImport.class");                         // NOI18N
        if ( wsImport == null ){
            return null;
        }
        FileObject wsImportRoot = classPath.findOwnerRoot(wsImport);
        FileObject manifest = wsImportRoot.getFileObject("META-INF/MANIFEST.MF");   // NOI18N
        try {
            Manifest mnfst = new Manifest( manifest.getInputStream());
            String version = mnfst.getMainAttributes().getValue(
                    "Implementation-Version");                                      // NOI18N
            if ( version == null ){
                return null;
            }
            if ( version.startsWith("2.2.")){                                       // NOI18N
                /*
                 *  version is 2.2 but it has minor release numbers so it is 
                 *  newer 2.2 version with fixed wsimport issue 
                 */
                if ( evaluator.getProperty(WSIMPORT_BAD_VERSION)!= null ){
                    setProjectProperty(helper, WSIMPORT_BAD_VERSION,                  
                            null);  
                }
                return null;
            }
            else if ( version.startsWith("2.2")){                                   // NOI18N
                // buggy 2.2 version
                FileObject badRoot = FileUtil.getArchiveFile(wsImport);
                if ( badRoot== null){
                    badRoot = classPath.findOwnerRoot(wsImport);
                }
                NotifyDescriptor notifyDescriptor =
                    new NotifyDescriptor.Message(NbBundle.getMessage(WSUtils.class, 
                            "ERR_WsimportBadVersion", version, badRoot.getPath()),  // NOI18N
                            NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(notifyDescriptor);
                Properties properties = new Properties();
                properties.put(WSIMPORT_BAD_VERSION, Boolean.TRUE.toString());    
                setProjectProperty(helper, WSIMPORT_BAD_VERSION,                  
                        Boolean.TRUE.toString());           
                return properties;
            }
            else {
                // version is not 2.2 ( older or newer )
                if ( evaluator.getProperty(WSIMPORT_BAD_VERSION)!= null ){
                    setProjectProperty(helper, WSIMPORT_BAD_VERSION,                  
                            null);  
                }
                return null;
            }
        }
        catch( IOException e ){
            Logger.getLogger(WSUtils.class.getName()).log(Level.INFO, null , e);
            return null;
        }
        catch (MutexException e) {
            Logger.getLogger(WSUtils.class.getName()).log(Level.INFO, null, e);
            return null;
        } 
    }

    private static void setProjectProperty( final AntProjectHelper helper,
            final String propertyName, final String value ) throws MutexException, IOException
    {
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            
            @Override
            public void run() {
             // and save the project
                try {
                    EditableProperties ep = helper.getProperties(
                            AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    if ( value == null ){
                        ep.remove(propertyName);
                    }
                    else {
                        ep.setProperty(propertyName, value);
                    }
                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                    Project project = FileOwnerQuery.getOwner(helper.getProjectDirectory());
                    ProjectManager.getDefault().saveProject(project);  
                } 
                catch(IOException ioe) {
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, 
                            ioe.getLocalizedMessage(), ioe);
                }                
            }
        });
    }
}
