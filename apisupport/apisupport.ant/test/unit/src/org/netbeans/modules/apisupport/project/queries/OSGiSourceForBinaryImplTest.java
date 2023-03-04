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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

public class OSGiSourceForBinaryImplTest extends NbTestCase {

    public OSGiSourceForBinaryImplTest(String n) {
        super(n);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        MockLookup.setLayersAndInstances();
        TestBase.initializeBuildProperties(getWorkDir(), null);
    }

    public void testSuiteComponentModule() throws Exception {
        SuiteProject suite = TestBase.generateSuite(getWorkDir(), "suite");
        NbModuleProject module = TestBase.generateSuiteComponent(suite, "module");
        URL jar = FileUtil.urlForArchiveOrDir(new File(getWorkDir(), "suite/build/osgi/org.example.module-1.0.0.whatever-impl.jar"));
        assertEquals(Collections.singletonList(module.getSourceDirectory()), Arrays.asList(SourceForBinaryQuery.findSourceRoots(jar).getRoots()));
    }

    // XXX testPlatformModule - how to set it up?

}
