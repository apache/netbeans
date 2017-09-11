/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
