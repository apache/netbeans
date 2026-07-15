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
package org.netbeans.libs.kotlin.lsp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.prefs.Preferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.OnStart;
import org.openide.modules.Places;
import org.openide.util.*;

@OnStart
public class UnpackAndInstall implements Runnable {

    private static final String KEY_KOTLIN_LSP_INSTALLED_COUNTER = "lsp-installed-counter";

    public void run() {
        File serverDir = Places.getCacheSubdirectory("kotlin-lsp/1.3.13");
        File lspExecutable = new File(new File(serverDir, "bin"), "kotlin-language-server");
        boolean modified = false;

        if (!lspExecutable.canRead()) {
            File serverZip = InstalledFileLocator.getDefault().locate("kotlin-lsp/server.zip", "org.netbeans.libs.kotlin.lsp", false);
            FileObject serverContent = FileUtil.getArchiveRoot(FileUtil.toFileObject(serverZip));
            Enumeration<? extends FileObject> children = serverContent.getChildren(true);

            while (children.hasMoreElements()) {
                FileObject current = children.nextElement();

                if (!current.isData()) {
                    continue;
                }

                File target = new File(serverDir, FileUtil.getRelativePath(serverContent, current).replace("server/", ""));

                target.getParentFile().mkdirs();

                try (InputStream in = current.getInputStream();
                     OutputStream out = new FileOutputStream(target)) {
                    FileUtil.copy(in, out);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            modified = true;
        }

        if (!lspExecutable.canExecute() && !Utilities.isWindows()) {
            lspExecutable.setExecutable(true, true);
            modified = true;
        }

        if (modified) {
            Preferences kotlinEditorSettingsRoot = NbPreferences.root().node("org/netbeans/modules/kotlin/editor");//NOI18N
            kotlinEditorSettingsRoot.putInt(KEY_KOTLIN_LSP_INSTALLED_COUNTER, kotlinEditorSettingsRoot.getInt(KEY_KOTLIN_LSP_INSTALLED_COUNTER, 0) + 1);
        }
    }

}
