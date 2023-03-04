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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.helper.JavaCompatibleProperties;
import org.netbeans.installer.utils.system.launchers.LauncherProperties;
import org.netbeans.installer.utils.system.launchers.LauncherResource;
import org.netbeans.installer.utils.progress.Progress;

/**
 *
 * @author Dmitry Lipin
 */
public class JarLauncher extends CommonLauncher {
    public static final String MIN_JAVA_VERSION_DEFAULT       = "1.5";
    
    public JarLauncher(LauncherProperties props) {
        super(props);
    }
    
    public void initialize() throws IOException {
        checkBundledJars();
        checkOutputFileName();
        checkCompatibleJava();
    }
    
    public File create(Progress progress) throws IOException {
        LogManager.log("Create Jar launcher... ");
        File out = null;
        for (LauncherResource file : jars) {
            if ( file.isBundled()) {
                // TODO
                // think about what should we do if we have more than 2 files for bundling...
                File jarFile = new File(file.getPath());
                if(jarFile.getCanonicalPath().equals(
                        outputFile.getCanonicalPath())) {
                    out = jarFile;
                } else {
                    out = outputFile;
                    FileUtils.copyFile(jarFile, out);
                }
                break;
            }
        }
        if(out!=null) {
            for (LauncherResource file : jars) {
                if ( !file.isBundled()) {
                    File jarFile = new File(file.getPath());
                    if(jarFile.getCanonicalPath().equals(
                            outputFile.getCanonicalPath())) {
                        out = jarFile;
                    } else {
                        out = outputFile;
                        //FileUtils.copyFile(jarFile, out);
                    }
                    break;
                }
            }
        }
        return out;
    }
    
    public String getExtension() {
        return FileUtils.JAR_EXTENSION;
    }
    @Override
    public String getI18NResourcePrefix() {
        return null;
    }
    @Override
    public String getI18NBundleBaseName() {
        return null;
    }
    
    public String [] getExecutionCommand() {
        File javaLocation = null;
        for(LauncherResource java : jvms) {
            switch(java.getPathType()) {
                case ABSOLUTE :
                    javaLocation = new File(java.getPath());
                    break;
                case RELATIVE_USERHOME:
                    javaLocation = new File(SystemUtils.getUserHomeDirectory(), java.getPath());
                    break;
                case RELATIVE_LAUNCHER_PARENT:
                    javaLocation = new File(outputFile.getParentFile(), java.getPath());
                    break;
                default:
                    break; // other is nonsense for jar launcher
            }
            if(javaLocation!=null) {
                // TODO
                // check java compatibility here and find necessary JVM on the system
                for(JavaCompatibleProperties javaCompat : compatibleJava) {
                }
                break;
            }
        }
        List <String> commandList = new ArrayList <String>();
        commandList.add(JavaUtils.getExecutableW(javaLocation).getAbsolutePath());
        commandList.add("-cp");
        String classpath = StringUtils.EMPTY_STRING;
        for(LauncherResource jar : jars) {
            switch(jar.getPathType()) {
                case RELATIVE_JAVAHOME :
                    classpath+=
                            new File(javaLocation, jar.getPath()) +
                            SystemUtils.getPathSeparator();
                    break;
                case ABSOLUTE :
                    classpath+=
                            new File(jar.getPath()) +
                            SystemUtils.getPathSeparator();
                    break;
                case RELATIVE_USERHOME:
                    classpath+=
                            new File(SystemUtils.getUserHomeDirectory(), jar.getPath()) +
                            SystemUtils.getPathSeparator();
                    break;
                case RELATIVE_LAUNCHER_PARENT:
                    classpath += new File(outputFile.getParentFile(), jar.getPath()) +
                            SystemUtils.getPathSeparator();
                    break;
                default:
                    break;
            }
        }
        commandList.add(classpath);
        commandList.addAll(jvmArguments);
        commandList.add(mainClass);
        commandList.addAll(appArguments);
        return commandList.toArray(new String [commandList.size()]);
    }
    
    public List<JavaCompatibleProperties> getDefaultCompatibleJava() {
        List <JavaCompatibleProperties> list = new ArrayList <JavaCompatibleProperties>();
        list.add(new JavaCompatibleProperties(
                MIN_JAVA_VERSION_DEFAULT, null, null, null, null));
        return list;
    }
    
}
