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

package org.netbeans.modules.j2ee.persistence.wizard.entity;

import java.io.File;
import org.netbeans.modules.j2ee.persistence.sourcetestsupport.SourceTestSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Erno Mononen
 */
public class EntityWizardTest extends SourceTestSupport{
    
    public EntityWizardTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception{
        super.setUp();
    }
    
    private FileObject getPkgFolder() throws Exception{
        File pkg = new File(getWorkDir(), "foobar"); 
        pkg.mkdirs();
        return FileUtil.toFileObject(pkg);
    }
    
    public void testGenerateEntityFieldAccess() throws Exception {
        FileObject result = EntityWizard.generateEntity(getPkgFolder(), "MyEntity", "Long", true);
        assertFile(result);
    }
    
    public void testGenerateEntityPropertyAccess() throws Exception {
        FileObject result = EntityWizard.generateEntity(getPkgFolder(), "MyEntity", "java.lang.Long", false);
        assertFile(result);
    }
}
