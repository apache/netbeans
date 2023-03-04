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

package org.openide.nodes;

import java.util.*;
import junit.framework.*;
import junit.textui.TestRunner;
import org.openide.nodes.*;

import org.netbeans.junit.*;

/** Tests HTML display name contracts for filter nodes and regular nodes
 *
 * @author Tim Boudreau
 */
public class NodeHtmlTest extends NbTestCase {

    public NodeHtmlTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(FilterNodeTest.class));
    }

    public void testDefaultHtmlDisplayNameIsNull() {
        AbstractNode a = new AbstractNode (Children.LEAF);
        a.setDisplayName("Finch");
        assertEquals("AbstractNode.getDisplayName is broken", "Finch", 
            a.getDisplayName());
        
        assertNull("Unless overridden, getHtmlDisplayName should return null, " +
            "not " + a.getHtmlDisplayName(), a.getHtmlDisplayName());
        
        FilterNode fn = new FilterNode (a);
        assertNull ("Filternode should have no default html display name unless" +
            " its original overrides getHtmlDisplayName", fn.getHtmlDisplayName());
    }
    
    public void testFilteredHtmlNameIsPropagated() {
        Node n = new HtmlNode();
        n.setDisplayName ("Whipporwill");
        FilterNode fn = new FilterNode (n);
        
        assertNotNull("This test is broken", n.getHtmlDisplayName());
        
        assertNotNull("If a filter node's original supplies an html display " +
            "name, the filter node's html display name should be non-null",
            fn.getHtmlDisplayName());
        
        assertEquals("FilterNode should propagate the html name of the original",
            fn.getHtmlDisplayName(), n.getHtmlDisplayName());
    }
    
    public void testFilteredHtmlNameNotPropagatedIfGetDisplayNameOverridden() {
        Node n = new HtmlNode();
        n.setDisplayName ("Lark");
        FilterNode fn = new HtmlDisplayNameNode (n);

        assertNotNull("This test is broken", n.getHtmlDisplayName());
        
        assertNotNull("This test is broken", n.getHtmlDisplayName());
        
        assertNull ("A filternode whose getDisplayName() method is overridden" +
            " should return null from getHtmlDisplayName() even though its " +
            " original returns non-null - got " + fn.getHtmlDisplayName(), 
            fn.getHtmlDisplayName());
    }
    
    
    private static final String HTML_STRING = "<b>this is <i>html</i></b>";
    private static class HtmlNode extends AbstractNode {
        public HtmlNode() {
            super (Children.LEAF);
        }
        
        public String getHtmlDisplayName() {
            return HTML_STRING;
        }
    }
    
    private static class HtmlDisplayNameNode extends FilterNode {
        public HtmlDisplayNameNode (Node orig) {
            super (orig);
        }
        public String getDisplayName() {
            return "Not the same name!";
        }
    }
}

