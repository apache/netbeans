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

package org.netbeans.modules.web.jsf.editor.index;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author marekfukala
 */
public class CompositeComponentModelTest {

    /**
     * Test of encode method, of class CompositeComponentModel.
     */
    @Test
    public void testEncodeDecode() {
        assertEncode("hello", "hello");
        assertEncode("he,llo", "he\\cllo");
        assertEncode("he,l=lo", "he\\cl\\elo");
        assertEncode("hel\\lo", "hel\\slo");

        assertDecode("hello", "hello");
        assertDecode("he\\cllo", "he,llo");
        assertDecode("he\\cl\\elo", "he,l=lo");
        assertDecode("hel\\slo", "hel\\lo");
    }

    private void assertEncode(String text, String encodedExpected) {
        String result = CompositeComponentModel.encode(text);
        assertEquals(encodedExpected, result);
    }

    private void assertDecode(String text, String decodedExpected) {
        String result = CompositeComponentModel.decode(text);
        assertEquals(decodedExpected, result);
    }


}