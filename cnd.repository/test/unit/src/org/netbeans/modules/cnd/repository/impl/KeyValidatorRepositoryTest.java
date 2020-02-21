/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.repository.impl;

import java.io.File;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.trace.FileModelTest;

/**
 * Test for Key uniquity 
 *
 */
public class KeyValidatorRepositoryTest extends FileModelTest {
    
    public KeyValidatorRepositoryTest(String testName) {
        super(testName);
    }
    
    public void testModelProvider() {
        CsmModel csmModel = CsmModelAccessor.getModel();
        assertNotNull("Null model", csmModel); //NOI18N
        assertTrue("Unknown model provider " + csmModel.getClass().getName(), csmModel instanceof ModelImpl); //NOI18N
    }

    @Override
    protected void postSetUp() {
        String dataPath = convertToModelImplDataDir("repository");
        String superClassNameAsPath = getClass().getSuperclass().getName().replace('.', File.separatorChar);
        System.setProperty("cnd.modelimpl.unit.data", dataPath + File.separator + superClassNameAsPath); //NOI18N
        System.setProperty("cnd.modelimpl.unit.golden", dataPath + File.separator + "goldenfiles" + File.separator + superClassNameAsPath); //NOI18N
        super.postSetUp();
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.repository.validate.keys", Boolean.TRUE.toString()); //NOI18N
        super.setUp();
    }
}
