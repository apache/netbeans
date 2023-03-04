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

package org.netbeans.modules.search;

import java.lang.reflect.Method;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author  Marian Petras
 */
public class BasicSearchFormTest extends NbTestCase {

    private Method isBackrefSyntaxUsedMethod;

    public BasicSearchFormTest() {
        super("BasicSearchFormTest");
    }

    @Override
    public void tearDown() throws Exception {
        isBackrefSyntaxUsedMethod = null;
    }

    public void testIsBackrefSyntaxUsed() throws Exception {
        assertFalse(callIsBackrefSyntaxUsed(""));

        assertFalse(callIsBackrefSyntaxUsed(" "));
        assertFalse(callIsBackrefSyntaxUsed("a"));
        assertFalse(callIsBackrefSyntaxUsed("0"));
        assertFalse(callIsBackrefSyntaxUsed("1"));
        assertFalse(callIsBackrefSyntaxUsed("5"));
        assertFalse(callIsBackrefSyntaxUsed("9"));
        assertFalse(callIsBackrefSyntaxUsed("\\"));

        assertFalse(callIsBackrefSyntaxUsed(" \\"));
        assertFalse(callIsBackrefSyntaxUsed("a\\"));
        assertFalse(callIsBackrefSyntaxUsed("0\\"));
        assertFalse(callIsBackrefSyntaxUsed("1\\"));
        assertFalse(callIsBackrefSyntaxUsed("5\\"));
        assertFalse(callIsBackrefSyntaxUsed("9\\"));
        assertFalse(callIsBackrefSyntaxUsed("\\\\"));

        assertFalse(callIsBackrefSyntaxUsed("\\ "));
        assertFalse(callIsBackrefSyntaxUsed("\\a"));
        assertTrue(callIsBackrefSyntaxUsed("\\0"));
        assertTrue(callIsBackrefSyntaxUsed("\\1"));
        assertTrue(callIsBackrefSyntaxUsed("\\5"));
        assertTrue(callIsBackrefSyntaxUsed("\\9"));
        assertFalse(callIsBackrefSyntaxUsed("\\\\"));

        assertFalse(callIsBackrefSyntaxUsed("x\\ "));
        assertFalse(callIsBackrefSyntaxUsed("x\\a"));
        assertTrue(callIsBackrefSyntaxUsed("x\\0"));
        assertTrue(callIsBackrefSyntaxUsed("x\\1"));
        assertTrue(callIsBackrefSyntaxUsed("x\\5"));
        assertTrue(callIsBackrefSyntaxUsed("x\\9"));
        assertFalse(callIsBackrefSyntaxUsed("x\\\\"));

        assertFalse(callIsBackrefSyntaxUsed("\\ x"));
        assertFalse(callIsBackrefSyntaxUsed("\\ax"));
        assertTrue(callIsBackrefSyntaxUsed("\\0x"));
        assertTrue(callIsBackrefSyntaxUsed("\\1x"));
        assertTrue(callIsBackrefSyntaxUsed("\\5x"));
        assertTrue(callIsBackrefSyntaxUsed("\\9x"));
        assertFalse(callIsBackrefSyntaxUsed("\\\\x"));

        assertFalse(callIsBackrefSyntaxUsed("alpha\\beta"));
        assertFalse(callIsBackrefSyntaxUsed("alpha\\ beta"));
        assertFalse(callIsBackrefSyntaxUsed("alpha\\ 9beta"));
        assertFalse(callIsBackrefSyntaxUsed("alpha9\\beta"));
        assertFalse(callIsBackrefSyntaxUsed("alpha\\beta9gamma"));
        assertFalse(callIsBackrefSyntaxUsed("alpha9beta\\gamma"));

        assertTrue(callIsBackrefSyntaxUsed("\\9beta"));
        assertTrue(callIsBackrefSyntaxUsed("\\9beta\\"));
        assertTrue(callIsBackrefSyntaxUsed("\\9beta\\ "));
        assertTrue(callIsBackrefSyntaxUsed("\\9beta\\\\"));
        assertTrue(callIsBackrefSyntaxUsed("\\9beta\\gamma"));
        assertTrue(callIsBackrefSyntaxUsed("alpha\\9beta\\"));
        assertTrue(callIsBackrefSyntaxUsed("alpha\\9beta\\ "));
        assertTrue(callIsBackrefSyntaxUsed("alpha\\9beta\\g"));
        assertTrue(callIsBackrefSyntaxUsed("alpha\\9beta\\\\"));
        assertTrue(callIsBackrefSyntaxUsed("alpha\\9beta\\gamma"));
    }

    private boolean callIsBackrefSyntaxUsed(String text) throws Exception {
        if (isBackrefSyntaxUsedMethod == null) {
            isBackrefSyntaxUsedMethod
                    = BasicSearchForm.class.getDeclaredMethod(
                                                        "isBackrefSyntaxUsed",
                                                        String.class);
            isBackrefSyntaxUsedMethod.setAccessible(true);
        }
        return Boolean.TRUE.equals(isBackrefSyntaxUsedMethod.invoke(null, text));
    }


}
