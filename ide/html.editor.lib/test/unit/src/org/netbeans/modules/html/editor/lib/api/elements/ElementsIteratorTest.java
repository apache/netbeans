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
package org.netbeans.modules.html.editor.lib.api.elements;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;

/**
 *
 * @author marekfukala
 */
public class ElementsIteratorTest extends NbTestCase {

    public ElementsIteratorTest(String name) {
        super(name);
    }

    //Bug 211134 - AssertionError: element length must be positive!
    public void testIssue211134() {
        //if the comment element is longer than Short.MAX_VALUE (32k)
        //the cast to short will cause the AE
        //Such situation may happen if one starts to type comment
        //delimiter at the beginning of a longer file so the whole
        //file is lexed as comment until the end delimiter is written.
        
        StringBuilder code = new StringBuilder();
        code.append("<!--");
        for(int i = 0; i < Short.MAX_VALUE; i++) {
            code.append("X");
        }
        
        HtmlSource source = new HtmlSource(code);
        ElementsIterator itr = new ElementsIterator(source);
        
        assertTrue(itr.hasNext());
        
        Element e = itr.next();
        assertNotNull(e);
        assertEquals(ElementType.COMMENT, e.type());
        assertEquals(0, e.from());
        assertEquals(code.length(), e.to());
        
    }
    
    //Bug 211222 - AssertionError at org.netbeans.modules.html.editor.lib.api.elements.ElementsIterator.tag
    public void testIssue211222() {
        //attribute name length > 127bytes
        StringBuilder code = new StringBuilder();
        code.append("<div ");
        for(int i = 0; i < Byte.MAX_VALUE + 10; i++) {
            code.append("a");
        }
        
        HtmlSource source = new HtmlSource(code);
        ElementsIterator itr = new ElementsIterator(source);
        
        assertTrue(itr.hasNext());
        assertNotNull(itr.next());
        
    }
    
}
