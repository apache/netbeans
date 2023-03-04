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
package org.netbeans.modules.php.project.connections.ftp;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.api.validation.ValidationResult;

public class FtpConfigurationValidatorTest extends NbTestCase {


    private static final String LOCALHOST = "localhost";
    private static final String FOREIGNHOST = "oracle.com";
    private static final String PORT = "22";
    private static final String USER = "john";
    private static final String INITIAL_DIRECTORY = "/pub";
    private static final String TIMEOUT = "30";
    private static final String KEEP_ALIVE_INTERVAL = "10";
    private static final String EXTERNAL_IP = "10.100.0.1";
    private static final String EXTERNAL_PORT_MIN = "100";
    private static final String EXTERNAL_PORT_MAX = "200";


    public FtpConfigurationValidatorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cleanupProxy();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        cleanupProxy();
    }

    public void testValidate() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidHost() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(null, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("host", result.getErrors().get(0).getSource());
    }

    public void testInvalidPort() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, null, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("port", result.getErrors().get(0).getSource());
    }

    public void testInvalidUser() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, false, null, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("user", result.getErrors().get(0).getSource());
    }

    public void testAnonymousLogin() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, null, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidInitialDirectory() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, null, TIMEOUT, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("initialDirectory", result.getErrors().get(0).getSource());
    }

    public void testInvalidTimeout() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, null, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("timeout", result.getErrors().get(0).getSource());
    }

    public void testInvalidKeepAliveInterval() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, null, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("keepAliveInterval", result.getErrors().get(0).getSource());
    }

    public void testNoProxyPassiveMode() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testNoProxyActiveMode() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, false, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testProxyPassiveModeLocalhost() {
        setupProxy();
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testProxyPassiveModeForeignhost() {
        setupProxy();
        ValidationResult result = new FtpConfigurationValidator()
                .validate(FOREIGNHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertEquals(1, result.getWarnings().size());
        assertEquals("proxy", result.getWarnings().get(0).getSource());
    }

    public void testProxyActiveModeLocalhost() {
        setupProxy();
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, false, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testProxyActiveModeForeignhost() {
        setupProxy();
        ValidationResult result = new FtpConfigurationValidator()
                .validate(FOREIGNHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, false, EXTERNAL_IP, EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertEquals(1, result.getWarnings().size());
        assertEquals("proxy", result.getErrors().get(0).getSource());
        assertEquals("proxy", result.getWarnings().get(0).getSource());
    }

    public void testInvalidExternalIp() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, false, "invalid ip address", EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("external.ip", result.getErrors().get(0).getSource());
    }

    public void testNoExternalIp() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, false, "", EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidExternalIpPassiveMode() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true, "invalid ip address", EXTERNAL_PORT_MIN, EXTERNAL_PORT_MAX)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidPortRange01() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, false, EXTERNAL_IP, "a", "b")
                .getResult();
        assertEquals(2, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("port.min", result.getErrors().get(0).getSource());
        assertEquals("port.max", result.getErrors().get(1).getSource());
    }

    public void testInvalidPortRange02() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, false, EXTERNAL_IP, "50", "20")
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("port.min", result.getErrors().get(0).getSource());
    }

    public void testInvalidPortRange03() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, false, EXTERNAL_IP, "a", "")
                .getResult();
        assertEquals(2, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("port.min", result.getErrors().get(0).getSource());
        assertEquals("port.max", result.getErrors().get(1).getSource());
    }

    public void testEmptyPortRange() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, false, EXTERNAL_IP, "", "")
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidPortRangePassiveMode() {
        ValidationResult result = new FtpConfigurationValidator()
                .validate(LOCALHOST, PORT, true, USER, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL, true, EXTERNAL_IP, "a", "b")
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    private void setupProxy() {
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost", "http://some.proxy.org");
        System.getProperties().put("proxyPort", "8080");
    }

    private void cleanupProxy() {
        System.getProperties().put("proxySet", "false");
        System.getProperties().put("proxyHost", "");
        System.getProperties().put("proxyPort", "");
    }

}
