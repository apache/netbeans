/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.db.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.TestCase;

public class Base64Test extends TestCase {
    
    public Base64Test(String testName) {
        super(testName);
    }
    
    public void testEncodeBytes() throws Exception {
        byte[] orig = "GumbyAndPokey".getBytes("UTF-8");
        
        String encoded = Base64.byteArrayToBase64(orig);
        
        byte[] decoded = Base64.base64ToByteArray(encoded);
        
        assertByteArrayEquals(orig, decoded);          
    }
    
    public void testAlternate() throws Exception {
        byte[] orig = "GumbyAndPokey".getBytes("UTF-8");
        
        String encoded = Base64.byteArrayToAltBase64(orig);
        
        byte[] decoded = Base64.altBase64ToByteArray(encoded);
        
        assertByteArrayEquals(orig, decoded);                  
    }
    
    private static void assertByteArrayEquals(byte[] a, byte[] b) {
        if ( a == null ) {
            assertTrue( b == null);
        }        
        
        assertEquals(a.length, b.length);
        
        for ( int i = 0  ; i < a.length ; i++ ) {
            assertEquals(a[i], b[i] );
        }
    }
        
}
