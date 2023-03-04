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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import org.openide.filesystems.FileObject;
import java.util.Enumeration;
import java.util.logging.Logger;
import org.netbeans.junit.*;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/** Trying to emulate issue 122295 but actually ending up with compeltely different one.
 *
 * @author Jaroslav Tulach
 */
public class ErrorThrownInFolderRecognizerTest extends NbTestCase {
    Logger LOG;
    
    public ErrorThrownInFolderRecognizerTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        LOG = Logger.getLogger("test." + getName());
        
        MockServices.setServices(Pool.class);
    }
    public void testRenameBehaviour() throws Exception {
        File dir = new File(getWorkDir(), "dir");
        dir.mkdirs();
        File old = new File(getWorkDir(), "old");
        old.mkdirs();
        for (int i = 0; i < 1000; i++) {
            File fJava = new File(old, "F" + i + ".java");
            fJava.createNewFile();
        }

        FileObject root = FileUtil.toFileObject(getWorkDir());
        assertNotNull("root found", root);
        
        final DataFolder f = DataFolder.findFolder(root.getFileObject("old"));
        final DataFolder target = DataFolder.findFolder(root.getFileObject("dir"));
        
        DataObject[] arr = f.getChildren();
        
        assertEquals("Just 999", 999, arr.length);
    }

    public static final class Pool extends DataLoaderPool {
        protected Enumeration<DataLoader> loaders () {
            return org.openide.util.Enumerations.<DataLoader>singleton(MyLoader.getLoader(MyLoader.class));
        }
    }
    
    public static final class MyLoader extends UniFileLoader {
        static DataFolder target;
        
        public MyLoader() {
            super(WithRenameObject.class.getName ());
            getExtensions().addExtension("java");
        }
        protected String displayName() {
            return "TwoPart";
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            WithRenameObject with = new WithRenameObject(this, primaryFile);
            
            if (primaryFile.getNameExt().equals("F500.java")) {
                fail("Just fail");
            }
            
            return with;
        }
    }
    public static final class WithRenameObject extends MultiDataObject {
        public WithRenameObject(MyLoader l, FileObject folder) throws DataObjectExistsException {
            super(folder, l);
        }
    }
    
}
