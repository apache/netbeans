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
package org.netbeans.modules.maven.model.settings.impl;

import java.util.Collections;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.settings.Configuration;
import org.netbeans.modules.maven.model.settings.Server;
import org.netbeans.modules.maven.model.settings.SettingsModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

/**
 *
 * @author skygo
 */
public class SettingsTest extends NbTestCase {

    public SettingsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }

    @Test
    public void testSomeMethod() throws Exception {
        FileObject settings = TestFileUtils.writeFile(FileUtil.toFileObject(getWorkDir()), "settings.xml",
                "<settings xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\"\n"
                + "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "      xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0\n"
                + "                          https://maven.apache.org/xsd/settings-1.0.0.xsd\"></settings>");
        Utilities.performSettingsModelOperations(settings,
                Collections.<ModelOperation<SettingsModel>>singletonList(new ModelOperation<SettingsModel>() {
                    @Override
                    public void performOperation(SettingsModel model) {
                        Server server1 = model.getFactory().createServer();
                        Server server2 = model.getFactory().createServer();
                        server1.setPassphrase("dummypass");
                        server1.setPrivateKey("dummykey");
                        server1.setUsername("dummyname");
                        Configuration config = model.getFactory().createConfiguration();
                        config.setSimpleParameter("testparam", "testvalue");
                        server1.setConfiguration(config);
                        model.getSettings().addServer(server1);
                        model.getSettings().addServer(server2);

                    }
                }));
        assertEquals("<settings xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\"\n"
                + "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "      xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0\n"
                + "                          https://maven.apache.org/xsd/settings-1.0.0.xsd\">\n"
                + "    <servers>\n"
                + "        <server>\n"
                + "            <passphrase>dummypass</passphrase>\n"
                + "            <privateKey>dummykey</privateKey>\n"
                + "            <username>dummyname</username>\n"
                + "            <configuration>\n"
                + "                <testparam>testvalue</testparam>\n"
                + "            </configuration>\n"
                + "        </server>\n"
                + "        <server/>\n"
                + "    </servers>\n"
                + "</settings>",
                settings.asText().replace("\r\n", "\n"));

    }

}
