/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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

