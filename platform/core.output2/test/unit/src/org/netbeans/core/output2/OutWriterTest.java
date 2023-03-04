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

import java.awt.Color;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.Exceptions;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/** Tests the OutWriter class
 *
 * @author Tim Boudreau
 */
public class OutWriterTest extends NbTestCase {

    public OutWriterTest(String testName) {
        super(testName);
    }

    public void testPositionOfLine() throws UnsupportedEncodingException {
        System.out.println("testPositionOfLine");

        OutWriter ow = new OutWriter ();
        
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        
        ow.println (second);
        
        ow.println (third);
        
        int pos = ow.getLines().getLineStart(0);
        
        assertTrue ("First line position should be 0 but is " + pos, pos == 0);
        
        int expectedPosition = first.length() + OutWriter.LINE_SEPARATOR.length();
        pos = ow.getLines().getLineStart(1);
        
        assertTrue ("Second line position should be length of first (" + first.length() + ") + line " +
            "separator length (" + OutWriter.LINE_SEPARATOR.length() + "), which should be " +
            expectedPosition + " but is " + pos, 
            pos == expectedPosition);
         
        
        pos = ow.getLines().getLineStart (2);
        int targetPos = first.length() + second.length() + 2 * OutWriter.LINE_SEPARATOR.length();
        
        assertTrue ("Third line position should be " + targetPos + " but is " + pos, pos == targetPos);
    }
    
    
    public void testPosition() throws UnsupportedEncodingException {
        System.out.println("testPosition");
        
        OutWriter ow = new OutWriter();

        
        String first = "This is the first string";
        String second ="This is the second string";
        String third = "This is the third string";
        
        assertTrue (ow.getLines().getLineCount() == 1);
        
        ow.println(first);
        
        assertTrue (ow.getLines().getLineCount() == 2);
        
        ow.println (second);
        
        assertTrue (ow.getLines().getLineCount() == 3);

        int targetLength = first.length() + second.length() + 2 * OutWriter.LINE_SEPARATOR.length();

        assertTrue ( 
            "After printing strings with length " + first.length() + " and " + 
            second.length() + " outfile position should be " + targetLength +
            " not " + ow.getLines().getCharCount(),
            ow.getLines().getCharCount() == targetLength);
        
        ow.println (third);
        
        targetLength = first.length() + second.length() + third.length() + 
            (3 *  OutWriter.LINE_SEPARATOR.length());
        
        assertTrue ("Length should be " + targetLength + " but position is "
            + ow.getLines().getCharCount(), targetLength == ow.getLines().getCharCount());
    }
    
    public void testLine() throws UnsupportedEncodingException {
        System.out.println("testLine");
        
        
        OutWriter ow = new OutWriter ();
        
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        
        ow.println (second);
        
        ow.println (third);
        
        assertTrue ("After writing 3 lines, linecount should be 4, not " + 
            ow.getLines().getLineCount(), ow.getLines().getLineCount() == 4);
        
        String firstBack = null;
        String secondBack = null;
        String thirdBack = null;
        try {
            firstBack = ow.getLines().getLine(0);
            secondBack = ow.getLines().getLine(1);
            thirdBack = ow.getLines().getLine(2);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail (ioe.getMessage());
        }
        String firstExpected = first + OutWriter.LINE_SEPARATOR;
        String secondExpected = second + OutWriter.LINE_SEPARATOR;
        String thirdExpected = third + OutWriter.LINE_SEPARATOR;
        
        assertEquals("First string should be \"" + firstExpected + "\" but was \"" + firstBack + "\"",
            firstBack, firstExpected);
        
        assertEquals("Second string should be \"" + secondExpected + "\" but was \"" + secondBack + "\"",
            secondBack, secondExpected);

        assertEquals("Third string should be \"" + thirdExpected + "\" but was \"" + thirdBack + "\"",
            thirdBack, thirdExpected);
        
    }
     
