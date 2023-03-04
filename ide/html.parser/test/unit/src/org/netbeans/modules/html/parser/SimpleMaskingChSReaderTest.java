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
package org.netbeans.modules.html.parser;

import org.netbeans.modules.html.editor.lib.api.foreign.SimpleMaskingChSReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author marekfukala
 */
public class SimpleMaskingChSReaderTest extends NbTestCase {

    public SimpleMaskingChSReaderTest(String name) {
        super(name);
    }

    public void testBasic() throws IOException {
        String code = "<html>blabla</html>";
        BufferedReader reader = new BufferedReader(new SimpleMaskingChSReader(code));
        String line = reader.readLine();
        assertEquals(code, line);
    }
    
    public void testMasking() throws IOException {
        String code = "<div @@@> @@@ <div>@@@@@@</div>";
        BufferedReader reader = new BufferedReader(new SimpleMaskingChSReader(code));
        String line = reader.readLine();
        assertEquals("<div    >     <div>      </div>", line);
    }
    
    public void testMaskingAtEnd() throws IOException {
        String code = "<div>@@@";
        BufferedReader reader = new BufferedReader(new SimpleMaskingChSReader(code));
        String line = reader.readLine();
        assertEquals("<div>   ", line);
        
        //won't mask
        code = "<div>@@";
        reader = new BufferedReader(new SimpleMaskingChSReader(code));
        line = reader.readLine();
        assertEquals("<div>@@", line);
        
        code = "<div>@";
        reader = new BufferedReader(new SimpleMaskingChSReader(code));
        line = reader.readLine();
        assertEquals("<div>@", line);
        
    }

    public void test10MBLongInput() throws IOException {
        StringBuilder sb = new StringBuilder();
        
        int size = 1024 * 10;
        for(int i = 0; i < size; i++) {
            sb.append("a");
        }
        Reader reader = new SimpleMaskingChSReader(sb);
        
        char[] buf = new char[size];
        int read = reader.read(buf);
        
        assertEquals(sb.length(), read);
    }
    
}
