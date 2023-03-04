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
package org.netbeans.modules.versioning.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Ondrej Vrabec
 */
public class UtilsTest extends NbTestCase {
    
    public UtilsTest (String name) {
        super(name);
    }

    @Override
    protected void setUp () throws Exception {
        super.setUp();
    }

    public void testGetLineEnding () throws Exception {
        String content = "abcd\nefgh\r\nijkl\rmnop";
        String lineEnding = null;
        try (InputStream is = new ByteArrayInputStream(content.getBytes())) {
            lineEnding = Utils.getLineEnding(is);
        }
        assertEquals("\n", lineEnding);
        
        content = "abcd\r\nefgh\nijkl\rmnop";
        lineEnding = null;
        try (InputStream is = new ByteArrayInputStream(content.getBytes())) {
            lineEnding = Utils.getLineEnding(is);
        }
        assertEquals("\r\n", lineEnding);
        
        content = "abcd\refgh\nijkl\r\nmnop";
        lineEnding = null;
        try (InputStream is = new ByteArrayInputStream(content.getBytes())) {
            lineEnding = Utils.getLineEnding(is);
        }
        assertEquals("\r", lineEnding);
        
        content = "abcdefghijklmnop";
        lineEnding = null;
        try (InputStream is = new ByteArrayInputStream(content.getBytes())) {
            lineEnding = Utils.getLineEnding(is);
        }
        assertTrue(lineEnding.isEmpty());
        
        content = "abcd\n\nefgh\r\nijkl\rmnop";
        lineEnding = null;
        try (InputStream is = new ByteArrayInputStream(content.getBytes())) {
            lineEnding = Utils.getLineEnding(is);
        }
        assertEquals("\n", lineEnding);
        
        content = "abcd\r\refgh\nijkl\rmnop";
        lineEnding = null;
        try (InputStream is = new ByteArrayInputStream(content.getBytes())) {
            lineEnding = Utils.getLineEnding(is);
        }
        assertEquals("\r", lineEnding);
        
        content = "abcd\n\refgh\rijkl\rmnop";
        lineEnding = null;
        try (InputStream is = new ByteArrayInputStream(content.getBytes())) {
            lineEnding = Utils.getLineEnding(is);
        }
        assertEquals("\n", lineEnding);
        
        content = "abcdefghijklmnop\r\n";
        lineEnding = null;
        try (InputStream is = new ByteArrayInputStream(content.getBytes())) {
            lineEnding = Utils.getLineEnding(is);
        }
        assertEquals("\r\n", lineEnding);
    }
    
}
