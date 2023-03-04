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

package org.netbeans.modules.j2ee.deployment.plugins.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistryTestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 */
public class InstancePropertiesTest extends ServerRegistryTestBase {

    private static final String TEST_URL_PREFIX = "fooservice:";

    private static final String TEST_USERNAME = "username";

    private static final String TEST_PASSWORD = "password";

    private static final String TEST_DISPLAY_NAME = "name";

    public InstancePropertiesTest(String name) {
        super(name);
    }

    public void testCreateProperties() throws InstanceCreationException {
        String url = TEST_URL_PREFIX + "createProperties";

        Map<String, String> expected = new HashMap<String, String>();
        expected.put(InstanceProperties.URL_ATTR, url);
        expected.put(InstanceProperties.USERNAME_ATTR, TEST_USERNAME);
        expected.put(InstanceProperties.PASSWORD_ATTR, TEST_PASSWORD);
        expected.put(InstanceProperties.DISPLAY_NAME_ATTR, TEST_DISPLAY_NAME);

        InstanceListener listener = new TestInstanceListener(url, expected);
        ServerRegistry.getInstance().addInstanceListener(listener);
        try {
            InstanceProperties props = InstanceProperties.createInstanceProperties(
                    url, TEST_USERNAME, TEST_PASSWORD, TEST_DISPLAY_NAME);
            assertPropertiesEquals(expected, props);
        } finally {
            ServerRegistry.getInstance().removeInstanceListener(listener);
        }

        try {
            InstanceProperties.createInstanceProperties(
                    url, TEST_USERNAME, TEST_PASSWORD, TEST_DISPLAY_NAME);
            fail("Duplicate instance created");
        } catch (InstanceCreationException ex) {
            // expected
        }
    }

    public void testCreatePropertiesWithDefaults() throws InstanceCreationException {
        Map<String, String> defaults = new HashMap<String, String>();
        defaults.put("property1", "value1");
        defaults.put("property2", "value2");
        defaults.put("property3", "value3");
        defaults.put(InstanceProperties.URL_ATTR, "ignored");
        defaults.put(InstanceProperties.USERNAME_ATTR, "ignored");
        defaults.put(InstanceProperties.PASSWORD_ATTR, "ignored");
        defaults.put(InstanceProperties.DISPLAY_NAME_ATTR, "ignored");

        String url = TEST_URL_PREFIX + "createPropertiesExtended";

        Map<String, String> expected = new HashMap<String, String>();
        expected.put(InstanceProperties.URL_ATTR, url);
        expected.put(InstanceProperties.USERNAME_ATTR, TEST_USERNAME);
        expected.put(InstanceProperties.PASSWORD_ATTR, TEST_PASSWORD);
        expected.put(InstanceProperties.DISPLAY_NAME_ATTR, TEST_DISPLAY_NAME);
        expected.put("property1", "value1");
        expected.put("property2", "value2");
        expected.put("property3", "value3");

        InstanceListener listener = new TestInstanceListener(url, expected);
        ServerRegistry.getInstance().addInstanceListener(listener);
        try {
            InstanceProperties props = InstanceProperties.createInstanceProperties(
                    url, TEST_USERNAME, TEST_PASSWORD, TEST_DISPLAY_NAME, defaults);

            assertPropertiesEquals(expected, props);
        } finally {
            ServerRegistry.getInstance().removeInstanceListener(listener);
        }

        try {
            InstanceProperties.createInstanceProperties(
                    url, TEST_USERNAME, TEST_PASSWORD, TEST_DISPLAY_NAME);
            fail("Duplicate instance created"); // NOI18N
        } catch (InstanceCreationException ex) {
            // expected
        }
    }

