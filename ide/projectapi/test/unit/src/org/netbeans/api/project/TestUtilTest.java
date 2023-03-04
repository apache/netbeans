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

package org.netbeans.api.project;

import java.net.URL;
import java.util.Date;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Test functionality of TestUtil.
 * @author Jesse Glick
 */
public class TestUtilTest extends NbTestCase {

    public TestUtilTest(String name) {
        super(name);
    }

    public void testCreateFileFromContent() throws Exception {
        URL content = TestUtilTest.class.getResource("TestUtilTest.class");
        assertNotNull("have TestUtilTest.class", content);
        int length = content.openConnection().getContentLength();
        assertTrue("have some length", length > 0);
        FileObject scratch = TestUtil.makeScratchDir(this);
        assertTrue("scratch is a dir", scratch.isFolder());
        assertEquals("scratch is empty", 0, scratch.getChildren().length);
        FileObject a = TestUtil.createFileFromContent(content, scratch, "d/a");
        assertTrue("a is a file", a.isData());
        assertEquals("right path", "d/a", FileUtil.getRelativePath(scratch, a));
        assertEquals("right length", length, (int)a.getSize());
        FileObject b = TestUtil.createFileFromContent(null, scratch, "d2/b");
        assertTrue("b is a file", b.isData());
        assertEquals("right path", "d2/b", FileUtil.getRelativePath(scratch, b));
        assertEquals("b is empty", 0, (int)b.getSize());
        Date created = b.lastModified();
        Thread.sleep(1500); // Unix has coarse timestamp marking
        assertEquals("got same b back", b, TestUtil.createFileFromContent(null, scratch, "d2/b"));
        Date modified = b.lastModified();
        assertTrue("touched and changed timestamp", modified.after(created));
    }
    
}
