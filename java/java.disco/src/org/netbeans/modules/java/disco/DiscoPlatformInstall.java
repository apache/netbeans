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
package org.netbeans.modules.java.disco;

import java.io.File;
import javax.swing.filechooser.FileSystemView;
import org.netbeans.spi.java.platform.CustomPlatformInstall;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "DiscoPlatformInstall.displayName=Download OpenJDK (via Foojay.io Disco API)",
    "Disco.clientName=Apache NetBeans",
    "Disco.defaultDistribution=Zulu",
    "Disco.defaultDownloadDir=NetBeansJDKs"
})
public class DiscoPlatformInstall extends CustomPlatformInstall {

    private DiscoPlatformInstall() {
    }

    @Override
    public InstantiatingIterator<WizardDescriptor> createIterator() {
        return new DiscoPlatformIt();
    }

    @Override
    public String getDisplayName() {
        return Bundle.DiscoPlatformInstall_displayName();
    }

    public static DiscoPlatformInstall create() {
        return new DiscoPlatformInstall();
    }

    static String clientName() {
        return Bundle.Disco_clientName();
    }

    static String defaultDistribution() {
        return Bundle.Disco_defaultDistribution();
    }

    // @TODO - this uses similar logic to OpenProjectListSettings::getProjectsFolder
    // but should use preferences to store the last used location
    static File getDefaultDownloadFolder() {
        String dirName = Bundle.Disco_defaultDownloadDir();
        File dir;
        if (Boolean.getBoolean("netbeans.full.hack")) { // NOI18N
            File tmp = new File(System.getProperty("java.io.tmpdir", "")); // NOI18N
            dir = new File(tmp, dirName);
        } else {
            File parent = FileSystemView.getFileSystemView().getDefaultDirectory();
            if (parent != null && parent.isDirectory()) {
                dir = new File(parent, dirName);
            } else {
                dir = new File(System.getProperty("user.home", ""), dirName); // NOI18N
            }
        }
        if (!dir.exists()) {
            dir.mkdir();
        }
        return FileUtil.normalizeFile(dir);
    }

}
