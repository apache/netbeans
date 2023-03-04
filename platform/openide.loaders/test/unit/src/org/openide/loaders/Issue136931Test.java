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

import org.openide.filesystems.*;
import java.io.IOException;
import org.netbeans.junit.*;

/*
 * Checks whether a during a modify operation (copy, move) some
 * other thread can get a grip on unfinished and uncostructed
 * content on filesystem.
 *
 * @author Jaroslav Tulach
 */
public class Issue136931Test extends NbTestCase {
    private DataFolder root;
    private DataFolder to;
    private DataObject a;
    private DataObject res;

    /** Creates the test */
    public Issue136931Test(String name) {
        super(name);
    }

    // For each test setup a FileSystem and DataObjects
    public void testCreateFromTemplate () throws Exception {
        clearWorkDir();
        FileUtil.setMIMEType("attr", "text/x-art");
        FileUtil.setMIMEType("block", "text/x-block");
        FileObject fo = FileUtil.createData(
            FileUtil.getConfigRoot(),
            "Loaders/text/x-art/Factories/" + BLoader.class.getName().replace('.', '-') + ".instance"
        );
        FileObject bo = FileUtil.createData(
            FileUtil.getConfigRoot(),
            "Loaders/text/x-block/Factories/" + BLoader.class.getName().replace('.', '-') + ".instance"
        );
        
        String fsstruct [] = new String [] {
            "source/A.attr", 
            "B.attr",
            "dir/",
            "fake/A.instance"
        };
        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem fs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);
        root = DataFolder.findFolder (fs.getRoot ());
        
        to = DataFolder.findFolder (fs.findResource (fsstruct[2]));
        
        fs.findResource (fsstruct[0]).setAttribute ("A", Boolean.TRUE);
        
        a = DataObject.find (fs.findResource (fsstruct[0]));

        assertEquals("Right loader for the template", BLoader.class, a.getLoader().getClass());
    
        res = a.createFromTemplate (to);

        assertTrue("Handle copy called correctly", PostDataObject.called);
    }
    
       
    /** Calls super to do some copying and then does some post processing
     */
    public static final class PostDataObject extends MultiDataObject {
        static boolean called;

        public PostDataObject(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
            super(fo, loader);
        }


        @Override
        protected DataObject handleCreateFromTemplate(DataFolder df, String name) throws IOException {
            DataObject retValue;
            retValue = super.handleCreateFromTemplate(df, name);

            FileObject artificial = FileUtil.createData(
                    retValue.getPrimaryFile().getParent(),
                    "x.block");

            DataObject obj = DataObject.find(artificial);
            assertEquals("Object really created", obj.getPrimaryFile(), artificial);
            assertEquals("Right loader", BLoader.class, obj.getLoader().getClass());

            called = true;

            return retValue;
        }

        @Override
        protected FileObject handleMove(DataFolder df) throws IOException {
            FileObject retValue;

            retValue = super.handleMove(df);
            return retValue;
        }
    }

    public static final class BLoader extends UniFileLoader {

        public BLoader() {
            super(DataObject.class.getName());
        }

        @Override
        protected void initialize() {
            super.initialize();
            getExtensions().addExtension("attr");
            getExtensions().addExtension("block");
        }

        protected String displayName() {
            return getClass().getName();
        }

        @Override
        protected org.openide.filesystems.FileObject findPrimaryFile(org.openide.filesystems.FileObject fileObject) {
            org.openide.filesystems.FileObject retValue;

            retValue = super.findPrimaryFile(fileObject);
            return retValue;
        }

        protected MultiDataObject createMultiObject(FileObject pf) throws IOException {
            return new PostDataObject(pf, this);
        }
    }
}

