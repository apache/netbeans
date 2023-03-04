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
package org.netbeans.modules.php.project.connections.sftp;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.api.validation.ValidationResult;

public class SftpConfigurationValidatorTest extends NbTestCase {

    private static final String HOST = "localhost";
    private static final String PORT = "22";
    private static final String USER = "john";
    private static final String INITIAL_DIRECTORY = "/pub";
    private static final String TIMEOUT = "30";
    private static final String KEEP_ALIVE_INTERVAL = "10";

    private final String identityFile;
    private final String knownHostsFile;


    public SftpConfigurationValidatorTest(String name) {
        super(name);
        File dummyTxt = new File(getDataDir(), "dummy.txt");
        assertTrue(dummyTxt.isFile());

        identityFile = dummyTxt.getAbsolutePath();
        knownHostsFile = dummyTxt.getAbsolutePath();
    }

    public void testValidate() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(HOST, PORT, USER, identityFile, knownHostsFile, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidHost() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(null, PORT, USER, identityFile, knownHostsFile, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("host", result.getErrors().get(0).getSource());
    }

    public void testInvalidPort() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(HOST, null, USER, identityFile, knownHostsFile, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("port", result.getErrors().get(0).getSource());
    }

    public void testInvalidUser() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(HOST, PORT, null, identityFile, knownHostsFile, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("user", result.getErrors().get(0).getSource());
    }

    public void testInvalidIdentityFile() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(HOST, PORT, USER, "nofile", knownHostsFile, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("identityFile", result.getErrors().get(0).getSource());
    }

    public void testNoIdentityFile() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(HOST, PORT, USER, null, knownHostsFile, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidKnownHostsFile() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(HOST, PORT, USER, identityFile, "nofile", INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("knownHostsFile", result.getErrors().get(0).getSource());
    }

    public void testNoKnownHostsFile() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(HOST, PORT, USER, identityFile, null, INITIAL_DIRECTORY, TIMEOUT, KEEP_ALIVE_INTERVAL)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testInvalidInitialDirectory() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(HOST, PORT, USER, identityFile, knownHostsFile, null, TIMEOUT, KEEP_ALIVE_INTERVAL)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("initialDirectory", result.getErrors().get(0).getSource());
    }

    public void testInvalidTimeout() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(HOST, PORT, USER, identityFile, knownHostsFile, INITIAL_DIRECTORY, null, KEEP_ALIVE_INTERVAL)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("timeout", result.getErrors().get(0).getSource());
    }

    public void testInvalidKeepAliveInterval() {
        ValidationResult result = new SftpConfigurationValidator()
                .validate(HOST, PORT, USER, identityFile, knownHostsFile, INITIAL_DIRECTORY, TIMEOUT, null)
                .getResult();
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertEquals("keepAliveInterval", result.getErrors().get(0).getSource());
    }

}