    public void testLineForPosition() {
        System.out.println("testLineForPosition");
        
        
        OutWriter ow = new OutWriter ();
        
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        
        ow.println (second);
        
        ow.println (third);
        
        int line = ow.getLines().getLineAt (first.length() / 2);
        
        assertTrue ("Position halfway through first line should map to line 0," +
            " not " + line,
            line == 0);
        
        line = ow.getLines().getLineAt (first.length() + OutWriter.LINE_SEPARATOR.length() +
            (second.length() / 2));
        
        assertTrue ("Position halfway through line 1 should map to line 1, not " +
            line,
            line == 1);
        
        //XXX do some more tests here for very large buffers, to ensure no
        //off-by-ones
        
    }
    
    public void testLineCount() {
        System.out.println("testLineCount");
        
        OutWriter ow = new OutWriter ();
        
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        String appended = "Appended string to last line";
        
        ow.println(first);
        
        ow.println (second);
        
        ow.println (third);
        
        ow.flush();
        processEQ();
        Thread.yield();
        
        assertTrue ("Linecount should be 4 after printing 3 lines, not " +
            ow.getLines().getLineCount(), ow.getLines().getLineCount() == 4);
        
        ow.print(appended);
        ow.print(appended);
        ow.flush();

        assertTrue("Linecount should be still 4 after appending text to last line, not " +
                ow.getLines().getLineCount(), ow.getLines().getLineCount() == 4);

    }
    
    public void testAddChangeListener() {
        System.out.println("testAddChangeListener");
        OutWriter ow = new OutWriter ();

        CL cl = new CL();
        try {
            ow.getLines().addChangeListener(cl);
        } catch (Exception e) {
            e.printStackTrace();
            fail ("Caught exception " + e);
        }

        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        ow.println (second);
        ow.println (third);
        ow.flush();
        processEQ();
        cl.assertChanged();
    }

    public void testMultilineText() {
        System.out.println("testMultilineText");
        OutWriter ow = new OutWriter ();
        String threeLines = "This is\nthree lines of\nText";
        ow.print(threeLines);
        assertTrue ("Line count should be 3, not " + ow.getLines().getLineCount(), ow.getLines().getLineCount() == 3);
        ow.println("This is another line");
        assertTrue ("Line count should be 4, not " + ow.getLines().getLineCount(), ow.getLines().getLineCount() == 4);
        ow.println(threeLines);
        assertTrue ("Line count should be 7, not " + ow.getLines().getLineCount(), ow.getLines().getLineCount() == 7);
    }

    @RandomlyFails // NB-Core-Build #1887
    public void testRemoveChangeListener() {
        System.out.println("testRemoveChangeListener");
        
        
        
        OutWriter ow = new OutWriter ();
        
        CL cl = new CL();
        try {
            ow.getLines().addChangeListener(cl);
        } catch (Exception e) {
            e.printStackTrace();
            fail ("Caught exception " + e);
        }

        cl.clear();
        ow.getLines().removeChangeListener(cl);

        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        
        ow.println (second);
        
        ow.println (third);
        
        ow.flush();        
        
        cl.assertNoChange();
    }
    
    public void testCheckDirty() {
        System.out.println("testCheckDirty");
        
        
        OutWriter ow = new OutWriter ();

        boolean dirty = ow.getLines().checkDirty(true);
        
        String first = "This is the a test";
        
        ow.println(first);
        
        
        //plan to delete checkDirty
    }
    
    public void testSubstring() {
        System.out.println("testSubstring");
        
        OutWriter ow = new OutWriter ();
        
        String first = "This is the first string";
        String second = "This is the second string, ain't it?";
        String third = "This is the third string";
        
        ow.println(first);
        ow.println (second);
        ow.println (third);
        ow.flush();
        
        //First test intra-line substrings
        
        String expected = first.substring(5, 15);
        String gotten = ow.getLines().getText (5, 15);
        System.err.println("\nGot " + gotten + "\n");
        
        assertEquals ("Should have gotten string \"" + expected + "\" but got \"" + gotten + "\"", expected, gotten);
        
        
    }    
    
