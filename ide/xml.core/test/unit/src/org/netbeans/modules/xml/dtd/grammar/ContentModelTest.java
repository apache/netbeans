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

package org.netbeans.modules.xml.dtd.grammar;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import junit.framework.TestCase;

/**
 * Tests DTD code completion for exact content models.
 * It contains tescases covering all known bugs.
 *
 * @author Petr Kuzel
 */
public class ContentModelTest extends TestCase {

    public ContentModelTest(String testName) {
        super(testName);
    }

    /** Test of parseContentModel method, of class org.netbeans.modules.xml.text.completion.dtd.ContentModel. */
    public void testParseContentModel() {
        System.out.println("testParseContentModel");

        // test for exceptions only
        
        try {
            ContentModel.parseContentModel("(simple)");
            ContentModel.parseContentModel("(se,qu,en,ce)");
            ContentModel.parseContentModel("(ch|oi|ce)");
            ContentModel.parseContentModel("(opt?,mand+,end)");
            ContentModel.parseContentModel("(#PCDATA|opt|mand+|end)");
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }
    
    /** Test of whatCanFollow method, of class org.netbeans.modules.xml.text.completion.dtd.ContentModel. */
    public void testWhatCanFollow() {
        System.out.println("testWhatCanFollow");

        Enumeration in, gold;

        // test Element and multiplicity group models ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        in = new InputEnumeration("");
        gold = new InputEnumeration("element");
        probe("(element)", in, gold);        
        
        in = new InputEnumeration("element");
        gold = new InputEnumeration("element");
        probe("(element)*", in, gold);        
        
        in = new InputEnumeration("element");
        gold = new InputEnumeration("element");
        probe("(element)+", in, gold);        
        
        in = new InputEnumeration("element");
        gold = new InputEnumeration("");
        probe("(element)?", in, gold);        
        
        in = new InputEnumeration("element");
        gold = new InputEnumeration("");
        probe("(element)", in, gold);        

        in = new InputEnumeration("invalid-element");
        gold = null;
        probe("(element)", in, gold);        

        //!!! offers element (not so bad)        
//        in = new InputEnumeration("invalid-element");
//        gold = null;
//        probe("(element*)", in, gold);        
        
        // test sequence ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        
        in = new InputEnumeration("se qu");
        gold = new InputEnumeration("en");
        probe("(se,qu,en,ce)", in, gold);        

        in = new InputEnumeration("se invalid-qu");
        gold = null;
        probe("(se,qu,en,ce)", in, gold);        
        
        // test choice ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        
        in = new InputEnumeration("");
        gold = new InputEnumeration("element element2");
        probe("(element|element2)", in, gold);

        in = new InputEnumeration("invalid-element");        
        gold = null;
        probe("(element|element2)", in, gold);

        in = new InputEnumeration("");
        gold = new InputEnumeration("element");
        probe("(element)?", in, gold);        
        
        in = new InputEnumeration("element");
        gold = new InputEnumeration("");
        probe("(element|element2)", in, gold);

        in = new InputEnumeration("element2");
        gold = new InputEnumeration("");
        probe("(element|element2)", in, gold);

        in = new InputEnumeration("");
        gold = new InputEnumeration("element");
        probe("(element|element)", in, gold);

        in = new InputEnumeration("");
        gold = new InputEnumeration("element element2 element3 element4");
        probe("((element|element2)|(element3|element4))", in, gold);

        in = new InputEnumeration("invalid-element");
        gold = null;
        probe("((element|element2)|(element3|element4))", in, gold);
        
        in = new InputEnumeration("element");
        gold = new InputEnumeration("");
        probe("((element|element2)|(element3|element4))", in, gold);
        
        // group of optional CM is also optional
        in = new InputEnumeration("");
        gold = new InputEnumeration("element element2 element3");
        probe("((element*|element2*),element3)", in, gold);

        // #47738 test case
        in = new InputEnumeration("a");
        gold = new InputEnumeration("a element element2");
        probe("(a*, (element*|element2*)", in, gold);

        // uniq subpath of choice implies that other options are invalid
        in = new InputEnumeration("element");
        gold = new InputEnumeration("element");
        probe("(element*|element2*)", in, gold);
        
        
        // test options in sequence ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        in = new InputEnumeration("se qu");
        gold = new InputEnumeration("en ce");
        probe("(se*,qu?,en?,ce)", in, gold);
        
        in = new InputEnumeration("se qu");
        gold = new InputEnumeration("en ce");        
        probe("(se,qu,en?,ce)", in, gold);

        in = new InputEnumeration("se");
        gold = new InputEnumeration("qu en ce");        
        probe("(se?,(qu*|en*),ce?)", in, gold);        

        // one sequence element is partialy feeded
        in = new InputEnumeration("se partial");
        gold = new InputEnumeration("partial en ce ");        
        probe("(se?,(partial*|y*),(en+|ce+))", in, gold);        
        
        // missing optional, mandatory, choice
        in = new InputEnumeration("pointer");
        gold = new InputEnumeration("post");
        probe("(option?, pointer, post)", in, gold);
        
        // test a choice of conflicting sequences

        in = new InputEnumeration("conflict");
        gold = new InputEnumeration("qu ce se");        
        probe("((conflict,qu) | (conflict,ce?,se))", in, gold);
        
        in = new InputEnumeration("");
        gold = new InputEnumeration("a b");
        probe("(a*, b*)", in, gold);
        
        in = new InputEnumeration("a");
        gold = new InputEnumeration("a b");
        probe("(a*, b*)", in, gold);
                
        in = new InputEnumeration("a a a a a a");
        gold = new InputEnumeration("a b");
        probe("(a*, b*)", in, gold);
        
        in = new InputEnumeration("a b");
        gold = new InputEnumeration("b");
        probe("(a*, b*)", in, gold);
                
        in = new InputEnumeration("a b b b");
        gold = new InputEnumeration("b");
        probe("(a*, b*)", in, gold);

        in = new InputEnumeration("book");
        gold = new InputEnumeration("book price");
        probe("(book+, price?)", in, gold);

    }

    /**
     * Perform whatCanFollow() and compare it to expected result.
     */
    private void probe(final String modelDesc, final Enumeration in, final Enumeration gold) {
        System.out.println("Probing: " + modelDesc + " for: " + in);
                
        ContentModel model = ContentModel.parseContentModel(modelDesc);
        
        Enumeration out = model.whatCanFollow(in);
        
        if (gold != null) {
            assertNotNull("\tNon-null enumeration expected!", out);
            ProbeEnum outp = new ProbeEnum(out);
            ProbeEnum goldp = new ProbeEnum(gold);
            assertEquals("Enums must be same!", goldp, outp);            
        } else {
            assertNull("Null result expected.", out);
        }

    }

    /**
     * Subclass StringTokenizer for better toString() reports.
     */
    private static class InputEnumeration extends StringTokenizer {
        
        private final String in;
        
        InputEnumeration(String in) {
            super(in);
            this.in = in;
        }
        
        public String toString() {
            return in;
        }
    }
    
    /**
     * Two enumerations are same if contains the sama value <b>set</b>.
     */
    private class ProbeEnum {
        
        private List list = new ArrayList(9);        
        private Set set = new HashSet(9);
        
        public ProbeEnum(Enumeration en) {
            while (en.hasMoreElements()) {
                Object next = en.nextElement();
                list.add(next);
                set.add(next);
            }
        }
        
        public boolean equals(Object obj) {
            if (obj instanceof ProbeEnum) return equals((ProbeEnum) obj);
            return super.equals(obj);
        }
        
        public boolean equals(ProbeEnum peer) {
            return set.containsAll(peer.set) && peer.set.containsAll(set);
        }
        
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            for (Iterator it = list.iterator(); it.hasNext(); ) {
                Object next = it.next();
                buffer.append(next.toString() + ",");
            }
            return buffer.toString();
        }
    }
}
