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

package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.test.JarBuilder;

public class TestUtil {

    /**
     * Make a temporary copy of a whole folder into some new dir in the scratch
     * area. Stolen from ant/freeform.
     */
    public static File copyFolder(File d, File workdir) throws IOException {
        assert d.isDirectory();
        String name = d.getName();
        while (name.length() < 3) {
            name = name + "x";
        }
        File todir = File.createTempFile(name, null, workdir);
        todir.delete();
        doCopy(d, todir);
        return todir;
    }

    private static void doCopy(File from, File to) throws IOException {
        if (from.isDirectory()) {
            to.mkdir();
            String[] kids = from.list();
            for (int i = 0; i < kids.length; i++) {
                doCopy(new File(from, kids[i]), new File(to, kids[i]));
            }
        } else {
            assert from.isFile() : from;
            InputStream is = new FileInputStream(from);
            try {
                OutputStream os = new FileOutputStream(to);
                try {
                    FileUtil.copy(is, os);
                } finally {
                    os.close();
                }
            } finally {
                is.close();
            }
        }
    }

    /**
     * Create a fresh JAR file.
     * @param jar the file to create
     * @param contents keys are JAR entry paths, values are text contents (will be written in UTF-8)
     * @param manifest a manifest to store (or null for none)
     * @deprecated use {@link JarBuilder} instead
     */
    @Deprecated
    public static void createJar(File jar, Map<String,String> contents, Manifest manifest) throws IOException {
        if (manifest != null) {
            manifest.getMainAttributes().putValue("Manifest-Version", "1.0"); // workaround for JDK bug
        }
        jar.getParentFile().mkdirs();
        OutputStream os = new FileOutputStream(jar);
        try {
            JarOutputStream jos = manifest != null ? new JarOutputStream(os, manifest) : new JarOutputStream(os);
            for (Map.Entry<String,String> entry : contents.entrySet()) {
                String path = entry.getKey();
                byte[] data = entry.getValue().getBytes(StandardCharsets.UTF_8);
                JarEntry je = new JarEntry(path);
                je.setSize(data.length);
                CRC32 crc = new CRC32();
                crc.update(data);
                je.setCrc(crc.getValue());
                jos.putNextEntry(je);
                jos.write(data);
            }
            jos.close();
        } finally {
            os.close();
        }
    }

    /** @deprecated Use {@link TestFileUtils#writeFile} instead. */
    @Deprecated
    public static void dump(FileObject f, String contents) throws IOException {
        OutputStream os = f.getOutputStream();
        try {
            Writer w = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            w.write(contents);
            w.flush();
        } finally {
            os.close();
        }
    }

    private TestUtil() {}

}
