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

package org.netbeans.installer.utils.system.launchers.impl;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.JavaCompatibleProperties;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.NativeUtils;
import org.netbeans.installer.utils.system.launchers.LauncherProperties;
import org.netbeans.installer.utils.system.launchers.LauncherResource;

/**
 *
 * @author Dmitry Lipin
 */
public class ExeLauncher extends CommonLauncher {
    private static final String EXE_EXT = ".exe"; //NOI18N
    private static final int EXE_STUB_FILL_SIZE = 450000;
    private static final long MAXDWORD = 4294967296L; // actually it is MAXDWORD + 1
    
    public static final String DEFAULT_WINDOWS_RESOURCE_SUFFIX =
            NativeUtils.NATIVE_LAUNCHER_RESOURCE_SUFFIX +
            "windows/"; //NOI18N
    public static final String I18N = "i18n"; //NOI18N
    public static final String EXE_LAUNCHER_STUB_NAME = "nlw.exe"; //NOI18N
    
    public static final String EXE_LAUNCHER_STUB =
            DEFAULT_WINDOWS_RESOURCE_SUFFIX + EXE_LAUNCHER_STUB_NAME;
    public static final String DEFAULT_WINDOWS_RESOURCE_I18N =
            DEFAULT_WINDOWS_RESOURCE_SUFFIX + I18N + "/";//NOI18N
    public static final String DEFAULT_WINDOWS_RESOURCE_I18N_BUNDLE_NAME =
            "launcher"; //NOI18N
    /**
     * See <code>ShLauncher#MIN_JAVA_VERSION_UNIX</code> for details.
     */
    public static final String MIN_JAVA_VERSION_WINDOWS_ALL   = "1.8.0";
    public static final String MIN_JAVA_VERSION_WINDOWS       = "1.5.0_03";
    public static final String MIN_JAVA_VERSION_WINDOWS_VISTA = "1.5.0_11";
    public static final String MIN_JAVA_VERSION_WINDOWS_2K8   = "1.5.0_17";
    public static final String MIN_JAVA_VERSION_WINDOWS_7     = "1.5.0_19";
    
    /* IBM does not report the update number so allow to work even on 1.5.0 */
    public static final String MIN_IBM_JAVA_VERSION = "1.5.0";

    public static final String OSNAME_WINDOWS    = "Windows";
    public static final String OSNAME_WINDOWS_XP = "XP";
    public static final String OSNAME_WINDOWS_VISTA = "Vista";
    public static final String OSNAME_WINDOWS_2K = "2000";
    public static final String OSNAME_WINDOWS_2K3 = "2003";
    public static final String OSNAME_WINDOWS_2K8 = "2008";
    public static final String OSNAME_WINDOWS_7   = "Windows 7";
    
    public ExeLauncher(LauncherProperties props) {
        super(props);
    }
    
    @Override
    public void initialize() throws IOException {
        LogManager.log("Checking EXE launcher parameters..."); //NOI18N
        checkAllParameters();
    }
    
    @Override
    public File create(Progress progress) throws IOException {
        
        FileOutputStream fos = null;
        try {
            progress.setPercentage(Progress.START);
            fos = new FileOutputStream(outputFile,false);
            
            long bundledSize = getBundledFilesSize();
            long total = bundledSize;
            if(stubFile!=null) {
                total += FileUtils.getSize(stubFile);
            }
            
            addExeInitialStub(fos, progress, total);
            
            LogManager.log("Adding i18n..."); //NOI18N
            addI18NStrings(fos);
            
            // jvm args
            addData(fos,jvmArguments, true);
            LogManager.log("JVM Arguments: " + //NOI18N
                    ((jvmArguments!=null) ?
                        StringUtils.asString(jvmArguments, " ") :
                        StringUtils.EMPTY_STRING));
            
            // app args
            addData(fos,appArguments, true);
            LogManager.log("App Arguments: " + //NOI18N
                    ((appArguments!=null) ?
                        StringUtils.asString(appArguments, " ") :
                        StringUtils.EMPTY_STRING));
            
            addData(fos, mainClass, true);
            LogManager.log("Main Class : " +   //NOI18N
                    mainClass);
            
            addData(fos, testJVMClass, true);
            LogManager.log("TestJVM Class : " +   //NOI18N
                    testJVMClass);
            
            // add java compatibility properties number
            addNumber(fos, Long.parseLong("" + compatibleJava.size()));
            addJavaCompatibleProperties(fos);
            
            //add overall bundled number and size
            addNumber(fos, getBundledFilesNumber());
            addNumber(fos, bundledSize, true);
            
            //add testJVM section
            addFileSection(fos, testJVMFile, progress,total);
            
            //java locations that the launcher should see at first
            LogManager.log("Adding JVM external locations and bundled files"); //NOI18N
            addData(fos, jvms, progress, total);
            
            // number of bundled and external jars
            LogManager.log("Adding bundled and external jars"); //NOI18N
            addData(fos, jars, progress, total);
            
            // a number of other resources
            LogManager.log("Adding other resources"); //NOI18N
            addData(fos, otherResources, progress, total);
            
        } catch (IOException ex) {
            LogManager.log(ex);
            try {
                if(fos!=null) {
                    fos.close();
                }
            } catch (IOException e) {
                LogManager.log(e);
            }
            
            try {
                FileUtils.deleteFile(outputFile);
            } catch (IOException e) {
                LogManager.log(e);
            }
            fos = null;
        } finally {
            if(fos!=null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    LogManager.log(ex.toString());
                    throw ex;
                }
            }
            progress.setPercentage(Progress.COMPLETE);
        }
        
