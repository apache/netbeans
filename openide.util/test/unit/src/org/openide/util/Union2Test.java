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

package org.openide.util;

import org.netbeans.junit.NbTestCase;
import org.openide.util.Union2;

/**
 * @author Jesse Glick
 */
public class Union2Test extends NbTestCase {

    public Union2Test(String name) {
        super(name);
    }

    public void testUnions() throws Exception {
        Union2<Integer,String> union = Union2.createFirst(3);
        assertEquals(3, union.first().intValue());
        try {
            union.second();
            fail();
        } catch (IllegalArgumentException e) {/*OK*/}
        assertTrue(union.hasFirst());
        assertFalse(union.hasSecond());
        assertEquals("3", union.toString());
        assertTrue(union.equals(Union2.createFirst(3)));
        assertFalse(union.equals(Union2.createFirst(4)));
        assertEquals(union.hashCode(), Union2.createFirst(3).hashCode());
        assertEquals(union, NbCollectionsTest.cloneBySerialization(union));
        assertEquals(union, union.clone());
        int i = union.clone().first();
        assertEquals(3, i);
        // Second type now.
        union = Union2.createSecond("hello");
        try {
            union.first();
            fail();
        } catch (IllegalArgumentException e) {/*OK*/}
        assertEquals("hello", union.second());
        assertFalse(union.hasFirst());
        assertTrue(union.hasSecond());
        assertEquals("hello", union.toString());
        assertTrue(union.equals(Union2.createSecond("hello")));
        assertFalse(union.equals(Union2.createSecond("there")));
        assertEquals(union.hashCode(), Union2.createSecond("hello").hashCode());
        assertEquals(union, NbCollectionsTest.cloneBySerialization(union));
        assertEquals(union, union.clone());
        String s = union.clone().second();
        assertEquals("hello", s);
    }

    public void testNulls() throws Exception {
        Union2<Void,Boolean> union1 = Union2.createFirst(null);
        assertEquals("null", union1.toString());
        assertEquals(0, union1.hashCode());
        assertTrue(union1.equals(null));
        assertFalse(union1.equals(Union2.createSecond(true)));
        Union2<Boolean,Void> union2 = Union2.createSecond(null);
        assertEquals("null", union2.toString());
        assertEquals(0, union2.hashCode());
        assertTrue(union2.equals(null));
        assertFalse(union2.equals(Union2.createFirst(true)));
    }

}
