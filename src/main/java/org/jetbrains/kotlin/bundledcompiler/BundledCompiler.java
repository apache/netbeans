/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.bundledcompiler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jetbrains.kotlin.log.KotlinLogger;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;

public class BundledCompiler {

    private static void unZipFile(InputStream fis, FileObject destDir) throws IOException {
        try {
            ZipInputStream str = new ZipInputStream(fis);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(destDir, entry.getName());
                } else {
                    FileObject fo = FileUtil.createData(destDir, entry.getName());
                    FileLock lock = fo.lock();
                    try {
                        OutputStream out = fo.getOutputStream(lock);
                        try {
                            FileUtil.copy(str, out);
                        } finally {
                            out.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                }
            }
        } finally {
            fis.close();
        }
    }
    
    public static void getKotlinc() {
        try {
            InputStream inputStream = BundledCompiler.class.getClassLoader().getResourceAsStream("/org/jetbrains/kotlin/kotlinc/kotlinc.zip");

            File userDirectory = Places.getUserDirectory();
            if (userDirectory == null) {
                KotlinLogger.INSTANCE.logWarning("User directory is null");
                return;
            }
            FileObject destFileObj = FileUtil.toFileObject(userDirectory);
            unZipFile(inputStream, destFileObj);
        } catch (IOException ex) {
            KotlinLogger.INSTANCE.logException("unZipFile() exception", ex);
        }
    }
    
}
