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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
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
     * Create or overwrite a file with specified initial contents.
     * @param f a file to create (parents will be created automatically)
     * @param body the complete contents of the new file (in UTF-8 encoding)
     */
    public static File writeFile(File f, String body) throws IOException {
        f.getParentFile().mkdirs();
        Files.write(f.toPath(), body.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        // TODO jdk11
//        Files.writeString(f.toPath(), body, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return f;
    }

    /**
     * Read the contents of a file as a single string.
     * @param file data file
     * @return its contents (in UTF-8 encoding)
     */
    public static String readFile(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        // TODO jdk11, inline candidate
//        return Files.readString(file.toPath());
    }

    /**
     * Read the contents of a file as a byte array.
     * @param file data file
     * @return its raw binary contents
     */
    public static byte[] readFileBin(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    /**
     * @see #deleteFile(java.nio.file.Path) 
     */
    public static void deleteFile(File file) throws IOException {
        deleteFile(file.toPath());
    }

    /**
     * Deletes the file or recursively deletes the directory.
     */
    public static void deleteFile(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
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
        Map<String, byte[]> binary = new LinkedHashMap<>();
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
        try (ZipOutputStream zos = new ZipOutputStream(os)) {
            Set<String> parents = new HashSet<>();
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
        }
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
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zip))) {
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
                    try (OutputStream os = new FileOutputStream(f)) {
                        int read;
                        while ((read = zis.read(buf)) != -1) {
                            os.write(buf, 0, read);
                        }
                    }
                }
            }
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
