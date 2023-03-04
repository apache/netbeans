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
package org.netbeans.modules.csl.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

public class GsfDataObjectTest extends NbTestCase {
    static {
        FileUtil.setMIMEType("test", "text/x-test");
    }

    public GsfDataObjectTest(String name) {
        super(name);
    }

    public void testSetModifiedRemovesSaveCookie() throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject f = fs.getRoot().createData("index.test");
        DataObject dob = DataObject.find(f);
        assertEquals("The right object", GsfDataObject.class, dob.getClass());

        dob.getLookup().lookup(EditorCookie.class).openDocument().insertString(0,
                "modified", null);
        assertTrue("Should be modified.", dob.isModified());
        dob.setModified(false);
        assertFalse("Should not be modified.", dob.isModified());
        assertNull("Should not have SaveCookie.",
                dob.getLookup().lookup(SaveCookie.class));
    }
    
    public void testSetModifiedNestedChange() throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject f = fs.getRoot().createData("index.test");
        DataObject dob = DataObject.find(f);
        assertEquals("The right object", GsfDataObject.class, dob.getClass());
        dob.getLookup().lookup(EditorCookie.class).openDocument().insertString(0,
                "modified", null);
        assertTrue("Should be modified.", dob.isModified());
        dob.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String s = evt.getPropertyName();
                if (DataObject.PROP_MODIFIED.equals(s) && !dob.isModified()) {
                    dob.setModified(true);
                }
            }
        });
        dob.setModified(false);
        assertTrue("Should be still modified.", dob.isModified());
        assertNotNull("Still should have save cookie.",
                dob.getLookup().lookup(SaveCookie.class));
    }
}
