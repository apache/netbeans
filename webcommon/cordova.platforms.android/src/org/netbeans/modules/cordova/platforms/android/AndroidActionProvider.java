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
package org.netbeans.modules.cordova.platforms.android;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.platforms.spi.BuildPerformer;
import org.netbeans.modules.cordova.platforms.spi.Device;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.spi.ProjectBrowserProvider;
import org.netbeans.spi.project.ActionProvider;

import static org.netbeans.spi.project.ActionProvider.COMMAND_BUILD;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Cordova Action Provider. Invokes cordova build.
 * @author Jan Becicka
 */
@NbBundle.Messages({
    "ERR_Title=Error",
    "LBL_CheckingDevice=Connecting to android device...",
    
    "ERR_WebDebug=Cannot connect to Chrome.\nMake sure, that:\n"
        + "\u2022 Device is attached\n"
        + "\u2022 USB Debugging is enabled on your device\n"
        + "\u2022 Your computer and Android device are connected to the same WiFi network",        
    
    "ERR_NO_Cordova=NetBeans cannot find cordova or git on your PATH. Please install cordova and git.\n" +
            "NetBeans might require restart for changes to take effect.\n"
})
public class AndroidActionProvider implements ActionProvider {

    private final Project p;
    private static final Logger LOGGER = Logger.getLogger(AndroidActionProvider.class.getName());

    public AndroidActionProvider(Project p) {
        this.p = p;
    }
    
    @Override
    public String[] getSupportedActions() {
        return new String[]{
                    COMMAND_BUILD,
                    COMMAND_CLEAN,
                    COMMAND_RUN,
                    COMMAND_RUN_SINGLE,
                    COMMAND_REBUILD
                };
    }

