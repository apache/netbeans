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

package org.netbeans.modules.java.source.parsing;

import java.util.EnumSet;
import java.util.List;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Tomas Zezula
 */
public class MemoryFileManagerTest extends NbTestCase {
    
    public MemoryFileManagerTest (String name) {
        super (name);
    }
    
    public void testFileManager () throws Exception {
        final MemoryFileManager mgr = new MemoryFileManager();
        List<JavaFileObject> jfos = mgr.list(StandardLocation.SOURCE_PATH, "org.me", EnumSet.of(JavaFileObject.Kind.SOURCE), false);
        assertTrue(jfos.isEmpty());
        long mtime = System.currentTimeMillis();
        String content = "package org.me;\n class Foo{}\n";
        mgr.register(FileObjects.memoryFileObject("org.me", "Foo.java", null, mtime, content));
        jfos = mgr.list(StandardLocation.SOURCE_PATH, "org.me", EnumSet.of(JavaFileObject.Kind.SOURCE), false);
        assertEquals(1, jfos.size());        
        assertEquals("org.me.Foo", mgr.inferBinaryName(StandardLocation.SOURCE_PATH, jfos.get(0)));
        assertEquals(mtime, jfos.get(0).getLastModified());
        assertTrue(content.contentEquals(jfos.get(0).getCharContent(true)));
        mgr.register(FileObjects.memoryFileObject("org.me", "Test.java", null, mtime, content));
        jfos = mgr.list(StandardLocation.SOURCE_PATH, "org.me", EnumSet.of(JavaFileObject.Kind.SOURCE), false);
        assertEquals(2, jfos.size());
        JavaFileObject jfo = mgr.getJavaFileForInput(StandardLocation.SOURCE_PATH, "org.me.Foo", JavaFileObject.Kind.SOURCE);
        assertNotNull(jfo);
        assertEquals("org.me.Foo", mgr.inferBinaryName(StandardLocation.SOURCE_PATH, jfo));
        FileObject fo = mgr.getFileForInput(StandardLocation.SOURCE_PATH, "org.me", "Foo.java");
        assertNotNull(jfo);
        mgr.unregister("org.me.Foo");
        jfos = mgr.list(StandardLocation.SOURCE_PATH, "org.me", EnumSet.of(JavaFileObject.Kind.SOURCE), false);
        assertEquals(1, jfos.size());        
        assertEquals("org.me.Test", mgr.inferBinaryName(StandardLocation.SOURCE_PATH, jfos.get(0)));
    }
}
