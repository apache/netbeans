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

package org.netbeans.modules.spring.api.beans.model;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel.DocumentAccess;
import org.netbeans.modules.spring.beans.ConfigFileTestCase;
import org.netbeans.modules.spring.beans.TestUtils;

/**
 *
 * @author Andrei Badea
 */
public class SpringConfigModelTest extends ConfigFileTestCase {

    public SpringConfigModelTest(String testName) {
        super(testName);
    }

    public void testRunReadAction() throws Exception {
        SpringConfigModel model = createConfigModel();
        final boolean[] actionRun = { false };
        model.runReadAction(new Action<SpringBeans>() {
            public void run(SpringBeans springBeans) {
                actionRun[0] = true;
            }
        });
        assertTrue(actionRun[0]);
    }

    public void testExceptionPropagation() throws IOException {
        String contents = TestUtils.createXMLConfigText("");
        TestUtils.copyStringToFile(contents, configFile);
        SpringConfigModel model = createConfigModel(configFile);
        try {
            model.runReadAction(new Action<SpringBeans>() {
                public void run(SpringBeans parameter) {
                    throw new RuntimeException();
                }
            });
            fail();
        } catch (RuntimeException e) {
            // OK.
        }
        try {
            model.runDocumentAction(new Action<DocumentAccess>() {
                public void run(DocumentAccess parameter) {
                    throw new RuntimeException();
                }
            });
            fail();
        } catch (RuntimeException e) {
            // OK.
        }
    }

    public void testDocumentAction() throws IOException {
        String contents = TestUtils.createXMLConfigText("");
        TestUtils.copyStringToFile(contents, configFile);
        File configFile2 = createConfigFileName("dispatcher-servlet.xml");
        TestUtils.copyStringToFile(contents, configFile2);
        SpringConfigModel model = createConfigModel(configFile, configFile2);
        final Set<File> invokedForFiles = new HashSet<File>();
        model.runDocumentAction(new Action<DocumentAccess>() {
            public void run(DocumentAccess docAccess) {
                invokedForFiles.add(docAccess.getFile());
            }
        });
        assertEquals(2, invokedForFiles.size());
        assertTrue(invokedForFiles.contains(configFile));
        assertTrue(invokedForFiles.contains(configFile2));
    }
}
