/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils.system.launchers;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PropertyResourceBundle;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.JavaCompatibleProperties;

/**
 *
 * @author Dmitry Lipin
 */
public class LauncherProperties implements Cloneable {    
    protected File stubFile;
    protected List<LauncherResource> jars;
    protected List<LauncherResource> jvms;
    protected List <LauncherResource> otherResources;
    
    protected HashMap <String, PropertyResourceBundle> i18nMap;
    protected LauncherResource testJVMFile;
    protected File outputFile;
    protected boolean addExtenstion;
    protected List <String> jvmArguments;
    protected List <String> appArguments;
    protected String mainClass;
    protected String testJVMClass;
    protected List <JavaCompatibleProperties> compatibleJava;
    protected String i18nPrefix;
    protected String i18nBundleBaseName;

    public LauncherResource getTestJVMFile() {
        return testJVMFile;
    }
    
    public LauncherProperties(LauncherProperties nl) {
        appArguments = nl.appArguments;
        jvmArguments = nl.jvmArguments;
        i18nMap = nl.i18nMap;
        i18nPrefix = nl.i18nPrefix;
        i18nBundleBaseName = nl.i18nBundleBaseName;
        jars = nl.jars;
        jvms = nl.jvms;
        outputFile = nl.outputFile;
        addExtenstion = nl.addExtenstion;
        compatibleJava = nl.compatibleJava;
        mainClass = nl.mainClass;
        testJVMClass = nl.testJVMClass;
        stubFile = nl.stubFile;
        testJVMFile = nl.testJVMFile;
        otherResources = nl.otherResources;    
    }
    public LauncherProperties() {
        compatibleJava = new ArrayList <JavaCompatibleProperties> ();
        jvmArguments = new ArrayList <String>();
        appArguments = new ArrayList <String>();
        i18nMap = new HashMap <String, PropertyResourceBundle>();
        i18nPrefix = null;
        i18nBundleBaseName = null;
        jars = new ArrayList <LauncherResource> ();
        jvms = new ArrayList <LauncherResource> ();
        otherResources = new ArrayList<LauncherResource> ();        
    }
    public void setLauncherStub(File launcherStub) {
        this.stubFile = launcherStub;
    }
    
    public void addJar(LauncherResource file) {
        jars.add(file);
    }
    public String getMainClass() {
        return mainClass;
    }
    public String getTestJVMClass() {
        return testJVMClass;
    }
    
    public void setJvmArguments(String[] jvmArguments) {
        this.jvmArguments = new ArrayList <String> ();
        for(String s : jvmArguments) {
            this.jvmArguments.add(s);
        }
    }
    public void setJvmArguments(List <String> jvmArguments) {
        this.jvmArguments = jvmArguments;        
    }
    
    public void setI18n(File i18nDir) throws IOException {
        loadPropertiesMap(getPropertiesFiles(i18nDir));
    }
    
    public void setI18n(File [] files) throws IOException  {
        loadPropertiesMap(files);
    }
    
    public void setI18n(String [] resources) throws IOException {
        loadPropertiesMap(resources);
    }
    
    public void setI18n(List <String>resources) throws IOException {
        loadPropertiesMap(resources);
    }
    
    public void setI18nPrefix(String i18nPrefix) throws IOException {
        this.i18nPrefix = i18nPrefix;
    }
    public void setI18nBundleBaseName(String i18nBundleBaseName) throws IOException {
        this.i18nBundleBaseName = i18nBundleBaseName;
    }
    public String getI18NResourcePrefix() {
        return i18nPrefix;
    }
    public String getI18NBundleBaseName() {
        return i18nBundleBaseName;
    }

    public void setOutput(File output) {
        setOutput(output, false);
    }
    
    public void setOutput(File output, boolean addExt) {
        this.outputFile    = output;
        this.addExtenstion = addExt;
    }
    
    public void setTestJVM(LauncherResource testJVM) {
        this.testJVMFile = testJVM;
    }
    
    public void addCompatibleJava(JavaCompatibleProperties javaProp) {
        compatibleJava.add(javaProp);
    }
    
    public void setAppArguments(String[] appArguments) {
        this.appArguments = new ArrayList <String> ();
        for(String s : appArguments) {
            this.appArguments.add(s);
        }
    }
    public void setAppArguments(List <String> appArguments) {
        this.appArguments = appArguments;
    }
    
