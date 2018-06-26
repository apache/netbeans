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
            File jasmine = File.createTempFile("jasmine", "zip"); // NOI18N
            File jasmineJSTD = File.createTempFile("jasmine-jstd", "zip"); // NOI18N
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
