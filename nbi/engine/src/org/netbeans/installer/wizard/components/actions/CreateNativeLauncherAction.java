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
