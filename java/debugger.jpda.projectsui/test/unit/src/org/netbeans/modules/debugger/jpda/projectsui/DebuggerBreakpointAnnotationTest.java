/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * Contributor(s): markiewb@netbeans.org
 */
package org.netbeans.modules.debugger.jpda.projectsui;

import org.netbeans.modules.debugger.jpda.projectsui.DebuggerBreakpointAnnotation;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.openide.text.Line;
import org.openide.util.Lookup;

/**
 *
 * @author markiewb@netbeans.org
 */
public class DebuggerBreakpointAnnotationTest {

    private LineBreakpoint breakpoint;

    @Before
    public void setUp () {
        breakpoint = LineBreakpoint.create("", 2);
    }

    @Test
    public void testGetShortDescription_Condition_None () {
        DebuggerBreakpointAnnotation sut = new DebuggerBreakpointAnnotation((EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE), new LineMock(), breakpoint);
        assertEquals("Conditional Breakpoint", sut.getShortDescription());
    }

    @Test
    public void testGetShortDescription_Condition_HitCount_Equal () {
        DebuggerBreakpointAnnotation sut = new DebuggerBreakpointAnnotation((EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE), new LineMock(), breakpoint);
        breakpoint.setHitCountFilter(42, Breakpoint.HIT_COUNT_FILTERING_STYLE.EQUAL);

        assertEquals("Conditional Breakpoint\nHits when:\nHit count = 42", sut.getShortDescription());
    }

    @Test
    public void testGetShortDescription_Condition_HitCount_Greater () {
        DebuggerBreakpointAnnotation sut = new DebuggerBreakpointAnnotation((EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE), new LineMock(), breakpoint);
        breakpoint.setHitCountFilter(42, Breakpoint.HIT_COUNT_FILTERING_STYLE.GREATER);

        assertEquals("Conditional Breakpoint\nHits when:\nHit count > 42", sut.getShortDescription());
    }

    @Test
    public void testGetShortDescription_Condition_HitCount_MultipleOf () {
        DebuggerBreakpointAnnotation sut = new DebuggerBreakpointAnnotation((EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE), new LineMock(), breakpoint);
        breakpoint.setHitCountFilter(2, Breakpoint.HIT_COUNT_FILTERING_STYLE.MULTIPLE);

        assertEquals("Conditional Breakpoint\nHits when:\nHit count is multiple of 2", sut.getShortDescription());
    }

    @Test
    public void testGetShortDescription_Condition_Code_Null () {
        DebuggerBreakpointAnnotation sut = new DebuggerBreakpointAnnotation((EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE), new LineMock(), breakpoint);
        breakpoint.setCondition(null);

        assertEquals("Conditional Breakpoint", sut.getShortDescription());
    }

    @Test
    public void testGetShortDescription_Condition_Code_NotNull () {
        DebuggerBreakpointAnnotation sut = new DebuggerBreakpointAnnotation((EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE), new LineMock(), breakpoint);
        breakpoint.setCondition("a==6");

        assertEquals("Conditional Breakpoint\nHits when:\na==6", sut.getShortDescription());
    }

    @Test
    public void testGetShortDescription_Condition_Multiple () {
        DebuggerBreakpointAnnotation sut = new DebuggerBreakpointAnnotation((EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE), new LineMock(), breakpoint);
        breakpoint.setCondition("a==6");
        breakpoint.setHitCountFilter(42, Breakpoint.HIT_COUNT_FILTERING_STYLE.EQUAL);

        assertEquals("Conditional Breakpoint\nHits when:\na==6\nHit count = 42", sut.getShortDescription());
    }

    /**
     * Test whether {@link FieldBreakpoint}, {@link MethodBreakpoint} and {@link
     * LineBreakpoint} are supported. Tests the {@link DebuggerBreakpointAnnotation#getCondition(org.netbeans.api.debugger.Breakpoint)} too.
     */
    @Test
    public void testGetShortDescription_Condition_SupportedBreakpointTypes () {
        {
            FieldBreakpoint b = FieldBreakpoint.create(
                    "org.netbeans.modules.editor.EditorPanel",
                    "state",
                    FieldBreakpoint.TYPE_MODIFICATION);
            DebuggerBreakpointAnnotation sut = new DebuggerBreakpointAnnotation((EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE), new LineMock(), b);
            b.setCondition("a==6");
            assertEquals("Conditional Breakpoint\nHits when:\na==6", sut.getShortDescription());
        }
        {
            MethodBreakpoint b = MethodBreakpoint.create(
                    "org.netbeans.modules.editor.EditorPanel",
                    "state");
            DebuggerBreakpointAnnotation sut = new DebuggerBreakpointAnnotation((EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE), new LineMock(), b);
            b.setCondition("a==6");

            assertEquals("Conditional Breakpoint\nHits when:\na==6", sut.getShortDescription());

        }
        {
            DebuggerBreakpointAnnotation sut = new DebuggerBreakpointAnnotation((EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE), new LineMock(), breakpoint);
            breakpoint.setCondition("a==6");

            assertEquals("Conditional Breakpoint\nHits when:\na==6", sut.getShortDescription());

        }

    }

    private static class LineMock extends Line {

        public LineMock () {
            super(Lookup.EMPTY);
        }

        @Override
        public int getLineNumber () {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void show (int kind, int column) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setBreakpoint (boolean b) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isBreakpoint () {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void markError () {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void unmarkError () {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void markCurrentLine () {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void unmarkCurrentLine () {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
