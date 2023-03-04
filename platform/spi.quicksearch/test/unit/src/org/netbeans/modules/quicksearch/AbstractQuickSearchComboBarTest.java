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
package org.netbeans.modules.quicksearch;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author jhavlin
 */
public class AbstractQuickSearchComboBarTest {

    private AbstractQuickSearchComboBar.InvalidSearchTextDocumentFilter filter;

    @Before
    public void setUP() {
        filter = new AbstractQuickSearchComboBar.InvalidSearchTextDocumentFilter();
    }

    @Test
    public void testInvalidSearchTextDocumentFilterIsLengthInLimit() {
        assertTrue(filter.isLengthInLimit("a b c d e f g h i j", 1000));
        assertFalse(filter.isLengthInLimit(
                "a b c d e f g h i j k l m n o p q r s t u v", 1000));
        assertTrue(filter.isLengthInLimit("abcde", 5));
        assertFalse(filter.isLengthInLimit("abcde", 4));
    }

    @Test
    public void testInvalidSearchTextDocumentFilterNormalizeWhiteSpace() {
        assertEquals("a b c d", filter.normalizeWhiteSpaces("\na\r\rb  c\td "));
        assertEquals("keep single space",
                " ", filter.normalizeWhiteSpaces(" "));
    }
}
