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
package org.netbeans.modules.css.lib.properties;

import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.css.lib.CssTestBase;
import org.netbeans.modules.css.lib.api.properties.FixedTextGrammarElement;
import org.netbeans.modules.css.lib.api.properties.GrammarElement;
import org.netbeans.modules.css.lib.api.properties.GroupGrammarElement;
import org.netbeans.modules.css.lib.api.properties.NodeUtil;

/**
 *
 * @author marekfukala
 */
public class GrammarParserTest extends CssTestBase {

    public GrammarParserTest(String testName) {
        super(testName);
    }

    public void testQuotedValues() {
        String g = " \" a | b \" ";
        GroupGrammarElement root = GrammarParser.parse(g);
        
        List<GrammarElement> children = root.elements();
        assertNotNull(children);
        assertEquals(1, children.size());
        
        GrammarElement ge = children.get(0);
        assertTrue(ge instanceof FixedTextGrammarElement);
        
        FixedTextGrammarElement value = (FixedTextGrammarElement)ge;
        assertEquals(" a | b ", value.getValue());
    }
    
    public void testNamedElements() {
        String g = "[ a | b ]($my) && c ($yours)";
        GroupGrammarElement root = GrammarParser.parse(g);
        
        System.out.println(root.toString2(0));
        
        
        List<GrammarElement> children = root.elements();
        assertNotNull(children);
        assertEquals(2, children.size());  //the a|b group and c
        
        Iterator<GrammarElement> itr = children.iterator();
        GroupGrammarElement g1 = (GroupGrammarElement)itr.next();
        assertNotNull(g1);
        assertEquals("my", g1.getName());
        
        FixedTextGrammarElement g2 = (FixedTextGrammarElement)itr.next();
        assertNotNull(g2);
        assertEquals("c", g2.getValue());
        assertEquals("yours", g2.getName());
        
    }
    
    public void testNamedElementsWithStarMultiplicity() {
        String g = "[ x ]($my)*";
        GroupGrammarElement root = GrammarParser.parse(g);
        
        System.out.println(root.toString2(0));
        
        
        List<GrammarElement> children = root.elements();
        assertNotNull(children);
        assertEquals(1, children.size());
        
        Iterator<GrammarElement> itr = children.iterator();
        GroupGrammarElement g1 = (GroupGrammarElement)itr.next();
        assertNotNull(g1);
        assertEquals("my", g1.getName());
        
        assertEquals(0, g1.getMinimumOccurances());
        assertEquals(Integer.MAX_VALUE, g1.getMaximumOccurances());
        
    }
    
    public void testNamedElementsWithParticularMultiplicity() {
        String g = "[ x ] ($my) {3,10}";
        GroupGrammarElement root = GrammarParser.parse(g);
        
        System.out.println(root.toString2(0));
        
        
        List<GrammarElement> children = root.elements();
        assertNotNull(children);
        assertEquals(1, children.size());
        
        Iterator<GrammarElement> itr = children.iterator();
        GroupGrammarElement g1 = (GroupGrammarElement)itr.next();
        assertNotNull(g1);
        assertEquals("my", g1.getName());
        
        assertEquals(3, g1.getMinimumOccurances());
        assertEquals(10, g1.getMaximumOccurances());
        
        
    }
    
    public void testParseNoNamedElements() {
        String g = "( )";
        GroupGrammarElement root = GrammarParser.parse(g);
        
        System.out.println(root.toString2(0));
        
        
        List<GrammarElement> children = root.elements();
        assertNotNull(children);
        assertEquals(2, children.size());  
        
        Iterator<GrammarElement> itr = children.iterator();
        FixedTextGrammarElement g1 = (FixedTextGrammarElement)itr.next();
        assertNotNull(g1);
        assertEquals("(", g1.getValue());
        assertNull(g1.getName());
        
        FixedTextGrammarElement g2 = (FixedTextGrammarElement)itr.next();
        assertNotNull(g2);
        assertEquals(")", g2.getValue());
        assertNull(g2.getName());
        
    }
    
}
