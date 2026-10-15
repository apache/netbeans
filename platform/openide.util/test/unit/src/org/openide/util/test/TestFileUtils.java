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

package org.openide.util.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.junit.Assert;

/**
 * Common utility methods for massaging and inspecting files from tests.
 */
public class TestFileUtils {

    private TestFileUtils() {}

    /**
     * Create a new data file with specified initial contents.
     * @param f a file to create (parents will be created automatically)
     * @param body the complete contents of the new file (in UTF-8 encoding)
     */
    public static File writeFile(File f, String body) throws IOException {
        f.getParentFile().mkdirs();
        OutputStream os = new FileOutputStream(f);
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
        pw.print(body);
        pw.flush();
        os.close();
        return f;
    }

    /**
     * Read the contents of a file as a single string.
     * @param a data file
     * @return its contents (in UTF-8 encoding)
     */
    public static String readFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int read;
        while ((read = is.read(buf)) != -1) {
            baos.write(buf, 0, read);
        }
        is.close();
        return baos.toString("UTF-8");
    }

    /**
     * Read the contents of a file as a byte array.
     * @param a data file
     * @return its raw binary contents
     */
    public static byte[] readFileBin(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int read;
        while ((read = is.read(buf)) != -1) {
            baos.write(buf, 0, read);
        }
        is.close();
        return baos.toByteArray();
    }

    /**
     * Create a new ZIP file.
     * @param jar the ZIP file to create
     * @param entries a list of entries in the form of "filename:UTF8-contents"; parent dirs created automatically
     * @return the {@code jar} parameter, for convenience
     * @throws IOException for the usual reasons
     */
    public static File writeZipFile(File jar, String... entries) throws IOException {
        jar.getParentFile().mkdirs();
        writeZipFile(new FileOutputStream(jar), entries);
        return jar;
    }

    /**
     * Create a new ZIP file.
     * @param os a stream to which the ZIP will be written
     * @param entries a list of entries in the form of "filename:UTF8-contents"; parent dirs created automatically
     * @throws IOException for the usual reasons
     */
    public static void writeZipFile(OutputStream os, String... entries) throws IOException {
        Map<String,byte[]> binary = new LinkedHashMap<String,byte[]>();
        for (String entry : entries) {
            int colon = entry.indexOf(':');
            assert colon != -1 : entry;
            binary.put(entry.substring(0, colon), entry.substring(colon + 1).getBytes(StandardCharsets.UTF_8));
        }
        writeZipFile(os, binary);
    }

    /**
     * Create a new ZIP file.
     * @param os a stream to which the ZIP will be written
     * @param entries entries as maps from filename to binary contents; parent dirs created automatically
     * @throws IOException for the usual reasons
     */
    public static void writeZipFile(OutputStream os, Map<String,byte[]> entries) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(os);
        Set<String> parents = new HashSet<String>();
        if (entries.isEmpty()) {
            entries = Collections.singletonMap("PLACEHOLDER", new byte[0]);
        }
        for (Map.Entry<String,byte[]> entry : entries.entrySet()) {
            String name = entry.getKey();
            assert name.length() > 0 && !name.endsWith("/") && !name.startsWith("/") && name.indexOf("//") == -1 : name;
            for (int i = 0; i < name.length(); i++) {
                if (name.charAt(i) == '/') {
                    String parent = name.substring(0, i + 1);
                    if (parents.add(parent)) {
                        ZipEntry ze = new ZipEntry(parent);
                        ze.setMethod(ZipEntry.STORED);
                        ze.setSize(0);
                        ze.setCrc(0);
                        ze.setTime(0);
                        zos.putNextEntry(ze);
                        zos.closeEntry();
                    }
                }
            }
            byte[] data = entry.getValue();
            ZipEntry ze = new ZipEntry(name);
            ze.setMethod(ZipEntry.STORED);
            ze.setSize(data.length);
            CRC32 crc = new CRC32();
            crc.update(data);
            ze.setCrc(crc.getValue());
            ze.setTime(0);
            zos.putNextEntry(ze);
            zos.write(data, 0, data.length);
            zos.closeEntry();
        }
        zos.finish();
        zos.close();
        os.close();
    }

    /**
     * Unpacks a ZIP file to disk.
     * All entries are unpacked, even {@code META-INF/MANIFEST.MF} if present.
     * Parent directories are created as needed (even if not mentioned in the ZIP);
     * empty ZIP directories are created too.
     * Existing files are overwritten.
     * @param zip a ZIP file
     * @param dir the base directory in which to unpack (need not yet exist)
     * @throws IOException in case of problems
     */
    public static void unpackZipFile(File zip, File dir) throws IOException {
        byte[] buf = new byte[8192];
        InputStream is = new FileInputStream(zip);
        try {
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                int slash = name.lastIndexOf('/');
                File d = new File(dir, name.substring(0, slash).replace('/', File.separatorChar));
                if (!d.isDirectory() && !d.mkdirs()) {
                    throw new IOException("could not make " + d);
                }
                if (slash != name.length() - 1) {
                    File f = new File(dir, name.replace('/', File.separatorChar));
                    OutputStream os = new FileOutputStream(f);
                    try {
                        int read;
                        while ((read = zis.read(buf)) != -1) {
                            os.write(buf, 0, read);
                        }
                    } finally {
                        os.close();
                    }
                }
            }
        } finally {
            is.close();
        }
    }

    /**
     * Make sure the timestamp on a file changes.
     * @param f a file to touch (make newer)
     * @param ref if not null, make f newer than this file; else make f newer than it was before
     */
    @SuppressWarnings("SleepWhileInLoop")
    public static void touch(File f, File ref) throws IOException, InterruptedException {
        long older = f.lastModified();
        if (ref != null) {
            older = Math.max(older, ref.lastModified());
        } else {
            older = Math.max(older, System.currentTimeMillis());
        }
        int maxPause = 9999;
        /* XXX consider this (as yet untested):
        long curr = System.currentTimeMillis();
        if (older > curr + maxPause) {
            throw new IllegalArgumentException("reference too far into the future, by " + (older - curr) + "msec");
        }
         */
        for (long pause = 1; pause < maxPause; pause *= 2) {
            Thread.sleep(pause);
            f.setLastModified(System.currentTimeMillis() + 1);  // plus 1 needed for FileObject tests (initially FO lastModified is set to currentTimeMillis)
            if (f.lastModified() > older) {
                while (f.lastModified() >= System.currentTimeMillis()) {
//                    LOG.log(Level.INFO, "Modification time is in future {0}", System.currentTimeMillis());
                    Thread.sleep(10);
                }
                return;
            }
        }
        Assert.fail("Did not manage to touch " + f);
    }

}
