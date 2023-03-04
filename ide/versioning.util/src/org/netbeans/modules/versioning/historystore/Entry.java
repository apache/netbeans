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
package org.netbeans.modules.versioning.historystore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author ondra
 */
class Entry{

    private final File storeFile;

    Entry(File storeFile) {
        this.storeFile = storeFile;
    }

    public OutputStream getStoreFileOutputStream() throws FileNotFoundException, IOException {
        return createStoreFileOutputStream(storeFile);
    }

    public InputStream getStoreFileInputStream() throws FileNotFoundException, IOException {
        storeFile.setLastModified(System.currentTimeMillis());
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(storeFile)));
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.getName().equals(storeFile.getName())) {
                return zis;
            }
        }
        throw new FileNotFoundException();
    }

    private OutputStream createStoreFileOutputStream(File storeFile) throws FileNotFoundException, IOException {
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(storeFile)));
        ZipEntry entry = new ZipEntry(storeFile.getName());
        zos.putNextEntry(entry);
        return zos;
    }
}
