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
package org.netbeans.modules.cordova.wizard;

import org.netbeans.modules.cordova.platforms.api.PlatformManager;
import org.netbeans.modules.cordova.platforms.spi.Device;
import java.io.File;
import java.io.IOException;
import org.netbeans.modules.cordova.platforms.spi.MobilePlatform;
import org.netbeans.modules.cordova.project.ConfigUtils;
import org.netbeans.modules.web.clientproject.api.ClientSideModule;
import org.netbeans.modules.web.clientproject.spi.ClientProjectExtender;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Becicka
 */
@ServiceProvider(service = ClientProjectExtender.class)
public class CordovaProjectExtender implements ClientProjectExtender {

    @Override
    public Panel<WizardDescriptor>[] createWizardPanels() {
        return new Panel[0];
    }

    @Override
    @NbBundle.Messages({
        "LBL_iPhoneDevice=iPhone Device",
    })

    public void apply(FileObject projectRoot, FileObject siteRoot, String librariesPath) {
        try {
            createMobileConfigs(projectRoot);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void createMobileConfigs(FileObject projectRoot) throws IOException {
        
        File f = new File(projectRoot.getPath() + "/nbproject/configs/ios.properties"); // NOI18N
        if (!f.exists()) {
            EditableProperties ios = new EditableProperties(true);
            ios.put(ConfigUtils.DISPLAY_NAME_PROP, Bundle.LBL_iPhoneSimulator());
            ios.put(Device.TYPE_PROP, PlatformManager.IOS_TYPE);
            ios.put(Device.DEVICE_PROP, Device.EMULATOR);
            MobilePlatform iosPlatform = PlatformManager.getPlatform(PlatformManager.IOS_TYPE);
            ios.put("ios.build.sdk", iosPlatform == null ? "" : iosPlatform.getPrefferedTarget().getIdentifier()); // NOI18N
            ios.put("ios.build.arch", "i386"); // NOI18N

            ConfigUtils.createConfigFile(projectRoot, PlatformManager.IOS_TYPE, ios);//NOI18N
        }
        FileObject fob = FileUtil.toFileObject(f);
        assert fob!=null;

        f = new File(projectRoot.getPath() + "/nbproject/configs/ios_1.properties"); // NOI18N
        if (!f.exists()) {

            EditableProperties iosdev = new EditableProperties(true);
            iosdev.put(ConfigUtils.DISPLAY_NAME_PROP, Bundle.LBL_iPhoneDevice());
            iosdev.put(Device.TYPE_PROP, PlatformManager.IOS_TYPE);
            iosdev.put(Device.DEVICE_PROP, Device.DEVICE);
            MobilePlatform iosPlatform = PlatformManager.getPlatform(PlatformManager.IOS_TYPE);
            String sim = iosPlatform == null ? "" : iosPlatform.getPrefferedTarget().getIdentifier();
            iosdev.put("ios.build.sdk", sim.replace("iphonesimulator", "iphoneos")); // NOI18N
            iosdev.put("ios.build.arch", sim.startsWith("iphonesimulator6")?"armv6 armv7":"armv7 armv7s"); // NOI18N

            ConfigUtils.createConfigFile(projectRoot, PlatformManager.IOS_TYPE, iosdev);//NOI18N
        }
        fob = FileUtil.toFileObject(f);
        assert fob!=null;

        f = new File(projectRoot.getPath() + "/nbproject/configs/android.properties"); // NOI18N
        if (!f.exists()) {

            EditableProperties androide = new EditableProperties(true);
            androide.put(ConfigUtils.DISPLAY_NAME_PROP, Bundle.LBL_AndroidEmulator());
            androide.put(Device.TYPE_PROP, PlatformManager.ANDROID_TYPE);//NOI18N
            androide.put(Device.DEVICE_PROP, Device.EMULATOR);//NOI18N
            ConfigUtils.createConfigFile(projectRoot, PlatformManager.ANDROID_TYPE, androide);//NOI18N
        }
        fob = FileUtil.toFileObject(f);
        assert fob!=null;

        f = new File(projectRoot.getPath() + "/nbproject/configs/android_1.properties"); // NOI18N
        if (!f.exists()) {

            EditableProperties androidd = new EditableProperties(true);
            androidd.put(ConfigUtils.DISPLAY_NAME_PROP, Bundle.LBL_AndroidDevice());
            androidd.put(Device.TYPE_PROP, PlatformManager.ANDROID_TYPE);//NOI18N
            androidd.put(Device.DEVICE_PROP, Device.DEVICE);//NOI18N
            ConfigUtils.createConfigFile(projectRoot, PlatformManager.ANDROID_TYPE, androidd);//NOI18N
        }
        fob = FileUtil.toFileObject(f);
        assert fob!=null;
    }

    @Override
    public Panel<WizardDescriptor>[] createInitPanels() {
        return new Panel[0];
    }

    @Override
    public void initialize(WizardDescriptor wizardDescriptor) {
    }
}
