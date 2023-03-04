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
package org.netbeans.modules.html.editor.lib.plain;

import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author marekfukala
 */
public class AttributeElementTest extends NbTestCase {

    public AttributeElementTest(String name) {
        super(name);
    }

    public void testAttributeRange() {
        CharSequence source = "<div id/>";
        //                     0123456789
        Attribute id = new AttributeElement(source, 5, (byte)2);
        
        assertEquals(5, id.from());
        assertEquals(7, id.to());
        
        source = "<div id=val/>)";
        //        01234567890123
        id = new AttributeElement(source, 5, 8, (byte)2, (short)3);
        
        assertEquals(5, id.from());
        assertEquals(11, id.to());
        
    }
    
}
