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
package org.netbeans.modules.masterfs.filebasedfs;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.providers.BaseAnnotationProvider;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class RecursiveValidityTest extends NbTestCase {
    private FileObject root;
    private FileObject next;
    private File rf;

    public RecursiveValidityTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        rf = new File(getWorkDir(), "wd");
        next = FileUtil.toFileObject(recreateFolders(rf));
        root = FileUtil.toFileObject(rf);

        MockServices.setServices(AP.class);
        AP.il = new IL();
    }

    public void testConsistencyWhenDeletingRoot() throws Exception {
        assertTrue("Is valid", root.isValid());
        assertTrue("Is valid leaf", next.isValid());

        clearWorkDir();
        assertFalse("Root file is gone", rf.exists());

        root.refresh();

        assertFalse("Became invalid", root.isValid());
        assertFalse("Leaf is invalid as well", next.isValid());
        
    }

    public void testConsistencyWhenDeletingRootAndRecreatingInMiddle() throws Exception {
        assertTrue("Is valid", root.isValid());
        assertTrue("Is valid leaft", next.isValid());
        
        FileObject ch1 = root.getChildren()[0];

        clearWorkDir();
        assertFalse("Root file is gone", rf.exists());

        cnt = 5;
        root.refresh();

        assertFalse("Whole tree invalidated", root.isValid());
        assertFalse("Leaf invalidated too", next.isValid());
        assertFalse("But the first child of root is certainly gone", ch1.isValid());
    }

    int cnt;
    final void assertValidity() {
        if (next.isValid()) {
            FileObject test = next;
            do {
                test = test.getParent();
                if (!next.isValid()) {
                    break;
                }
                assertTrue("Leaf is valid" + next + " and thus " + test + " has to be too", test.isValid());
            } while (test != root);
        }
        if (--cnt == 0) {
            try {
                recreateFolders(rf);
            } catch (IOException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
    }

    private File recreateFolders(File from) throws IOException {
        File r = from;
        for (int i = 0; i < 10; i++) {
            r = new File(r, "i" + i);
        }
        r.getParentFile().mkdirs();
        r.createNewFile();
        return r;
    }
    
    class IL extends ProvidedExtensions {
        @Override
        public void beforeCreate(FileObject parent, String name, boolean isFolder) {
            assertValidity();
        }

        @Override
        public void createSuccess(FileObject fo) {
            assertValidity();
        }

        @Override
        public void createFailure(FileObject parent, String name, boolean isFolder) {
            assertValidity();
        }

        @Override
        public void beforeDelete(FileObject fo) {
            assertValidity();
        }

        @Override
        public void deleteSuccess(FileObject fo) {
            assertValidity();
        }

        @Override
        public void deleteFailure(FileObject fo) {
            assertValidity();
        }

        @Override
        public void createdExternally(FileObject fo) {
            assertValidity();
        }

        @Override
        public void deletedExternally(FileObject fo) {
            assertValidity();
        }

        @Override
        public void moveSuccess(FileObject from, File to) {
            assertValidity();
        }

        @Override
        public void moveFailure(FileObject from, File to) {
            assertValidity();
        }

        @Override
        public long refreshRecursively(File dir, long lastTimeStamp, List<? super File> children) {
            assertValidity();
            return super.refreshRecursively(dir, lastTimeStamp, children);
        }
        
    }
    
    public static final class AP extends BaseAnnotationProvider {
        static InterceptionListener il;
        
        @Override
        public String annotateName(String name, Set<? extends FileObject> files) {
            return name;
        }


        @Override
        public String annotateNameHtml(String name, Set<? extends FileObject> files) {
            return name;
        }

        @Override
        public InterceptionListener getInterceptionListener() {
            return il;
        }
        
    }
}
