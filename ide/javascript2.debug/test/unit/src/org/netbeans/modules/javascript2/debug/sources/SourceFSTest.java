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

package org.netbeans.modules.javascript2.debug.sources;

import java.io.IOException;
import org.junit.Test;
import org.openide.filesystems.FileObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author Martin Entlicher
 */
public class SourceFSTest {
    
    @Test
    public void testFilesCreation() throws IOException {
        SourceFS fs = new SourceFS();
        checkFileCreation(fs, "simpleName", "Simple Content");
        checkFileCreation(fs, "simpleName.js", "Simple JS Content");
        checkFileCreation(fs, "<eval>.js", "Eval");
        checkFileCreation(fs, "a/b/c/d.js", "ABCD");
        checkFileCreation(fs, "/e/f/g/h.js", "Absolute ABCD");
        checkFileCreation(fs, "a//bb.js", "Two slashes file");
        checkFileCreation(fs, "6911ca99//Users/someone/tools/scripts/script.js#15:15<eval>@1.js", "Wild eval file");
        assertEquals("Simple JS Content", fs.findResource("simpleName.js").asText());
        assertEquals("ABCD", fs.findResource("a/b/c/d.js").asText());
        assertEquals("Wild eval file", fs.findResource("6911ca99//Users/someone/tools/scripts/script.js#15:15<eval>@1.js").asText());
    }
    
    private FileObject checkFileCreation(SourceFS fs, String name, String content) throws IOException {
        FileObject fo = fs.createFile(name, new SourceFilesCache.StringContent(content));
        assertNotNull(name, fo);
        assertEquals(content, fo.asText());
        return fo;
    }
    
}
