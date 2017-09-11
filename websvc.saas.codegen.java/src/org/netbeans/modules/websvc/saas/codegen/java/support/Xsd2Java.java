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
