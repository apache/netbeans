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
package org.netbeans.modules.diff.builtin;

import static org.junit.Assert.assertArrayEquals;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class Base64Test {

    private static final byte[] data = ("Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
            + "Aenean pharetra augue ex, sit amet efficitur sapien lacinia nec.\n"
            + "Etiam vehicula mi ac urna ornare aliquam id id lorem.").getBytes(StandardCharsets.UTF_8);

    private static final List<String> encodedData = Arrays.asList(
            "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdC4K",
            "QWVuZWFuIHBoYXJldHJhIGF1Z3VlIGV4LCBzaXQgYW1ldCBlZmZpY2l0dXIgc2FwaWVuIGxhY2luaWEgbmVjLgo=",
            "RXRpYW0gdmVoaWN1bGEgbWkgYWMgdXJuYSBvcm5hcmUgYWxpcXVhbSBpZCBpZCBsb3JlbS4=");

    /**
     * Test of decode method, of class Base64.
     */
    @Test
    public void testDecode() {
        byte[] result = Base64.decode(encodedData);
        assertArrayEquals(data, result);
    }

}