    public File getOutputFile() {
        return outputFile;
    }
    
    public List<LauncherResource> getJars() {
        return jars;
    }
    public List <String> getAppArguments() {
        return appArguments;
    }
    
    public List<String> getJvmArguments() {
        return jvmArguments;
    }
    
    public List <JavaCompatibleProperties> getJavaCompatibleProperties() {
        return compatibleJava;
    }
    
    public File getStubFile() {
        return stubFile;
    }
    
    public void addJVM(LauncherResource location) {
        jvms.add(location);
    }
    public List<LauncherResource> getJVMs() {
        return jvms;
    }
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }
    public void setTestJVMClass(String testClass) {
        this.testJVMClass = testClass;
    }
    HashMap <String, PropertyResourceBundle> getI18nMap() {
        return i18nMap;
    }
    
    public void addOtherResource(LauncherResource resource) {
        otherResources.add(resource);
    }
    
    public List <LauncherResource> getOtherResources() {
        return otherResources;
    }
    
    private String getLocaleName(String name) {
        String loc = StringUtils.EMPTY_STRING;
        int idx = name.indexOf("_");
        int end = name.indexOf(FileUtils.PROPERTIES_EXTENSION);
        if(idx!=-1) {
            loc = name.substring(idx+1,end);
        }
        return loc;
    }
    
    private PropertyResourceBundle getBundle(File file) throws IOException {
        return getBundle(file.getPath(), new FileInputStream(file));
    }
    
    private PropertyResourceBundle getBundle(String dest, InputStream is) throws IOException {
        if(is==null) {
            throw new IOException(ResourceUtils.getString(LauncherProperties.class, 
                    ERROR_CANNOT_LOAD_BUNDLE_KEY, dest)); //NOI18N
        }
        
        try {
            return new PropertyResourceBundle(is);
        } catch (IOException ex) {
            throw new IOException(ResourceUtils.getString(LauncherProperties.class, 
                    ERROR_CANNOT_LOAD_BUNDLE_KEY, dest)); //NOI18N
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                ex=null;
            }
        }
    }
    
// resources should be in form of <dir>/<dir>/<dir>/<file>
    private void loadPropertiesMap(String [] resources) throws IOException {
        i18nMap.clear();
        for(String resource: resources) {
            String loc = getLocaleName(ResourceUtils.getResourceFileName(resource));
            i18nMap.put(loc, getBundle(resource, ResourceUtils.getResource(resource)));
        }
    }
    private void loadPropertiesMap(List<String> resources) throws IOException {
        String [] array = new String [resources.size()];
        for(int i=0;i<resources.size();i++) {
            array [i] = resources.get(i);
        }
        loadPropertiesMap(array);
    }
    
    private File[] getPropertiesFiles(File dir) throws IOException {
        if(!dir.exists()) {
            throw new IOException(ResourceUtils.getString(
                    LauncherProperties.class, ERROR_DIRECTORY_DONT_EXIST_KEY,dir));
        }
        if(!dir.isDirectory()) {
            throw  new IOException(ResourceUtils.getString(
                    LauncherProperties.class, ERROR_NOT_DIRECTORY_KEY, dir));
        }
        
        File[] files = dir.listFiles(new FileFilter() {
            public boolean accept(File filename) {
                return filename.getName().endsWith(FileUtils.PROPERTIES_EXTENSION); }
        }
        );
        
        if(files==null || files.length==0) {
            throw  new IOException(ResourceUtils.getString(
                    LauncherProperties.class, ERROR_NO_FILES_KEY, dir));
        }
        return files;
    }
    
    private void loadPropertiesMap(File [] files) throws IOException {
        i18nMap.clear();
        for(File f: files) {
            String loc = getLocaleName(f.getName());
            LogManager.log("Adding bundle with locale [" + loc + "] using file " + f);
            i18nMap.put(loc,getBundle(f));
        }
    }
    private static final String ERROR_CANNOT_LOAD_BUNDLE_KEY = 
            "LP.error.cannot.load.bundle";//NOI18N
    private static final String ERROR_NO_FILES_KEY = 
            "LP.error.no.files";//NOI18N
    private static final String ERROR_NOT_DIRECTORY_KEY = 
            "LP.error.not.directory";//NOI18N
    private static final String ERROR_DIRECTORY_DONT_EXIST_KEY =
            "LP.error.directory.do.not.exist";//NOI18N
}