        return outputFile;
    }
    
    @Override
    public String[] getExecutionCommand() {
        return new String [] {outputFile.getAbsolutePath()};
    }
    
    @Override
    public List<JavaCompatibleProperties> getDefaultCompatibleJava(Version version) {
        if (version.equals(Version.getVersion("1.5"))) {
            List<JavaCompatibleProperties> list = new ArrayList<JavaCompatibleProperties>();
            list.add(new JavaCompatibleProperties(
                    MIN_JAVA_VERSION_WINDOWS_VISTA, null, null, OSNAME_WINDOWS_VISTA, null));
            list.add(new JavaCompatibleProperties(
                    MIN_JAVA_VERSION_WINDOWS, null, null, OSNAME_WINDOWS_XP, null));
            list.add(new JavaCompatibleProperties(
                    MIN_JAVA_VERSION_WINDOWS, null, null, OSNAME_WINDOWS_2K, null));
            list.add(new JavaCompatibleProperties(
                    MIN_JAVA_VERSION_WINDOWS, null, null, OSNAME_WINDOWS_2K3, null));
            list.add(new JavaCompatibleProperties(
                    MIN_JAVA_VERSION_WINDOWS_2K8, null, null, OSNAME_WINDOWS_2K8, null));
            list.add(new JavaCompatibleProperties(
                    MIN_JAVA_VERSION_WINDOWS_7, null, null, OSNAME_WINDOWS_7, null));
            list.add(new JavaCompatibleProperties(
                    MIN_IBM_JAVA_VERSION, null, "IBM Corporation", null, null));
            list.add(new JavaCompatibleProperties(
                    MIN_JAVA_VERSION_WINDOWS_ALL, null, null, OSNAME_WINDOWS, null));
            return list;
        } else {
            return super.getDefaultCompatibleJava(version);            
        }        
    }
    
    private String changeJavaPropertyCounter(final String string) {
        String str = string;
        if(str!=null) {
            int counter = 0 ;
            while(str.indexOf(getJavaCounter(counter))!=-1) {
                str=str.replace(getJavaCounter(counter++), "%s");
            }
        }
        return str;
    }
    
    private void addExeInitialStub(FileOutputStream fos, Progress progress, long total) throws IOException {
        long stubSize;
        if(stubFile!=null)  {
            checkParameter("stub file", stubFile); //NOI18N
            stubSize = addData(fos, stubFile,progress, total);
        } else {
            stubSize = addData(fos, ResourceUtils.getResource(EXE_LAUNCHER_STUB), progress, 0);
        }
        long length = EXE_STUB_FILL_SIZE - stubSize;
        for(long i=0;i<length ;i++) {
            addData(fos); // fill with some chars
        }
    }
    
    private void addI18NStrings(FileOutputStream fos) throws IOException {
        addNumber(fos,  i18nMap.size()); // number of locales
        
        PropertyResourceBundle defaultBundle = i18nMap.get("");
        //properties names
        List <String> props = new LinkedList <String> ();
        Enumeration <String> en = defaultBundle.getKeys();
        long numberOfProperties = 0;
        while(en.hasMoreElements()) {
            en.nextElement();
            numberOfProperties++;
        }
        addNumber(fos,numberOfProperties); // number of properties
        
        String propertyName;
        en = defaultBundle.getKeys();
        while(en.hasMoreElements()) {
            propertyName = en.nextElement();
            props.add(propertyName);
            addData(fos, propertyName, false); // save property name as ascii
        }
        
        
        addData(fos, defaultBundle, null, StringUtils.EMPTY_STRING, props);
        i18nMap.remove(StringUtils.EMPTY_STRING);
        Object [] locales = i18nMap.keySet().toArray();
        
        for(int i=0;i<locales.length;i++) {
            addData(fos,i18nMap.get(locales[i]), defaultBundle,(String) locales[i], props);
        }
    }
    
    private void addJavaCompatibleProperties(FileOutputStream fos) throws IOException {
        LogManager.log("Total compatible java properties : " + compatibleJava.size()); //NOI18N
        LogManager.indent();
        for(int i=0;i<compatibleJava.size();i++) {
            // min and max jvm version
            JavaCompatibleProperties prop = compatibleJava.get(i);
            LogManager.log("... adding compatible jvm [" + i + "] : " + prop.toString()); //NOI18N
            
            addData(fos,prop.getMinVersion(), false);
            addData(fos,prop.getMaxVersion(), false);
            addData(fos,prop.getVendor(), false);
            addData(fos,prop.getOsName(), false);
            addData(fos,prop.getOsArch(), false);
        }
        LogManager.unindent();
    }
    private void addNumber(FileOutputStream fos, long number) throws IOException {
        fos.write(Long.toString(number).getBytes());
        fos.write(0);
    }
    private void addNumber(FileOutputStream fos, long number, boolean separateBits) throws IOException {
        if(separateBits) {
            addNumber(fos, number % MAXDWORD);
            addNumber(fos, (number - (number % MAXDWORD)) / MAXDWORD);
        } else {
            addNumber(fos, number);
        }
    }
    
    private void addData(FileOutputStream fos, boolean isTrue) throws IOException {
        addNumber(fos, isTrue ? 1L : 0L);
    }
    
    private void addData(FileOutputStream fos, List<LauncherResource> list, Progress progress, long total) throws IOException{
        addNumber(fos, list.size());
        //add every entry section
        LogManager.log("... overall number of files : " +  //NOI18N
                list.size());//NOI18N
        for(LauncherResource file : list) {
            LogManager.log("    adding file " +  //NOI18N
                    file.getPath());//NOI18N
            addFileSection(fos, file, progress, total);
        }
    }
    private void addData(FileOutputStream fos, List <String> strings, boolean isUnicode) throws IOException {
        addData(fos, strings.toArray(new String[0]),isUnicode);
    }
    private void addData(FileOutputStream fos, String [] strings, boolean isUnicode) throws IOException {
        
        if(strings!=null) {
            addNumber(fos, Integer.valueOf(strings.length).longValue()); // number of array elements
            for(String s: strings) {
                addData(fos, s, isUnicode);
            }
        } else {
            addNumber(fos, 0L); // no elements
        }
        
    }
    
    private void addFileSection(FileOutputStream fos, LauncherResource file, Progress progress, long total) throws IOException {
        addNumber(fos, file.getPathType().toLong());
        String path;
        if(file.isBasedOnResource()) {
            path = file.getPathType().getPathString(
                    ResourceUtils.getResourceFileName(file.getPath()));
        } else {
            path = file.getAbsolutePath();
        }
        addData(fos, path, true);
        
        if(file.isBundled()) {
            addNumber(fos, file.getSize(), true);
            InputStream is = null;
            try {
                is = file.getInputStream();
                addNumber(fos, FileUtils.getCrc32(is), false);
                is.close();
                is = file.getInputStream();
                addData(fos, is, progress, total);
                is.close();
                is = null;
            } finally {
                if(is!=null) {
                    try {
                        is.close();
                    } catch(IOException e) {
                        LogManager.log(e);
                    }
                }
            }
        }
    }
    
    private void addData(FileOutputStream fos, PropertyResourceBundle bundle, PropertyResourceBundle backupBundle, String localeName, List <String> propertiesNames) throws IOException {
        String propertyName;
        String localizedString;
        addData(fos, localeName, true);
        Enumeration <String> en = bundle.getKeys();
        for(int i=0;i<propertiesNames.size();i++) {
            String str = null;
            try {
                str = bundle.getString(propertiesNames.get(i));
            } catch (MissingResourceException e) {
                if(backupBundle!=null) {
                    str = backupBundle.getString(propertiesNames.get(i));
                }
            }
            str = changeJavaPropertyCounter(str);
            addData(fos, str, true); // localized string as UNICODE
            
        }
    }
    private void addData(FileOutputStream fos, Version version, boolean isUnicode) throws IOException {
        addData(fos,(version==null) ? null : version.toJdkStyle(),isUnicode);
    }
    
    private void addData(FileOutputStream fos, String str, boolean isUnicode) throws IOException {
        if(str!=null) {
            addString(fos, str, isUnicode);
        }
        if(isUnicode) {
            fos.write(0);
        }
        fos.write(0);
    }
    
    @Override
    public String getExtension() {
        return EXE_EXT;
    }
    @Override
    public String getI18NResourcePrefix() {
        return i18nPrefix !=null ? i18nPrefix :
            DEFAULT_WINDOWS_RESOURCE_I18N;
    }
    @Override
    public String getI18NBundleBaseName() {
        return i18nBundleBaseName != null ? i18nBundleBaseName :
            DEFAULT_WINDOWS_RESOURCE_I18N_BUNDLE_NAME;
    }
}
