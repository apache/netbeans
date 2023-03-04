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

