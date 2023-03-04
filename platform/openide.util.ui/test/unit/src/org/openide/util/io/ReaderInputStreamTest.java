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

package org.openide.util.io;

import java.io.IOException;
import java.io.StringReader;
import org.netbeans.junit.NbTestCase;

public class ReaderInputStreamTest extends NbTestCase {

    public ReaderInputStreamTest(String name) {
        super(name);
    }

    public void testZeroLengthRead() throws Exception { // #137881
        assertEquals(0, new ReaderInputStream(new StringReader(("abc"))).read(new byte[256], 0, 0));
    }

    public void testTextDataRead() throws IOException {
        ReaderInputStream ris = new ReaderInputStream(new StringReader("0123456789"), "UTF-8");
        assertEquals("Wrong number of bytes read.", 10, ris.read(new byte[10], 0, 10));
    }

    /** Tests stream doesn't hang with invalid input data (see #153987). */
    public void testBinaryDataRead() throws IOException {
        ReaderInputStream ris = new ReaderInputStream(new StringReader(""+((char)0xD8FF)), "UTF-8");
        try {
            ris.read(new byte[10], 0, 10);
            fail("ReaderInputStream should refuse input characteres between \\uD800 and \\uDBFF (see OutputStreamWriter javadoc).");
        } catch (IOException e) {
            // OK
        }
    }
}
