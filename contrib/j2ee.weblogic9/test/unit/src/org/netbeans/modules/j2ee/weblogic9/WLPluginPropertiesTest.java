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

package org.netbeans.modules.j2ee.weblogic9;

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
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author sherold
 */
public class WLPluginPropertiesTest extends NbTestCase {

    public WLPluginPropertiesTest(String testName) {
        super(testName);
    }

    public void testGetServerVersion() throws Exception {
        File baseFolder = getWorkDir();
        File libFolder = new File(baseFolder, "server/lib");
        libFolder.mkdirs();

        File file = new File(libFolder, "weblogic.jar");
        createJar(file, "Implementation-Version: 10.0.0.1");
        Version version = WLPluginProperties.getServerVersion(baseFolder);
        assertEquals("10.0.0.1", version.toString());
        assertEquals(10, version.getMajor().intValue());
        assertEquals(1, version.getUpdate().intValue());
        assertTrue(file.delete());
    }

    public void testJvmVendor() {
        assertEquals(WLPluginProperties.JvmVendor.SUN,
                WLPluginProperties.JvmVendor.fromPropertiesString("Sun"));
        assertEquals(WLPluginProperties.JvmVendor.ORACLE,
                WLPluginProperties.JvmVendor.fromPropertiesString("Oracle"));
        assertEquals(WLPluginProperties.JvmVendor.DEFAULT,
                WLPluginProperties.JvmVendor.fromPropertiesString(""));
        assertEquals(WLPluginProperties.JvmVendor.DEFAULT,
                WLPluginProperties.JvmVendor.fromPropertiesString("  "));
        assertEquals("something",
                WLPluginProperties.JvmVendor.fromPropertiesString("something").toPropertiesString());

        WLPluginProperties.JvmVendor vendor1 = WLPluginProperties.JvmVendor.fromPropertiesString("something1");
        WLPluginProperties.JvmVendor vendor2 = WLPluginProperties.JvmVendor.fromPropertiesString("something2");
        WLPluginProperties.JvmVendor vendor3 = WLPluginProperties.JvmVendor.fromPropertiesString("something1");

        assertNotSame(vendor1, vendor2);
        assertNotSame(vendor1, vendor3);
        assertNotSame(vendor2, vendor3);
    }

    public void testDomainList() throws IOException {
        File dir = getDataDir();
        File registry = new File(dir, "domain-registry.xml");
        File nodeManager = new File(dir, "nodemanager.domains");

        File work = getWorkDir();
        File wlServer = new File(work, "wlserver");
        wlServer.mkdir();

        File out = new File(work, registry.getName());

        copyFile(registry, out);

        String[] ret = WLPluginProperties.getRegisteredDomainPaths(wlServer.getAbsolutePath());
        assertEquals(1, ret.length);
        assertEquals("/home/test/software/wls12120/user_projects/domains/mydomain", ret[0]);

        assertTrue(out.delete());

        FileObject folder = FileUtil.createFolder(FileUtil.toFileObject(wlServer), "common/nodemanager");
        copyFile(nodeManager, new File(FileUtil.toFile(folder), nodeManager.getName()));

        ret = WLPluginProperties.getRegisteredDomainPaths(wlServer.getAbsolutePath());
        assertEquals(1, ret.length);
        assertEquals("/home/test/software/wls1036_dev/user_projects/domains/base_domain", ret[0]);
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
