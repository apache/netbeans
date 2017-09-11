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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipOutputStream;
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.EngineUtils;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.launchers.LauncherProperties;
import org.netbeans.installer.utils.system.launchers.LauncherResource;
import org.netbeans.installer.utils.system.launchers.impl.CommandLauncher;
import org.netbeans.installer.wizard.components.WizardAction;

/**
 *
 * @author Dmitry Lipin
 */
public class CreateMacOSAppLauncherAction extends WizardAction {
    // Constants
    public static final String DEFAULT_TITLE = ResourceUtils.getString(
            CreateMacOSAppLauncherAction.class,
            "CMALA.title"); // NOI18N
    
    public static final String DEFAULT_DESCRIPTION = ResourceUtils.getString(
            CreateMacOSAppLauncherAction.class,
            "CMALA.description"); // NOI18N
    public static final String DEFAULT_ERROR_FAILED_CREATE_LAUNCHER =
            ResourceUtils.getString(
            CreateMacOSAppLauncherAction.class,
            "CMALA.error.failed.create.launcher");//NOI18N
    public static final String ERROR_FAILED_CREATE_LAUNCHER_PROPERTY =
            "error.failed.create.launcher";//NOI18N
    
    public static final String APP_NAME_PROPERTY =
            "nbi.macosx.application.directory.name"; // NOI18N
    
    public static final String DEFAULT_APP_DIRECTORY_NAME =
            "NetBeans Installer"; // NOI18N
    
    public static final String DEFAULT_ICNS_ICON_NAME =
            "icon.icns"; //NOI18N
    
    
    public CreateMacOSAppLauncherAction() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        setProperty(ERROR_FAILED_CREATE_LAUNCHER_PROPERTY,
                DEFAULT_ERROR_FAILED_CREATE_LAUNCHER);
    }
    
    public void execute() {
        LogManager.logEntry("creating the macosx app launcher");
        
        final String targetPath =
                System.getProperty(Registry.CREATE_BUNDLE_PATH_PROPERTY);
        final File targetFile = new File(targetPath);
        
        final Progress progress = new Progress();
        
        getWizardUi().setProgress(progress);
        
        try {
            final Platform platform = Registry.getInstance().getTargetPlatform();
            final LauncherProperties properties = new LauncherProperties();
            
            final String appNameSystem = System.getProperty(APP_NAME_PROPERTY);
            
            final String appName = (appNameSystem!=null) ?
                appNameSystem :
                DEFAULT_APP_DIRECTORY_NAME;
            
            final String testJDKName = ResourceUtils.getResourceFileName(JavaUtils.TEST_JDK_RESOURCE);
            properties.addJar(new LauncherResource(
                    LauncherResource.Type.RELATIVE_LAUNCHER_PARENT,
                    "../Resources/" +
                    appName +
                    "/" + new File(targetPath).getName()));
            properties.setTestJVM(new LauncherResource(
                    LauncherResource.Type.RELATIVE_LAUNCHER_PARENT,
                    "../Resources/" +
                    appName +
                    "/" +
                    testJDKName));
            
            properties.setJvmArguments(new String[]{
                "-Xmx256m",
                "-Xms64m"
            });
            
            properties.setMainClass(EngineUtils.getEngineMainClass().getName());
            properties.setTestJVMClass(JavaUtils.TEST_JDK_CLASSNAME);
            
            File tmpDirectory =
                    FileUtils.createTempFile(SystemUtils.getTempDirectory(), false, true);
            FileUtils.mkdirs(tmpDirectory);
            
            File appDirectory       = new File(tmpDirectory, appName + ".app");
            File contentsDirectory  = new File(appDirectory, "Contents");
            File resDirectory       = new File(contentsDirectory, "Resources");
            File macosDirectory     = new File(contentsDirectory, "MacOS");
            File appInsideDir       = new File(resDirectory, appName);
            File outputFile         = new File(macosDirectory, "executable");
            
            FileUtils.mkdirs(appDirectory);
            FileUtils.mkdirs(contentsDirectory);
            FileUtils.mkdirs(resDirectory);
            FileUtils.mkdirs(appInsideDir);
            FileUtils.mkdirs(macosDirectory);
            
            final String iconName = DEFAULT_ICNS_ICON_NAME;
            
            properties.setOutput(outputFile, false);
            
            
            String iconUri = System.getProperty(CommandLauncher.JAVA_APPLICATION_ICON_PROPERTY);
            
            if(iconUri == null) {
                iconUri = CommandLauncher.JAVA_APPLICATION_ICON_DEFAULT_URI;
            }
            
            File iconFile = FileProxy.getInstance().getFile(iconUri,true);
            
            File iconFileTarget = new File(resDirectory, iconName);
            
            FileUtils.copyFile(iconFile,iconFileTarget);
            
            properties.getJvmArguments().add("-Xdock:icon=" +
                    LauncherResource.Type.RELATIVE_LAUNCHER_PARENT.
                    getPathString("../Resources/" + iconName));
            
            File file = SystemUtils.createLauncher(properties, platform, progress).
                    getOutputFile();
            
            File testJDKFile = FileProxy.getInstance().getFile(JavaUtils.TEST_JDK_URI,true);
            
            FileUtils.copyFile(testJDKFile,
                    new File(appInsideDir, testJDKName));
            
            FileUtils.copyFile(targetFile,
                    new File(appInsideDir, targetFile.getName()));
            
            File infoplist = new File(contentsDirectory, "Info.plist");
            FileUtils.writeFile(infoplist, StringUtils.format(
                    FileUtils.INFO_PLIST_STUB, appName, 1.0, 0,
                    outputFile.getName(),
                    iconFileTarget.getName()));
            
            final String name = targetFile.getName();
            final int index = name.lastIndexOf(".");
            final String zipName = name.substring(0, (index==-1) ? name.length() : index) + ".zip";
            
            File zipFile = new File(targetFile.getParentFile(), zipName);
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
            
            FileUtils.zip(appDirectory, zos, appDirectory.getParentFile(), new ArrayList <File> ());
            zos.close();
            FileUtils.deleteFile(tmpDirectory, true);
            
            System.setProperty(
                    Registry.CREATE_BUNDLE_PATH_PROPERTY,
                    zipFile.getPath());
            if ( !targetFile.equals(file)) {
                FileUtils.deleteFile(targetFile);
                System.setProperty(
                        Registry.CREATE_BUNDLE_PATH_PROPERTY,
                        file.getPath());
                
            }
        } catch (IOException e) {
            ErrorManager.notifyError(getProperty(ERROR_FAILED_CREATE_LAUNCHER_PROPERTY), e);
        } catch (DownloadException e) {
            ErrorManager.notifyError(getProperty(ERROR_FAILED_CREATE_LAUNCHER_PROPERTY), e);
        }
        
        LogManager.logExit("finished creating the app launcher");
    }

    @Override
    public boolean canExecuteForward() {
        return !Boolean.getBoolean(Registry.CREATE_BUNDLE_SKIP_NATIVE_LAUNCHER_PROPERTY);
    }    
}
