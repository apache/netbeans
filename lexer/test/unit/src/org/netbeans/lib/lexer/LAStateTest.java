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

package org.netbeans.lib.lexer;

import java.util.ArrayList;
import junit.framework.*;
import java.util.List;

/**
 *
 * @author mmetelka
 */
public class LAStateTest extends TestCase {

    public LAStateTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testLAStateClassTypes() throws Exception {
        LAState laState = LAState.empty();
        laState = laState.add(1, null); // should remain NoState
        assertEquals(laState.getClass(), LAState.NoState.class);

        laState = LAState.empty();
        laState = laState.add(127, null);
        assertEquals(laState.getClass(), LAState.NoState.class);

        laState = LAState.empty();
        laState = laState.add(127, new Integer(127));
        assertEquals(laState.getClass(), LAState.ByteState.class);

        laState = LAState.empty();
        laState = laState.add(128, null);
        assertEquals(laState.getClass(), LAState.LargeState.class);

        laState = LAState.empty();
        laState = laState.add(0, new Object());
        assertEquals(laState.getClass(), LAState.LargeState.class);
    }

    public void testLAState() {
        List<Object> expected = new ArrayList<Object>();
        LAState laState = LAState.empty();
        laState = add(expected, laState, 0, null);
        laState = add(expected, laState, 1, null);
        laState = add(expected, laState, 0, new Object());
        laState = add(expected, laState, 127, null);
        laState = add(expected, laState, 127, new Integer(-1));
        remove(expected, laState, 1, 3);

        List<Object> expectedInner = expected;
        LAState laStateInner = laState;

        expected = new ArrayList<Object>();
        laState = laState.empty();
        laState = add(expected, laState, 1, null);
        laState = add(expected, laState, 7, null);
        laState = add(expected, laState, 5, null);
        laState = addAll(expected, laState, 1, expectedInner, laStateInner);
        laState = addAll(expected, laState, laState.size(), expectedInner, laStateInner);
        remove(expected, laState, 4, 3);
        laState = addAll(expected, laState, 0, expectedInner, laStateInner);
    }

    private static LAState add(List<Object> expectedLAState, LAState laState, int lookahead, Object state) {
        expectedLAState.add(Integer.valueOf(lookahead));
        expectedLAState.add(state);
        laState = laState.add(lookahead, state);
        consistencyCheck(expectedLAState, laState);
        return laState;
    }

    private static LAState addAll(List<Object> expectedLAState, LAState laState, int index,
    List<Object> expectedLAStateToAdd, LAState laStateToAdd) {
        expectedLAState.addAll(index << 1, expectedLAStateToAdd);
        laState = laState.addAll(index, laStateToAdd);
        consistencyCheck(expectedLAState, laState);
        return laState;
    }

    private static void remove(List<Object> expectedLAState, LAState laState, int index, int count) {
        for (int i = count << 1; i > 0; i--) {
            expectedLAState.remove(index << 1);
        }
        laState.remove(index, count);
        consistencyCheck(expectedLAState, laState);
    }

    private static void consistencyCheck(List<Object> expectedLAState, LAState laState) {
        // Ensure the test laState class is equal to expected one
        assertEquals(expectedLAState.size() & 1, 0);
        assertEquals("Invalid size", expectedLAState.size() >> 1, laState.size());
        for (int i = 0; i < expectedLAState.size(); i++) {
            assertEquals("Invalid lookahead", ((Integer)expectedLAState.get(i)).intValue(), laState.lookahead(i >> 1));
            i++;
            assertEquals("Invalid state", expectedLAState.get(i), laState.state(i >> 1));
        }
    }

}
