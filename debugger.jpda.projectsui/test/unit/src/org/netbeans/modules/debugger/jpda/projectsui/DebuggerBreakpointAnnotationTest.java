/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013, 2016 Oracle and/or its affiliates. All rights reserved.
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
