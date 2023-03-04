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
package org.netbeans.modules.debugger.jpda.projects;

import java.io.File;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author martin
 */
public class FieldLNCacheTest extends NbTestCase {
    
    public FieldLNCacheTest(String name) {
        super(name);
    }
    
    public void testRelease() throws Exception {
        FieldLNCache fc = new FieldLNCache();
        File testFile = File.createTempFile("test", "tst");
        testFile.deleteOnExit();
        FileObject testFO = FileUtil.toFileObject(testFile);
        fc.putLine("testURL", "testClass", "testField", testFO, 42);
        assertEquals(new Integer(42), fc.getLine("testURL", "testClass", "testField"));
        assertNull(fc.getLine("testURL", "testClass", "testField2"));
        
        WeakReference testFORef = new WeakReference(testFO);
        testFO = null;
        assertGC("FileObject", testFORef);
        assertNull(fc.getLine("testURL", "testClass", "testField"));
        
        testFO = FileUtil.toFileObject(testFile);
        assertNull(fc.getLine("testURL", "testClass", "testField"));
        fc.putLine("testURL", "testClass", "testField", testFO, 42);
        assertEquals(new Integer(42), fc.getLine("testURL", "testClass", "testField"));
        PrintStream printStream = new PrintStream(testFO.getOutputStream());
        printStream.print("Changed.");
        printStream.close();
        assertNull(fc.getLine("testURL", "testClass", "testField")); // is reset after change
        
        fc.putLine("testURL", "testClass", "testField", testFO, 43);
        assertEquals(new Integer(43), fc.getLine("testURL", "testClass", "testField"));
        testFO.delete();
        assertNull(fc.getLine("testURL", "testClass", "testField"));
    }
    
}
