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
package org.netbeans.nbbuild;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URL;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Manifest;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;

/**
 * Predefines commonly used utilities.
 */
abstract class TestBase extends NbTestCase {

    protected TestBase(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    // XXX look for other copy-pasted utility methods, like createNewJarFile

    protected final String readFile(java.io.File f) throws java.io.IOException {
        int s = (int) f.length();
        byte[] data = new byte[s];
        assertEquals("Read all data", s, new java.io.FileInputStream(f).read(data));

        return new String(data);
    }
    protected final String getBuildFileInClassPath(String filename) {
        // Hack to find where classes
        return getDataDir().getAbsolutePath().replace("data", "classes") + 
                File.separator + "org" + File.separator + "netbeans" + File.separator + "nbbuild" + File.separator+ filename;
    }
    protected final Manifest createManifest() {
        Manifest m = new Manifest();
        m.getMainAttributes().putValue(java.util.jar.Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
        return m;
    }

    protected final File extractString(String res) throws Exception {
        File f = File.createTempFile("res", ".xml", getWorkDir());

        try (FileOutputStream os = new FileOutputStream(f)) {
            InputStream is = new ByteArrayInputStream(res.getBytes("UTF-8"));
            for (;;) {
                int ch = is.read();
                if (ch == -1) {
                    break;
                }
                os.write(ch);
            }
        }

        return f;
    }

    protected final File extractResource(String res) throws Exception {
        File f = File.createTempFile("res", ".xml", getWorkDir());
        extractResource(f, res);
        return f;
    }

    protected final void extractResource(File f, String res) throws Exception {
        URL u = getClass().getResource(res);
        assertNotNull("Resource should be found " + res, u);


        try (FileOutputStream os = new FileOutputStream(f)) {
            InputStream is = u.openStream();
            for (;;) {
                int ch = is.read();
                if (ch == -1) {
                    break;
                }
                os.write(ch);
            }
        }
    }     
}
