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
