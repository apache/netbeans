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
package org.netbeans.modules.php.project;

import org.netbeans.api.project.Project;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.project.util.PhpTestCase;
import org.netbeans.modules.php.project.util.TestUtils;
import org.netbeans.spi.search.SearchInfoDefinition;

public class PhpProjectTest extends PhpTestCase {

    public PhpProjectTest(String name) {
        super(name);
    }

    public void testCreateProject() throws Exception {
        Project project = TestUtils.createPhpProject(getWorkDir());
        assertTrue("Not PhpProject but: " + project.getClass().getName(), project instanceof PhpProject);
        assertNotNull("PhpModule should be found", project.getLookup().lookup(PhpModule.class));
        assertNotNull("SearchInfo should be found", project.getLookup().lookup(SearchInfoDefinition.class));
        assertNotNull("SubTreeSearchOptions should be found", project.getLookup().lookup(SearchInfoDefinition.class));
    }
}
