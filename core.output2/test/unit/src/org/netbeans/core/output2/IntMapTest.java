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

import java.util.Arrays;
import junit.framework.TestCase;

/**
 *
 * @author Tim Boudreau
 */
public class IntMapTest extends TestCase {

    public IntMapTest(String testName) {
        super(testName);
    }

    public void testFirst() {
        System.out.println("testFirst");
        IntMap map = new IntMap();

        int[] indices = new int [] { 5, 12, 23, 62, 247, 375, 489, 5255};

        Object[] values = new Object[] {
            "zeroth", "first", "second", "third", "fourth", "fifth", "sixth", 
            "seventh"};
        
        assert indices.length == values.length;
        
        for (int i=0; i < indices.length; i++) {
            map.put (indices[i], values[i]);
        }
        
        assertTrue ("First entry should be 5", map.first() == 5);
    }
    
    public void testNextEntry() {
        System.out.println("testNextEntry");
        IntMap map = new IntMap();
        
        int[] indices = new int [] { 5, 12, 23, 62, 247, 375, 489, 5255};
        
        Object[] values = new Object[] {
            "zeroth", "first", "second", "third", "fourth", "fifth", "sixth", 
            "seventh"};
        
        assert indices.length == values.length;
        
        for (int i=0; i < indices.length; i++) {
            map.put (indices[i], values[i]);
        }
        
        for (int i=0; i < indices.length-1; i++) {
            int val = indices[i+1];
            int next = map.nextEntry (indices[i]);
            assertTrue ("Entry after " + indices[i] + " should be " + val + " not " + next, next == val);
        }
    }
    
    public void testPrevEntry() {
        System.out.println("testPrevEntry");
        IntMap map = new IntMap();
        
        int[] indices = new int [] { 5, 12, 23, 62, 247, 375, 489, 5255};
        
        Object[] values = new Object[] {
            "zeroth", "first", "second", "third", "fourth", "fifth", "sixth", 
            "seventh"};
        
        assert indices.length == values.length;
        
        for (int i=0; i < indices.length; i++) {
            map.put (indices[i], values[i]);
        }
        
        for (int i=indices.length-1; i > 0; i--) {
            int val = indices[i-1];
            int next = map.prevEntry (indices[i]);
            assertTrue ("Entry before " + indices[i] + " should be " + val + " not " + next, next == val);
        }
    }
    
    public void testNearest() {
        System.out.println("testNearest");
        IntMap map = new IntMap();
        
        int[] indices = new int [] { 5, 12, 23, 62, 247, 375, 489, 5255};
        
        Object[] values = new Object[] {
            "zeroth", "first", "second", "third", "fourth", "fifth", "sixth", 
            "seventh"};
        
        assert indices.length == values.length;
        
        for (int i=0; i < indices.length; i++) {
            map.put (indices[i], values[i]);
        }
        
        for (int i=0; i < indices.length-1; i++) {
            int toTest = indices[i] + ((indices[i+1] - indices[i]) / 2);
            int next = map.nearest (toTest, false);
            assertTrue ("Nearest value to " + toTest + " should be " + indices[i+1] + ", not " + next, next == indices[i+1]);
        }
        
        assertTrue ("Value after last entry should be 0th", map.nearest (indices[indices.length-1] + 1000, false) == indices[0]);
        
        assertTrue ("Value before first entry should be last", map.nearest (-1, true) == indices[indices.length-1]);
        
        assertTrue ("Value after < first entry should be 0th", map.nearest (-1, false) == indices[0]);
        
        for (int i = indices.length-1; i > 0; i--) {
//            int toTest = indices[i] - (indices[i-1] + ((indices[i] - indices[i-1]) / 2));
            int toTest = indices[i-1] + ((indices[i] - indices[i-1]) / 2);
            int prev = map.nearest (toTest, true);
            assertTrue ("Nearest value to " + toTest + " should be " + indices[i-1] + ", not " + prev, prev == indices[i-1]);
        }
        
        assertTrue ("Entry previous to value lower than first entry should be last entry", 
            map.nearest(indices[0] - 1, true) == indices[indices.length -1]);
        
        assertTrue ("Value after > last entry should be last 0th", map.nearest(indices[indices.length-1] + 100, false) == indices[0]);
        
        assertTrue ("Value before > last entry should be last entry", map.nearest(indices[indices.length-1] + 100, true) == indices[indices.length-1]);
        
        assertTrue ("Value after < first entry should be 0th", map.nearest(-10, false) == indices[0]);
        
    }    
    
    
    /**
     * Test of get method, of class org.netbeans.core.output2.IntMap.
     */
    public void testGet() {
        System.out.println("testGet");
        
        IntMap map = new IntMap();
        
        int[] indices = new int [] { 5, 12, 23, 62, 247, 375, 489, 5255};
        
        Object[] values = new Object[] {
            "zeroth", "first", "second", "third", "fourth", "fifth", "sixth", 
            "seventh"};
        
        assert indices.length == values.length;
        
        for (int i=0; i < indices.length; i++) {
            map.put (indices[i], values[i]);
        }
        
        for (int i=0; i < indices.length; i++) {
            assertTrue (map.get(indices[i]) == values[i]);
        }
    }
    
    public void testGetKeys() {
        IntMap map = new IntMap();
        
        int[] indices = new int [] { 5, 12, 23, 62, 247, 375, 489, 5255};
        
        Object[] values = new Object[] {
            "zeroth", "first", "second", "third", "fourth", "fifth", "sixth", 
            "seventh"};
            
        for (int i=0; i < indices.length; i++) {
            map.put (indices[i], values[i]);
        }            

        int[] keys = map.getKeys();
        assertTrue ("Keys returned should match those written.  Expected: " + i2s(indices) + " Got: " + i2s(keys), Arrays.equals(keys, indices));
    }
    
    private static String i2s (int[] a) {
        StringBuffer result = new StringBuffer(a.length*2);
        for (int i=0; i < a.length; i++) {
            result.append (a[i]);
            if (i != a.length-1) {
                result.append(',');
            }
        }
        return result.toString();
    }
}
