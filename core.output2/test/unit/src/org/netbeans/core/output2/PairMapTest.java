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

package org.netbeans.core.output2;

import java.lang.ref.WeakReference;
import junit.framework.TestCase;

/**
 *
 * @author tim
 */
public class PairMapTest extends TestCase {

    public PairMapTest(String testName) {
        super(testName);
    }

    private PairMap map;
    protected void setUp() throws Exception {
        map = new PairMap();
    }

    /**
     * Test of size method, of class org.netbeans.core.output2.PairMap.
     */
    public void testSize() {
        System.out.println("testSize");

        for (int i=0; i < 10; i++) {
            String name = "Item " + i;
            map.add (name, new NbIO(name));
        }
        
        assertTrue ("Map.size returns " + map.size() + " but should return 10",
            map.size() == 10);
        
    }
    
    /**
     * Test of isEmpty method, of class org.netbeans.core.output2.PairMap.
     */
    public void testIsEmpty() {
        assertTrue ("Map should be initially empty", map.isEmpty());
        
        String[] s = new String[10];
        for (int i=0; i < s.length; i++) {
            s[i] = "Item " + i;
            map.add (s[i], new NbIO(s[i]));
        }
        
        assertFalse("Map should not be empty, it should contain 10 items", 
            map.isEmpty());
        
        for (int i=0; i < s.length; i++) {
            s[i] = "Item " + i;
            map.remove(s[i]);
        }
        
        assertTrue ("After removing the 10 items added, map should be empty",
            map.isEmpty());
        
        map.setWeak(true);
        for (int i=0; i < 5; i++) {
            System.gc();
        }
        assertTrue ("After gc'ing, map should be empty", map.isEmpty());
    }
    
    /**
     * Test of setWeak method, of class org.netbeans.core.output2.PairMap.
     */
    public void testSetWeak() {
        System.out.println("testSetWeak");
        
        String[] s = new String[10];
        for (int i=0; i < s.length; i++) {
            s[i] = "Item " + i;
            map.add (s[i], new NbIO(s[i]));
        }
        
        Object[] vals = map.vals;
        for (int i=0; i < vals.length; i++) {
            assertTrue ("Should be null or NbIO, not " + vals[i].getClass() + " at " + i,
            vals[i] == null || vals[i] instanceof NbIO);
        }

        map.setWeak(true);
        for (int i=0; i < vals.length; i++) {
            assertTrue ("Should be null or NbIO, not " + vals[i].getClass() + " at " + i,
            vals[i] == null || vals[i] instanceof WeakReference);
        }
        
        map.setWeak(false);
        for (int i=0; i < vals.length; i++) {
            assertTrue ("Should be null or NbIO, not " + vals[i].getClass() + " at " + i,
            vals[i] == null || vals[i] instanceof NbIO);
        }
        
        map.clear();
        NbIO[] ios = new NbIO[s.length];
        for (int i=0; i < s.length; i++) {
            NbIO io = new NbIO(s[i]);
            if (i % 2 == 0) {
                ios[i] = io;
            }
            s[i] = "Item " + i;
            map.add (s[i], io);
        }
        
        map.setWeak(true);
        for (int i=0; i < 5; i++) {
            System.gc();
        }
        int size = map.size();
        assertTrue ("Five object should have been garbage collected", size == 5);
        ios = null;
        for (int i=0; i < 5; i++) {
            System.gc();
        }
        
        size = map.size();
        assertTrue ("All object should have been garbage collected, but size is " + size, size == 0);
        
    }
    
}
