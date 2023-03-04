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

package org.netbeans.modules.weblogic.common;

import org.netbeans.modules.weblogic.common.api.WebLogicLayout;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.weblogic.common.api.Version;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author sherold
 */
public class WebLogicLayoutTest extends NbTestCase {

    public WebLogicLayoutTest(String testName) {
        super(testName);
    }

    public void testGetServerVersion() throws Exception {
        File baseFolder = getWorkDir();
        File libFolder = new File(baseFolder, "server/lib");
        libFolder.mkdirs();

        File file = new File(libFolder, "weblogic.jar");
        createJar(file, "Implementation-Version: 10.0.0.1");
        Version version = WebLogicLayout.getServerVersion(baseFolder);
        assertEquals("10.0.0.1", version.toString());
        assertEquals(10, version.getMajor().intValue());
        assertEquals(1, version.getUpdate().intValue());
        assertTrue(file.delete());
    }

    public void testIsSupportedVersion() throws Exception {
        File baseFolder = getWorkDir();
        File libFolder = new File(baseFolder, "server/lib");
        libFolder.mkdirs();
        File file = new File(libFolder, "weblogic.jar");
        createJar(file, "Implementation-Version: 10.0.0.0");
        assertTrue(WebLogicLayout.isSupportedVersion(WebLogicLayout.getServerVersion(baseFolder)));
        createJar(file, "Implementation-Version: 9.0.0.0");
        assertTrue(WebLogicLayout.isSupportedVersion(WebLogicLayout.getServerVersion(baseFolder)));
        createJar(file, "Implementation-Version: 8.0.0.0");
        assertFalse(WebLogicLayout.isSupportedVersion(WebLogicLayout.getServerVersion(baseFolder)));
        createJar(file, "Missing-Implementation-Version: 10.0.0.0");
        assertFalse(WebLogicLayout.isSupportedVersion(WebLogicLayout.getServerVersion(baseFolder)));
    }

    public void testGetWeblogicJar() throws Exception {
        File baseFolder = getWorkDir();
        File libFolder = new File(baseFolder, "server/lib");
        libFolder.mkdirs();

        File file = new File(libFolder, "weblogic.jar");

        File wlJar = WebLogicLayout.getWeblogicJar(baseFolder);
        assertNotNull(wlJar);
        assertEquals(file, wlJar);

        createJar(file, "Implementation-Version: 9.0.0.0");
        wlJar = WebLogicLayout.getWeblogicJar(baseFolder);
        assertNotNull(wlJar);
        assertEquals(file, wlJar);
    }

    private void createJar(File file, String... manifestLines) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Manifest-Version: 1.0\n");
        for (String line : manifestLines) {
            stringBuilder.append(line).append("\n");
        }

        InputStream is = new ByteArrayInputStream(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
        try {
            new JarOutputStream(new FileOutputStream(file), new Manifest(is)).close();
        } finally {
            is.close();
        }
    }

    private void copyFile(File src, File dest) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(src));
        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(dest));
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
