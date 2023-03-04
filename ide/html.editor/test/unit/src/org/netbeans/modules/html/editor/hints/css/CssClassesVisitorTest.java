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
package org.netbeans.modules.html.editor.hints.css;

import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author marekfukala
 */
public class CssClassesVisitorTest extends NbTestCase {

    public CssClassesVisitorTest(String name) {
        super(name);
    }
    
    public void testClassesPattern() {
        Pattern p = CssClassesVisitor.CLASSES_PATTERN;
        CharSequence input = "  one   two ";
        String[] parts = p.split(input);
        assertEquals(3, parts.length);
        assertTrue(parts[0].trim().isEmpty());
        assertEquals("one", parts[1]);
        assertEquals("two", parts[2]);
    }
}
