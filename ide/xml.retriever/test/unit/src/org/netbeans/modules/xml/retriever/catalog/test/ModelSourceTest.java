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

/*
 * ModelSourceTest.java
 * JUnit based test
 *
 * Created on January 22, 2007, 6:38 PM
 */

package org.netbeans.modules.xml.retriever.catalog.test;

import java.io.File;
import java.io.IOException;
import junit.framework.*;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author girix
 */
public class ModelSourceTest extends TestCase {
    
    public ModelSourceTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public void testModelSource(){
                
        //To create a model source use this code
        
        //ModelSource ms = TestCatalogModel.getDefault().createTestModelSource(FileObject, editable);
        
        /*Sample code*/
        File file = null;
        try {
            file = File.createTempFile("modelsource", "deleteme");
            file.deleteOnExit();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //create ur own file object here
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        ModelSource ms = null;
        try {
            ms = TestCatalogModel.getDefault().createTestModelSource(fo, true);
        } catch (CatalogModelException ex) {
            ex.printStackTrace();
        }
        
        System.out.println(ms.getLookup().lookup(FileObject.class));
        
    }
    
}
