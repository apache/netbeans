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
package org.netbeans.modules.php.composer.options;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.api.validation.ValidationResult;

public class ComposerOptionsValidatorTest extends NbTestCase {

    public ComposerOptionsValidatorTest(String name) {
        super(name);
    }

    public void testValidVendor() {
        ValidationResult result = new ComposerOptionsValidator()
                .validateVendor("my-company")
                .getResult();
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateVendor("me-09")
                .getResult();
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
    }

    public void testInvalidVendor() {
        ValidationResult result = new ComposerOptionsValidator()
                .validateVendor("MyCompany")
                .getResult();
        assertTrue(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateVendor("my company")
                .getResult();
        assertTrue(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateVendor("my.company")
                .getResult();
        assertTrue(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateVendor("my_company")
                .getResult();
        assertTrue(result.hasWarnings());
    }

    public void testValidAuthorEmail() {
        ValidationResult result = new ComposerOptionsValidator()
                .validateAuthorEmail("john.doe@domain.net")
                .getResult();
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateAuthorEmail("john1-doe@domain.net")
                .getResult();
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
    }

    public void testInvalidAuthorEmail() {
        ValidationResult result = new ComposerOptionsValidator()
                .validateAuthorEmail("john.doe")
                .getResult();
        assertTrue(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateAuthorEmail("john.doe@")
                .getResult();
        assertTrue(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateAuthorEmail("john.doe@domain")
                .getResult();
        assertTrue(result.hasWarnings());
    }

    public void testValidAuthorName() {
        ValidationResult result = new ComposerOptionsValidator()
                .validateAuthorName("Jon Doe")
                .getResult();
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateAuthorName("John Doe von Bahnhof")
                .getResult();
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
    }

    public void testInvalidAuthorName() {
        ValidationResult result = new ComposerOptionsValidator()
                .validateAuthorName(null)
                .getResult();
        assertTrue(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateAuthorName("")
                .getResult();
        assertTrue(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateAuthorName("1")
                .getResult();
        assertTrue(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateAuthorName("junichi11")
                .getResult();
        assertTrue(result.hasWarnings());
        result = new ComposerOptionsValidator()
                .validateAuthorName("junichi 11 junichi")
                .getResult();
        assertTrue(result.hasWarnings());
    }

}
