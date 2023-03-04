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

package org.netbeans.modules.apisupport.project.ui.wizard.librarydescriptor;

import org.netbeans.junit.NbTestCase;

/**
 * Tests {@link DataModel}.
 *
 * @author Radek Matous
 */
public class DataModelTest extends NbTestCase {
    public DataModelTest(String name) {
        super(name);
    }
    
    public void testValidityOfDataModel() throws Exception {
    /* XXX rewrite to use mock data
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        WizardDescriptor wd = new WizardDescriptor() {};
        wd.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, project);
        DataModel data = new DataModel(wd);
        
        assertEquals(project, data.getProject());
        
        assertFalse(data.isValidLibraryDisplayName());
        assertFalse(data.isValidLibraryName());
        
        data.setLibraryName("");
        assertFalse(data.isValidLibraryName());

        data.setLibraryDisplayName("");
        assertFalse(data.isValidLibraryDisplayName());
        
        data.setLibraryName("mylibrary");
        assertTrue(data.isValidLibraryName());

        data.setLibraryDisplayName("mylibrary is great");
        assertTrue(data.isValidLibraryDisplayName());
        
        
        assertFalse(data.libraryAlreadyExists());
        LayerHandle h = LayerHandle.forProject(data.getProject());
        FileSystem fs = h.layer(true);
        FileObject fo = FileUtil.createData(fs.getRoot(),CreatedModifiedFilesProvider.getLibraryDescriptorEntryPath(data.getLibraryName()));
        assertNotNull(fo);
        assertTrue(data.libraryAlreadyExists());
        */
    }    
}

