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

package org.netbeans.modules.websvc.saas.codegen.java.support;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 *
 * @author nam
 */
public class Xsd2Java {
    
    public static final String JAXB_SCRIPT_PATH = "modules/ext/build-jaxb.xml";
    
    /**
     * absolute location of schema file
     */
    public static final String PROP_XSD_FILE = "xsd.file"; 
    /**
     *  package name for generated JAXB classes
     */
    public static final String PROP_PACKAGE_NAME = "packageName"; 
    /**
     *  work directory for generating sources and classes into
     */
    public static final String PROP_JAXB_BASE = "jaxb.base";
    /**
     *  jaxb jar file path relative to jaxb.base
     */
    public static final String PROP_JAXB_JAR = "jaxb.jar";
    /**
     *  jaxb source jar file path relative to jaxb.base
     */
    public static final String PROP_JAXB_SRC_JAR = "jaxb.src.jar";
    /**
     *  Absolute path to NetBeans build.properties file under userdir
     */        
    public static final String PROP_USER_BUILD_PROPERTIES = "user.build.properties.file";
    
    private FileObject xsdFile;
    private String packageName;
    private String jaxbFolder;
    private String jaxbJarPath;
    private String jaxbSourceJarPath;

    public Xsd2Java(FileObject xsdFile, String packageName) {
        this.xsdFile = xsdFile;
        this.packageName = packageName;
        this.jaxbFolder = deriveJaxbBase(xsdFile);
        this.jaxbJarPath = deriveJaxbJarPath(xsdFile);
        this.jaxbSourceJarPath = deriveJaxbSourceJarPath(xsdFile);
    }
    
    private static String deriveJaxbBase(FileObject file) {
        return file.getName() + "-jaxb";  //NOI18N
    }
    
    private static String deriveJaxbJarPath(FileObject file) {
        return file.getName() + ".jar"; //NOI18N
    }
    
    private static String deriveJaxbSourceJarPath(FileObject file) {
        return file.getName() + "-src.jar"; //NOI18N
    }
    
    /**
     * Compile the XSD, if needed, to generate JAX-B binding library jars
     */
    public boolean compile() throws IOException {
        if (getJaxbJarFile() != null) {
            return true;
        }
        
        FileObject jaxbBaseDir = getOrCreateJaxbFolder();
        FileObject userBuildFile = SaasServicesModel.getWebServiceHome()
                .getParent().getParent().getFileObject("build.properties");
        File jaxbBasePath = FileUtil.normalizeFile(FileUtil.toFile(jaxbBaseDir));
        File xsdFilePath = FileUtil.normalizeFile(FileUtil.toFile(xsdFile));
        File userBuildPath = FileUtil.normalizeFile(FileUtil.toFile(userBuildFile));
        
        Properties p = new Properties();
        p.setProperty(PROP_XSD_FILE, xsdFilePath.getPath());
        p.setProperty(PROP_PACKAGE_NAME, packageName);
        p.setProperty(PROP_JAXB_BASE, jaxbBasePath.getPath());
        p.setProperty(PROP_JAXB_JAR, jaxbJarPath);
        p.setProperty(PROP_JAXB_SRC_JAR, jaxbSourceJarPath);
        p.setProperty(PROP_USER_BUILD_PROPERTIES, userBuildPath.getPath());
        
        return createJaxBJar(p);
    }
    
    /**
     * Compile or recompile the XSD
     */
    public boolean recompile() throws IOException {
        FileObject jaxbBaseDir = xsdFile.getParent().getFileObject(jaxbFolder);
        if (jaxbBaseDir != null) {
            jaxbBaseDir.delete();
        }
        return compile();
    }
    
    public FileObject getJaxbFolder() {
        return xsdFile.getParent().getFileObject(jaxbFolder);
    }
    
    public FileObject getOrCreateJaxbFolder() throws IOException {
        FileObject result = getJaxbFolder();
        if (result == null) {
            result = xsdFile.getParent().createFolder(jaxbFolder);
        }
        return result;
    }
    
    public FileObject getJaxbJarFile() {
        FileObject jaxbBaseDir = getJaxbFolder();
        if (jaxbBaseDir == null) {
            return null;
        }
        return jaxbBaseDir.getFileObject(jaxbJarPath);
    }
    
    public FileObject getJaxbSourceJarFile() {
        FileObject jaxbBaseDir = getJaxbFolder();
        if (jaxbBaseDir == null) {
            return null;
        }
        return jaxbBaseDir.getFileObject(jaxbSourceJarPath);
    }
    
    private boolean createJaxBJar(Properties properties) {
        try {
            ExecutorTask executorTask = ActionUtils.runTarget(FileUtil.toFileObject(getAntScript()),
                    new String[] {"xjc-jar"}, properties);
            executorTask.waitFinished();
            return (executorTask.result() == 0);
            
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }
    
    private File getAntScript() {
        return InstalledFileLocator.getDefault().locate(JAXB_SCRIPT_PATH,
                    "", // NOI18N
                    false);
    }
}
