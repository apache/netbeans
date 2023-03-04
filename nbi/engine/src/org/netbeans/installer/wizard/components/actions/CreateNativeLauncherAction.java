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

package org.netbeans.installer.wizard.components.actions;

import java.io.File;
import java.io.IOException;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.system.launchers.LauncherResource;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.launchers.LauncherProperties;
import org.netbeans.installer.wizard.components.WizardAction;

/**
 *
 * @author Dmitry Lipin
 */
public class CreateNativeLauncherAction extends WizardAction {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE = ResourceUtils.getString(
            CreateNativeLauncherAction.class,
            "CNLA.title"); // NOI18N
    
    public static final String DEFAULT_DESCRIPTION = ResourceUtils.getString(
            CreateNativeLauncherAction.class,
            "CNLA.description"); // NOI18N
    public static final String DEFAULT_ERROR_FAILED_CREATE_LAUNCHER =
            ResourceUtils.getString(
            CreateNativeLauncherAction.class,
            "CNLA.error.failed.create.launcher");//NOI18N
    public static final String ERROR_FAILED_CREATE_LAUNCHER_PROPERTY =
            "error.failed.create.launcher";//NOI18N
    public static final String BUNDLED_JVM_FILE_PROPERTY = 
            "nbi.bundled.jvm.file";
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public CreateNativeLauncherAction() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        setProperty(ERROR_FAILED_CREATE_LAUNCHER_PROPERTY,
                DEFAULT_ERROR_FAILED_CREATE_LAUNCHER);
    }
    
    public void execute() {
        LogManager.logEntry("creating the native launcher");
        
        final String targetPath =
                System.getProperty(Registry.CREATE_BUNDLE_PATH_PROPERTY);
        final File targetFile = new File(targetPath);
        
        final Progress progress = new Progress();
        
        getWizardUi().setProgress(progress);
        try {
            final Platform platform = Registry.getInstance().getTargetPlatform();
            final LauncherProperties properties = new LauncherProperties();
            
            properties.addJar(new LauncherResource(new File(targetPath)));
            
            properties.setJvmArguments(new String[]{
                "-Xmx256m",
                "-Xms64m"
            });
            if(System.getProperty(BUNDLED_JVM_FILE_PROPERTY)!=null) {
                final LauncherResource jvm = new LauncherResource(
                        new File(System.getProperty(BUNDLED_JVM_FILE_PROPERTY)));                        
                properties.addJVM(jvm);
                properties.getJvmArguments().add(
                        "-D" + BUNDLED_JVM_FILE_PROPERTY + "=" + 
                        jvm.getAbsolutePath());
            }
            File file = SystemUtils.createLauncher(
                    properties, platform, progress).getOutputFile();
            
            if ( !targetFile.equals(file)) {
                FileUtils.deleteFile(targetFile);
                System.setProperty(
                        Registry.CREATE_BUNDLE_PATH_PROPERTY,
                        file.getPath());
                
            }
        } catch (IOException e) {
            ErrorManager.notifyError(
                    getProperty(ERROR_FAILED_CREATE_LAUNCHER_PROPERTY), e);
        } 
        LogManager.logExit("finished creating the native launcher");
    }
    @Override
    public boolean canExecuteForward() {
        return !Boolean.getBoolean(Registry.CREATE_BUNDLE_SKIP_NATIVE_LAUNCHER_PROPERTY);
    }  
}
