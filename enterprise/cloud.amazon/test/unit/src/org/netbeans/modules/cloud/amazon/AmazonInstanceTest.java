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
package org.netbeans.modules.cloud.amazon;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.Assert;
import org.junit.Test;

public class AmazonInstanceTest {

    public AmazonInstanceTest() {
    }

    @Test
    public void testCreateEmptyWar() throws IOException {
        AmazonInstance ai = new AmazonInstance("dommy", "dummy", "dummy", "dummy", "dummy");
        byte[] warData = ai.createEmptyWar();
        String filePath = System.getProperty("java.io.tmpdir") + "/test.zip";
        Files.write(Paths.get(filePath), warData, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        Assert.assertNotNull(warData);
        Assert.assertTrue(warData.length > 0);
        boolean welcomePageFound = false;
        boolean webXmlFound = false;
        boolean manifestFound = false;
        try(ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(warData))) {
            ZipEntry ze;
            while((ze = zip.getNextEntry()) != null) {
                if("welcome.jsp".equals(ze.getName())) {
                    welcomePageFound = streamContainsData(zip);
                } else if ("WEB-INF/web.xml".equals(ze.getName())) {
                    webXmlFound = streamContainsData(zip);
                } if("META-INF/MANIFEST.MF".equals(ze.getName())) {
                    manifestFound = streamContainsData(zip);
                }
            }
        }
        Assert.assertTrue(welcomePageFound);
        Assert.assertTrue(webXmlFound);
        Assert.assertTrue(manifestFound);
        
        Files.deleteIfExists(Paths.get(filePath));
    }

    private boolean streamContainsData(InputStream is) throws IOException {
        return is.read() >= 0;
    }
}