    public void testPrintln() {
        System.out.println("testPrintln");

        try {
            OutWriter ow = new OutWriter ();

            String first = "This is a test string";

            ow.println(first);
            ow.flush();
            
            String firstExpected = first + OutWriter.LINE_SEPARATOR;
            String firstReceived = ow.getLines().getLine(0);
            
            assertEquals ("First line should be \"" + firstExpected + "\" but was \"" + firstReceived + "\"", firstExpected, firstReceived);
        
        } catch (Exception e) {
            e.printStackTrace();
            fail (e.getMessage());
        }
        
    }
    
    public void testReset() {
        System.out.println("testReset");
        
    }
    
    public void testFlush() {
        System.out.println("testFlush");
        
    }
    
    public void testClose() {
        System.out.println("testClose");
        
    }

    public void testCheckError() {
        System.out.println("testCheckError");
        
    }

    public void testSetError() {
        System.out.println("testSetError");
        
    }

    public void testWrite() {
        System.out.println("testWrite");
        try {
            OutWriter ow = new OutWriter ();
  
            ow.write('x');
            ow.write('y');
            ow.write('z');
            ow.println();
            ow.flush();
            assertEquals(2, ow.getLines().getLineCount());
            String firstReceived = ow.getLines().getLine(0);
            assertEquals ("xyz" + OutWriter.LINE_SEPARATOR, firstReceived);
        
            ow = new OutWriter();
            ow.println("firstline");
            ow.write('x');
            ow.println("yz");
            ow.flush();
            assertEquals(3, ow.getLines().getLineCount());
            firstReceived = ow.getLines().getLine(1);
            assertEquals ("xyz" + OutWriter.LINE_SEPARATOR, firstReceived);
            
            ow = new OutWriter();
            ow.println("firstline");
            ow.write(new char[] {'x', 'y', 'z'});
            ow.write(new char[] {'x', 'y', 'z'});
            ow.println("-end");
            ow.flush();
            assertEquals(3, ow.getLines().getLineCount());
            firstReceived = ow.getLines().getLine(1);
            assertEquals ("xyzxyz-end" + OutWriter.LINE_SEPARATOR, firstReceived);
            
            ow = new OutWriter();
            ow.write(new char[] {'x', 'y', '\n', 'z', 'z', 'z', '\n', 'A'});
            ow.flush();
            assertEquals(3, ow.getLines().getLineCount());
            assertEquals("xy" + OutWriter.LINE_SEPARATOR, ow.getLines().getLine(0));
            assertEquals("zzz" + OutWriter.LINE_SEPARATOR, ow.getLines().getLine(1));
            assertEquals("A", ow.getLines().getLine(2));
            
            ow = new OutWriter();
            ow.write(new char[] {'x', 'y', '\n', 'z', 'z', 'z', '\n'});
            ow.flush();
            assertEquals(3, ow.getLines().getLineCount());
            assertEquals("xy" + OutWriter.LINE_SEPARATOR, ow.getLines().getLine(0));
            assertEquals("zzz" + OutWriter.LINE_SEPARATOR, ow.getLines().getLine(1));
            
            ow = new OutWriter();
            ow.write(new char[] {'\n', '\n', '\n', 'z', 'z', 'z', '\n'});
            ow.flush();
            assertEquals(5, ow.getLines().getLineCount());
            assertEquals(OutWriter.LINE_SEPARATOR, ow.getLines().getLine(0));
            assertEquals(OutWriter.LINE_SEPARATOR, ow.getLines().getLine(1));
            assertEquals(OutWriter.LINE_SEPARATOR, ow.getLines().getLine(2));
            assertEquals("zzz" + OutWriter.LINE_SEPARATOR, ow.getLines().getLine(3));
            
            ow = new OutWriter();
            ow.write("Some \u001B[31;42mColor \u001B[32;41mText and "
                    + "unsupported \u001B[3Ksequence");
            ow.flush();
            assertEquals(1, ow.getLines().getLineCount());
            LineInfo.Segment[] segments = ow.getLines().getLineInfo(0)
                    .getLineSegments().toArray(new LineInfo.Segment[4]);
            assertNull(segments[0].getCustomBackground());
            assertNull(segments[0].getCustomColor());
            assertEquals("Some ", ow.getLines().getText(
                    0, segments[0].getEnd()));

            Color secondSegmentFg = segments[1].getCustomColor();
            Color secondSegmentBg = segments[1].getCustomBackground();
            assertNotNull(secondSegmentBg);
            assertNotNull(secondSegmentFg);
            assertEquals("Color ", ow.getLines().getText(
                    segments[0].getEnd(), segments[1].getEnd()));

            assertNotNull(segments[2].getCustomBackground());
            assertNotNull(segments[2].getCustomColor());
            assertNotSame(secondSegmentBg, segments[2].getCustomBackground());
            assertNotSame(secondSegmentFg, segments[2].getCustomColor());
            assertEquals("Text and unsupported sequence", ow.getLines().getText(
                    segments[1].getEnd(), segments[2].getEnd()));
            assertNull(segments[3]);
            
        } catch (Exception e) {
            e.printStackTrace();
            fail (e.getMessage());
        }
    }
    
