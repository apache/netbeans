/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cordova.wizard;

import org.netbeans.modules.cordova.platforms.api.PlatformManager;
import org.netbeans.modules.cordova.platforms.spi.Device;
import java.io.File;
import java.io.IOException;
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
            ios.put("ios.build.sdk", PlatformManager.getPlatform(PlatformManager.IOS_TYPE).getPrefferedTarget().getIdentifier()); // NOI18N
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
            String sim = PlatformManager.getPlatform(PlatformManager.IOS_TYPE).getPrefferedTarget().getIdentifier();
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
