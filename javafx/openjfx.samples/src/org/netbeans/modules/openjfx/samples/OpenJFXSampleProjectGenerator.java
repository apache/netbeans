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

package org.netbeans.modules.openjfx.samples;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Create a sample java project by unzipping a template into some directory.
 * Modify active platform and JavaFX related properties.
 *
 * @author Martin Grebac, Tomas Zezula, Anton Chechel, Petr Somol
 */
public class OpenJFXSampleProjectGenerator {

    private OpenJFXSampleProjectGenerator() {}

    public static FileObject createProjectFromTemplate(final FileObject template,
            File projectLocation, final String name) throws IOException {
        assert template != null && projectLocation != null && name != null;
        FileObject prjLoc = createProjectFolder(projectLocation);
        if (template.getExt().endsWith("zip")) { // NOI18N
            unzip(template.getInputStream(), prjLoc);
            prjLoc.refresh(false);
        }
        return prjLoc;
    }

    private static FileObject createProjectFolder(File projectFolder) throws IOException {
        FileObject projLoc;
        Stack<String> nameStack = new Stack<String>();
        while ((projLoc = FileUtil.toFileObject(projectFolder)) == null) {
            nameStack.push(projectFolder.getName());
            projectFolder = projectFolder.getParentFile();
        }
        while (!nameStack.empty()) {
            projLoc = projLoc.createFolder(nameStack.pop());
            assert projLoc != null;
        }
        return projLoc;
    }

    private static void unzip(InputStream source, FileObject targetFolder) throws IOException {
        //installation
        ZipInputStream zip = new ZipInputStream(source);
        try {
            ZipEntry ent;
            while ((ent = zip.getNextEntry()) != null) {
                if (ent.isDirectory()) {
                    FileUtil.createFolder(targetFolder, ent.getName());
                } else {
                    FileObject destFile = FileUtil.createData(targetFolder, ent.getName());
                    FileLock lock = destFile.lock();
                    try {
                        OutputStream out = destFile.getOutputStream(lock);
                        try {
                            FileUtil.copy(zip, out);
                        } finally {
                            out.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                }
            }
        } finally {
            zip.close();
        }
    }

}