    public void testClearLineANSI() throws IOException {
        OutWriter ow = new OutWriter();
        for (int i = 0; i <= 10; i++) {
            ow.print("\u001B[2K" + i + "0%: ");
            for (int j = 0; j < i; j++) {
                ow.print("=");
            }
        }
        ow.println();
        assertEquals("100%: ==========\n", ow.getLines().getLine(0));
    }

    public void testWriteWithBackspace() throws IOException {

        OutWriter ow = new OutWriter();

        ow.write("Helle\bo World");
        assertEquals("Hello World", ow.getLines().getLine(0));
    }

    public void testWriteLineTerminators() throws IOException {

        OutWriter ow = new OutWriter();

        ow.write("Months:\nJanuary\nFebruary\nMarch\r\nApril\n\r\nMay");

        assertEquals("January\n", ow.getLines().getLine(1));
        assertEquals("February\n", ow.getLines().getLine(2));
        assertEquals("March\n", ow.getLines().getLine(3));
        assertEquals("April\n", ow.getLines().getLine(4));
        assertEquals("\n", ow.getLines().getLine(5));
        assertEquals("May", ow.getLines().getLine(6));
    }

    public void testCarriageReturn() throws IOException {

        OutWriter ow = new OutWriter();

        ow.write("Test\rRewrite\n");
        ow.write("Original\rNew\n");

        ow.write("Test1");
        ow.write("\rReplace1\n");

        ow.write("Test2");
        ow.write("\rRep2\n");

        assertEquals("Rewrite\n", ow.getLines().getLine(0));
        assertEquals("New\n", ow.getLines().getLine(1));
        assertEquals("Replace1\n", ow.getLines().getLine(2));
        assertEquals("Rep2\n", ow.getLines().getLine(3));
    }

    public void testBackspaceChars() throws IOException {

        OutWriter ow = new OutWriter();

        ow.write("Enter input:\n");
        ow.write(">");
        ow.write("\b");
        ow.write("Output\n");

        assertEquals("Enter input:\n", ow.getLines().getLine(0));
        assertEquals("Output\n", ow.getLines().getLine(1));
    }

    public void testWritePartial() {
        System.out.println("testWritePartial");
        try {
            OutWriter ow = new OutWriter ();

            ow.write('x');
            assertEquals(1, ow.getLines().getLineCount());
            assertEquals ("x", ow.getLines().getLine(0));
            ow.write('y');
            assertEquals ("xy", ow.getLines().getLine(0));
            ow.write('z');
            assertEquals ("xyz", ow.getLines().getLine(0));
            ow.println();
            assertEquals ("xyz" + OutWriter.LINE_SEPARATOR, ow.getLines().getLine(0));
            ow.write('a');
            assertEquals(2, ow.getLines().getLineCount());
            assertEquals ("a", ow.getLines().getLine(1));
            
            
            ow = new OutWriter();
            ow.write(new char[] { 'x', 'y', 'z', '\n', 'A'});
            assertEquals(2, ow.getLines().getLineCount());
            assertEquals ("xyz" + OutWriter.LINE_SEPARATOR, ow.getLines().getLine(0));
            assertEquals ("A", ow.getLines().getLine(1));
            ow.write('B');
            assertEquals(2, ow.getLines().getLineCount());
            assertEquals ("AB", ow.getLines().getLine(1));
            ow.println("CD");
            assertEquals(3, ow.getLines().getLineCount());
            assertEquals ("ABCD" + OutWriter.LINE_SEPARATOR, ow.getLines().getLine(1));
            
        } catch (Exception e) {
            e.printStackTrace();
            fail (e.getMessage());
        }
        
    }
    
