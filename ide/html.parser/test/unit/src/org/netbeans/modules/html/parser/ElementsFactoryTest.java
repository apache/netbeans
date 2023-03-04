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
package org.netbeans.modules.html.parser;

import java.util.Collection;
import org.junit.Test;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import static org.junit.Assert.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.html.parser.ElementsFactory.CommonAttribute;
import org.netbeans.modules.html.parser.ElementsFactory.ModifiableOpenTag;

/**
 *
 * @author marekfukala
 */
public class ElementsFactoryTest extends NbTestCase {

    public ElementsFactoryTest(String name) {
        super(name);
    }
    
    //http://netbeans.org/bugzilla/show_bug.cgi?id=210628
    //Bug 210628 - java.util.ConcurrentModificationException at java.util.AbstractList$Itr.checkForComodification 
    public void testAddRemoveChildren() {
        //test if one passes the children itself to the add/removeChildren() methods
        ElementsFactory factory = new ElementsFactory("<div><a>");
        //                                             012345678
        ModifiableOpenTag div = factory.createOpenTag(0, 5, (byte)3);
        ModifiableOpenTag a = factory.createOpenTag(5, 8, (byte)1);
        
        div.addChild(a);
        
        Collection<Element> div_children = div.children();
        assertNotNull(div_children);
        assertEquals(1, div_children.size());
        
        assertSame(a, div_children.iterator().next());
        
        //pass the same collection instance to the removeChildren()
        div.removeChildren(div_children);
        assertEquals(0, div.children().size());
        
        //restore
        div.addChild(a);
        
        //
        //pass the same collection instance to the addChildren()
        div.addChildren(div.children());
        assertEquals(2, div.children().size());
        
    }
    
    public void testAttributeUnquotedValue() {
        //test that attribute.unquotedValue() wont's cause NPE on non-value attribute
        ElementsFactory factory = new ElementsFactory("<div id/>");
        //                                             0123456789
        CommonAttribute id = factory.createAttribute(5, (byte)2);
        
        assertNull(id.unquotedValue());
        
    }
    
    public void testAttributeRange() {
        ElementsFactory factory = new ElementsFactory("<div id/>");
        //                                             0123456789
        CommonAttribute id = factory.createAttribute(5, (byte)2);
        
        assertEquals(5, id.from());
        assertEquals(7, id.to());
        
        factory = new ElementsFactory("<div id=val/>");
        //                             01234567890123
        id = factory.createAttribute(5, 8, (byte)2, (short)3);
        
        assertEquals(5, id.from());
        assertEquals(11, id.to());
        
    }
}
