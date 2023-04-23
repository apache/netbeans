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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.PropertyResourceBundle;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StreamUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.JavaCompatibleProperties;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.system.launchers.LauncherProperties;
import org.netbeans.installer.utils.system.launchers.LauncherResource;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.NativeUtils;

/**
 *
 * @author Dmitry Lipin
 */
public class ShLauncher extends CommonLauncher {
    public static final String SH_LAUNCHER_STUB_NAME = "launcher.sh"; //NOI18N
    public static final String DEFAULT_UNIX_RESOURCE_SUFFIX =
            NativeUtils.NATIVE_LAUNCHER_RESOURCE_SUFFIX +
            "unix/"; //NOI18N
    public static final String I18N = "i18n"; //NOI18N
    public static final String SH_LAUNCHER_STUB =
            DEFAULT_UNIX_RESOURCE_SUFFIX + SH_LAUNCHER_STUB_NAME;
    public static final String DEFAULT_UNIX_RESOURCE_I18N =
            DEFAULT_UNIX_RESOURCE_SUFFIX + I18N + "/"; //NOI18N
    public static final String DEFAULT_UNIX_RESOURCE_I18N_BUNDLE_NAME =
            "launcher"; //NOI18N
    
    private static final String SH_EXT = ".sh"; //NOI18N
    private static final int SH_BLOCK = 1024;
    private static final String SH_INDENT = "        "; //NOI18N
    private static final String SH_LINE_SEPARATOR = StringUtils.LF;
    private static final String SH_COMMENT = "#";
    
    /**
     *  Minimal supported Java version for the launcher.<br><br>
     *  Due to the URLConnection issues with "spaced" names set this value to 1.5.0_03
     *  as these issues were fixed somewhere between Update 1 and Update 3. <br>
     *  Related issues:
     *  <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5103449">5103449</a>
     *  <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6181108">6181108</a>
     *  <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6227551">6227551</a>
     *  <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6219199">6219199</a>
     *  <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4979820">4979820</a>
     */
    public static final String MIN_JAVA_VERSION_UNIX = "1.5.0_03";
    
    /* IBM does not report the update number so allow to work even on 1.5.0*/
    public static final String MIN_IBM_JAVA_VERSION = "1.5.0";
    
    private static final String [] JAVA_COMMON_LOCATIONS = {
        "/usr/java*", "/usr/java/*",
        "/usr/jdk*",  "/usr/jdk/*",
        "/usr/j2se",  "/usr/j2se/*",
        "/usr/j2sdk", "/usr/j2sdk/*",
        
        "/usr/java/jdk*", "/usr/java/jdk/*",
        "/usr/jdk/instances", "/usr/jdk/instances/*",
        
        "/usr/local/java", "/usr/local/java/*",
        "/usr/local/jdk*",  "/usr/local/jdk/*",
        "/usr/local/j2se", "/usr/local/j2se/*",
        "/usr/local/j2sdk","/usr/local/j2sdk/*",
        
        "/opt/java*",  "/opt/java/*",
        "/opt/jdk*",  "/opt/jdk/*",
        "/opt/j2sdk", "/opt/j2sdk/*",
        "/opt/j2se",  "/opt/j2se/*",
        
        "/usr/lib/jvm",
        "/usr/lib/jvm/*",
        "/usr/lib/jdk*",
        
        "/export/jdk*",   "/export/jdk/*",
        "/export/java",  "/export/java/*",
        "/export/j2se",  "/export/j2se/*",
        "/export/j2sdk", "/export/j2sdk/*"
    };
    
    public ShLauncher(LauncherProperties props) {
        super(props);
    }
    
    public void initialize() throws IOException {
        LogManager.log("Checking SH launcher parameters..."); // NOI18N
        checkAllParameters();
    }
    
