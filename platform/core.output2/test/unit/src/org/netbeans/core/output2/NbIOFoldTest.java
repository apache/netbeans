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
package org.netbeans.core.output2;

import static org.junit.Assert.*;
import org.junit.Test;
import org.openide.windows.FoldHandle;
import org.openide.windows.IOFolding;

/**
 * @author jhavlin
 */
public class NbIOFoldTest {

    /**
     * Test writing folded InputOutput to the first-level InputOutput.
     */
    @Test
    public void printToNbIO2() throws Exception {

        AbstractLines lines = createTestLines();

        assertEquals("Start.\n", lines.getLine(0));
        assertEquals("FoldA\n", lines.getLine(1));
        assertEquals("  FoldA1\n", lines.getLine(2));
        assertEquals("  FoldB\n", lines.getLine(3));
        assertEquals("    FoldB1\n", lines.getLine(4));
        assertEquals("    FoldC\n", lines.getLine(5));
        assertEquals("      FoldC1\n", lines.getLine(6));
        assertEquals("      FoldC2\n", lines.getLine(7));
        assertEquals("    FoldB2\n", lines.getLine(8));
        assertEquals("  FoldD\n", lines.getLine(9));
        assertEquals("    FoldD1\n", lines.getLine(10));
        assertEquals("  FoldA2\n", lines.getLine(11));
        assertEquals("End.\n", lines.getLine(12));
        assertEquals("", lines.getLine(13));

        IntListSimple foldOffsets = lines.getFoldOffsets();

        assertEquals(0, foldOffsets.get(0));
        assertEquals(0, foldOffsets.get(1));
        assertEquals(1, foldOffsets.get(2));
        assertEquals(2, foldOffsets.get(3));
        assertEquals(1, foldOffsets.get(4));
        assertEquals(2, foldOffsets.get(5));
        assertEquals(1, foldOffsets.get(6));
        assertEquals(2, foldOffsets.get(7));
        assertEquals(5, foldOffsets.get(8));
        assertEquals(8, foldOffsets.get(9));
        assertEquals(1, foldOffsets.get(10));
        assertEquals(10, foldOffsets.get(11));
    }

    @Test
    public void testFoldLengths() {
        AbstractLines lines = createTestLines();
        assertEquals(10, lines.foldLength(1));
        assertEquals(5, lines.foldLength(3));
        assertEquals(2, lines.foldLength(5));
        assertEquals(1, lines.foldLength(9));
    }

    @Test
    public void testRealAndVisibleIndexes() {
        AbstractLines lines = createTestLines();
        assertEquals(8, lines.realToVisibleLine(8));

        lines.hideFold(5);
        assertEquals(5, lines.realToVisibleLine(5));
        assertEquals(-1, lines.realToVisibleLine(6));
        assertEquals(-1, lines.realToVisibleLine(7));
        assertEquals(6, lines.realToVisibleLine(8));

        assertEquals(5, lines.visibleToRealLine(5));
        assertEquals(8, lines.visibleToRealLine(6));

        lines.showFold(5);
        assertEquals(6, lines.realToVisibleLine(6));
        assertEquals(7, lines.realToVisibleLine(7));
        assertEquals(8, lines.realToVisibleLine(8));

        assertEquals(5, lines.visibleToRealLine(5));
        assertEquals(6, lines.visibleToRealLine(6));
        assertEquals(7, lines.visibleToRealLine(7));
        assertEquals(8, lines.visibleToRealLine(8));

        lines.hideFold(5);
        assertEquals(8, lines.visibleToRealLine(6));
        assertEquals(6, lines.realToVisibleLine(8));
    }

    @Test
    public void testFoldFirstLine() {
        NbIO nbIO = new NbIO("test");
        nbIO.getOut().println("FoldA");
        FoldHandle foldA = IOFolding.startFold(nbIO, true);
        nbIO.getOut().println("  A");
        nbIO.getOut().println("  B");
        foldA.finish();
        AbstractLines lines = (AbstractLines) ((NbWriter) nbIO.getOut())
                .out().getLines();
        assertEquals(1, lines.visibleToRealLine(1));
        assertEquals(2, lines.visibleToRealLine(2));
        assertEquals(1, lines.realToVisibleLine(1));
        assertEquals(2, lines.realToVisibleLine(2));
        lines.hideFold(0);
        assertEquals(-1, lines.realToVisibleLine(1));
        assertEquals(-1, lines.realToVisibleLine(2));
        lines.showFold(0);
        assertEquals(1, lines.visibleToRealLine(1));
        assertEquals(2, lines.visibleToRealLine(2));
        assertEquals(1, lines.realToVisibleLine(1));
        assertEquals(2, lines.realToVisibleLine(2));
    }

    @Test
    public void testCollapseAndExpandFoldWithNestedHiddenFold() {
        AbstractLines lines = createTestLines();
        lines.hideFold(5);
        assertEquals(6, lines.realToVisibleLine(8));
        lines.hideFold(3);
        assertEquals(4, lines.realToVisibleLine(9));
        lines.showFold(3);
        assertEquals(5, lines.realToVisibleLine(5));
        assertEquals(6, lines.realToVisibleLine(8));
        assertEquals(7, lines.realToVisibleLine(9));
    }

