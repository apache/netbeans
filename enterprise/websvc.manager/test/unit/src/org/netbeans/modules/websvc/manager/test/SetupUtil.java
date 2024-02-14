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

package org.netbeans.modules.websvc.manager.test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.Manifest;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.websvc.manager.WebServiceManager;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbCollections;

/**
 *
 * @author quynguyen
 */
public class SetupUtil {
    private static final String WORKDIR_SPACES = "user directory";
    private static final String WORKDIR = "userdirectory";
    private static final String TEST_WSDL = "/org/netbeans/modules/websvc/manager/resources/uszip-asmx-catalog/www.webservicemart.com/uszip.asmx.wsdl";
    private static final String TEST_CATALOG = "/org/netbeans/modules/websvc/manager/resources/uszip-asmx-catalog/catalog.xml";
    private static final String CATALOG_FILE = "uszip-asmx-catalog/catalog.xml";
    private static final String WSDL_FILE = "uszip-asmx-catalog/www.webservicemart.com/uszip.asmx.wsdl";
    
    private static final String ENDORSED_REF = "modules/ext/jaxws22/api/jakarta.xml.ws-api.jar";
    private static final String JAXWS_LIB_PROPERTY = "libs.jaxws21.classpath";
    
    public static SetupData commonSetUp(File workingDir) throws Exception {
        SetupData data = new SetupData();
        
        File userDir = new File(workingDir.getParentFile(),
                System.getProperty("os.name").startsWith("Windows") ? WORKDIR_SPACES : WORKDIR);
        System.getProperties().setProperty("netbeans.user", userDir.getAbsolutePath());
        
        File websvcHome = new File(WebServiceManager.WEBSVC_HOME);
        data.setWebsvcHome(websvcHome);
        
        File websvcUserDir = new File(WebServiceManager.WEBSVC_HOME);
        websvcUserDir.mkdirs();
        
        File wsdlFile = new File(websvcUserDir, WSDL_FILE);
        File catalogFile = new File(websvcUserDir, CATALOG_FILE);

        retrieveURL(wsdlFile, SetupUtil.class.getResource(TEST_WSDL));
        retrieveURL(catalogFile, SetupUtil.class.getResource(TEST_CATALOG));
        
        copy(wsdlFile, workingDir);
        
        data.setLocalWsdlFile(wsdlFile);
        data.setLocalCatalogFile(catalogFile);
        data.setLocalOriginalWsdl(new File(workingDir, wsdlFile.getName()));
        
        MainFS fs = new MainFS();
        fs.setConfigRootDir(websvcHome.getParentFile());
        TestRepository.defaultFileSystem = fs;
        
        MockServices.setServices(DialogDisplayerNotifier.class, InstalledFileLocatorImpl.class, TestRepository.class);
        
        InstalledFileLocatorImpl locator = (InstalledFileLocatorImpl)Lookup.getDefault().lookup(InstalledFileLocator.class);
        locator.setUserConfigRoot(websvcHome.getParentFile());
        
        File targetBuildProperties = new File(websvcUserDir.getParentFile().getParentFile(), "build.properties");
        generatePropertiesFile(targetBuildProperties);
        
        return data;
    }

    public static void commonTearDown() throws Exception {
        DialogDisplayer dd = DialogDisplayer.getDefault();
        if (dd instanceof DialogDisplayerNotifier) {
            ((DialogDisplayerNotifier)dd).removeAllListeners();
        }
        
        MockServices.setServices();
    }
    
    public static void copy(File src, File target) throws Exception {        
        if (src.isFile()) {
            File targetFile = new File(target, src.getName());
            
            FileInputStream is = new FileInputStream(src);
            FileOutputStream os = new FileOutputStream(targetFile);
            
            FileChannel inputChannel = is.getChannel();
            FileChannel outputChannel = os.getChannel();
            
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            outputChannel.close();
        }else {
            File newDir = new File(target, src.getName());
            newDir.mkdirs();
            
            File[] dirFiles = src.listFiles();
            if (dirFiles != null) {
                for (int i = 0; i < dirFiles.length; i++) {
                    copy(dirFiles[i], newDir);
                }
            }
        }
    }
    
