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

package org.openide.actions;

import java.io.IOException;
import java.lang.ref.WeakReference;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jaroslav Tulach
 */
public class SaveAllActionGCTest extends NbTestCase {

    public SaveAllActionGCTest (String testName) {
        super (testName);
    }

    @Override
    protected boolean runInEQ () {
        return true;
    }

    public void testIssue162686() throws IOException {
        SaveAllAction a = SaveAllAction.get(SaveAllAction.class);
        assertNotNull("Action found", a);
        assertFalse("Nothing is modified", a.isEnabled());

        FileObject fo = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), "Folder/JR.txt");
        DataObject obj = DataObject.find(fo);

        assertFalse("Nothing is modified2", a.isEnabled());
        obj.setModified(true);
        assertTrue("SaveAll enabled now", a.isEnabled());
        obj.setModified(false);
        assertFalse("SaveAll disabled now", a.isEnabled());


        WeakReference<?> ref = new WeakReference<Object>(a);
        a = null;
        assertGC("The action can be GCed", ref);

        a = SaveAllAction.get(SaveAllAction.class);
        assertNotNull("But we can always create new one", a);
        assertFalse("It is disbabled initially", a.isEnabled());

        obj.setModified(true);
        assertTrue("But enables as soon an object is modified", a.isEnabled());
    }
}
