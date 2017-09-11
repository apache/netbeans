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

import junit.framework.TestCase;

/**
 * Test for org.netbeans.core.output2.SparseIntList.
 *
 * @author Tim Boudreau
 */
public class SparseIntListTest extends TestCase {

    public SparseIntListTest(String testName) {
        super(testName);
    }

    private SparseIntList l = null;
    @Override
    protected void setUp() throws Exception {
        l = new SparseIntList(20);
    }

    public void testGetLessThanZeroReturnsZero() {
        System.out.println("testGetLessThanZeroReturnsZero");
        assertTrue (l.get(-1) == 0);
        assertTrue (l.get(Integer.MIN_VALUE) == 0);
    }
    
    public void testGetFromEmptyListReturnsRequestedIndex() {
        System.out.println("testGetFromEmptyListReturnsRequestedIndex");
        assertTrue (l.get(20) == 21);
        assertTrue (l.get(Integer.MAX_VALUE-1) == Integer.MAX_VALUE);
        assertTrue (l.get(1) == 2);
        assertTrue (l.get(0) == 1);
    }
    
    public void testGetBelowFirstEntryReturnsIndex() {
        System.out.println("testGetBelowFirstEntryReturnsIndex");
        l.add (11, 20);
        for (int i = 0; i < 11; i++) {
            assertTrue (l.get(i) == i+1);
        }
    }
    
    public void testAdd() {
        System.out.println("testAdd");
        l.add (11, 20);
        int val = l.get(11);
        assertTrue ("After add(11, 20), value at 11 should be 20, not " + val, val == 20);
        val = l.get(12);
        assertTrue ("After add(11, 20), value at 12 should be 21, not " + val, val == 21);
        
        l.add (12, 30);
        val = l.get(12);
        assertTrue ("After add(12, 30), value at 12 should be 30, not " + val, val == 30);
        val = l.get(13);
        assertTrue ("After add(12, 30), value at 13 should be 31, not " + val, val == 31);
        
        l.add (30, 80);
        val = l.get(12);
        assertTrue ("After add(30, 80), value at 12 should still be 30, not " + val + " adding an entry above should not change it", val == 30);
        val = l.get(13);
        assertTrue ("After add(30, 80), value at 13 should be 31, not " + val + " adding an entry above should not change it", val == 31);
        val = l.get(31);
        assertTrue ("After add(30, 80), value at 31 should be 81, not " + val, val == 81);
        
        for (int i=0; i < 10; i++) {
            val = l.get(i);
            assertTrue ("In a populated map, get() on an index below the first added entry should return the index, but get(" + i + ") returns " + val, val == i+1);
        }
        
    }
    
    public void testBadValuesThrowExceptions() {
        System.out.println("testBadValuesThrowExceptions");
        l.add (20, 11);
        Exception e = null;
        try {
            l.add (19, 13);
        } catch (Exception ex) {
            e = ex;
        }
        assertNotNull(e);
        
        try {
            l.add (21, 10);
        } catch (Exception ex2) {
            e = ex2;
        }
        assertNotNull(e);
        
    }
    
    public void testGetAboveFirstEntryReturnsEntryPlusIndexDiff() {
        System.out.println("testGetAboveFirstEntryReturnsEntryPlusIndexDiff");
        l.add (11, 20);
        int x = 21;
        for (int i = 12; i < 40; i++){
            assertTrue ("Entry at " + i + " should be " + x + ", not " + l.get(i), l.get(i) == x);
            x++;
        }
    }
}
