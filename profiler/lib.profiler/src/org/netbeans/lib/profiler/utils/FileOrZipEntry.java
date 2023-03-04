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

package org.netbeans.lib.profiler.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * A container that can represent either a plain file, or an entry in the zip/jar archive.
 * Used for unification of read operations on both types of files. So far likely not the most clean implementation.
 *
 * @author  Misha Dmitriev
 */
public class FileOrZipEntry {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private File file;
    private String dirOrJar;
    private String fileName;
    private boolean isZipEntry;
    private long len;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public FileOrZipEntry(String dirOrJar, String fileName) {
        this.dirOrJar = dirOrJar;
        this.fileName = fileName;

        String lcd = dirOrJar.toLowerCase();
        isZipEntry = (lcd.endsWith(".jar") || lcd.endsWith(".zip")); // NOI18N
        len = -1;
    }

    public FileOrZipEntry(File file) {
        this.file = file;
        isZipEntry = false;
        len = -1;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public File getFile() {
        if (isZipEntry) {
            return null;
        }

        if (file == null) {
            file = new File(dirOrJar, fileName);
        }

        return file;
    }

    public boolean isFile() {
        return (!isZipEntry);
    }

    public String getFullName() {
        if (!isZipEntry) {
            if (file == null) {
                file = new File(dirOrJar, fileName);
            }

            return file.getAbsolutePath();
        } else {
            return dirOrJar + "/" + fileName; // NOI18N
        }
    }

    public InputStream getInputStream() throws IOException {
        if (file != null) {
            return new FileInputStream(file);
        } else if (!isZipEntry) {
            file = new File(dirOrJar, fileName);
            len = file.length();

            return new FileInputStream(file);
        } else {
            ZipFile zip = new ZipFile(dirOrJar);
            ZipEntry entry = zip.getEntry(fileName);
            len = entry.getSize();

            return zip.getInputStream(entry);
        }
    }

    public long getLength() throws IOException {
        if (len != -1) {
            return len;
        } else if (file != null) {
            return file.length();
        } else if (!isZipEntry) {
            return (new File(dirOrJar, fileName)).length();
        } else {
            ZipFile zip = new ZipFile(dirOrJar);

            return zip.getEntry(fileName).getSize();
        }
    }

    public String getName() {
        if (isZipEntry) {
            int lastSlashIdx = fileName.lastIndexOf('/'); // NOI18N

            if (lastSlashIdx == -1) {
                return fileName;
            } else {
                return fileName.substring(lastSlashIdx + 1);
            }
        } else {
            if (fileName == null) {
                fileName = file.getName();
            }

            return fileName;
        }
    }
}
