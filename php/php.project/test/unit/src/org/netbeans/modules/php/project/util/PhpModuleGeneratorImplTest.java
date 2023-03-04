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
package org.netbeans.modules.php.project.util;

import java.io.File;
import java.nio.charset.Charset;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleGenerator;

public class PhpModuleGeneratorImplTest extends PhpTestCase {

    public PhpModuleGeneratorImplTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    public void testCreateModule() throws Exception {
        PhpModule phpModule = new PhpModuleGeneratorImpl().createModule(new PhpModuleGenerator.CreateProperties()
                .setName("Project 1")
                .setSourcesDirectory(new File(getWorkDir(), "project1"))
                .setPhpVersion(PhpVersion.PHP_54)
                .setCharset(Charset.defaultCharset()));
        assertNotNull(phpModule);
    }

    public void testFailCreateModule() throws Exception {
        try {
            new PhpModuleGeneratorImpl().createModule(new PhpModuleGenerator.CreateProperties()
                    .setName("Project 1")
                    .setSourcesDirectory(new File(getWorkDir(), "project1"))
                    //.setPhpVersion(PhpVersion.PHP_54)
                    .setCharset(Charset.defaultCharset()));
            fail("should not get here");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

}