    public static void retrieveURL(File targetFile, URL url) throws IOException {
        targetFile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(targetFile);
        byte[] readBuffer = new byte[1024];
        
        InputStream is = url.openStream();
        int bytesRead = 0;
        while ( (bytesRead = is.read(readBuffer, 0, 1024)) > 0) {
            fos.write(readBuffer, 0, bytesRead);
        }
        fos.flush();
        fos.close();
        is.close();
    }
    
    private static void generatePropertiesFile(File target) throws IOException {
        String separator = System.getProperty("path.separator");
        File apiBase = InstalledFileLocator.getDefault().locate(ENDORSED_REF, null, true).getParentFile();
        File jaxWsBase = apiBase.getParentFile();
        
        FileFilter jarFilter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(".jar");
            }
        };
        
        File[] apiJars = apiBase.listFiles(jarFilter);
        File[] implJars = jaxWsBase.listFiles(jarFilter);
        
        Properties result = new Properties();
        StringBuffer classpath = new StringBuffer();
        
        for (int i = 0; i < apiJars.length; i++) {
            String pathElement = apiJars[i].getAbsolutePath() + separator;
            classpath.append(pathElement);
        }
        
        for (int i = 0; i < implJars.length; i++) {
            classpath.append(implJars[i].getAbsolutePath());
            if (i != implJars.length - 1) {
                classpath.append(separator);
            }
        }
        
        result.setProperty(JAXWS_LIB_PROPERTY, classpath.toString());
        
        FileOutputStream fos = new FileOutputStream(target);
        result.store(fos, "build.properties file");
    }
    
    public static final class TestRepository extends Repository {
        static FileSystem defaultFileSystem = null;
        
        public TestRepository() {
            super(defaultFileSystem);
        }
    }
    
    // Taken from org.openide.filesystems.ExternalUtil to allow layer files to be
    // loaded into the default filesystem (since core/startup is in the classpath
    // and registers a default Repository that we do not want)
    public static final class MainFS extends MultiFileSystem implements LookupListener {
        private final Lookup.Result<FileSystem> ALL = Lookup.getDefault().lookupResult(FileSystem.class);
        private final FileSystem MEMORY = FileUtil.createMemoryFileSystem();
        private final XMLFileSystem layers = new XMLFileSystem();
        
        private final LocalFileSystem configRoot = new LocalFileSystem();
        
        public void setConfigRootDir(File root) throws Exception {
            configRoot.setRootDirectory(root);
        }
        
        public MainFS() {
            ALL.addLookupListener(this);
            
            List<URL> layerUrls = new ArrayList<URL>();
            ClassLoader l = Thread.currentThread().getContextClassLoader();
            try {
                for (URL manifest : NbCollections.iterable(l.getResources("META-INF/MANIFEST.MF"))) { // NOI18N
                    InputStream is = manifest.openStream();
                    try {
                        Manifest mani = new Manifest(is);
                        String layerLoc = mani.getMainAttributes().getValue("OpenIDE-Module-Layer"); // NOI18N
                        if (layerLoc != null) {
                            URL layer = l.getResource(layerLoc);
                            if (layer != null) {
                                layerUrls.add(layer);
                            }
                        }
                    } finally {
                        is.close();
                    }
                }
                layers.setXmlUrls(layerUrls.toArray(new URL[0]));
            } catch (Exception x) {
            }
            resultChanged(null); // run after add listener - see PN1 in #26338
        }
        
        private FileSystem[] computeDelegates() {
            List<FileSystem> arr = new ArrayList<FileSystem>();
            arr.add(MEMORY);
            arr.add(layers);
            arr.add(configRoot);
            arr.addAll(ALL.allInstances());
            return arr.toArray(new FileSystem[0]);
        }
    
        public void resultChanged(LookupEvent ev) {
            setDelegates(computeDelegates());
        }
    }
    
    
}
