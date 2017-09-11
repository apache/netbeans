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
package org.netbeans.api.java.source.gen;

import com.sun.org.apache.bcel.internal.generic.ARRAYLENGTH;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.save.ListMatcher;
import static org.netbeans.modules.java.source.save.ListMatcher.*;

/**
 * Test adding/removing separators.
 * 
 * @author Pavel Flaska
 */
public class SeparatorTest extends NbTestCase {

    /** Creates a new instance of SeparatorTest */
    public SeparatorTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(SeparatorTest.class);
        return suite;
    }

    public void testAddToEmpty() {
        String[] oldL = { };
        String[] newL = { "A", "B", "C" };
        String golden = 
                "head {insert} A next \n" +
                "{insert} B next \n" +
                "{insert} C tail \n";
        ListMatcher matcher = ListMatcher.instance(oldL, newL);
        if (matcher.match()) {
            Separator s = new Separator(matcher.getTransformedResult(), JavaTokenId.COMMA);
            s.compute();
            System.err.println("testAddToEmpty: ");
            System.err.println(p(oldL, newL));
            System.err.println(s.print());
            assertEquals(golden, s.print());
        } else {
            assertTrue("No match!", false);
        }
    }
    
    public void testModify1() {
        String[] oldL = { "A", "X", "C" };
        String[] newL = { "A", "B", "C" };
        String golden = "";
        ListMatcher matcher = ListMatcher.instance(oldL, newL);
        if (matcher.match()) {
            Separator s = new Separator(matcher.getTransformedResult(), JavaTokenId.COMMA);
            s.compute();
            System.err.println("testModify1: ");
            System.err.println(p(oldL, newL));
            System.err.println(s.print());
            assertEquals(golden, s.print());
        } else {
            assertTrue("No match!", false);
        }
    }

    public void testRemoveAll() {
        String[] oldL= { "A", "B", "C" };
        String[] newL = { };
        String golden = 
                "head {delete} A next \n" +
                "{delete} B next \n" +
                "{delete} C tail \n";
        ListMatcher matcher = ListMatcher.instance(oldL, newL);
        if (matcher.match()) {
            Separator s = new Separator(matcher.getTransformedResult(), JavaTokenId.COMMA);
            s.compute();
            System.err.println("testRemoveAll: ");
            System.err.println(p(oldL, newL));
            System.err.println(s.print());
            assertEquals(golden, s.print());
        } else {
            assertTrue("No match!", false);
        }
    }
    
    public void testAddToIndex0() {
        String[] oldL= { "B" };
        String[] newL = { "A", "B" };
        String golden = 
                "{insert} A next \n";
        ListMatcher matcher = ListMatcher.instance(oldL, newL);
        if (matcher.match()) {
            Separator s = new Separator(matcher.getTransformedResult(), JavaTokenId.COMMA);
            s.compute();
            System.err.println("testAddToIndex0:");
            System.err.println(p(oldL, newL));
            System.err.println(s.print());
            assertEquals(golden, s.print());
        } else {
            assertTrue("No match!", false);
        }
    }
    
    public void testRemoveAtIndex0() {
        String[] oldL = { "A", "B" };
        String[] newL = { "B" };
        String golden = 
                "{delete} A next \n";
        ListMatcher matcher = ListMatcher.instance(oldL, newL);
        if (matcher.match()) {
            Separator s = new Separator(matcher.getTransformedResult(), JavaTokenId.COMMA);
            s.compute();
            System.err.println("testRemoveAtIndex0:");
            System.err.println(p(oldL, newL));
            System.err.println(s.print());
            assertEquals(golden, s.print());
        } else {
            assertTrue("No match!", false);
        }
    }
    
    public void testRemoveLastTwo() {
        String[] oldL = { "A", "B", "C" };
        String[] newL = { "A" };
        String golden = 
                "previous {delete} B next \n" +
                "{delete} C \n";
        ListMatcher matcher = ListMatcher.instance(oldL, newL);
        if (matcher.match()) {
            Separator s = new Separator(matcher.getTransformedResult(), JavaTokenId.COMMA);
            s.compute();
            System.err.println("testRemoveLastTwo:");
            System.err.println(p(oldL, newL));
            System.err.println(s.print());
            assertEquals(golden, s.print());
        } else {
            assertTrue("No match!", false);
        }
    }
    
    public void testRemoveLastThree() {
        String[] oldL = { "A", "B", "C", "D" };
        String[] newL = { "A" };
        String golden = 
                "previous {delete} B next \n" +
                "{delete} C next \n" +
                "{delete} D \n";
        ListMatcher matcher = ListMatcher.instance(oldL, newL);
        if (matcher.match()) {
            Separator s = new Separator(matcher.getTransformedResult(), JavaTokenId.COMMA);
            s.compute();
            System.err.println("testRemoveLastThree:");
            System.err.println(p(oldL, newL));
            System.err.println(s.print());
            assertEquals(golden, s.print());
        } else {
            assertTrue("No match!", false);
        }
    }
    
    public void testRemoveLast() {
        String[] oldL = { "A", "B" };
        String[] newL = { "A" };
        String golden = 
                "previous {delete} B \n";
        ListMatcher matcher = ListMatcher.instance(oldL, newL);
        if (matcher.match()) {
            Separator s = new Separator(matcher.getTransformedResult(), JavaTokenId.COMMA);
            s.compute();
            System.err.println("testRemoveLast:");
            System.err.println(p(oldL, newL));
            System.err.println(s.print());
            assertEquals(golden, s.print());
        } else {
            assertTrue("No match!", false);
        }
    }
    
    public void testRemoveLast2() {
        String[] oldL = { "A" };
        String[] newL = { };
        String golden = 
                "head {delete} A tail \n";
        ListMatcher matcher = ListMatcher.instance(oldL, newL);
        if (matcher.match()) {
            Separator s = new Separator(matcher.getTransformedResult(), JavaTokenId.COMMA);
            s.compute();
            System.err.println("testRemoveLast2:");
            System.err.println(p(oldL, newL));
            System.err.println(s.print());
            assertEquals(golden, s.print());
        } else {
            assertTrue("No match!", false);
        }
    }
    
    public void testComplex() {
        String[] oldL = { "A", "B", "C", "D", "E", "F", "G" };
        String[] newL = { "B", "C", "C1", "D", "E", "G", "H" };
        String golden = 
                "{delete} A next \n" +
                "{insert} C1 next \n" +
                "{delete} F next \n" +
                "previous {insert} H \n";
        ListMatcher matcher = ListMatcher.instance(oldL, newL);
        if (matcher.match()) {
            Separator s = new Separator(matcher.getTransformedResult(), JavaTokenId.COMMA);
            s.compute();
            System.err.println("testComplex: ");
            System.err.println(p(oldL, newL));
            System.err.println(s.print());
            assertEquals(golden, s.print());
        } else {
            assertTrue("No match!", false);
        }
    }
    
    private String p(String[] o1, String o2[]) {
        StringBuffer sb = new StringBuffer(128);
        for (int i = 0; i < o1.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(o1[i]);
        }
        sb.append("\n");
        for (int i = 0; i < o2.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(o2[i]);
        }
        sb.append("\n");
        return sb.toString();
    }
}
