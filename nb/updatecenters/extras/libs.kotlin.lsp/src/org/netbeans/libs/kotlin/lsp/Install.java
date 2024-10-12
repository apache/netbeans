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
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInfo;
import org.openide.modules.ModuleInstall;
import org.openide.modules.Places;
import org.openide.util.*;

public class Install extends ModuleInstall {

    private static final String LSP_SERVER_VERSION = "1.3.3";
    
    @Override
    public void restored() {
        File serverCache = Places.getCacheSubdirectory("kotlin-lsp/server-" + LSP_SERVER_VERSION);
        File serverDir = new File(serverCache, "server");
        File executable = new File(new File(serverDir, "bin"), "kotlin-language-server");

        if (!executable.isFile()) {
            File serverZip = InstalledFileLocator.getDefault().locate("kotlin-lsp/server-" + LSP_SERVER_VERSION +".zip", null, false);
            try (ZipFile zf = new ZipFile(serverZip)) {
                Enumeration<? extends ZipEntry> en = zf.entries();

                while (en.hasMoreElements()) {
                    ZipEntry ze = en.nextElement();
                    if (ze.isDirectory()) {
                        continue;
                    }
                    File target = new File(serverCache, ze.getName());
                    target.getParentFile().mkdirs();
                    try (OutputStream out = new FileOutputStream(target)) {
                        FileUtil.copy(zf.getInputStream(ze), out);
                    }
                }
                if (!Utilities.isWindows()) {
                    executable.setExecutable(true);
                }
                for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
                    if ("org.netbeans.modules.kotlin.editor".equals(mi.getCodeNameBase())) {
                        Class<?> utils = mi.getClassLoader().loadClass("org.netbeans.modules.kotlin.editor.lsp.Utils");
                        NbPreferences.forModule(utils).node("lsp").put("location", serverDir.getAbsolutePath());
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

}