    @NbBundle.Messages({
        "LBL_AvdManager=AVD Manager",
        "ERR_NO_JDK=NetBeans is currently running on JRE. Cordova Android build requires JDK. Please run NetBeans on JDK to continue."
    })
    @Override
    public void invokeAction(String command, final Lookup context) throws IllegalArgumentException {
        final BuildPerformer build = Lookup.getDefault().lookup(BuildPerformer.class);
        String checkAndroid = checkAndroid();
        if (checkAndroid != null) {
            NotifyDescriptor not = new NotifyDescriptor(
                    checkAndroid,
                    Bundle.ERR_Title(),
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE,
                    null,
                    null);
            Object value = DialogDisplayer.getDefault().notify(not);
            if (NotifyDescriptor.CANCEL_OPTION != value) {
                OptionsDisplayer.getDefault().open("Html5/MobilePlatforms"); // NOI18N
            }
            return;
        }

        // Quick fix for issue when IDE is running on JRE, Android build fails because it needs javac
        String jdkHome = System.getProperty("jdk.home"); //NOI18N
        NotifyDescriptor.Message notJDK = new DialogDescriptor.Message(
                Bundle.ERR_NO_JDK(),
                DialogDescriptor.ERROR_MESSAGE);
        if (jdkHome == null || jdkHome.isEmpty()) {
            DialogDisplayer.getDefault().notify(notJDK);
            return;
        } else {
            FileObject jdkHomeFO = FileUtil.toFileObject(new File(jdkHome, "bin")); //NOI18N
            String javacFileName = Utilities.isWindows() ? "javac.exe" : "javac"; //NOI18N
            FileObject javacFO = jdkHomeFO.getFileObject(javacFileName);
            if (javacFO == null) {
                DialogDisplayer.getDefault().notify(notJDK);
                return;
            }
        }

        if (COMMAND_BUILD.equals(command) || COMMAND_CLEAN.equals(command) || COMMAND_REBUILD.equals(command)) {
            try {
                switch (command) {
                    case COMMAND_BUILD:
                        build.perform(BuildPerformer.BUILD_ANDROID, p);
                        break;
                    case COMMAND_CLEAN:
                        build.perform(BuildPerformer.CLEAN_ANDROID, p);
                        break;
                    case COMMAND_REBUILD:
                        build.perform(BuildPerformer.REBUILD_ANDROID, p);
                        break;
                }
            } catch (UnsupportedOperationException ex) {
                NotifyDescriptor.Message not = new DialogDescriptor.Message(
                        Bundle.ERR_NO_Cordova(),
                        DialogDescriptor.ERROR_MESSAGE);
                Object value = DialogDisplayer.getDefault().notify(not);
                return;
            }
        } else if (COMMAND_RUN.equals(command) || COMMAND_RUN_SINGLE.equals(command)) {
            BaseProgressUtils.runOffEventDispatchThread(new Runnable() {
                @Override
                public void run() {
                    String checkDevices = checkDevices(p);                    
                    while (checkDevices != null) {
                        NotifyDescriptor not = new NotifyDescriptor(
                                checkDevices,
                                Bundle.ERR_Title(),
                                NotifyDescriptor.DEFAULT_OPTION,
                                NotifyDescriptor.ERROR_MESSAGE,
                                checkDevices.equals(Bundle.ERR_RunAndroidEmulator())
                                        ? new Object[]{
                                    DialogDescriptor.OK_OPTION,
                                    DialogDescriptor.CANCEL_OPTION,
                                    Bundle.LBL_AvdManager()
                                } : null,
                                null);
                        Object value = DialogDisplayer.getDefault().notify(not);
                        if (NotifyDescriptor.CANCEL_OPTION == value || checkDevices.equals(Bundle.ERR_AdbNotFound())) {
                            return;
                        } else if (Bundle.LBL_AvdManager().equals(value)) {
                            RequestProcessor.getDefault().post(new Runnable() {
                                @Override
                                public void run() {
                                    AndroidPlatform.getDefault().manageDevices();
                                }
                            });
                            return;
                        } else {
                            checkDevices = checkDevices(p);
                        }
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                build.perform(BuildPerformer.RUN_ANDROID, p);
                            } catch (UnsupportedOperationException ex) {
                                NotifyDescriptor not = new NotifyDescriptor(
                                        Bundle.ERR_NO_Cordova(),
                                        Bundle.ERR_Title(),
                                        NotifyDescriptor.OK_CANCEL_OPTION,
                                        NotifyDescriptor.ERROR_MESSAGE,
                                        null,
                                        null);
                                Object value = DialogDisplayer.getDefault().notify(not);
                                if (NotifyDescriptor.CANCEL_OPTION != value) {
                                    OptionsDisplayer.getDefault().open("Html5/MobilePlatforms"); // NOI18N
                                }
                                return;
                            } catch (IllegalStateException ex) {
                                StatusDisplayer.getDefault().setStatusText(ex.getMessage());
                            }
                        }
                    });
                }
            }, Bundle.LBL_CheckingDevice(), new AtomicBoolean(), false);
        }
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return true;
    }

    @NbBundle.Messages({
        "ERR_ConnectAndroidDevice=Please connect Android device and make sure that:\n"
        + "\u2022 USB Debugging is enabled on your device\n"
        + "\u2022 Your computer and Android device are connected to the same WiFi network",
        "ERR_RunAndroidEmulator=Please run Android Emulator.",
        "ERR_Unknown=Unknown Error.",
        "ERR_AdbNotFound=ADB not found. Please make sure that:\n"
        + "\u2022 Android SDK has been installed correctly,\n"
        + "\u2022 Close Android SDK Manager, if it's opened."
    })
    static String checkDevices(Project p) {
        if (!AndroidPlatform.getDefault().adbCommandExists()) {
            return Bundle.ERR_AdbNotFound();
        }
        ProjectBrowserProvider provider = p.getLookup().lookup(ProjectBrowserProvider.class);
        WebBrowser activeConfiguration = provider.getActiveBrowser();
        try {
            if (activeConfiguration.getId().endsWith("_1")) { //NOI18N
                for (Device dev : AndroidPlatform.getDefault().getConnectedDevices()) {
                    if (!dev.isEmulator()) {
                        return null;
                    }
                }
                return Bundle.ERR_ConnectAndroidDevice();
            } else {
                for (Device dev : AndroidPlatform.getDefault().getConnectedDevices()) {
                    if (dev.isEmulator()) {
                        return null;
                    }
                }
                return Bundle.ERR_RunAndroidEmulator();
            }
        } catch (IOException iOException) {
            Exceptions.printStackTrace(iOException);
        }
        return Bundle.ERR_Unknown();
    }
    
    @NbBundle.Messages("ERR_AndroidNotConfigured=Android Platform is not configured.\nConfigure?")
    static String checkAndroid() {
        if (!AndroidPlatform.getDefault().isReady()) {
            return Bundle.ERR_AndroidNotConfigured();
        }
        return null;
    }
    
}
