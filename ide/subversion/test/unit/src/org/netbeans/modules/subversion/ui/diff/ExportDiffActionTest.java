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
package org.netbeans.modules.subversion.ui.diff;

import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;

public class ExportDiffActionTest {

    /**
     * Test of encodeToBase64 method, of class ExportDiffAction.
     */
    @Test
    public void testEncodeToWrappedBase64() {
        final byte[] data = ("Encode this string to Base64, then wrap it in lines of"
                + " no more than 60 chars using platform line separator")
                .getBytes(StandardCharsets.UTF_8);
        final String expected = "RW5jb2RlIHRoaXMgc3RyaW5nIHRvIEJhc2U2NCwgdGhlbiB3cmFwIGl0IGlu" + System.getProperty("line.separator")
                + "IGxpbmVzIG9mIG5vIG1vcmUgdGhhbiA2MCBjaGFycyB1c2luZyBwbGF0Zm9y" + System.getProperty("line.separator")
                + "bSBsaW5lIHNlcGFyYXRvcg==";

        assertEquals(expected, ExportDiffAction.encodeToWrappedBase64(data));
    }

}
