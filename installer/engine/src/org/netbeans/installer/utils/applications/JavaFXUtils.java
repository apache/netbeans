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
package org.netbeans.installer.utils.applications;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.helper.PlatformConstants;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.system.WindowsNativeUtils;
import org.netbeans.installer.utils.system.windows.WindowsRegistry;
import static org.netbeans.installer.utils.system.windows.WindowsRegistry.HKLM;

public class JavaFXUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static    

    public static String getJavaFXSDKInstallationPath (Platform platform) {        
        return getFXRegistryValue(platform, FXSDK_KEY, FX_SDK_HOME_PATH);
    }

    public static String getJavaFXRuntimeInstallationPath (Platform platform) {     
        return getFXRegistryMaxValue(platform, FXRUNTIME_INSTALLATION_KEY);
    }

    /////////////////////////////////////////////////////////////////////////////////

    private static String getFXRegistryValue (Platform platform, String registryKey, String registryItemKey) {
        String result = null;
        try {
            if(SystemUtils.isWindows()) {
                String arch = platform.getHardwareArch();
                WindowsRegistry winreg = ((WindowsNativeUtils) SystemUtils.getNativeUtils()).getWindowsRegistry();
                if(arch != null && winreg.isAlternativeModeSupported()) {
                    final int mode = arch.equals(PlatformConstants.HARDWARE_X86)? WindowsRegistry.MODE_32BIT:
                        WindowsRegistry.MODE_64BIT;
                    LogManager.log("... changing registry mode to: " + mode);
                    winreg.setMode(mode);
                }
                LogManager.log("... getting JavaFX " + registryKey + " value: " + registryItemKey);
                if (winreg.keyExists(HKLM, registryKey)) {
                    if (winreg.valueExists(HKLM, registryKey, registryItemKey)) {
                        result = winreg.getStringValue(HKLM, registryKey, registryItemKey);
                    } else {
                        LogManager.log("... cannot find " + registryItemKey + " value for this product");
                    }                    
                } else {
                    LogManager.log("... cannot find " + registryKey + " for this product");
                }
            }
        } catch (NativeException e) {
            LogManager.log(e);
        }
        return result;
    }

    private static String getFXRegistryMaxValue (Platform platform, String registryKey) {
        String result = null;
        try {
            if(SystemUtils.isWindows()) {
                String arch = platform.getHardwareArch();
                WindowsRegistry winreg = ((WindowsNativeUtils) SystemUtils.getNativeUtils()).getWindowsRegistry();
                if(arch != null && winreg.isAlternativeModeSupported()) {
                    final int mode = arch.equals(PlatformConstants.HARDWARE_X86)? WindowsRegistry.MODE_32BIT:
                        WindowsRegistry.MODE_64BIT;
                    LogManager.log("... changing registry mode to: " + mode);
                    winreg.setMode(mode);
                }
                
                if (winreg.keyExists(HKLM, registryKey)) {
                    String[] javaFXSubKeys = winreg.getSubKeyNames(HKLM, registryKey);

                    Version prevVersion = null;
                    for (String singleKey : javaFXSubKeys) {
                        LogManager.log("... getting JavaFX " + registryKey + " value: " + singleKey);
                        if (winreg.valueExists(HKLM, registryKey + singleKey, PATH)) {
                            Version actualVersion = Version.getVersion(singleKey);
                            if (actualVersion == null || prevVersion == null || actualVersion.newerThan(prevVersion)) {
                                result = winreg.getStringValue(HKLM, registryKey + singleKey, PATH);
                            }
                            prevVersion = actualVersion;                
                        } else {
                            LogManager.log("... cannot find " + singleKey + " value for this product");
                        }                        
                    }                 
                } else {
                    LogManager.log("... cannot find " + registryKey + " for this product");
                }
            }
        } catch (NativeException e) {
            LogManager.log(e);
        }
        return result;
    }
    
    public static boolean jdkContainsJavaFX(File jdkLocation) {
        File javaFXSDK = new File(jdkLocation, "lib" + File.separator + "ant-javafx.jar");
        if (!javaFXSDK.exists())
            return false;
        
        File javaFXRuntimeFolder = new File(jdkLocation, "jre" + File.separator + "lib");
        
        File javaFXRuntimeDeply = new File(javaFXRuntimeFolder, "deploy.jar");
        if (!javaFXRuntimeDeply.exists())
            return false;
        
        File javaFXRuntimeJavaws = new File(javaFXRuntimeFolder, "javaws.jar");
        if (!javaFXRuntimeJavaws.exists())
            return false;
        
        File javaFXRuntimeJfxrt = new File(javaFXRuntimeFolder, "jfxrt.jar");
        if (!javaFXRuntimeJfxrt.exists())
            return false;
        
        File javaFXRuntimePlugin = new File(javaFXRuntimeFolder, "plugin.jar");
        if (!javaFXRuntimePlugin.exists())
            return false;
        
        return true;
    }
    
    /**
     * Registers JavaFX into NetBeans IDE.
     * 
     * CAN BE CALLED AFTER IDE IS INSTALLED!!!
     * 
     * @param nbLocation
     * @param sdkLocation
     * @param reLocation
     * @return
     * @throws IOException 
     */
    public static boolean registerJavaFX(File nbLocation, File sdkLocation, File reLocation) throws IOException {
        File javaExe = JavaUtils.getExecutable(new File(System.getProperty("java.home")));
        String [] cp = {
            "platform/core/core.jar",
            "platform/lib/boot.jar",
            "platform/lib/org-openide-modules.jar",
            "platform/core/org-openide-filesystems.jar",
            "platform/lib/org-openide-util.jar",
            "platform/lib/org-openide-util-lookup.jar",
            "javafx/modules/org-netbeans-modules-javafx2-platform.jar"
        };
        for(String c : cp) {
            File f = new File(nbLocation, c);
            if(!FileUtils.exists(f)) {
                LogManager.log("... cannot find jar required for JavaFX integration: " + f);
                return false;
            }
        }
        String mainClass = "org.netbeans.modules.javafx2.platform.registration.AutomaticRegistration";
        List <String> commands = new ArrayList <String> ();
        File nbCluster = new File(nbLocation, "nb");
        commands.add(javaExe.getAbsolutePath());
        commands.add("-cp");
        commands.add(StringUtils.asString(cp, File.pathSeparator));
        commands.add(mainClass);        
        commands.add(nbCluster.getAbsolutePath());     
        commands.add(sdkLocation.getAbsolutePath());
        commands.add(reLocation.getAbsolutePath());
        
        return SystemUtils.executeCommand(nbLocation, commands.toArray(new String[]{})).getErrorCode() == 0;
    }

    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private JavaFXUtils() {
        // does nothing
    }

    /////////////////////////////////////////////////////////////////////////////////
    // Constants

    public static final String FXSDK_KEY =
            "SOFTWARE\\JavaSoft\\JavaFX SDK"; // NOI18N
    public static final String FXRUNTIME_KEY =
            "SOFTWARE\\JavaSoft\\JavaFX"; // NOI18N
    public static final String FXRUNTIME_INSTALLATION_KEY =
            "SOFTWARE\\Oracle\\JavaFX\\"; //NOI18N

    public static final String VERSION
            = "Version"; // NOI18N
    public static final String FX_VERSION
            = "FXVersion"; // NOI18N
    public static final String FX_SDK_HOME_PATH
            = "JFXSDKHome"; //NOI18N
    public static final String PATH
            = "Path"; //NOI18N
}
