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
package org.netbeans.modules.php.project.connections.common;

import org.netbeans.junit.NbTestCase;

public class RemoteValidatorTest extends NbTestCase {

    public RemoteValidatorTest(String name) {
        super(name);
    }

    public void testValidateHost() {
        assertNull(RemoteValidator.validateHost("host"));
        // errors
        assertNotNull(RemoteValidator.validateHost(null));
        assertNotNull(RemoteValidator.validateHost(""));
        assertNotNull(RemoteValidator.validateHost(" "));
        assertNotNull(RemoteValidator.validateHost("a "));
        assertNotNull(RemoteValidator.validateHost("a b"));
    }

    public void testValidateUser() {
        assertNull(RemoteValidator.validateUser("user"));
        assertNull(RemoteValidator.validateUser("a"));
        assertNull(RemoteValidator.validateUser(" b "));
        // errors
        assertNotNull(RemoteValidator.validateUser(null));
        assertNotNull(RemoteValidator.validateUser(""));
        assertNotNull(RemoteValidator.validateUser(" "));
    }

    public void testValidatePort() {
        assertNull(RemoteValidator.validatePort("10"));
        assertNull(RemoteValidator.validatePort(String.valueOf(RemoteValidator.MINIMUM_PORT)));
        assertNull(RemoteValidator.validatePort(String.valueOf(RemoteValidator.MAXIMUM_PORT)));
        // errors
        assertNotNull(RemoteValidator.validatePort(null));
        assertNotNull(RemoteValidator.validatePort(""));
        assertNotNull(RemoteValidator.validatePort(" "));
        assertNotNull(RemoteValidator.validatePort("a"));
        assertNotNull(RemoteValidator.validatePort(" a "));
        assertNotNull(RemoteValidator.validatePort(String.valueOf(RemoteValidator.MINIMUM_PORT - 1)));
        assertNotNull(RemoteValidator.validatePort(String.valueOf(RemoteValidator.MAXIMUM_PORT + 1)));
    }

    public void testValidateUploadDirectory() {
        assertNull(RemoteValidator.validateUploadDirectory("/upload/path/to/project"));
        // errors
        assertNotNull(RemoteValidator.validateUploadDirectory(null));
        assertNotNull(RemoteValidator.validateUploadDirectory(""));
        assertNotNull(RemoteValidator.validateUploadDirectory(RemoteValidator.INVALID_SEPARATOR));
        assertNotNull(RemoteValidator.validateUploadDirectory("a" + RemoteValidator.INVALID_SEPARATOR + "b"));
        assertNotNull(RemoteValidator.validateUploadDirectory("no/slash/"));
    }

    public void testValidatePositiveNumber() {
        assertNull(RemoteValidator.validatePositiveNumber("10", null, null));
        assertNull(RemoteValidator.validatePositiveNumber("0", null, null));
        // errors
        final String errorNotPositive = "errorNotPositive";
        final String errorNotNumeric = "errorNotNumeric";
        assertEquals(errorNotNumeric, RemoteValidator.validatePositiveNumber(null, errorNotPositive, errorNotNumeric));
        assertEquals(errorNotNumeric, RemoteValidator.validatePositiveNumber("", errorNotPositive, errorNotNumeric));
        assertEquals(errorNotNumeric, RemoteValidator.validatePositiveNumber(" ", errorNotPositive, errorNotNumeric));
        assertEquals(errorNotNumeric, RemoteValidator.validatePositiveNumber("a", errorNotPositive, errorNotNumeric));
        assertEquals(errorNotPositive, RemoteValidator.validatePositiveNumber("-1", errorNotPositive, errorNotNumeric));
        assertEquals(errorNotPositive, RemoteValidator.validatePositiveNumber("-100", errorNotPositive, errorNotNumeric));
    }

}
