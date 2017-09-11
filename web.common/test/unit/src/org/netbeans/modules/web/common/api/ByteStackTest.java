/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.common.api;

import org.netbeans.junit.NbTestCase;


public class ByteStackTest extends NbTestCase {

    private ByteStack stack;


    public ByteStackTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        stack = new ByteStack();
    }

    public void testGeneral() {
        assertTrue(stack.isEmpty());
        try {
            stack.peek();
            fail("Should not get here");
        } catch (ArrayIndexOutOfBoundsException exc) {
            // expected
        }
        try {
            stack.pop();
            fail("Should not get here");
        } catch (ArrayIndexOutOfBoundsException exc) {
            // expected
        }
        stack.push(7);
        assertEquals(7, stack.peek());
        assertEquals(1, stack.size());
        assertFalse(stack.isEmpty());
        assertFalse(stack.contains(8));
        assertTrue(stack.contains(7));
        int first = stack.pop();
        assertEquals(7, first);
        assertTrue(stack.isEmpty());
        stack.push(5);
        stack.push(8);
        stack.push(15);
        assertEquals(15, stack.peek());
        assertEquals(3, stack.size());
        assertFalse(stack.isEmpty());
        assertFalse(stack.contains(7));
        assertTrue(stack.contains(8));
        stack.clear();
        assertEquals(0, stack.size());
        assertTrue(stack.isEmpty());
        assertFalse(stack.contains(7));
        assertFalse(stack.contains(8));
    }

    public void testCopying() {
        stack.push(2);
        ByteStack copy = stack.copyOf();
        assertFalse(copy.isEmpty());
        assertEquals(1, copy.size());
        assertEquals(2, copy.peek());
        stack.push(17);
        copy = stack.copyOf();
        assertEquals(2, copy.size());
        assertEquals(17, copy.peek());
        assertTrue(stack.equals(copy));
        stack.clear();
        assertFalse(copy.isEmpty());
        ByteStack other = new ByteStack();
        other.push(4);
        other.push(16);
        other.push(35);
        stack.copyFrom(other);
        assertEquals(3, stack.size());
        assertEquals(35, stack.peek());
        assertTrue(stack.equals(other));
        other.clear();
        assertFalse(stack.isEmpty());
    }

}
