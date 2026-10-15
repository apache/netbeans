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
package org.netbeans.modules.maven.htmlui;

import java.awt.EventQueue;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFileChooser;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.util.NbBundle.Messages;
import net.java.html.json.Model;
import net.java.html.json.Property;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.ModelOperation;
import net.java.html.json.OnPropertyChange;
import net.java.html.json.OnReceive;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

@Model(className = "WizardData", properties = {
    @Property(name = "current", type = String.class),
    @Property(name = "ok", type = boolean.class),
    @Property(name = "warning", type = String.class),
    @Property(name = "archetype", type = ArchetypeData.class),
    @Property(name = "archetypes", type = ArchetypeData.class, array = true),
    @Property(name = "android", type = boolean.class),
    @Property(name = "ios", type = boolean.class),
    @Property(name = "iosMoe", type = boolean.class),
    @Property(name = "iosRoboVM", type = boolean.class),
    @Property(name = "availableSimulators", type = Device.class, array = true),
    @Property(name = "selectedSimulator", type = Device.class),
    @Property(name = "web", type = boolean.class),
    @Property(name = "netbeans", type = boolean.class),
    @Property(name = "installExample", type = boolean.class),
    @Property(name = "androidSdkPath", type = String.class),
    @Property(name = "nbhome", type = String.class),
    @Property(name = "nbInstallationDefined", type = boolean.class),
})
public class DukeScriptWizard {
    @TemplateRegistration(
            position = 955,
            page = "dukeScriptWizard.html",
            content = "dukescript.archetype",
            folder = "Project/Maven2",
            displayName = "#DukeScriptWizard_displayName",
            iconBase = "org/netbeans/modules/maven/htmlui/DukeHTML.png",
            description = "description.html"
    )
    
    @Messages("DukeScriptWizard_displayName=DukeScript Frontend Application")
    public static WizardData javafxWebViewAppWizard() {
        WizardData data = new WizardData();
        data.init(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);
        return data;
    }

    @Model(className = "ArchetypeData", properties = {
        @Property(name = "artifactId", type = String.class),
        @Property(name = "groupId", type = String.class),
        @Property(name = "example", type = boolean.class),
        @Property(name = "version", type = String.class),
        @Property(name = "name", type = String.class),
        @Property(name = "description", type = String.class),
        @Property(name = "url", type = String.class),
        @Property(name = "open", array = true, type = String.class),})
    static class ArchetypeViewModel {
    }

    @ModelOperation
    static void init(WizardData data,
            Boolean android, Boolean ios, Boolean web, Boolean netbeans
    ) {
        File nbHome = new File(System.getProperty("netbeans.home"));
        data.setNbhome(nbHome.getParent());

        final ArchetypeData koArch = new ArchetypeData(
                "knockout4j-archetype",
                "com.dukescript.archetype",
                true, MAVEN_ARCHETYPES_VERSION,
                "Basic DukeScript Template", "Default skeletal application",
                null
        );
        data.setArchetype(koArch);
        data.getArchetypes().add(koArch);
        final ArchetypeData crudArch = new ArchetypeData(
                "crud4j-archetype",
                "com.dukescript.archetype",
                false, MAVEN_ARCHETYPES_VERSION,
                "DukeScript CRUD Template", "Client-Server Application demonstrating communication and reuse of DataModels",
                null
        );
        data.getArchetypes().add(crudArch);
        final ArchetypeData visArch = new ArchetypeData(
                "visual-archetype",
                "com.dukescript.archetype",
                false, MAVEN_ARCHETYPES_VERSION,
                "DukeScript Visual Archetype", "A sample application demonstrating Canvas, Charts & Maps",
                null
        );
        data.getArchetypes().add(visArch);
        data.setIosMoe(true);
        String srvPath = Boolean.getBoolean("staging.archetypes") ? "stage" : "archetypes";
        data.loadArchetypes(srvPath);
        data.setAndroidSdkPath(MavenUtilities.getDefault().readAndroidSdkPath());
    }
    private static final String MAVEN_ARCHETYPES_VERSION = "0.41";

    @ComputedProperty
    static String archetypeGroupId(ArchetypeData archetype) {
        return archetype == null ? null : archetype.getGroupId();
    }

    @ComputedProperty
    static String archetypeArtifactId(ArchetypeData archetype) {
        return archetype == null ? null : archetype.getArtifactId();
    }

    @ComputedProperty
    static String archetypeVersion(ArchetypeData archetype) {
        return archetype == null ? null : archetype.getVersion();
    }

