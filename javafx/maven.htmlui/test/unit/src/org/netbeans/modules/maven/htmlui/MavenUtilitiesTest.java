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
package org.netbeans.modules.maven.htmlui;

import java.io.File;
import java.io.FileWriter;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;

public class MavenUtilitiesTest extends NbTestCase {

    public MavenUtilitiesTest(String n) {
        super(n);
    }

    @Test
    public void testFewAdditionsToSettings() throws Exception {
        clearWorkDir();
        File settings = new File(getWorkDir(), "set.xml");
        assertFalse("settings file doesn't exist yet", settings.isFile());

        MavenUtilities u = new MavenUtilities(settings);
        assertNull("No moe device", u.readMoeDevice());

        u.writeMoeDevice("3465");
        assertEquals("3465", u.readMoeDevice());

        u.writeMoeDevice("6543");
        assertEquals("6543", u.readMoeDevice());
    }

    @Test
    public void testStandardContentOfSettings() throws Exception {
        clearWorkDir();
        File settings = new File(getWorkDir(), "setts.xml");
        try (FileWriter w = new FileWriter(settings)) {
            w.write(
                "<settings xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "          xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd\">\n" +
                "    <!--proxies>\n" +
                "        <proxy>\n" +
                "            <host>my.proxy.host</host>\n" +
                "        </proxy>\n" +
                "    </proxies-->\n" +
                "\n" +
                "    <!--pluginGroups>\n" +
                "        <pluginGroup>org.codehaus.mojo</pluginGroup>\n" +
                "    </pluginGroups-->\n" +
                "</settings>"
            );
        }

        assertTrue("settings file exists", settings.isFile());

        MavenUtilities u = new MavenUtilities(settings);
        assertNull("No moe device", u.readMoeDevice());

        u.writeMoeDevice("3465");
        assertEquals("3465", u.readMoeDevice());

        u.writeMoeDevice("6543");
        assertEquals("6543", u.readMoeDevice());
    }

}
