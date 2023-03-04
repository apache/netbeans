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
package org.netbeans.modules.php.editor.indent;

import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

public class PHPDataObjectTest extends NbTestCase {

    public PHPDataObjectTest(String name) {
        super(name);
    }

    public void testSetModifiedRemovesSaveCookie() throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject f = fs.getRoot().createData("index.php");
        DataObject dob = DataObject.find(f);

        dob.getLookup().lookup(EditorCookie.class).openDocument().insertString(0,
                "modified", null);
        assertTrue("Should be modified.", dob.isModified());
        dob.setModified(false);
        assertFalse("Should not be modified.", dob.isModified());
        assertNull("Should not have SaveCookie.",
                dob.getLookup().lookup(SaveCookie.class));
    }
}
