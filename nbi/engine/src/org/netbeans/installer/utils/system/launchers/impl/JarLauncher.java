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
