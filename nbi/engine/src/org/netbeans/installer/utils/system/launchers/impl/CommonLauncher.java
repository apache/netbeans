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

package org.netbeans.installer.utils.system.launchers.impl;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StreamUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.helper.EngineResources;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.JavaCompatibleProperties;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.system.launchers.Launcher;
import org.netbeans.installer.utils.system.launchers.LauncherProperties;
import org.netbeans.installer.utils.system.launchers.LauncherResource;
import org.netbeans.installer.utils.progress.Progress;

/**
 *
 * @author Dmitry Lipin
 */
public abstract class CommonLauncher extends Launcher {
    private static final int BUF_SIZE = 102400;
    
    protected CommonLauncher(LauncherProperties pr) {
        super(pr);
    }
    protected long addData(FileOutputStream fos, InputStream is, Progress progress, long total) throws IOException{
        byte[] buffer = new byte[BUF_SIZE];
        int readBytes;
        int start = progress.getPercentage();
        long totalRead = 0;
        long perc = 0;
        while (is.available() > 0) {
            readBytes = is.read(buffer);
            totalRead += readBytes;
            fos.write(buffer, 0, readBytes);
            if(total!=0) {
                perc = (Progress.COMPLETE * totalRead) / total;
                progress.setPercentage(start + (int) perc);
            }
        }
        fos.flush();
        return totalRead;
    }
    
