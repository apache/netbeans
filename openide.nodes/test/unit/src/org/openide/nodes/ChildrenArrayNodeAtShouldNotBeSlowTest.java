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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;

/** If using Children.Array the node.getChildren().getNodeAt(int) used to iterate slowly.
 * @author Jaroslav Tulach
 */
@RandomlyFails // NB-Core-Build #1206
public class ChildrenArrayNodeAtShouldNotBeSlowTest extends NbTestCase {
    /** start time of the test */
    private long time;
    /** table with test resutls Integer -> Long */
    private static HashMap times = new HashMap ();
    /** node to work on */
    private Node node;
    
    
    public ChildrenArrayNodeAtShouldNotBeSlowTest (String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        int count = getNumber ().intValue ();
        
        final Node[] arr = new Node[count];
        for (int i = 0; i < count; i++) {
            AbstractNode n = new AbstractNode (Children.LEAF);
            n.setName (String.valueOf (i));
            arr[i] = n;
        }

        Children.Array ch = new Children.Array ();
        ch.add (arr);
        node = new AbstractNode (ch);
        
        assertEquals (count, node.getChildren ().getNodesCount ());
        assertEquals (String.valueOf (count - 1), node.getChildren ().getNodeAt (count - 1).getName ());

        // warmup a bit
        for (int i = 0; i < 5; i++) {
            createChildren ();
        }
        
        time = System.currentTimeMillis ();
    }
    
    /** @return the size of this test */
    private Integer getNumber () {
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile ("test[a-zA-Z]*([0-9]+)").matcher (getName ());
            assertTrue ("Name does not contain numbers: " + getName (), m.find ());
            return Integer.valueOf (m.group (1));
        } catch (Exception ex) {
            ex.printStackTrace();
            fail ("Name: " + getName () + " does not represent number");
            return null;
        }
    }
    
    protected void tearDown() throws Exception {
        node = null;
        
        long now = System.currentTimeMillis ();
        
        times.put (getNumber (), new Long (now - time));

        // and verify
        assertNumbersAreSane ();
        
    }
    
    private void createChildren () {
        int middle = node.getChildren ().getNodesCount () / 2;
        String middleName = String.valueOf (middle);
        Node prev = null;
        for (int i = 0; i < 100000; i++) {
            Node n = node.getChildren ().getNodeAt (middle);
            if (prev != null) {
                assertSame ("The node is still the same", prev, n);
            }
            prev = n;
            assertEquals (middleName, n.getName ());
        }
    }
    
    public void test10 () throws Exception {
        createChildren ();
    }
    
    public void test140 () throws java.io.IOException {
        createChildren ();
    }
    
    public void test599 () throws java.io.IOException {
        createChildren ();
    }

    public void test1245 () throws java.io.IOException {
        createChildren ();
    }
    
    public void test3553 () throws java.io.IOException {
        createChildren ();
    }
    
    public void test10746 () throws Exception {
        createChildren ();
    }
    
    /** Compares that the numbers are in sane bounds */
    private void assertNumbersAreSane () {
        StringBuffer error = new StringBuffer ();
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        int maxIndex = -1;
        {
            Iterator it = times.entrySet ().iterator ();
            int cnt = 0;
            while (it.hasNext ()) {
                Map.Entry en = (Map.Entry)it.next ();
                error.append ("Test "); error.append (en.getKey ());
                error.append (" took "); error.append (en.getValue ());
                
                Long l = (Long)en.getValue ();
                if (l.longValue () > max) {
                    max = l.longValue ();
                    maxIndex = ((Integer)en.getKey ()).intValue ();
                }
                if (l.longValue () < min && l.longValue() > 0) {
                    min = l.longValue ();
                }
                error.append (" ms\n");
                
                cnt++;
            }
        }
        
        
        if (min * 10 < max && maxIndex > 3) {
            fail ("Too big differences when various number of shadows is used:\n" + error.toString ());
        }
        
        System.err.println(error.toString ());
    }
    
}

