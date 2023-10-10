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
        
        System.out.println(dumpGETree(root));
        
        
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
        
        System.out.println(dumpGETree(root));
        
        
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
        
        System.out.println(dumpGETree(root));
        
        
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
        
        System.out.println(dumpGETree(root));
        
        
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

    public void testParsingMultiplicity() {
        GroupGrammarElement m1 = GrammarParser.parse("a{1,4}");
        assertEquals(1, m1.elements().size());
        assertEquals(1, m1.elements().get(0).getMinimumOccurances());
        assertEquals(4, m1.elements().get(0).getMaximumOccurances());

        GroupGrammarElement m2 = GrammarParser.parse("a{4}");
        assertEquals(1, m2.elements().size());
        assertEquals(4, m2.elements().get(0).getMinimumOccurances());
        assertEquals(4, m2.elements().get(0).getMaximumOccurances());

        GroupGrammarElement m3 = GrammarParser.parse("a{4,}");
        assertEquals(1, m3.elements().size());
        assertEquals(4, m3.elements().get(0).getMinimumOccurances());
        assertEquals(Integer.MAX_VALUE, m3.elements().get(0).getMaximumOccurances());

        GroupGrammarElement m4 = GrammarParser.parse("a{,4}");
        assertEquals(1, m4.elements().size());
        assertEquals(0, m4.elements().get(0).getMinimumOccurances());
        assertEquals(4, m4.elements().get(0).getMaximumOccurances());

        GroupGrammarElement m5 = GrammarParser.parse("a?");
        assertEquals(1, m5.elements().size());
        assertEquals(0, m5.elements().get(0).getMinimumOccurances());
        assertEquals(1, m5.elements().get(0).getMaximumOccurances());

        GroupGrammarElement m6 = GrammarParser.parse("a+");
        assertEquals(1, m6.elements().size());
        assertEquals(1, m6.elements().get(0).getMinimumOccurances());
        assertEquals(Integer.MAX_VALUE, m6.elements().get(0).getMaximumOccurances());

        GroupGrammarElement m7 = GrammarParser.parse("a*");
        assertEquals(1, m7.elements().size());
        assertEquals(0, m7.elements().get(0).getMinimumOccurances());
        assertEquals(Integer.MAX_VALUE, m7.elements().get(0).getMaximumOccurances());
    }
}
