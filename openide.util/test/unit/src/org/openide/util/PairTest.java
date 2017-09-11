/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.openide.util;

import org.netbeans.junit.NbTestCase;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
public class PairTest extends NbTestCase {

    public PairTest(String name) {
        super(name);
    }


    public void testPairs() {
        final Pair<Integer,Integer> p1a = Pair.of(1, 1);
        final Pair<Integer,Integer> p1b = Pair.of(1, 1);
        final Pair<Integer,Integer> p2 = Pair.of(1, 2);
        final Pair<Integer,Integer> p3 = Pair.of(2, 1);
        final Pair<Integer,Integer> p4 = Pair.of(null, 1);
        final Pair<Integer,Integer> p5 = Pair.of(1, null);
        final Pair<Integer,Integer> p6 = Pair.of(null, null);
        assertTrue(p1a.equals(p1a));
        assertTrue(p1a.equals(p1b));
        assertFalse(p1a.equals(p2));
        assertFalse(p1a.equals(p3));
        assertFalse(p1a.equals(p4));
        assertFalse(p1a.equals(p5));
        assertFalse(p1a.equals(p6));
        assertEquals(p1a.hashCode(), p1b.hashCode());
        assertEquals(p4.hashCode(), p4.hashCode());
        assertEquals(p5.hashCode(), p5.hashCode());
        assertEquals(p6.hashCode(), p6.hashCode());
        assertFalse(p4.hashCode() == p5.hashCode());
    }

}
