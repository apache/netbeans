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
package org.netbeans.core.startup.logging;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.Test;
import static org.junit.Assert.*;

public class NbFormatterTest {
    
    public NbFormatterTest() {
    }

    @Test public void nestedExceptionsArePrintedToSomeLevel() {
        Exception ex = new IOException("Root cause");
        for (int i = 0; i < 1000; i++) {
            ex = new IllegalStateException("Derived exception #" + i, ex);
        }
        
        StringWriter w = new StringWriter();
        NbFormatter.printStackTrace(ex, new PrintWriter(w));
        
        int notFound = w.toString().indexOf("Root cause");
        assertEquals("Not all exceptions were printed: " + w, -1, notFound);
        
        
        notFound = w.toString().indexOf("Derived exception #980");
        assertEquals("Not even #980 was printed", -1, notFound);
        
        int found = w.toString().indexOf("Derived exception #990");
        assertTrue("First 10 exceptions is printed only", found >= 0);
    }
    
}