    protected long addData(FileOutputStream fos, File file, Progress progress, long total) throws IOException{
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return addData(fos,fis,progress,total);
        } finally {
            if(fis!=null) {
                try {
                    fis.close();
                } catch(IOException ex) {
                    LogManager.log(ex);
                }
            }
        }
        
    }
    
    //add rnd data
    protected void addData(FileOutputStream fos) throws IOException {
        double rand = Math.random() * Byte.MAX_VALUE;
        //fos.write(new byte[] {(byte)rand});
        fos.write(new byte[] {'#'});
    }
    
    protected long addString(FileOutputStream fos, String string, boolean isUnicode) throws IOException {
        byte [] bytes;
        if(isUnicode) {
            bytes = string.getBytes("UNICODE"); //NOI18N
        } else {
            bytes = string.getBytes();
        }
        fos.write(bytes);
        return bytes.length;
    }
    
    protected long addStringBuilder(FileOutputStream fos, StringBuilder builder, boolean isUnicode) throws IOException {
        return addString(fos, builder.toString() , isUnicode);
    }
    
    
    protected void checkAllParameters() throws IOException {
        checkBundledJars();
        checkJvmFile();
        checkOutputFileName();
        checkI18N();
        checkMainClass();
        checkTestJVMFile();
        checkTestJVMClass();
        checkCompatibleJava();
    }
    
    
    
    private void checkI18N() throws IOException {
        // i18n properties suffix
        LogManager.log(ErrorLevel.DEBUG, "Check i18n...");
        String prefix   = getI18NResourcePrefix();
        String baseName = getI18NBundleBaseName();

        if(i18nMap.isEmpty() && prefix!=null && baseName!=null) {
            LogManager.log("... i18n properties were not set. using default from resources");
            loadI18n(prefix, baseName);
        }
    }

    private void loadI18n(String prefix, String baseName) throws IOException {
        // load from engine`s entries list
        LogManager.log("... loading i18n properties using prefix \"" + prefix + "\" with base name \"" + baseName + "\"");
        InputStream is = ResourceUtils.getResource(EngineResources.ENGINE_CONTENTS_LIST);
        String[] resources = StringUtils.splitByLines(StreamUtils.readStream(is));
        is.close();
        List<String> list = new ArrayList<String>();
        LogManager.log("... total engine resources: " + resources.length); //NOI18N
        for (String res : resources) {
            if (res.startsWith(prefix + baseName) &&
                    res.endsWith(FileUtils.PROPERTIES_EXTENSION)) {
                list.add(res);
            }
        }
        LogManager.log("... total i18n resources: " + list.size()); //NOI18N
        setI18n(list);
    }

    protected void checkBundledJars()  throws IOException  {
        LogManager.log(ErrorLevel.DEBUG, "Checking bundled jars...");
        for(LauncherResource f : jars) {
            if(f.isBundled()) {
                checkParameter("bundled JAR", f.getPath());
            }
        }
        if(jars.size()==0) {
            throw new IOException(ResourceUtils.getString(
                    CommonLauncher.class, ERROR_NO_JAR_FILES_KEY));
        }
    }
    
    protected void checkJvmFile()  throws IOException  {
        LogManager.log(ErrorLevel.DEBUG, "Checking JVMs...");
        for(LauncherResource file: jvms) {
            if(file.isBundled()) {
                InputStream is = null;
                try {
                    is = file.getInputStream();
                    if(is == null) {
                        throw new IOException(ResourceUtils.getString(
                                CommonLauncher.class,ERROR_CANNOT_FIND_JVM_FILE_KEY, file.getPath()));
                    }
                } finally {
                    if(is!=null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            LogManager.log(e);
                        }
                    }
                }
                
            }}
    }
    
    private void checkMainClass() throws IOException {
        LogManager.log(ErrorLevel.DEBUG, "Checking main class...");
        // check main-class parameter
        // read main class from jar file if it is not specified
        if(mainClass==null) {
            // get the first            
            for(LauncherResource file : jars) {
                if(file.isBundled() && !file.isBasedOnResource()) {
                    JarFile jarFile = new JarFile(new File(file.getPath()));
                    Manifest manifest = jarFile.getManifest();
                    jarFile.close();
                    if(manifest!=null) {
                        mainClass = manifest.getMainAttributes().
                                getValue(Attributes.Name.MAIN_CLASS);
                    }
                    if(mainClass!=null) {
                        return;
                    }
                }
            }
            throw new IOException(ResourceUtils.getString(CommonLauncher.class, 
                    ERROR_MAIN_CLASS_UNSPECIFIED_KEY));
        } else{
            for(LauncherResource file : jars) {
                if(file.isBundled() && !file.isBasedOnResource() ) {
                    JarFile jarFile = new JarFile(new File(file.getPath()));
                    boolean mainClassExists = jarFile.getJarEntry(
                            ResourceUtils.getResourceClassName(mainClass))!= null;
                    jarFile.close();
                    if(mainClassExists) {                        
                        return;
                    }                    
                } else {
                    return;
                }
            }
            
            throw new IOException(ResourceUtils.getString(CommonLauncher.class, 
                    ERROR_CANNOT_FIND_CLASS_KEY, mainClass));
        }
    }
    private void checkTestJVMClass() throws IOException {
        LogManager.log(ErrorLevel.DEBUG, "Checking testJVM class...");
        if(testJVMClass==null) {
            testJVMClass = JavaUtils.TEST_JDK_CLASSNAME;
        }
    }
    
    private void checkParameter(String paramDescr, String parameter) throws IOException {
        if(parameter==null) {
            throw new IOException("Parameter " + paramDescr + " can`t be null");
        }
    }
    
    protected void checkParameter(String paramDescr, File parameter) throws IOException {
        if(parameter==null) {
            throw new IOException("Parameter " + paramDescr + " can`t be null");
        }
        if(!parameter.exists()) {
            throw new IOException(paramDescr + " doesn`t exist at " + parameter);
        }
    }
    
    protected void checkCompatibleJava() throws IOException {
        LogManager.log(ErrorLevel.DEBUG, "Checking compatible java properties...");
        if(compatibleJava.isEmpty()) {
            compatibleJava.addAll(getDefaultCompatibleJava(getMinimumJavaVersion()));
        }        
    }

    @Override
    public List<JavaCompatibleProperties> getDefaultCompatibleJava(Version version) {
        final List<JavaCompatibleProperties> list = new ArrayList<JavaCompatibleProperties>();
        list.add(new JavaCompatibleProperties(version.toJdkStyle(), null, null, null, null));
        return list;
    }
    
    private int getMajorVersion(InputStream is) throws IOException {
        DataInputStream classfile = null;
        int minor_version = 0;
        int major_version = 0;
        classfile = new DataInputStream(is);
        int magic = classfile.readInt();
        if (magic == 0xcafebabe) {
            minor_version = classfile.readUnsignedShort();
            major_version = classfile.readUnsignedShort();
        }

        if (major_version == 45 && minor_version == 3) {
            return 1;
        } else if (minor_version == 0) {
            switch(major_version) {
                case 46: return 2;
                case 47: return 3;
                case 48: return 4;
                case 49: return 5;
                case 50: return 6;
                case 51: return 7;
                case 52: return 8;
                case 53: return 9;
                case 54: return 10;
                case 55: return 11;
                case 56: return 12;
                case 57: return 13;
                case 58: return 14;
                default : return -1;
            }
        } else {            
            return -1;
        }
    }
    private int getMajorVersion(JarFile jar, final String resource) {
        
        InputStream is = null;

        try {
            is = jar.getInputStream(jar.getJarEntry(resource));
            if (is != null) {
                return getMajorVersion(is);
            }
        } catch (IOException e) {
            LogManager.log(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                LogManager.log(e);
            }
        }
        return -1;
    }
          
    protected Version getMinimumJavaVersion() {
        int majorVersion = -1;

        for (LauncherResource file : jars) {
            if (file.isBundled() && !file.isBasedOnResource()) {
                File jarFile = new File(file.getPath());
                JarFile jar = null;
                try {
                    jar = new JarFile(jarFile);
                    Manifest manifest = jar.getManifest();
                    String resource = null;
                    if (manifest != null) {
                        String mainClassName = manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
                        if (mainClassName != null) {
                            resource = ResourceUtils.getResourceClassName(mainClassName);
                            if(jar.getJarEntry(resource)==null) {
                                resource = null;
                            }
                        }
                    }
                    if (resource == null) {
                        // no main class.. search for other .class resources
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry e = entries.nextElement();
                            if (e.getName().endsWith(".class")) {
                                resource = e.getName();
                                break;
                            }
                        }
                    }
                    if (resource != null) {
                        majorVersion = Math.max(majorVersion, getMajorVersion(jar, resource));
                    }
                } catch (IOException e) {
                    LogManager.log(e);
                } finally {
                    if (jar != null) {
                        try {
                            jar.close();
                        } catch (IOException e) {
                            LogManager.log(e);
                        }
                    }
                }
            }
        }
        if (majorVersion == -1) {
            try {
                final String resource = ResourceUtils.getResourceClassName(CommonLauncher.class);
                majorVersion = getMajorVersion(ResourceUtils.getResource(resource));
            } catch (IOException e) {
                LogManager.log(e);
            }
        }
        
        return Version.getVersion((majorVersion == -1) ?
             System.getProperty("java.specification.version") :
                 "1." + majorVersion);
    }
    protected void checkTestJVMFile()   throws IOException {
        LogManager.log(ErrorLevel.DEBUG, "Checking testJVM file...");
        if(testJVMFile==null) {
            testJVMFile = new LauncherResource(JavaUtils.TEST_JDK_RESOURCE);
        }
    }
    
    protected void checkOutputFileName() throws IOException {
        LogManager.log(ErrorLevel.DEBUG, "Checking output file name...");
        if(outputFile==null) {
            LogManager.log(ErrorLevel.DEBUG, "... output file name is not specified, getting name from the first bundled file");
            String outputFileName  = null;
            for(LauncherResource file : jars) {
                if(file.isBundled() && !file.isBasedOnResource()) {
                    File jarFile = new File(file.getPath());
                    String name = jarFile.getName();
                    if(name.endsWith(FileUtils.JAR_EXTENSION)) {
                        outputFileName = name.substring(0,
                                name.lastIndexOf(FileUtils.JAR_EXTENSION));
                    }
                    outputFileName += getExtension();
                    outputFile = new File(jarFile.getParent(), outputFileName);
                    break;
                }
            }
            if(outputFile==null) {
                String exString = ResourceUtils.getString(CommonLauncher.class, ERROR_CANNOT_GET_OUTPUT_NAME_KEY);
                LogManager.log(exString);
                throw new IOException(exString);
            }
        } else if (addExtenstion) {
            LogManager.log(ErrorLevel.DEBUG, "... output is defined, adding extension");
            // outfile is defined but we need to set launcher-dependent extension
            outputFile = new File(outputFile.getParent(),
                    outputFile.getName() + getExtension());
            addExtenstion = false;
        }
        LogManager.log("... out file : " + outputFile); //NOI18N
    }
    protected String getJavaCounter(int counter) {
        return "{" + counter + "}";
    }
    protected long getBundledFilesSize() throws IOException {
        long total = 0;
        
        for (LauncherResource jvmFile : jvms) {
            total += jvmFile.getSize();
        }
        total += testJVMFile.getSize();
        
        for (LauncherResource jarFile : jars) {
            total += jarFile.getSize();
        }
        for(LauncherResource other : otherResources) {
            total += other.getSize();
        }
        return total;
    }
    protected long getBundledFilesNumber() {
        long total=0;
        for (LauncherResource jvmFile : jvms) {
            if ( jvmFile .isBundled()) {
                total ++;
            }
        }
        if(testJVMFile.isBundled()) {
            total++;
        }
        for (LauncherResource jarFile : jars) {
            if ( jarFile.isBundled()) {
                total ++;
            }
        }
        for (LauncherResource other : otherResources) {
            if (other.isBundled()) {
                total ++;
            }
        }
        return total;
    }
    
    private static final String ERROR_NO_JAR_FILES_KEY =
            "CnL.error.no.jars";//NOI18N
    private static final String ERROR_CANNOT_FIND_JVM_FILE_KEY =
            "CnL.error.cannot.find.jvm.file";//NOI18N
    private static final String ERROR_CANNOT_FIND_CLASS_KEY =
            "CnL.error.cannot.find.class";//NOI18N
    private static final String ERROR_MAIN_CLASS_UNSPECIFIED_KEY =
            "CnL.error.main.class.unspecified";//NOI18N
    private static final String ERROR_CANNOT_GET_OUTPUT_NAME_KEY =
            "CnL.error.cannot.get.output.name";//NOI18N
}
