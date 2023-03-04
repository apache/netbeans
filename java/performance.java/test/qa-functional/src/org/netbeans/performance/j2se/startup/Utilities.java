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
package org.netbeans.performance.j2se.startup;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utilities methods.
 *
 * @author Pavel Flaska
 */
public class Utilities {

    /**
     * Prevent creation.
     */
    private Utilities() {
    }

    /**
     * Unzip the file <code>f</code> to folder <code>destDir</code>.
     *
     * @param f         file to unzip
     * @param destDir   destination directory
     */
    public static void unzip(File f, String destDir) {
        final int BUFFER = 2048;
        try {
            BufferedOutputStream dest = null;
            FileInputStream fis = new FileInputStream(f);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    File dir = new File(destDir + '/' + entry.getName());
                    dir.mkdir();
                } else {
                    int count;
                    byte contents[] = new byte[BUFFER];
                    // write the files to the disk
                    FileOutputStream fos = new FileOutputStream(destDir + "/" + entry.getName());
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = zis.read(contents, 0, BUFFER)) != -1) {
                        dest.write(contents, 0, count);
                    }
                    dest.flush();
                    dest.close();
                }
            }
            zis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String projectOpen(String path, String tmpFile) {

    /* Temporary solution - download jEdit from internal location */

        OutputStream out = null;
        URLConnection conn = null;
        InputStream in = null;
        int BUFFER = 2048;

        try {
            URL url = new URL(path);
            System.err.println("");
            File dir = new File(System.getProperty("nbjunit.workdir") + File.separator + "tmpdir" + File.separator);
            if (!dir.exists()) dir.mkdirs();
            out = new BufferedOutputStream(new FileOutputStream(dir.getAbsolutePath() + File.separator + tmpFile));
            conn = url.openConnection();
            in = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int numRead;
            while ((numRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, numRead);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
            }
        }

        try {
            BufferedOutputStream dest = null;
            FileInputStream fis = new FileInputStream(new File(System.getProperty("nbjunit.workdir") + File.separator + "tmpdir" + File.separator + tmpFile));
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    new File(System.getProperty("nbjunit.workdir") + File.separator + ".." + File.separator + "data" + File.separator + entry.getName()).mkdirs();
                    continue;
                }
                int count;
                byte data[] = new byte[BUFFER];
                FileOutputStream fos = new FileOutputStream(System.getProperty("nbjunit.workdir") + File.separator + ".." + File.separator + "data" + File.separator + entry.getName());
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
            zis.close();
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }

        return System.getProperty("nbjunit.workdir") + File.separator + "tmpdir" + File.separator + tmpFile;
    }
}
