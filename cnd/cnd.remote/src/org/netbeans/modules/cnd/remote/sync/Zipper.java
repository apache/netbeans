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

package org.netbeans.modules.cnd.remote.sync;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Creates a zip file, adds entries.
 * The clas is NOT thread safe
 */
public class Zipper {

    private final File zipFile;
    private ZipOutputStream zipOutputStream;
    private int count;

    public Zipper(File zipFile) throws FileNotFoundException {
        this.zipFile = zipFile;
        this.count = 0;
        zipOutputStream = null;
    }

    /**
     * Lazily gets ZipOutputStream.
     * Don't call this if you aren't going to add entries,
     * otherwise you'll get ZipException (ZIP file must have at least one entry)
     * when closing
     */
    private ZipOutputStream getZipOutputStream() throws FileNotFoundException {
        if (zipOutputStream == null) { // Not thread safe!
            zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
        }
        return zipOutputStream;
    }

    public void add(File srcDir, FileFilter filter) throws IOException {
        add(srcDir, filter, null);
    }

    public void add(File srcDir, FileFilter filter, String base) throws IOException {
        // Create a buffer for reading the files
        byte[] readBuf = new byte[1024*32];
        if (srcDir.isDirectory()) {
            File[] srcFiles = srcDir.listFiles(filter);
            if (srcFiles != null) {
                // Compress the files
                for (File file : srcFiles) {
                    addImpl(file, readBuf, base, filter);
                }
            }
        } else {
            addImpl(srcDir, readBuf, base, filter);
        }
    }

//    private void addImpl(File file, byte[] readBuf, String base, FileFilter filter) throws IOException, FileNotFoundException {
//
//    }

    public void close() throws IOException {
        if (zipOutputStream != null) { // Not thread safe!
            zipOutputStream.close();
        }
    }

    public int getFileCount() {
        return count;
    }

    private static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    private void addImpl(File file, byte[] readBuf, String base, FileFilter filter) throws IOException, FileNotFoundException {
        //System.err.printf("Zipping %s %s...\n", (file.isDirectory() ? " DIR  " : " FILE "), file.getAbsolutePath());
        if (file.isDirectory()) {
            File[] children = file.listFiles(filter);
            if (children != null) {
                for (File child : children) {
                    String newBase = isEmpty(base) ? file.getName() : (base + "/" + file.getName()); // NOI18N
                    addImpl(child, readBuf, newBase, filter);
                }
            }
            return;
        }
        count++;
        InputStream in = getFileInputStream(file);
        // Add ZIP entry to output stream.
        String name = isEmpty(base) ? file.getName() : base + '/' + file.getName();
        ZipEntry entry = new ZipEntry(name);
        entry.setTime(file.lastModified());
        //System.err.printf("Zipping %s\n", name);
        getZipOutputStream().putNextEntry(entry);
        // Transfer bytes from the file to the ZIP file
        int len;
        while ((len = in.read(readBuf)) > 0) {
            getZipOutputStream().write(readBuf, 0, len);
        }
        // Complete the entry
        getZipOutputStream().closeEntry();
        in.close();
    }

    protected InputStream getFileInputStream(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }
}
