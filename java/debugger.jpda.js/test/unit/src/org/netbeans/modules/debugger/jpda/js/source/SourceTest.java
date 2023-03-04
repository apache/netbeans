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
package org.netbeans.modules.debugger.jpda.js.source;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class SourceTest extends NbTestCase {
   public static final String AVATAR_PREFIX = "(function (exports, require, module, __filename, __dirname) {";
   public static final String AVATAR_SUFFIX = "\n});";
  
    public SourceTest(String name) {
        super(name);
    }

    public void testAvatarJSPrefix() throws Exception {
        String js = 
            "(function() {\n"
          + "  alert('Hello');\n"
          + ")();\n";
        
        FileObject fo = FileUtil.createMemoryFileSystem().getRoot().createData("test.js");
        final OutputStream os = fo.getOutputStream();
        os.write(js.getBytes(StandardCharsets.UTF_8));
        os.close();
        
        String wrap = AVATAR_PREFIX + js + AVATAR_SUFFIX;
        
        int shift = Source.getContentLineShift(fo.toURL(), wrap);
        assertEquals("No shift at all", 0, shift);
    }
    
    public void testTwoLinesPrefix() throws Exception {
        String js = 
            "(function() {\n"
          + "  alert('Hello');\n"
          + ")();\n";
        
        FileObject fo = FileUtil.createMemoryFileSystem().getRoot().createData("test.js");
        final OutputStream os = fo.getOutputStream();
        os.write(js.getBytes(StandardCharsets.UTF_8));
        os.close();
        
        String wrap = "// Written by Martin\n// Tested by Jarda\n" + js;
        
        int shift = Source.getContentLineShift(fo.toURL(), wrap);
        assertEquals("Two lines shift", 2, shift);
        
    }
}
