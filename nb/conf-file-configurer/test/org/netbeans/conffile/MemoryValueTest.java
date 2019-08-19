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
package org.netbeans.conffile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.netbeans.conffile.MemoryValue;

/**
 *
 * @author Tim Boudreau
 */
public class MemoryValueTest {

    @Test
    public void testParse() {
        MemoryValue value = MemoryValue.parse("-J-Xmx1024M");

        assertNotNull(value);
        assertEquals(MemoryValue.GIGABYTE, value.asBytes());
        assertEquals("1024M", value.toJvmString());
        assertEquals("1 Gb", value.toString());
//        assertEquals(MemoryValue.Kind.MAXIMUM_HEAP_SIZE, value.kind());

        MemoryValue equivalent = new MemoryValue("foo", MemoryValue.GIGABYTE, "-J-Xmx1G");
        assertEquals(value, equivalent);
        assertEquals(MemoryValue.GIGABYTE, equivalent.asBytes());
        assertEquals("foo", equivalent.toString());
        assertEquals("1G", equivalent.toJvmString());

        long mb4 = MemoryValue.MEGABYTE * 4;
        MemoryValue rawBytes = new MemoryValue(mb4 + "b", mb4, "-J-Xms" + mb4);

        MemoryValue parsedRawBytes = MemoryValue.parse(rawBytes.toJvmString());

        assertEquals(rawBytes, parsedRawBytes);
//        assertEquals(MemoryValue.Kind.INITIAL_HEAP_SIZE, rawBytes.kind());
//        assertEquals(MemoryValue.Kind.INITIAL_HEAP_SIZE, parsedRawBytes.kind());

        MemoryValue fractional = MemoryValue.parse("-J-Xmx1536K");
        assertEquals("1.5 Mb", fractional.toString());

        MemoryValue moreFractional = MemoryValue.parse("-J-Xmx2203K");
        assertEquals("2.2 Mb", moreFractional.toString());

        MemoryValue primeFractional = MemoryValue.parse("-Xmx982451653");
        assertEquals("936.9 Mb", primeFractional.toString());
    }
}
