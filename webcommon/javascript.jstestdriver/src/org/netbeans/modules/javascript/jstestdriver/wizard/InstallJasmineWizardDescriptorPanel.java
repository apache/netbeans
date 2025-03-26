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

package org.netbeans.modules.javascript.jstestdriver.wizard;

import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.event.ChangeListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 */
public class InstallJasmineWizardDescriptorPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    private InstallJasminePanel panel;
    private static final Logger LOGGER = Logger.getLogger(InstallJasmineWizardDescriptorPanel.class.getName());

    @Override
    public Component getComponent() {
        if (panel == null) {
            panel = new InstallJasminePanel();
        }
        return panel;
    }

    public boolean installJasmine() {
        return panel != null ? panel.installJasmine() : false;
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    @NbBundle.Messages("DownloadFailure=Download of remote files failed. See IDE log for more details.")
    public void downloadJasmine(FileObject libs) {
        try {
            File jasmine = Files.createTempFile("jasmine", "zip").toFile(); // NOI18N
            File jasmineJSTD = Files.createTempFile("jasmine-jstd", "zip").toFile(); // NOI18N
            download("https://github.com/pivotal/jasmine/zipball/v1.2.0", jasmine); // NOI18N
            download("https://github.com/ibolmo/jasmine-jstd-adapter/zipball/1.1.2", jasmineJSTD); // NOI18N
            unzip(new FileInputStream(jasmine), FileUtil.createFolder(libs, "jasmine"), "1.2.0"); // NOI18N
            unzip(new FileInputStream(jasmineJSTD), FileUtil.createFolder(libs, "jasmine-jstd-adapter"), "1.1.2"); // NOI18N
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "download of remote jasmine files failed", ex); // NOI18N
            DialogDisplayer.getDefault().notify(new DialogDescriptor.Message(
                    Bundle.DownloadFailure()));
        }
    }

    private static void download(String url, File target) throws IOException {
        try {
            InputStream is = new URL(url).openStream();
            try {
                copyToFile(is, target);
            } finally {
                is.close();
            }
        } catch (IOException ex) {
            // error => ensure file is deleted
            target.delete();
            throw ex;
        }
    }

    private static void unzip(InputStream source, FileObject root, String version) throws IOException {
        try {
            ZipInputStream str = new ZipInputStream(source);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                String entryName = entry.getName();
                String nameToCreate;
                if (entryName.endsWith("lib/jasmine-core/jasmine.css")) { // NOI18N
                    nameToCreate = "jasmine.css"; // NOI18N
                } else if (entryName.endsWith("lib/jasmine-core/jasmine.js")) { // NOI18N
                    nameToCreate = "jasmine.js"; // NOI18N
                } else if (entryName.endsWith("lib/jasmine-core/jasmine-html.js")) { // NOI18N
                    nameToCreate = "jasmine-html.js"; // NOI18N
                } else if (entryName.endsWith("lib/jasmine-core/json2.js")) { // NOI18N
                    nameToCreate = "json2.js"; // NOI18N
                } else if (entryName.endsWith("MIT.LICENSE")) { // NOI18N
                    nameToCreate = "MIT.LICENSE"; // NOI18N
                } else if (entryName.endsWith("src/JasmineAdapter.js")) { // NOI18N
                    nameToCreate = "JasmineAdapter.js"; // NOI18N
                } else {
                    continue;
                }
                FileObject fo = FileUtil.createData(root, nameToCreate);
                writeFile(str, fo);
            }
            FileObject fo = FileUtil.createData(root, "version.txt"); // NOI18N
            InputStream is2 = new ByteArrayInputStream(version.getBytes());
            writeFile(is2, fo);

        } finally {
            source.close();
        }
    }

    private static File copyToFile(InputStream is, File target) throws IOException {
        OutputStream os = new FileOutputStream(target);
        try {
            FileUtil.copy(is, os);
        } finally {
            os.close();
        }
        return target;
    }

    private static void writeFile(InputStream str, FileObject fo) throws IOException {
        OutputStream out = fo.getOutputStream();
        try {
            FileUtil.copy(str, out);
        } finally {
            out.close();
        }
    }

}