    public void testCreatePropertiesWithoutFSEvents() throws InstanceCreationException, IOException {
        final String url = "unknown:CreateInstanceWithoutUI";
        final Map<String, String> expected = new HashMap<String, String>();
        expected.put(InstanceProperties.URL_ATTR, url);
        expected.put(InstanceProperties.USERNAME_ATTR, TEST_USERNAME);
        expected.put(InstanceProperties.PASSWORD_ATTR, TEST_PASSWORD);
        expected.put(InstanceProperties.DISPLAY_NAME_ATTR, "unknown");

        try {
            InstanceProperties.createInstanceProperties(
                    url, TEST_USERNAME, TEST_PASSWORD, TEST_DISPLAY_NAME);
            fail("the unknown serverplugin should be unknown at this point"); // NOI18N
        } catch (InstanceCreationException ex) {
            // expected
        }

        FileUtil.runAtomicAction(new AtomicAction() {

          public void run() throws IOException {
                    FileObject folder = FileUtil.createFolder(
                            FileUtil.getConfigFile(ServerRegistry.DIR_JSR88_PLUGINS), "Unknown");
                    FileObject fo = folder.createData("Descriptor");
                    InputStream is = new ByteArrayInputStream(
                            ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                            + "<netbeans-deployment></netbeans-deployment>").getBytes(StandardCharsets.UTF_8));
                    try {
                        OutputStream os = fo.getOutputStream();
                        try {
                            FileUtil.copy(is, os);
                        } finally {
                            os.close();
                        }
                    } finally {
                        is.close();
                    }

                    fo = folder.createData("Factory", "instance");
                    fo.setAttribute("instanceClass",
                            "org.netbeans.modules.j2ee.deployment.plugins.api.InstancepropertiesTest.MockDF");
                    fo.setAttribute("instanceOf",
                            "import javax.enterprise.deploy.spi.factories.DeploymentFactory");
                    fo.setAttribute("instanceCreate",
                            new MockDF());

                    InstanceProperties props = InstanceProperties.createInstancePropertiesWithoutUI(
                            url, TEST_USERNAME, TEST_PASSWORD, TEST_DISPLAY_NAME, expected);
            }
        });
    }

    public void testPasswordInKeyring() throws Exception {
        assertNotNull(InstanceProperties.getInstanceProperties("fooservice"));

        // the instance from test layer
        assertEquals("Adminpasswd", new String(Keyring.read("j2eeserver:fooservice")));

        // new instance
        String url = TEST_URL_PREFIX + "passwordInKeyring";
        InstanceProperties.createInstanceProperties(
                url, TEST_USERNAME, TEST_PASSWORD, TEST_DISPLAY_NAME);
        assertEquals(TEST_PASSWORD, new String(Keyring.read("j2eeserver:" + url)));

        // all password attributes are converted to keyring
        FileObject fo = FileUtil.getConfigFile("J2EE/InstalledServers");
        for (FileObject child : fo.getChildren()) {
            assertNull(child.getAttribute(InstanceProperties.PASSWORD_ATTR));
        }
    }

    public void testKeyringCleanup() throws Exception {
        String url = TEST_URL_PREFIX + "keyringCleanup";
        InstanceProperties.createInstanceProperties(
                url, TEST_USERNAME, TEST_PASSWORD, TEST_DISPLAY_NAME);
        assertEquals(TEST_PASSWORD, new String(Keyring.read("j2eeserver:" + url)));

        ServerRegistry.getInstance().removeServerInstance(url);
        assertNull(Keyring.read("j2eeserver:" + url));
    }

    private static void assertPropertiesEquals(Map<String, String> expected, InstanceProperties props) {
        for (Map.Entry<String, String> entry : expected.entrySet()) {
            assertEquals(entry.getValue(), props.getProperty(entry.getKey()));
        }
    }

    private static class MockDF extends org.netbeans.tests.j2eeserver.plugin.jsr88.TestDeploymentFactory {

        public MockDF() {
            super("unknown:");
        }
    }

    private static class TestInstanceListener implements InstanceListener {

        private final String name;

        private final Map<String, String> expected = new HashMap<String, String>();

        public TestInstanceListener(String name, Map<String, String> expected) {
            this.name = name;
            this.expected.putAll(expected);
        }

        public void instanceAdded(String serverInstanceID) {
            if (name.equals(serverInstanceID)) {
                InstanceProperties props = InstanceProperties.getInstanceProperties(serverInstanceID);
                assertNotNull(props);

                assertPropertiesEquals(expected, props);
            }
        }

        public void changeDefaultInstance(String oldServerInstanceID, String newServerInstanceID) {
        }

        public void instanceRemoved(String serverInstanceID) {
        }

    }
}