    /**
     * Create Lines object for InputOutput that contains the following structure
     * of lines.
     *
     *  * Situation:
     *
     * <pre>
     * Line String           | IO   | Offset to the first line of parent fold
     * -----------             --     ---------------------------------------
     * Start.
     * FoldA                 | A    | 0
     *   FoldA1              | A    | 1
     * + FoldB               | B    | 2
     *     FoldB1            | B    | 1
     * +   FoldC             | C    | 2
     *       FoldC1          | C    | 1
     *       FoldC2          | C    | 2
     *     FoldB2            | B    | 5
     * + FoldD               | D    | 8
     *     FoldD1            | D    | 1
     *   FoldA2              | A    | 10
     * End.
     * </pre>
     */
    private AbstractLines createTestLines() {
        NbIO nbIO = new NbIO("test");
        nbIO.getOut().println("Start.");
        nbIO.getOut().println("FoldA");
        FoldHandle foldA = IOFolding.startFold(nbIO, true);
        nbIO.getOut().println("  FoldA1");
        nbIO.getOut().println("  FoldB");
        FoldHandle foldB = foldA.startFold(true);
        nbIO.getOut().println("    FoldB1");
        nbIO.getOut().println("    FoldC");
        FoldHandle foldC = foldB.startFold(true);
        nbIO.getOut().println("      FoldC1");
        nbIO.getOut().println("      FoldC2");
        foldC.finish();
        nbIO.getOut().println("    FoldB2");
        foldB.finish();
        nbIO.getOut().print("  ");
        nbIO.getOut().print("FoldD\n");
        FoldHandle foldD = foldA.startFold(true);
        nbIO.getOut().print("    FoldD");
        nbIO.getOut().println(1);
        foldD.finish();
        nbIO.getOut().println("  FoldA2");
        foldA.finish();
        nbIO.getOut().println("End.");
        AbstractLines lines = (AbstractLines) ((NbWriter) nbIO.getOut())
                .out().getLines();
        return lines;
    }

    @Test
    public void testCollapseFoldWhileStillWriting() {
        NbIO nbIO = new NbIO("test");
        AbstractLines lines = (AbstractLines) ((NbWriter) nbIO.getOut())
                .out().getLines();
        nbIO.getOut().println("Start.");
        nbIO.getOut().println("FoldA");
        FoldHandle foldA = IOFolding.startFold(nbIO, true);
        nbIO.getOut().println("  FoldA1");
        nbIO.getOut().println("  FoldB");
        foldA.setExpanded(false);
        assertEquals(3, lines.getVisibleLineCount());
        FoldHandle foldB = foldA.startFold(true);
        nbIO.getOut().println("    FoldB1");
        nbIO.getOut().println("    FoldC");
        foldB.finish();
        nbIO.getOut().close();
        foldA.setExpanded(true);
        assertEquals(7, lines.getVisibleLineCount());
    }

    @Test
    public void testExpandFoldWhileStillWriting() {
        NbIO nbIO = new NbIO("test");
        AbstractLines lines = (AbstractLines) ((NbWriter) nbIO.getOut())
                .out().getLines();
        nbIO.getOut().println("Start.");
        nbIO.getOut().println("FoldA");
        FoldHandle foldA = IOFolding.startFold(nbIO, false);
        nbIO.getOut().println("  FoldA1");
        nbIO.getOut().println("  FoldB");
        assertEquals(3, lines.getVisibleLineCount());
        FoldHandle foldB = foldA.startFold(false);
        nbIO.getOut().println("    FoldB1");
        nbIO.getOut().println("    FoldC");
        assertEquals(3, lines.getVisibleLineCount());
        foldB.finish();
        foldA.setExpanded(true);
        assertEquals(5, lines.getVisibleLineCount());
        nbIO.getOut().println("X");
        assertEquals(6, lines.getVisibleLineCount());
        foldB.setExpanded(true);
        assertEquals(8, lines.getVisibleLineCount());
        nbIO.getOut().println("Y");
        assertEquals(9, lines.getVisibleLineCount());
    }

    @Test
    public void testExpandingOfNestedFoldExpandsParentFolds() {
        NbIO nbIO = new NbIO("test");
        AbstractLines lines = (AbstractLines) ((NbWriter) nbIO.getOut())
                .out().getLines();
        nbIO.getOut().println("FoldA");
        FoldHandle foldA = IOFolding.startFold(nbIO, false);
        nbIO.getOut().println("  FoldB");
        FoldHandle foldB = foldA.startFold(false);
        nbIO.getOut().println("    FoldC");
        FoldHandle foldC = foldB.startFold(false);
        nbIO.getOut().println("      FoldC1");
        foldC.finish();
        foldB.finish();
        foldA.finish();
        nbIO.getOut().close();
        assertEquals(2, lines.getVisibleLineCount());
        foldC.setExpanded(true);
        assertEquals(5, lines.getVisibleLineCount());
    }

