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

package org.openide.filesystems;

import java.io.File;
import java.util.Collections;
import org.netbeans.junit.NbTestCase;

public class VoidValueTest extends NbTestCase {

    public VoidValueTest(String name) {
        super(name);
    }

    /** Test VoidValue attribute is not copied (#132801).
     * - create MultiFileSystem
     * - set String and VoidValue attributes for file object
     * - copy file object
     * - check String attribute is copied and VoidValue attribute not
     * Simulates issue 50852.
     */
    public void testCopyVoidValue132801() throws Exception {
        System.setProperty("workdir", getWorkDirPath());
        clearWorkDir();
        FileSystem lfs = TestUtilHid.createLocalFileSystem("mfs1" + getName(), new String[]{"/fold/file1"});
        FileSystem xfs = TestUtilHid.createXMLFileSystem(getName(), new String[]{"/xmlFold/xmlFile1"});
        FileSystem mfs = new MultiFileSystem(lfs, xfs);

        FileObject file1FO = mfs.findResource("/fold/file1");
        String stringValue = "abc";
        file1FO.setAttribute("STRING-ATTR", stringValue);
        MultiFileObject.VoidValue voidValue = new MultiFileObject.VoidValue();
        file1FO.setAttribute("VOIDVALUE-ATTR", voidValue);
        assertTrue("VoidValue not set as attribute.", file1FO.getAttribute("VOIDVALUE-ATTR") instanceof MultiFileObject.VoidValue);
        FileObject file11FO = file1FO.copy(file1FO.getParent(), "file1_1", "");
        assertEquals("String attribute is not copied.", stringValue, file11FO.getAttribute("STRING-ATTR"));
        assertNull("VoidValue should not be copied.", file11FO.getAttribute("VOIDVALUE-ATTR"));
    }

    public void testNullValue() throws Exception { // #16761
        clearWorkDir();
        LocalFileSystem local = new LocalFileSystem();
        local.setRootDirectory(getWorkDir());
        FileObject baseFile = local.getRoot().createData("file");
        MultiFileSystem mfs = new MultiFileSystem(local);
        FileObject derivedFile = mfs.findResource("file");
        assertNull(baseFile.getAttribute("nonexistent"));
        assertNull(derivedFile.getAttribute("nonexistent"));
        baseFile.setAttribute("nonexistent", null);
        assertNull(baseFile.getAttribute("nonexistent"));
        assertNull(derivedFile.getAttribute("nonexistent"));
        assertFalse("No file created yet", new File(getWorkDir(), ".nbattrs").isFile());
        derivedFile.setAttribute("nonexistent", null); // ought to be a no-op
        assertNull(baseFile.getAttribute("nonexistent"));
        assertNull(derivedFile.getAttribute("nonexistent"));
        assertEquals(Collections.emptyList(), Collections.list(baseFile.getAttributes()));
        assertEquals(Collections.emptyList(), Collections.list(derivedFile.getAttributes()));
        assertFalse(new File(getWorkDir(), ".nbattrs").isFile());
    }
    
    public void testVoidValue() throws Exception { 
        clearWorkDir();
        LocalFileSystem local = new LocalFileSystem();
        local.setRootDirectory(getWorkDir());
        FileObject baseFile = local.getRoot().createData("file");
        MultiFileSystem mfs = new MultiFileSystem(local);
        FileObject derivedFile = mfs.findResource("file");
        baseFile.setAttribute("real", "whatever");
        derivedFile.setAttribute("real", null);
        assertNull("Derived attribute nullified", derivedFile.getAttribute("real"));
        assertNull("Underlaying attribute is not void", baseFile.getAttribute("real"));
    }

}
