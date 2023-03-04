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

package org.netbeans.modules.i18n.form;

import java.lang.reflect.Method;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author  Marian Petras
 */
public final class FormI18nIntegerEditorTest {

    private Method integerValidatingMethod = null;

    @After
    public void tearDown() {
        integerValidatingMethod = null;
    }

    @Test
    public void testMnemonicIndexValidator() throws Exception {
        assertTrue(validateMnemonicIndex("0"));
        assertTrue(validateMnemonicIndex("1"));
        assertTrue(validateMnemonicIndex("2"));
        assertTrue(validateMnemonicIndex("3"));
        assertTrue(validateMnemonicIndex("4"));
        assertTrue(validateMnemonicIndex("5"));
        assertTrue(validateMnemonicIndex("6"));
        assertTrue(validateMnemonicIndex("7"));
        assertTrue(validateMnemonicIndex("8"));
        assertTrue(validateMnemonicIndex("9"));
        assertTrue(validateMnemonicIndex("00"));
        assertTrue(validateMnemonicIndex("01"));
        assertTrue(validateMnemonicIndex("02"));
        assertTrue(validateMnemonicIndex("03"));
        assertTrue(validateMnemonicIndex("04"));
        assertTrue(validateMnemonicIndex("05"));
        assertTrue(validateMnemonicIndex("06"));
        assertTrue(validateMnemonicIndex("07"));
        assertTrue(validateMnemonicIndex("08"));
        assertTrue(validateMnemonicIndex("09"));
        assertTrue(validateMnemonicIndex("20"));
        assertTrue(validateMnemonicIndex("10"));
        assertTrue(validateMnemonicIndex("11"));
        assertTrue(validateMnemonicIndex("12"));
        assertTrue(validateMnemonicIndex("13"));
        assertTrue(validateMnemonicIndex("14"));
        assertTrue(validateMnemonicIndex("15"));
        assertTrue(validateMnemonicIndex("16"));
        assertTrue(validateMnemonicIndex("17"));
        assertTrue(validateMnemonicIndex("18"));
        assertTrue(validateMnemonicIndex("19"));
        assertTrue(validateMnemonicIndex("20"));
        assertTrue(validateMnemonicIndex("21"));
        assertTrue(validateMnemonicIndex("22"));
        assertTrue(validateMnemonicIndex("23"));
        assertTrue(validateMnemonicIndex("24"));
        assertTrue(validateMnemonicIndex("25"));
        assertTrue(validateMnemonicIndex("26"));
        assertTrue(validateMnemonicIndex("27"));
        assertTrue(validateMnemonicIndex("28"));
        assertTrue(validateMnemonicIndex("29"));
        assertTrue(validateMnemonicIndex("000"));
        assertTrue(validateMnemonicIndex("123"));
        assertTrue(validateMnemonicIndex("0000"));
        assertTrue(validateMnemonicIndex("4567"));

        assertFalse(validateMnemonicIndex(""));
        assertFalse(validateMnemonicIndex(" "));
        assertFalse(validateMnemonicIndex("-"));
        assertFalse(validateMnemonicIndex("a"));
        assertFalse(validateMnemonicIndex("a0"));
        assertFalse(validateMnemonicIndex("a5"));
        assertFalse(validateMnemonicIndex("a9"));
        assertFalse(validateMnemonicIndex("0a"));
        assertFalse(validateMnemonicIndex("5a"));
        assertFalse(validateMnemonicIndex("9a"));
        assertFalse(validateMnemonicIndex("0a0"));
        assertFalse(validateMnemonicIndex("5a5"));
        assertFalse(validateMnemonicIndex("9a9"));
        assertFalse(validateMnemonicIndex("-0"));
        assertFalse(validateMnemonicIndex("-1"));
        assertFalse(validateMnemonicIndex("-5"));
        assertFalse(validateMnemonicIndex("-9"));
        assertFalse(validateMnemonicIndex("0-"));
        assertFalse(validateMnemonicIndex("1-"));
        assertFalse(validateMnemonicIndex("5-"));
        assertFalse(validateMnemonicIndex("9-"));
        assertFalse(validateMnemonicIndex("-22"));
        assertFalse(validateMnemonicIndex("0-0"));
        assertFalse(validateMnemonicIndex("1-1"));
        assertFalse(validateMnemonicIndex("5-5"));
        assertFalse(validateMnemonicIndex("9-9"));
        assertFalse(validateMnemonicIndex("01-2"));
        assertFalse(validateMnemonicIndex("12-3"));
        assertFalse(validateMnemonicIndex("23-4"));
    }

    private boolean validateMnemonicIndex(String value) throws Exception {
        if (integerValidatingMethod == null) {
            integerValidatingMethod
                    = FormI18nIntegerEditor.class
                      .getDeclaredMethod("isNonNegativeInteger", String.class);
            integerValidatingMethod.setAccessible(true);
        }
        Object result = integerValidatingMethod.invoke(null, value);
        assertTrue(result instanceof Boolean);
        return Boolean.TRUE.equals(result);
    }

}
