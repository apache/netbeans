/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.lsp.client.debugger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openide.text.Line;
import org.openide.util.Lookup;

public class DAPStackTraceAnnotationHolderTest {
    @Test
    public void testContains() {
        var l1 = new MockLine();
        var l2 = new MockLine();

        assertTrue("Same line", DAPStackTraceAnnotationHolder.contains(l1, l1));
        assertFalse("Different line", DAPStackTraceAnnotationHolder.contains(l1, l2));
        assertTrue("Line l1 in array", DAPStackTraceAnnotationHolder.contains(new Line[] { l1, l2 }, l1));
        assertTrue("Line l2 in array", DAPStackTraceAnnotationHolder.contains(new Line[] { l1, l2 }, l2));
        assertFalse("Line in empty array", DAPStackTraceAnnotationHolder.contains(new Line[0], l2));
    }

    private static final class MockLine extends Line {
        public MockLine() {
            super(Lookup.EMPTY);
        }

        @Override
        public int getLineNumber() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void show(int kind, int column) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setBreakpoint(boolean b) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isBreakpoint() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void markError() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void unmarkError() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void markCurrentLine() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void unmarkCurrentLine() {
            throw new UnsupportedOperationException();
        }
    }
}
