/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Tomas Zezula
 */
public class ProxyIterableTest extends NbTestCase {

    public ProxyIterableTest(final String name) {
        super(name);
    }

    public void testEmpty() {
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(Collections.<Collection<? extends Integer>>emptySet());
        assertFalse(pi.iterator().hasNext());
        try {
            pi.iterator().next();
            assertTrue(false);
        } catch (NoSuchElementException e) {}
    }
    
    public void testSingleList() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd);
        Iterator<? extends Integer> it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertFalse(it.hasNext());
    }
    
    public void testMultipleLists() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        l2.add(3);
        l2.add(4);
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(5);
        l3.add(6);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd);
        Iterator<? extends Integer> it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(3), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(4), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(5), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(6), it.next());
        assertFalse(it.hasNext());
    }

    public void testEmptyList() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(5);
        l3.add(6);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd);
        Iterator<? extends Integer> it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(5), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(6), it.next());
        assertFalse(it.hasNext());
    }

    public void testDuplicates() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        l2.add(1);
        l2.add(2);
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(1);
        l3.add(2);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd);
        Iterator<? extends Integer> it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertFalse(it.hasNext());
    }

    public void testNoDuplicates() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        l2.add(1);
        l2.add(2);
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(1);
        l3.add(2);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd, false);
        Iterator<? extends Integer> it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertFalse(it.hasNext());
    }

    public void testUnusual() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        l2.add(3);
        l2.add(4);
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(2);
        l3.add(3);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd);
        Iterator<? extends Integer> it = pi.iterator();
        for(int i = 0; i < 20; i++) {
            assertTrue("i=" + i, it.hasNext());
        }
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertEquals(Integer.valueOf(4), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertFalse(it.hasNext());
    }

    public void testUnusualNoDuplicates() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        l2.add(3);
        l2.add(4);
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(2);
        l3.add(3);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd, false);
        Iterator<? extends Integer> it = pi.iterator();
        for(int i = 0; i < 20; i++) {
            assertTrue("i=" + i, it.hasNext());
        }
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertEquals(Integer.valueOf(4), it.next());
        assertFalse(it.hasNext());
    }

    public void testEmptyCached() {
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(Collections.<Collection<? extends Integer>>emptySet(),true,true);
        //First run
        assertFalse(pi.iterator().hasNext());
        try {
            pi.iterator().next();
            assertTrue(false);
        } catch (NoSuchElementException e) {}
        //Second run
        assertFalse(pi.iterator().hasNext());
        try {
            pi.iterator().next();
            assertTrue(false);
        } catch (NoSuchElementException e) {}
    }

    public void testSingleListCached() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd,true,true);
        //First run
        Iterator<? extends Integer> it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertFalse(it.hasNext());
        //Second run
        it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertFalse(it.hasNext());
    }

    public void testMultipleListsCached() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        l2.add(3);
        l2.add(4);
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(5);
        l3.add(6);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd, true, true);
        //First run
        Iterator<? extends Integer> it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(3), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(4), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(5), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(6), it.next());
        assertFalse(it.hasNext());
        //First run
        it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(3), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(4), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(5), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(6), it.next());
        assertFalse(it.hasNext());
    }

    public void testEmptyListCached() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(5);
        l3.add(6);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd,true,true);
        //First run
        Iterator<? extends Integer> it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(5), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(6), it.next());
        assertFalse(it.hasNext());
        //Second run
        it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(5), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(6), it.next());
        assertFalse(it.hasNext());
    }

    public void testDuplicatesCached() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        l2.add(1);
        l2.add(2);
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(1);
        l3.add(2);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd,true,true);
        //First run
        Iterator<? extends Integer> it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertFalse(it.hasNext());
        //Second run
        it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertFalse(it.hasNext());
    }

    public void testNoDuplicatesCached() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        l2.add(1);
        l2.add(2);
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(1);
        l3.add(2);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd, false, true);
        //First run
        Iterator<? extends Integer> it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertFalse(it.hasNext());
        //First second
        it = pi.iterator();
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(2), it.next());
        assertFalse(it.hasNext());
    }

    public void testUnusualCached() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        l2.add(3);
        l2.add(4);
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(2);
        l3.add(3);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd,true,true);
        //First run
        Iterator<? extends Integer> it = pi.iterator();
        for(int i = 0; i < 20; i++) {
            assertTrue("i=" + i, it.hasNext());
        }
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertEquals(Integer.valueOf(4), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertFalse(it.hasNext());
        //First run
        it = pi.iterator();
        for(int i = 0; i < 20; i++) {
            assertTrue("i=" + i, it.hasNext());
        }
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertEquals(Integer.valueOf(4), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertFalse(it.hasNext());
    }

    public void testUnusualNoDuplicatesCached() {
        List<Integer> l1 = new LinkedList<Integer>();
        l1.add(1);
        l1.add(2);
        List<Integer> l2 = new LinkedList<Integer>();
        l2.add(3);
        l2.add(4);
        List<Integer> l3 = new LinkedList<Integer>();
        l3.add(2);
        l3.add(3);
        List<List<? extends Integer>> toAdd = new LinkedList<List<? extends Integer>>();
        toAdd.add(l1);
        toAdd.add(l2);
        toAdd.add(l3);
        ProxyIterable<Integer> pi = new ProxyIterable<Integer>(toAdd, false,true);
        //First run
        Iterator<? extends Integer> it = pi.iterator();
        for(int i = 0; i < 20; i++) {
            assertTrue("i=" + i, it.hasNext());
        }
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertEquals(Integer.valueOf(4), it.next());
        assertFalse(it.hasNext());
        //Second run
        it = pi.iterator();
        for(int i = 0; i < 20; i++) {
            assertTrue("i=" + i, it.hasNext());
        }
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertEquals(Integer.valueOf(4), it.next());
        assertFalse(it.hasNext());
    }
}
