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
package org.netbeans.modules.css.prep.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.modules.css.prep.CssPreprocessorType;
import static org.junit.Assert.*;

public class WarningsTest {

    public WarningsTest() {
    }

    @Before
    @After
    public void cleanup() {
        for (CssPreprocessorType type : CssPreprocessorType.values()) {
            Warnings.resetWarning(type);
        }
    }

    @Test
    public void testShowWarning() {
        assertTrue(Warnings.showWarning(CssPreprocessorType.LESS));
        assertFalse(Warnings.showWarning(CssPreprocessorType.LESS));
        assertFalse(Warnings.showWarning(CssPreprocessorType.LESS));

        assertTrue(Warnings.showWarning(CssPreprocessorType.SASS));
        assertFalse(Warnings.showWarning(CssPreprocessorType.SASS));
        assertFalse(Warnings.showWarning(CssPreprocessorType.SASS));
    }

    @Test
    public void testResetWarning() {
        assertTrue(Warnings.showWarning(CssPreprocessorType.LESS));
        assertFalse(Warnings.showWarning(CssPreprocessorType.LESS));
        assertTrue(Warnings.showWarning(CssPreprocessorType.SASS));
        assertFalse(Warnings.showWarning(CssPreprocessorType.SASS));
        Warnings.resetWarning(CssPreprocessorType.LESS);
        assertTrue(Warnings.showWarning(CssPreprocessorType.LESS));
        assertFalse(Warnings.showWarning(CssPreprocessorType.SASS));
        Warnings.resetWarning(CssPreprocessorType.SASS);
        assertFalse(Warnings.showWarning(CssPreprocessorType.LESS));
        assertTrue(Warnings.showWarning(CssPreprocessorType.SASS));
    }

}
