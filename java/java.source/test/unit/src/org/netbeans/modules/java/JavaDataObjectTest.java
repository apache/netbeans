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

package org.netbeans.modules.java;

import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Jan Lahoda
 */
public class JavaDataObjectTest extends NbTestCase {
    
    public JavaDataObjectTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        MockLookup.init();
    }

    public void testJES() throws Exception {
        MockLookup.setInstances(JavaDataLoader.getLoader(JavaDataLoader.class));
        
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject f = fs.getRoot().createData("test.java");
        
        DataObject od = DataObject.find(f);
        
        assertTrue(od instanceof JavaDataObject);
        
        Object c = od.getCookie(EditorCookie.class);
        
//        assertTrue(c instanceof JavaDataObject.JavaEditorSupport);
        assertTrue(c == od.getCookie(OpenCookie.class));
        
        assertTrue(c == od.getLookup().lookup(EditorCookie.class));
        assertTrue(c == od.getLookup().lookup(OpenCookie.class));
        assertTrue(c == od.getLookup().lookup(CloneableEditorSupport.class));
    }
}
