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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.junit.NbTestCase;

/**
 * Provides basic functionality for ProjectImporter tests.
 *
 * @author mkrauskopf
 */
public abstract class ProjectImporterTestCase extends NbTestCase {
    
    private static final int BUFFER = 2048;
    
    /*
     * If true a lot of information about parsed project will be written to a
     * console.
     */
    private static boolean verbose;
    
    /** Creates a new instance of ProjectImporterTestCase */
    public ProjectImporterTestCase(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        /* comment this out to see verbose info */
        // setVerbose(true);
        clearWorkDir();
    }
    
    protected void setVerbose(boolean verbose) {
        ProjectImporterTestCase.verbose = verbose;
    }
    
    /*
     * XXX - doesn't similar method already exist somewhere in the API?
     * XXX - If not replace with JarFileSystem as hinted by Radek :)
     */
    protected File extractToWorkDir(String archiveFile) throws IOException {
        return extractToWorkDir(archiveFile, this);
    }

    public static File extractToWorkDir(String archiveFile, NbTestCase testCase) throws IOException {
        ZipInputStream zis = null;
        BufferedOutputStream dest = null;
        try {
            FileInputStream fis = new FileInputStream(new File(testCase.getDataDir(), archiveFile));
            zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null) {
                byte data[] = new byte[BUFFER];
                File entryFile = new File(testCase.getWorkDir(), entry.getName());
                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    FileOutputStream fos = new FileOutputStream(entryFile);
                    dest = new BufferedOutputStream(fos, BUFFER);
                    int count;
                    while ((count = zis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                }
            }
        } finally {
            if (zis != null) { zis.close(); }
            if (dest != null) { dest.close(); }
        }
        // return the directory (without ".zip" - convention used here)
        return new File(testCase.getWorkDir(), archiveFile.substring(0, archiveFile.length() - 4));
    }
    
}
