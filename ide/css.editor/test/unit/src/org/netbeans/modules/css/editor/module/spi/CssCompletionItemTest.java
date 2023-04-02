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
package org.netbeans.modules.css.editor.module.spi;

import org.junit.Test;
import static org.junit.Assert.*;

public class CssCompletionItemTest {

    @Test
    public void testEscape() {
        assertEquals(null, CssCompletionItem.SelectorCompletionItem.escape(null));
        assertEquals("", CssCompletionItem.SelectorCompletionItem.escape(""));
        assertEquals("div", CssCompletionItem.SelectorCompletionItem.escape("div"));
        assertEquals("\\\\\\\\", CssCompletionItem.SelectorCompletionItem.escape("\\\\"));
        assertEquals("H\\000009allo", CssCompletionItem.SelectorCompletionItem.escape("H\tallo"));
        assertEquals("demo\\:with\\:colon", CssCompletionItem.SelectorCompletionItem.escape("demo:with:colon"));
        assertEquals("demo\\:with\\:\\000020colon", CssCompletionItem.SelectorCompletionItem.escape("demo:with: colon"));
        assertEquals("demo\\010437with\\010437highSurrogate", CssCompletionItem.SelectorCompletionItem.escape("demo\uD801\uDC37with\uD801\uDC37highSurrogate"));
        assertEquals("--", CssCompletionItem.SelectorCompletionItem.escape("--"));
        assertEquals("-\\5", CssCompletionItem.SelectorCompletionItem.escape("-5"));
    }

}
