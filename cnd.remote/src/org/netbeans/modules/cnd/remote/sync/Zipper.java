/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
