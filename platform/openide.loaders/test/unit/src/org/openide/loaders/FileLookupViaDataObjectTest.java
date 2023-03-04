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
package org.openide.loaders;

import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileObjectTestHid;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystemTest;

public class FileLookupViaDataObjectTest extends LocalFileSystemTest {
    public FileLookupViaDataObjectTest(Test test) {
        super(test);
    }
    
    public static Test suite() {
        NbTestSuite s = new NbTestSuite();
        s.addTestSuite(FileObjectTestHid.class);
        s.addTestSuite(Hid.class);
        return new FileLookupViaDataObjectTest(s);
    }
    
    public static final class Hid extends NbTestCase {
        public Hid(String name) {
            super(name);
        }
        
        public void testDataObjectIsPresentInLookup() throws Exception {
            clearWorkDir();
            FileObject root = FileUtil.toFileObject(getWorkDir());
            assertNotNull("masterfs is enabled, so file object can be found", root);
            FileObject aTxt = root.createData("a.txt");
            
            DataObject obj = DataObject.find(aTxt);
            DataObject lkpObj = aTxt.getLookup().lookup(DataObject.class);
            assertSame("DataObject is present in file object's lookup", obj, lkpObj);
        }
    }
}
