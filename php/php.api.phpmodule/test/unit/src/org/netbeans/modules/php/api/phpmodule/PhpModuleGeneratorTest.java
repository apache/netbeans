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
package org.netbeans.modules.php.api.phpmodule;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.api.validation.ValidationResult;

public class PhpModuleGeneratorTest {

    public PhpModuleGeneratorTest() {
    }

    @Test
    public void testValidCreateProperties() throws Exception {
        PhpModuleGenerator.CreateProperties properties = new PhpModuleGenerator.CreateProperties()
                .setName("Project 1")
                .setSourcesDirectory(new File("/tmp/project1"))
                .setPhpVersion(PhpVersion.PHP_54)
                .setCharset(Charset.defaultCharset());
        ValidationResult result = new PhpModuleGenerator.CreatePropertiesValidator()
                .validate(properties)
                .getResult();
        Assert.assertFalse(result.hasErrors());
        Assert.assertFalse(result.hasWarnings());
    }

    @Test
    public void testInvalidCreatePropertiesAll() throws Exception {
        PhpModuleGenerator.CreateProperties properties = new PhpModuleGenerator.CreateProperties();
        ValidationResult result = new PhpModuleGenerator.CreatePropertiesValidator()
                .validate(properties)
                .getResult();
        Assert.assertTrue(result.hasErrors());
        Assert.assertFalse(result.hasWarnings());
        assertContainsSource(result.getErrors(), "name");
        assertContainsSource(result.getErrors(), "sourcesDirectory");
        assertContainsSource(result.getErrors(), "phpVersion");
        assertContainsSource(result.getErrors(), "charset");
    }

    @Test
    public void testInvalidCreatePropertiesName() throws Exception {
        PhpModuleGenerator.CreateProperties properties = new PhpModuleGenerator.CreateProperties()
                .setSourcesDirectory(new File("/tmp/project1"))
                .setPhpVersion(PhpVersion.PHP_54)
                .setCharset(Charset.defaultCharset());
        ValidationResult result = new PhpModuleGenerator.CreatePropertiesValidator()
                .validate(properties)
                .getResult();
        Assert.assertTrue(result.hasErrors());
        Assert.assertFalse(result.hasWarnings());
        assertContainsSource(result.getErrors(), "name");
    }

    @Test
    public void testInvalidCreatePropertiesSourcesDirectory() throws Exception {
        PhpModuleGenerator.CreateProperties properties = new PhpModuleGenerator.CreateProperties()
                .setName("Project 1")
                .setPhpVersion(PhpVersion.PHP_54)
                .setCharset(Charset.defaultCharset());
        ValidationResult result = new PhpModuleGenerator.CreatePropertiesValidator()
                .validate(properties)
                .getResult();
        Assert.assertTrue(result.hasErrors());
        Assert.assertFalse(result.hasWarnings());
        assertContainsSource(result.getErrors(), "sourcesDirectory");
    }

    @Test
    public void testInvalidCreatePropertiesPhpVersion() throws Exception {
        PhpModuleGenerator.CreateProperties properties = new PhpModuleGenerator.CreateProperties()
                .setName("Project 1")
                .setSourcesDirectory(new File("/tmp/project1"))
                .setCharset(Charset.defaultCharset());
        ValidationResult result = new PhpModuleGenerator.CreatePropertiesValidator()
                .validate(properties)
                .getResult();
        Assert.assertTrue(result.hasErrors());
        Assert.assertFalse(result.hasWarnings());
        assertContainsSource(result.getErrors(), "phpVersion");
    }

    @Test
    public void testInvalidCreatePropertiesCharset() throws Exception {
        PhpModuleGenerator.CreateProperties properties = new PhpModuleGenerator.CreateProperties()
                .setName("Project 1")
                .setSourcesDirectory(new File("/tmp/project1"))
                .setPhpVersion(PhpVersion.PHP_54);
        ValidationResult result = new PhpModuleGenerator.CreatePropertiesValidator()
                .validate(properties)
                .getResult();
        Assert.assertTrue(result.hasErrors());
        Assert.assertFalse(result.hasWarnings());
        assertContainsSource(result.getErrors(), "charset");
    }

    private static void assertContainsSource(List<ValidationResult.Message> messages, Object source) {
        for (ValidationResult.Message message : messages) {
            if (source.equals(message.getSource())) {
                return;
            }
        }
        Assert.fail("Messages do not contain source: " + source);
    }

}
