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

import org.netbeans.modules.html.editor.lib.api.foreign.MaskingChSReader;
import java.io.BufferedReader;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author marekfukala
 */
public class MaskingChSReaderTest extends NbTestCase {

    public MaskingChSReaderTest(String name) {
        super(name);
    }
   
     public void testMasking() throws IOException {
        String code = "hello, how are you?";
        //             0123456789012345678
        
        int[] positions = new int[]{7};
        int[] lens = new int[]{3};
        
        BufferedReader reader = new BufferedReader(new MaskingChSReader(code, positions, lens));
        String line = reader.readLine();
        assertEquals("hello,     are you?", line);
    }
   
     public void testMasking2() throws IOException {
        String code = "hello, how are you?";
        //             0123456789012345678
        
        int[] positions = new int[]{7, 15};
        int[] lens = new int[]{3, 4};
        
        BufferedReader reader = new BufferedReader(new MaskingChSReader(code, positions, lens));
        String line = reader.readLine();
        assertEquals("hello,     are     ", line);
    }
    
    
   
}
