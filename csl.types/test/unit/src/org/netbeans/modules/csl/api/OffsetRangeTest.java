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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.csl.api;

import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.junit.NbTestCase;

/**
 * @author Tor Norbye
 */
public class OffsetRangeTest extends NbTestCase {

    public OffsetRangeTest(String testName) {
        super(testName);
    }

    public void testOverlaps() {
        OffsetRange range1 = new OffsetRange(1, 4);
        OffsetRange range2 = new OffsetRange(2, 6);
        OffsetRange range3 = new OffsetRange(4, 6);
        OffsetRange range4 = new OffsetRange(0, 1);
        OffsetRange range5 = new OffsetRange(0, 6);
        assertTrue(range1.overlaps(range2));
        assertTrue(range2.overlaps(range1));
        assertFalse(range1.overlaps(range3));
        assertFalse(range3.overlaps(range1));
        assertFalse(range1.overlaps(range4));
        assertFalse(range4.overlaps(range1));
        assertTrue(range1.overlaps(range5));
        assertTrue(range5.overlaps(range1));
        
        assertFalse(range1.overlaps(OffsetRange.NONE));
        assertFalse(OffsetRange.NONE.overlaps(range5));
        assertFalse(OffsetRange.NONE.overlaps(OffsetRange.NONE));
    }
    
    public void testGetStart() {
        OffsetRange range = new OffsetRange(1, 4);
        assertEquals(1, range.getStart());
    }

    public void testGetEnd() {
        OffsetRange range = new OffsetRange(1, 4);
        assertEquals(4, range.getEnd());
    }

    public void testGetLength() {
        OffsetRange range = new OffsetRange(1, 4);
        assertEquals(3, range.getLength());
    }

    public void testContainsInclusive() {
        OffsetRange range = new OffsetRange(1, 4);
        assertTrue(range.containsInclusive(1));
        assertTrue(range.containsInclusive(3));
        assertTrue(range.containsInclusive(4));
        assertFalse(range.containsInclusive(5));
        assertFalse(range.containsInclusive(0));
    }
    
    public void testEquals() {
        assertEquals(new OffsetRange(1,3), new OffsetRange(1,3));
        assertEquals(new OffsetRange(0,0), new OffsetRange(0,0));
        assertFalse(new OffsetRange(1,3).equals(new Object()));

        boolean success = false;
        try {
            // Should generate an assertion!
            new OffsetRange(9,8);
        } catch (AssertionError e) {
            success = true;
        }
        assertTrue(success);
    }
    
    public void testComparator() {
        assertTrue(new OffsetRange(1,3).compareTo(new OffsetRange(3,5)) < 0);
        assertTrue(new OffsetRange(3,5).compareTo(new OffsetRange(1,3)) > 0);
        assertTrue(new OffsetRange(3,5).compareTo(new OffsetRange(3,5)) == 0);
        assertTrue(new OffsetRange(1,3).compareTo(new OffsetRange(1,5)) < 0);
        assertTrue(new OffsetRange(1,5).compareTo(new OffsetRange(1,3)) > 0);
    }
    
    public void testEmpty() {
        assertTrue(new OffsetRange(5,5).isEmpty());
        assertFalse(new OffsetRange(5,6).isEmpty());
    }

    public void testBoundTo() {
        assertEquals(new OffsetRange(1,3), new OffsetRange(1,3).boundTo(1, 3));
        assertEquals(new OffsetRange(1,3), new OffsetRange(0,4).boundTo(1, 3));
        assertEquals(new OffsetRange(1,3), new OffsetRange(1,3).boundTo(0, 4));
        assertEquals(new OffsetRange(1,2), new OffsetRange(1,3).boundTo(0, 2));
        assertEquals(new OffsetRange(2,3), new OffsetRange(1,3).boundTo(2, 4));
        assertEquals(new OffsetRange(2,2), new OffsetRange(1,3).boundTo(2, 2));
        assertEquals(new OffsetRange(101,101), new OffsetRange(102,103).boundTo(0, 101));
        assertEquals(new OffsetRange(100,101), new OffsetRange(100,103).boundTo(0, 101));
        assertEquals(new OffsetRange(100,100), new OffsetRange(90,95).boundTo(100, 150));
    }
}