    @Test
    public void testCollapsingOfFoldNestedInCollapsedFold() {
        NbIO nbIO = new NbIO("test");
        AbstractLines lines = (AbstractLines) ((NbWriter) nbIO.getOut())
                .out().getLines();
        nbIO.getOut().println("FoldA");
        FoldHandle foldA = IOFolding.startFold(nbIO, false);
        nbIO.getOut().println("  FoldB");
        FoldHandle foldB = foldA.startFold(true);
        nbIO.getOut().println("    FoldC");
        FoldHandle foldC = foldB.startFold(true);
        nbIO.getOut().println("      FoldC1");
        foldC.finish();
        foldB.finish();
        foldA.finish();
        nbIO.getOut().close();
        assertEquals(2, lines.getVisibleLineCount());
        foldC.setExpanded(false);
        assertEquals(2, lines.getVisibleLineCount());
        foldB.setExpanded(false);
        assertEquals(2, lines.getVisibleLineCount());
    }

    @Test
    public void testCollapseAll() {
        AbstractLines l = createTestLines();
        assertEquals(14, l.getVisibleLineCount());
        l.hideAllFolds();
        assertEquals(4, l.getVisibleLineCount());
    }

    @Test
    public void testExpandAll() {
        AbstractLines l = createTestLines();
        l.hideAllFolds();
        assertEquals(4, l.getVisibleLineCount());
        l.showAllFolds();
        assertEquals(14, l.getVisibleLineCount());
    }

    @Test
    public void testCollapseTree() {
        AbstractLines l = createTestLines();
        l.hideFoldTree(3);
        assertEquals(9, l.getVisibleLineCount());
        l.showFold(3);
        assertEquals(12, l.getVisibleLineCount());
    }

    @Test
    public void testExpandTree() {
        AbstractLines l = createTestLines();
        l.hideAllFolds();
        l.showFold(1);
        assertEquals(8, l.getVisibleLineCount());
        l.showFoldTree(3);
        assertEquals(13, l.getVisibleLineCount());
    }

    /**
     * Bug 231304.
     */
    @Test
    public void testFoldOperationOnLastLineWithoutException() {
        AbstractLines l = createTestLines();
        int lastLine = l.getLineCount() - 1;
        l.hideFoldTree(lastLine);
        l.showFoldTree(lastLine);
        l.hideFold(lastLine);
        l.showFold(lastLine);
    }

    /**
     * Bug 229544.
     */
    @Test
    public void testSetIncorrectCurrentFoldStart() {
        NbIO nbIO = new NbIO("test229544");
        AbstractLines lines = (AbstractLines) ((NbWriter) nbIO.getOut())
                .out().getLines();
        nbIO.getOut().println("a");
        // This can be called e.g. by invalid FoldHandle, after a line count
        // limit was reached and old lines removed.
        lines.setCurrentFoldStart(999);
        nbIO.getOut().println("b");
        nbIO.dispose();
    }

    @Test
    public void testSetIncorrectCurrentFoldStartInWrappedMode() {
        NbIO nbIO = new NbIO("test229544");
        AbstractLines lines = (AbstractLines) ((NbWriter) nbIO.getOut())
                .out().getLines();
        nbIO.getOut().println("a0123456789012345678901234567890");
        // Start computing info for wrapped mode.
        lines.getLogicalLineCountIfWrappedAt(20);
        // This can be called e.g. by invalid FoldHandle, after a line count
        // limit was reached and old lines removed.
        lines.setCurrentFoldStart(999);
        nbIO.getOut().println("b");
        nbIO.dispose();
    }

    @Test
    public void testExpandFoldFreeInNonFoldLine() {
        AbstractLines l = createTestLines();
        l.hideFoldTree(0);
    }

    /**
     * Test for bug 242792 - NPE at
     * o.n.core.output2.NbIO$IOFoldingImpl$NbIoFoldHandleDefinition.finish
     */
    @Test
    public void testFoldingOnDisposedIO() {
        NbIO io = new NbIO("testFoldingOnDisposed");
        io.getOut();
        FoldHandle fh = IOFolding.startFold(io, true);
        io.dispose();

        // This should silently ignore the fact that the IO is finished.
        fh.silentFinish();

        boolean illegalStateExceptionThrown = false;
        try {
            // This should throw an IllegalStateException.
            fh.finish();
        } catch (IllegalStateException ex) {
            illegalStateExceptionThrown = true;
        }
        assertTrue(illegalStateExceptionThrown);
    }

    /**
     * Test for bug 255907 - IllegalStateException: No OutWriter available.
     */
    @Test
    public void testStartFoldOnDisposedIO() {
        NbIO io = new NbIO("testStartFoldingOnDisposedIO");
        io.closeInputOutput();
        FoldHandle fh = IOFolding.startFold(io, true);
        FoldHandle ch = fh.startFold(true);
        ch.setExpanded(false);
        ch.finish();
        fh.finish();
    }
}