    @ComputedProperty
    static String archetypeOpen(ArchetypeData archetype) {
        StringBuilder sb = new StringBuilder();
        if (archetype != null) {
            for (String item : archetype.getOpen()) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(item);
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    @ComputedProperty
    static String webpath(boolean web) {
        return web ? "client-web" : null;
    }

    @ComputedProperty
    static String androidpath(boolean android) {
        return android ? "client-android" : null;
    }

    @ComputedProperty
    static String iospath(boolean ios, boolean iosRoboVM) {
         return ios && iosRoboVM ? "client-ios" : null;
    }

    @ComputedProperty
    static String moepath(boolean ios, boolean iosMoe) {
       return ios && iosMoe ? "client-moe" : null;
    }
    
    @ComputedProperty
    static String netbeanspath(boolean netbeans) {
        return netbeans ? "client-netbeans" : null;
    }

    @ComputedProperty
    static String example(boolean installExample) {
        return Boolean.toString(installExample);
    }

    @ComputedProperty
    static int errorCode(
            String current,
            boolean android,
            String androidSdkPath,
            boolean netbeans,
            boolean nbInstallationDefined,
            boolean ios, boolean iosMoe, boolean iosRoboVM,
            Device selectedSimulator,
            String warning
    ) {
        if (android && "platforms".equals(current)) { // NOI18N
            if (androidSdkPath == null) {
                return 7;
            }
            if (!isValidAndroidSdk(new File(androidSdkPath))) {
                return 7;
            }
        }
        if (netbeans && "platforms".equals(current)) { // NOI18N
            if (!nbInstallationDefined) {
                return 8;
            }
        }
        if (warning != null) {
            return 6;
        }
        if (ios) {
            if (!iosMoe && !iosRoboVM) {
                return 3;
            }
            if (selectedSimulator == null || MavenUtilities.getDefault().readMoeDevice() == null) {
                return 4;
            }
        }
        return 0;
    }

    @Function
    static void cleanWarning(WizardData data) {
        data.setWarning(null);
    }

    @Function
    static void chooseAndroidSDK(final WizardData data) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                FileChooserBuilder b = new FileChooserBuilder(WizardData.class).
                        setSelectionApprover(new FileChooserBuilder.SelectionApprover() {
                            @Override
                            public boolean approve(File[] files) {
                                if (files.length != 1) {
                                    return false;
                                }
                                return isValidAndroidSdk(files[0]);
                            }
                        }).
                        setDirectoriesOnly(true);
                if (data.getAndroidSdkPath() != null) {
                    b.setDefaultWorkingDirectory(new File(data.getAndroidSdkPath()));
                }
                JFileChooser fc = b.createFileChooser();
                int res = fc.showOpenDialog(Utilities.findDialogParent());
                if (res == JFileChooser.APPROVE_OPTION) {
                    data.setAndroidSdkPath(fc.getSelectedFile().getPath());
                    MavenUtilities.getDefault().writeAndroidSdkPath(fc.getSelectedFile().getPath());
                }
            }
        });
    }

    private static boolean isValidAndroidSdk(File dir) {
        return new File(new File(dir, "platform-tools"), "adb.exe").exists()
                || new File(new File(dir, "platform-tools"), "adb").exists();
    }

    @OnPropertyChange(value = "netbeans")
    static void verifyNbInstallationDefined(WizardData data) {
        boolean ok = !data.isNetbeans() || MavenUtilities.getDefault().readNetBeansInstallation() != null;
        data.setNbInstallationDefined(ok);
    }

    @OnPropertyChange(value = "selectedSimulator")
    static void deviceSelected(WizardData data) {
        if (data.getSelectedSimulator() != null) {
            MavenUtilities.getDefault().writeMoeDevice(data.getSelectedSimulator().getId());
            String name = data.getSelectedSimulator().getName().replaceAll("\\(.*\\)", "").trim();
            MavenUtilities.getDefault().writeRobovmDeviceName(name);
        }
    }
    
    private static final RequestProcessor DEVICES = new RequestProcessor("List iOS Devices");
    @OnPropertyChange(value = "ios")
    static void verifySimulator(WizardData data) {
        final List<Device> arr = data.getAvailableSimulators();
        DEVICES.post(() -> {
            DeviceType.listDevices(arr);
            String selectedDevice = MavenUtilities.getDefault().readMoeDevice();
            Iterator<Device> it = arr.iterator();
            while (it.hasNext()) {
                Device d = it.next();
                if (d.getType() != DeviceType.SIMULATOR) {
                    if (d.getType() == null) {
                        data.setWarning(d.getInfo());
                    }
                    it.remove();
                    continue;
                }
                if (selectedDevice != null && selectedDevice.equals(d.getId())) {
                    data.setSelectedSimulator(d);
                }
            }
        });
    }

    @Function
    static void defineNbInstallation(final WizardData data) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                FileChooserBuilder b = new FileChooserBuilder(WizardData.class).
                        setSelectionApprover(new FileChooserBuilder.SelectionApprover() {
                            @Override
                            public boolean approve(File[] files) {
                                if (files.length != 1) {
                                    return false;
                                }
                                File platform = new File(files[0], "platform");
                                File lib = new File(platform, "lib");
                                File bootJar = new File(lib, "boot.jar");
                                return bootJar.exists();
                            }
                        }).
                        setDirectoriesOnly(true);
                JFileChooser fc = b.createFileChooser();
                File f = new File(System.getProperty("netbeans.home"));
                fc.setCurrentDirectory(f);
                int res = fc.showOpenDialog(Utilities.findDialogParent());
                if (res == JFileChooser.APPROVE_OPTION) {
                    MavenUtilities.getDefault().writeNetBeansInstallation(f.getParent());
                    if (MavenUtilities.getDefault().readNetBeansInstallation() != null) {
                        data.setNbInstallationDefined(true);
                    }
                }
            }
        });
    }

    @Messages({
        "ERR_NoData=Loaded data are corrupted!"
    })
    @OnReceive(url = "http://dukescript.com/presenters/{path}", onError = "loadError")
    static void loadArchetypes(WizardData model, List<ArchetypeData> found) {
        if (!found.isEmpty()) {
            final ArchetypeData first = found.get(0);
            if (first == null || first.getName() == null) {
                model.setWarning(Bundle.ERR_NoData());
                return;
            }
            model.getArchetypes().clear();
            model.getArchetypes().addAll(found);
            model.setArchetype(first);
        }
    }

    @Messages({
        "ERR_NoNetwork=Warning: No network connection. This wizard is based on Maven.\n" +
        "To work properly it needs a network connection. Please check your network settings: {0}\n",
    })
    static void loadError(WizardData model, Throwable t) {
        model.setWarning(Bundle.ERR_NoNetwork(t.getLocalizedMessage()));
    }

}
