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

package org.netbeans.modules.languages.parser;

import java.util.Collections;
import junit.framework.*;


/**
 *
 * @author Jan Jancura
 */
public class DGUtilsTest extends TestCase {
    
    public DGUtilsTest (String testName) {
        super (testName);
    }


    public void testReduce () {
        DG dg = DG.createDG (new Integer (1));
        dg.addNode (new Integer (2));
        dg.addNode (new Integer (3));
        dg.addNode (new Integer (4));
        dg.addEdge (new Integer (1), new Integer (2), new Character ('a'));
        dg.addEdge (new Integer (2), new Integer (3), new Character ('b'));
        dg.addEdge (new Integer (3), new Integer (4), new Character ('a'));
        dg.addEdge (new Integer (4), new Integer (3), new Character ('b'));
        dg.setStart (new Integer (1));
        dg.setEnds (Collections.singleton (new Integer (3)));
        
        dg.setProperty (new Integer (1), "jedna", "jedna");
        dg.setProperty (new Integer (2), "dve", "dve");
        dg.setProperty (new Integer (3), "tri", "tri");
        dg.setProperty (new Integer (4), "ctyri", "ctyri");
        dg.setProperty (new Integer (1), new Character ('a'), "jedna a", "jedna a");
        dg.setProperty (new Integer (2), new Character ('b'), "dve b", "dve b");
        dg.setProperty (new Integer (3), new Character ('a'), "tri a", "tri a");
        dg.setProperty (new Integer (4), new Character ('b'), "ctyri b", "ctyri b");
        dg = DGUtils.reduce (dg, nodeFactory);
        
        //S ystem.out.println(dg);
        
        assertEquals (3, dg.getNodes ().size ());
        Object n1 = dg.getStartNode ();
        assertEquals (1, dg.getEdges (n1).size ());
        assertEquals (1, dg.getProperties (n1).size ());
        assertEquals ("jedna", dg.getProperty (n1, "jedna"));

        Object e1 = dg.getEdge (n1, 'a');
        assertEquals (1, dg.getProperties (n1, e1).size ());
        assertEquals ("jedna a", dg.getProperty (n1, e1, "jedna a"));
        Object n2 = dg.getNode (n1, e1);
        assertNotNull (n2);
        assertEquals (1, dg.getEdges (n2).size ());
        assertNull (dg.getEdge (n1, 'b'));
        assertEquals (2, dg.getProperties (n2).size ());
        assertEquals ("dve", dg.getProperty (n2, "dve"));
        assertEquals ("ctyri", dg.getProperty (n2, "ctyri"));
        
        Object e2 = dg.getEdge (n2, 'b');
        assertEquals (2, dg.getProperties (n2, e2).size ());
        assertEquals ("dve b", dg.getProperty (n2, e2, "dve b"));
        assertEquals ("ctyri b", dg.getProperty (n2, e2, "ctyri b"));
        Object n3 = dg.getNode (n2, e2);
        assertNotNull (n3);
        assertEquals (1, dg.getEdges (n3).size ());
        assertNull (dg.getEdge (n2, 'a'));
        assertEquals (1, dg.getEnds().size ());
        assertEquals (n3, dg.getEnds ().iterator ().next ());
        assertEquals (1, dg.getProperties (n3).size ());
        assertEquals ("tri", dg.getProperty (n3, "tri"));
        
        Object e3 = dg.getEdge (n3, 'a');
        assertEquals (1, dg.getProperties (n3, e3).size ());
        assertEquals ("tri a", dg.getProperty (n3, e3, "tri a"));
        Object n4 = dg.getNode (n3, e3);
        assertEquals(n2, n4);
    }
    
    private NodeFactory nodeFactory = new NodeFactory<Integer> () {
        public Integer createNode() {
            return Integer.valueOf (counter++);
        }

        private int counter = 1;
    };
}
