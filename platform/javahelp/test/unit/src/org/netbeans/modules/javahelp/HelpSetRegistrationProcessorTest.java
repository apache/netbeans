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
package org.netbeans.modules.javahelp;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;

/**
 *
 * @author jhavlin
 */
public class HelpSetRegistrationProcessorTest extends NbTestCase {

    public static final String PROP_TMP_DIR = "java.io.tmpdir";

    public HelpSetRegistrationProcessorTest(String name) {
        super(name);
    }

    /** Use safe temp directory if the default one contains problematic
     * characters (e.g. plus sign "+" on Mac).
     * 
     * #201194.
     * 
     * We have to use java.io.File and File.createTempFile instead of NetBeans
     * specific classes and utility methods, as the code is called at 
     * compile-time.
     *
     * @throws IOException 
     */
    public void testCreateTempFile() throws IOException {

        if (Utilities.isWindows()) {
            if (!new File("c:\\temp").isDirectory()) {
                return;
            }
        } else if (Utilities.isUnix()) {
            if (!new File("/tmp").isDirectory()) {
                return;
            }
        } else {
            return;
        }

        // Create a problematic directory.
        File tempRoot = File.createTempFile("temp+Root", "");
        tempRoot.delete();
        tempRoot.mkdir();

        // Set the problematic directory as default temp directory.
        String origTmpDir = System.getProperty(PROP_TMP_DIR);
        System.setProperty(PROP_TMP_DIR, tempRoot.getAbsolutePath());

        File tempFile = null;
        File safeFile = null;

        try {

            assertFalse(
                    HelpSetRegistrationProcessor.isUrlCompatible(
                    tempRoot));

            tempFile = File.createTempFile("tempFile", ".tmp");

            if (!tempRoot.getAbsolutePath().equals(
                    tempFile.getParentFile().getAbsolutePath())) {
                // Default temp directory location hasn't been affected by
                // changing system property java.io.tmpdir, skipping the test.
                return;
            }

            // File created in custom method should be created in 
            // a non-problematic directory.
            safeFile = HelpSetRegistrationProcessor.createTempFile(
                    "tempFile2", ".txt");

            System.out.println("Temp root: " + tempRoot.getAbsolutePath());
            System.out.println("Safe temp file: " + safeFile.getAbsolutePath());

            if (Utilities.isWindows() || Utilities.isMac()
                    || Utilities.isUnix()) {

                assertNotSame(tempRoot.getAbsolutePath(), tempFile.getParent());
                HelpSetRegistrationProcessor.isUrlCompatible(
                        safeFile);
            }
        } finally {
            System.setProperty(PROP_TMP_DIR, origTmpDir);
            if (tempFile != null && tempFile.isFile()) {
                tempFile.delete();
            }
            if (safeFile != null && safeFile.isFile()) {
                safeFile.delete();
            }
            tempRoot.delete();
        }
    }
}
