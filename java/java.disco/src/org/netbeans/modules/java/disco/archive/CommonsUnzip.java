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
package org.netbeans.modules.java.disco.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.openide.windows.InputOutput;

public class CommonsUnzip implements Unarchiver {

    @Override
    public void uncompress(File zip, File targetDir, InputOutput io) throws IOException {
        try ( ZipArchiveInputStream i = new ZipArchiveInputStream(new FileInputStream(zip))) {
            ZipArchiveEntry entry = null;
            while ((entry = i.getNextZipEntry()) != null) {
                if (!i.canReadEntryData(entry))
                    // log something?
                    continue;
                File f = new File(targetDir, entry.getName()).getAbsoluteFile();
                if (!isAncestor(targetDir, f)) //bad entry?

                    continue;
                if (entry.isDirectory()) {
                    io.getOut().println("   creating: " + entry.getName());

                    if (!f.isDirectory() && !f.mkdirs())
                        throw new IOException("Could not create dirs" + f);
                } else {
                    File parent = f.getParentFile();
                    if (parent == null || (!parent.isDirectory() && !parent.mkdirs()))
                        throw new IOException("Could not create dirs" + parent);
                    io.getOut().println("  inflating: " + entry.getName());
                    try ( OutputStream o = Files.newOutputStream(f.toPath())) {
                        IOUtils.copy(i, o);
                    }
                    if (entry.getUnixMode() != 0)
                        //System.out.println("Entry " + entry.getName() + " has mode " + entry.getUnixMode());
                        if ((entry.getUnixMode() & 1) != 0)
                            f.setExecutable(true);

                }
            }
        }
    }

    private static boolean isAncestor(File targetDir, File child) {
        for (; child.getParentFile() != null; child = child.getParentFile()) {
            if (child.getParentFile().equals(targetDir))
                return true;
        }
        return false;
    }

    @Override
    public boolean isSupported(File input) {
        return input.getName().toLowerCase().endsWith(".zip");
    }

}
