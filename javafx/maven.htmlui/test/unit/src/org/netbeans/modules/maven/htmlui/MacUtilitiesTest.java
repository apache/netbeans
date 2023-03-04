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
package org.netbeans.modules.maven.htmlui;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.*;

public class MacUtilitiesTest {

    public MacUtilitiesTest() {
    }

    @Test
    public void testListDevices() throws Exception {
        String output = ""
                + "Known Devices:\n" +
"mymac [2012EC47-B54A-9930-A400-C71F0FDF0EF0]\n" +
"iPhone 6s Plus (11.2) [43641F54-4845-19147CB4FDE2] (Simulator)\n" +
"iPhone 8 Plus (11.2) + Apple Watch Series 3 - 42mm (4.2) [76199641-279E-411E-8751-EA504D6B4DA3] (Simulator)\n" +
"";
        List<Device> result = new ArrayList<>();
        MacUtilities.parseDevices(result, new ByteArrayInputStream(output.getBytes(StandardCharsets.UTF_8)));
        assertEquals("Found three devices", 3, result.size());
        assertEquals("mymac", result.get(0).getName());
        assertEquals("iPhone 6s Plus (11.2)", result.get(1).getName());
        assertEquals("iPhone 8 Plus (11.2) + Apple Watch Series 3 - 42mm (4.2)", result.get(2).getName());

        assertEquals(DeviceType.DEVICE, result.get(0).getType());
        assertEquals(DeviceType.SIMULATOR, result.get(1).getType());
        assertEquals(DeviceType.SIMULATOR, result.get(2).getType());
    }

    @Test
    public void testError() throws Exception {
        FilterInputStream is = new FilterInputStream(null) {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                throw new IOException("Artificial error");
            }
        };
        List<Device> result = new ArrayList<>();
        MacUtilities.parseDevices(result, is);
        assertEquals("One element", 1, result.size());
        assertEquals("No type signals error", null, result.get(0).getType());
        assertEquals("Cannot execute `instruments -s devices`: Artificial error", result.get(0).getInfo());
    }
}