    public void testLimitReached() throws IOException {
        OutWriter ow = new OutWriter();
        ((AbstractLines) ow.getLines()).setOutputLimits(
                new OutputLimits(6, 1024, 3));

        ow.println("first");
        ow.println("second");
        ow.println("third");
        ow.println("fourth");
        assertEquals(5, ow.getLines().getLineCount());

        ow.println("fifth"); // sixth line, remove three lines
        assertEquals(3, ow.getLines().getLineCount());
        assertEquals("fourth\n", ow.getLines().getLine(0));
        assertEquals("fifth\n", ow.getLines().getLine(1));
        assertEquals("", ow.getLines().getLine(2));
    }

    /**
     * Test for bug 245125 - NetBeans UI stops responding, ended with black
     * window.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void testSynchronizationOfGetLineInfo()
            throws IOException, InterruptedException {

        final OutWriter ow = new OutWriter();
        ((AbstractLines) ow.getLines()).setOutputLimits(
                new OutputLimits(4, 1024, 2));

        Thread writingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    delay(1);
                    ow.println("test" + i, new OutputListener() {

                        @Override
                        public void outputLineSelected(OutputEvent ev) {
                        }

                        @Override
                        public void outputLineAction(OutputEvent ev) {
                        }

                        @Override
                        public void outputLineCleared(OutputEvent ev) {
                        }
                    });
                }
            }
        }, "writingThread");
        writingThread.start();
        for (int i = 0; i < 1000; i++) {
            LineInfo lineInfo = ow.getLines().getLineInfo(
                    ow.getLines().getLineCount() - 1);
            assertNotNull(lineInfo);
        }
        writingThread.join();
    }

    /**
     * Test for bug 232547 - There are LineInfo object even for lines that has
     * no special properties.
     */
    public void testNoExtraLineInfoObjectsByPrintln() {

        OutWriter ow = new OutWriter();
        ow.println("Months:\nJanuary\nFebruary\rMarch\r\nApril\n\rMay");
        assertNoLineInfo(ow);
    }

    /**
     * Test for bug 232547 - There are LineInfo object even for lines that has
     * no special properties.
     */
    public void testNoExtraLineInfoObjectsByPrint() {

        OutWriter ow = new OutWriter();
        ow.print("Months:\nJanuary\nFebruary\rMarch\r\nApril\n\rMay",
                null, false);
        assertNoLineInfo(ow);
    }

    /**
     * Test for bug 244645 - Npm install tab looks to be still active although
     * it's done.
     */
    public void testNotClosedAfterCreation() {
        OutWriter ow = new OutWriter();
        assertFalse(ow.isClosed());
        ow.dispose();
        assertTrue(ow.isClosed());
    }

    private void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Assert that no line in the OutWriter has assigned a LineInfo object. That
     * would be waste of memory if the lines have no special properties.
     */
    private void assertNoLineInfo(OutWriter ow) {
        AbstractLines lines = (AbstractLines) ow.getLines();
        for (int i = 0; i < lines.getLineCount(); i++) {
            assertNull("No LineInfo for basic line",
                    lines.getExistingLineInfo(i));
        }
    }

    void processEQ() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    System.currentTimeMillis();
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private class CL implements ChangeListener {
        
        public void assertChanged () {
            ChangeEvent oldCE = ce;
            ce = null;
            assertTrue ("No change happened", oldCE != null);
        }
        
        public void assertNoChange() {
            ChangeEvent oldCE = ce;
            ce = null;
            assertFalse ("Change happened", oldCE != null);
        }
        
        private ChangeEvent ce = null;
        public void stateChanged(ChangeEvent changeEvent) {
            ce = changeEvent;
        }
        
        void clear() {
            ce = null;
        }
    }
}