    public File create( Progress progress) throws IOException {
        
        FileOutputStream fos = null;
        try {
            
            progress.setPercentage(Progress.START);
            long total = getBundledFilesSize();
            fos = new FileOutputStream(outputFile,false);
            
            StringBuilder sb = new StringBuilder(getStubString());
            
            addShInitialComment(sb);
            addPossibleJavaLocations(sb);
            addI18NStrings(sb);
            addTestJVMFile(sb);
            addClasspathJars(sb);
            addJavaCompatible(sb);
            addOtherResources(sb);
            addNumberVariable(sb, "TOTAL_BUNDLED_FILES_SIZE", getBundledFilesSize());
            addNumberVariable(sb, "TOTAL_BUNDLED_FILES_NUMBER", getBundledFilesNumber());
            
            LogManager.log("Main Class : " + mainClass);
            addStringVariable(sb, "MAIN_CLASS", mainClass);
            
            LogManager.log("TestJVM Class : " + testJVMClass);
            addStringVariable(sb, "TEST_JVM_CLASS", testJVMClass);
            
            addNumberVariable(sb, "JVM_ARGUMENTS_NUMBER", jvmArguments.size());
            int counter = 0;
            for(String arg : jvmArguments) {
                addStringVariable(sb, "JVM_ARGUMENT_" + (counter), 
                        escapeVarSign(escapeSlashes(arg)));
                LogManager.log("... jvm argument [" + counter + "] = " + arg);
                counter++;
            }
            
            addNumberVariable(sb, "APP_ARGUMENTS_NUMBER", appArguments.size());
            counter = 0;
            for(String arg : appArguments) {
                addStringVariable(sb, "APP_ARGUMENT_" + (counter), 
                        escapeVarSign(escapeSlashes(arg)));
                LogManager.log("... app argument [" + counter + "] = " + arg);
                counter++;
            }
            
            
            
            String token = "_^_^_^_^_^_^_^_^"; // max size: (10^16-1) bytes
            
            sb.append("LAUNCHER_STUB_SIZE=" + token + SH_LINE_SEPARATOR);
            
            sb.append("entryPoint \"$@\"" + SH_LINE_SEPARATOR);
            nextLine(sb);
            
            long size = sb.length();
            
            long fullBlocks = (size - (size % SH_BLOCK)) / SH_BLOCK + 1;
            
            String str = Long.toString(fullBlocks);
            int spaces = token.length() - str.length();
            
            for(int j=0;j < spaces; j++) {
                str+= StringUtils.SPACE;
            }
            
            sb.replace(sb.indexOf(token), sb.indexOf(token) + token.length(), str);
            
            long pads = fullBlocks * SH_BLOCK - size;
            
            for ( long i=0; i < pads;i++) {
                sb.append(SH_COMMENT);
            }
            addStringBuilder(fos,sb,false);
            addBundledData(fos, progress, total);
        } catch (IOException ex) {
            LogManager.log(ex);
            try {
                fos.close();
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
                    LogManager.log(ex);
                    throw ex;
                }
            }
            progress.setPercentage(Progress.COMPLETE);
        }
        return outputFile;
    }
    
    public String[] getExecutionCommand() {
        return new String [] {outputFile.getAbsolutePath()};
    }
    
    @Override
    public List <JavaCompatibleProperties> getDefaultCompatibleJava(Version version) {
        if (version.equals(Version.getVersion("1.5"))) {
            List <JavaCompatibleProperties> list = new ArrayList <JavaCompatibleProperties>();
            list.add(new JavaCompatibleProperties(
                MIN_JAVA_VERSION_UNIX, null, null, null, null));
            list.add(new JavaCompatibleProperties(
                MIN_IBM_JAVA_VERSION, null, "IBM Corporation", null, null));
            return list;
        } else {
            return super.getDefaultCompatibleJava(version);            
        }
    }
    
    protected void addOtherResources(StringBuilder sb) throws IOException {
        int counter = 0;
        addNumberVariable(sb, "OTHER_RESOURCES_NUMBER", otherResources.size());
        
        for(LauncherResource resource : otherResources) {
            addLauncherResource(sb, resource, "OTHER_RESOURCE_" + counter);
            counter ++;
        }
        
    }
    
    private void addLauncherResource(StringBuilder sb, LauncherResource resource, String id) throws IOException {
        long type = resource.getPathType().toLong();
        addNumberVariable(sb, id + "_TYPE", type); //NOI18N
        
        String path;
        if(resource.isBundled()) {
            long size = resource.getSize();
            
            if(resource.isBasedOnResource()) {
                path = resource.getPathType().getPathString(
                        ResourceUtils.getResourceFileName(resource.getPath()));
            } else {
                path = resource.getAbsolutePath();
            }
            addNumberVariable(sb, id + "_SIZE", size);
            addStringVariable(sb, id + "_MD5", resource.getMD5());
        } else {
            path = resource.getAbsolutePath();
        }
        
        
        addStringVariable(sb, id + "_PATH",
                escapeVarSign(escapeSlashesAndChars(path)));
        
    }
    @Override
    public String getI18NResourcePrefix() {
        return i18nPrefix != null ? i18nPrefix :
            DEFAULT_UNIX_RESOURCE_I18N;
    }
    @Override
    public String getI18NBundleBaseName() {
        return i18nBundleBaseName != null ? i18nBundleBaseName :
            DEFAULT_UNIX_RESOURCE_I18N_BUNDLE_NAME;
    }
    
    public String getExtension() {
        return SH_EXT;
    }
    
    private String escapeChars(String str) {
        return (str==null) ? StringUtils.EMPTY_STRING :
            str.replace("\n","\\n").
                replace("\t","\\\\t").
                replace("\r","\\\\r").
                replace("`","\\`").
                replace("\"","\\\\\"");
        
    }
    private String escapeSlashesAndChars(String str) {
        return escapeSlashes(escapeChars(str));
    }
    
    private String escapeVarSign(String str) {
        return (str==null) ? StringUtils.EMPTY_STRING :
            str.replace("$", "\\$");
    }
    private String escapeSlashes(String str) {
        return (str==null) ? StringUtils.EMPTY_STRING :
            str.replace(StringUtils.BACK_SLASH,
                StringUtils.DOUBLE_BACK_SLASH);
    }
    
    private String getUTF8(String str, boolean changePropertyCounterStyle) throws UnsupportedEncodingException{
        if(!changePropertyCounterStyle) {
            return getUTF8(str);
        } else {
            String string = StringUtils.EMPTY_STRING;
            int maxCounter=0;
            while(str.indexOf(getJavaCounter(maxCounter))!=-1) {
                maxCounter++;
            }
            boolean con;
            String jc;
            for(int i=0;i<str.length();i++) {
                con = false;
                for(int j=0;j<maxCounter;j++) {
                    jc = getJavaCounter(j);
                    if(str.indexOf(jc)== i) {
                        string += "$" + (j + 1);
                        i+=jc.length();
                        con = true;
                        break;
                    }
                }
                if(!con) {
                    string+=getUTF8(str.substring(i,i+1));
                }
            }
            
            return string;
        }
    }
    
    private String getStubString() throws IOException {
        InputStream stubStream;
        
        if(stubFile!=null)  {
            checkParameter("stub file", stubFile);
            stubStream = new FileInputStream(stubFile);
        } else {
            stubStream = ResourceUtils.getResource(SH_LAUNCHER_STUB);
        }
        CharSequence cs = StreamUtils.readStream(stubStream);
        stubStream.close();
        
        String [] strings = StringUtils.splitByLines(cs);
        String stubString = StringUtils.asString(strings, SH_LINE_SEPARATOR);
        return stubString;
    }
    private String getUTF8(String str) throws UnsupportedEncodingException{
        String repr = StringUtils.EMPTY_STRING;
        for(byte oneByte : str.getBytes(StringUtils.ENCODING_UTF8)) {
            repr+= StringUtils.BACK_SLASH + Integer.toOctalString(256 + oneByte);
        }
        return repr;
    }
    
    private String changePropertyCounterStyle(String string)  {
        int counter = 0;
        String jp;
        String str = string;
        do {
            jp = getJavaCounter(counter);
            if(str.indexOf(jp)!=-1) {
                str = str.replace(jp, "$" + (counter + 1) );
            } else {
                break;
            }
            counter++;
        }
        while (true);
        return str;
    }
    private void addVersionVariable(StringBuilder sb, String name, Version version)  {
        String str = (version != null) ? version.toJdkStyle() : StringUtils.EMPTY_STRING;
        sb.append(name + StringUtils.EQUAL + StringUtils.QUOTE +
                str + StringUtils.QUOTE + SH_LINE_SEPARATOR);
    }
    private void addStringVariable(StringBuilder sb, String name, String value)  {
        String str = (value != null) ? value : StringUtils.EMPTY_STRING;
        sb.append(name + StringUtils.EQUAL + StringUtils.QUOTE +
                str + StringUtils.QUOTE + SH_LINE_SEPARATOR);
    }
    
    private void addNumberVariable(StringBuilder sb, String name, long value) {
        sb.append(name + StringUtils.EQUAL + value +  SH_LINE_SEPARATOR);
    }
    private void nextLine(StringBuilder sb) {
        sb.append(SH_LINE_SEPARATOR);
    }
    private void addJavaCompatible(StringBuilder sb) throws IOException {
        // add java compatibility properties number
        nextLine(sb);
        
        LogManager.log("Total compatible java properties : " + compatibleJava.size()); //NOI18N
        addNumberVariable(sb, "JAVA_COMPATIBLE_PROPERTIES_NUMBER", compatibleJava.size());
        
        for(int i=0;i<compatibleJava.size();i++) {
            nextLine(sb);
            sb.append("setJavaCompatibilityProperties_" + i + "() {" + SH_LINE_SEPARATOR);
            
            JavaCompatibleProperties prop = compatibleJava.get(i);
            LogManager.log("... adding compatible jvm [" + i + "] : " + prop.toString()); //NOI18N
            addVersionVariable(sb, "JAVA_COMP_VERSION_MIN", prop.getMinVersion());
            addVersionVariable(sb, "JAVA_COMP_VERSION_MAX", prop.getMaxVersion());
            addStringVariable(sb, "JAVA_COMP_VENDOR", prop.getVendor());
            addStringVariable(sb, "JAVA_COMP_OSNAME", prop.getOsName());
            addStringVariable(sb, "JAVA_COMP_OSARCH", prop.getOsArch());
            sb.append("}");
            nextLine(sb);
        }
    }
    private void addTestJVMFile(StringBuilder sb) throws IOException {
        nextLine(sb);
        addLauncherResource(sb, testJVMFile, "TEST_JVM_FILE");
    }
    
    private void addClasspathJars(StringBuilder sb) throws IOException {
        nextLine(sb);
        
        addNumberVariable(sb, "JARS_NUMBER",  jars.size()); //NOI18N
        
        int counter = 0;
        for(LauncherResource jarFile : jars) {
            addLauncherResource(sb, jarFile, "JAR_" + counter);
            
            counter++;
        }
        nextLine(sb);
    }
    
    private void addI18NStrings(StringBuilder sb) throws IOException {
        Object [] locales = i18nMap.keySet().toArray();
        addNumberVariable(sb,"LAUNCHER_LOCALES_NUMBER",locales.length); //NOI18N
        
        for(int i=0;i<locales.length;i++) {
            addStringVariable(sb,"LAUNCHER_LOCALE_NAME_" + i, //NOI18N
                    locales[i].toString());
        }
        
        nextLine(sb);
        
        for(int i=0;i<locales.length;i++) {
            String locale = locales[i].toString();
            sb.append("getLocalizedMessage_" + locale + "() {" + SH_LINE_SEPARATOR );
            sb.append(SH_INDENT + "arg=$1" + SH_LINE_SEPARATOR );
            sb.append(SH_INDENT + "shift" + SH_LINE_SEPARATOR );
            sb.append(SH_INDENT + "case $arg in" + SH_LINE_SEPARATOR );
            PropertyResourceBundle rb = i18nMap.get(locales[i]);
            Enumeration <String>en = rb.getKeys();
            while(en.hasMoreElements()) {
                String name  = en.nextElement();
                String value =  rb.getString(name);
                sb.append(SH_INDENT + "\"" + name + "\")" + SH_LINE_SEPARATOR);
                String printString = value;
                if(Arrays.equals(printString.getBytes("ISO-8859-1"), printString.getBytes("UTF-8"))) {
                    printString = escapeChars(changePropertyCounterStyle(printString));
                } else {
                    printString = getUTF8(printString, true);
                }
                sb.append(SH_INDENT + SH_INDENT + "printf \"" + printString + "\\n" + "\"" + SH_LINE_SEPARATOR);
                sb.append(SH_INDENT + SH_INDENT + ";;" + SH_LINE_SEPARATOR);
                
            }
            sb.append(SH_INDENT + "*)" + SH_LINE_SEPARATOR);
            sb.append(SH_INDENT + SH_INDENT + "printf \"$arg\\n\"" + SH_LINE_SEPARATOR);
            sb.append(SH_INDENT + SH_INDENT + ";;" + SH_LINE_SEPARATOR);
            sb.append(SH_INDENT + "esac" + SH_LINE_SEPARATOR);
            sb.append("}" + SH_LINE_SEPARATOR);
            nextLine(sb);
        }
        
    }
    private void addShInitialComment(StringBuilder sb) throws IOException {
        nextLine(sb);
        nextLine(sb);
        for(int i=0;i<80;i++) {
            sb.append(SH_COMMENT);
        }
        nextLine(sb);
        sb.append(SH_COMMENT + " Added by the bundle builder" + //NOI18N
                SH_LINE_SEPARATOR);
        addNumberVariable(sb,"FILE_BLOCK_SIZE", SH_BLOCK);//NOI18N
        nextLine(sb);
    }
    
    private int addJavaPaths(int count, StringBuilder sb, List<LauncherResource> list) throws IOException {
        int counter = count;
        for(LauncherResource location : list) {
            addLauncherResource(sb, location, "JAVA_LOCATION_" + counter);
            counter ++;
        }
        return counter;
    }
    private int addJavaPaths(int count, StringBuilder sb, String  [] paths) throws IOException {
        List <LauncherResource> list = new ArrayList <LauncherResource> ();
        for(String path : paths) {
            list.add(new LauncherResource(LauncherResource.Type.ABSOLUTE, path));
        }
        return addJavaPaths(count, sb, list);
    }
    protected String [] getCommonSystemJavaLocations() {
        return JAVA_COMMON_LOCATIONS;
    }
    private void addPossibleJavaLocations(StringBuilder sb) throws IOException {
        int total = 0;
        total = addJavaPaths(total, sb, jvms);
        total = addJavaPaths(total, sb, getCommonSystemJavaLocations());
        addNumberVariable(sb, "JAVA_LOCATION_NUMBER", total); //NOI18N
        nextLine(sb);
    }
    
    private void fillWithPads(FileOutputStream fos, long sz) throws IOException {
        long d = (SH_BLOCK - (sz % SH_BLOCK)) % SH_BLOCK;
        for ( int i=0; i < d; i++) {
            addString(fos, SH_LINE_SEPARATOR, false);
        }
    }
    
    private void addLauncherResourceData(FileOutputStream fos, LauncherResource resource, Progress progress, long total)  throws IOException {
        if(resource.isBundled()) { // if bundle TestJVM
            LogManager.log("Bundle testJVM file..."); //NOI18N
            InputStream is = null;
            try {
                String path = resource.getPath();
                LogManager.log("... path is " + path); //NOI18N
                is = resource.getInputStream();                                
                addData(fos, is, progress, total);
                fillWithPads(fos, resource.getSize());
            } finally {
                try {
                    if(is!=null) {
                        is.close();
                    }
                } catch (IOException ex) {
                    LogManager.log(ex);
                }
            }
            LogManager.log("... done bundle launcher resource file");//NOI18N
        }
    }
    private void addBundledData(FileOutputStream fos, Progress progress, long total) throws IOException {
        addLauncherResourceData(fos, testJVMFile, progress, total);
        
        for(LauncherResource jvm : jvms) {
            addLauncherResourceData(fos, jvm,progress,total);
        }
        for(LauncherResource jar : jars) {
            addLauncherResourceData(fos, jar,progress,total);
        }
        for(LauncherResource other : otherResources) {
            addLauncherResourceData(fos, other,progress,total);
        }
    }
}
