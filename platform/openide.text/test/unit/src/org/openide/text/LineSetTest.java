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

package org.openide.text;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


/** Testing LineSet impl for CloneableEditorSupport.
 *
 * @author Jaroslav Tulach
 */
public class LineSetTest extends NbTestCase implements CloneableEditorSupport.Env {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    /** the support to work with */
    private CloneableEditorSupport support;
    /** the content of lookup of support */
    private InstanceContent ic;

    
    // Env variables
    private String content = "";
    private boolean valid = true;
    private boolean modified = false;
    private java.util.Date date = new java.util.Date ();
    private java.util.List<PropertyChangeListener> propL = new ArrayList<PropertyChangeListener>();
    private java.beans.VetoableChangeListener vetoL;

    
    public LineSetTest(java.lang.String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp () {
        ic = new InstanceContent ();
        support = new CES (this, new AbstractLookup (ic));
        ic.add (this);
    }

    public void testLineSetIsEmpty () throws Exception {
        support.openDocument ();
        
        Line.Set set = support.getLineSet();
        assertEquals ("empty means one line ;-)", 1, set.getLines().size ());
    }
    
    public void testLineSetContainsTwoLinesWhenOpen () throws Exception {
        content = "Ahoj\n";
        support.openDocument ();
        
        Line.Set set = support.getLineSet();
        assertEquals ("Two lines", 2, set.getLines().size ());
    }
    
    public void testLineNumberUpdatedWhenInsertedNewLine () throws Exception {
        content = "Ahoj\n";
        javax.swing.text.Document doc = support.openDocument ();

        Line.Set set = support.getLineSet ();
        java.util.List lines = set.getLines ();
        assertEquals ("Two lines", 2, lines.size ());
        
        Line line = set.getCurrent (1);
        assertEquals ("Line number is one", 1, line.getLineNumber ());
        assertGetOriginal ("Original number is of course 1", set, line, 1);
        
        doc.insertString (0, "New line\n", null);
        
        assertEquals ("Line number is now 2", 2, line.getLineNumber ());
        
        Line whereIsOriginalLineOne = set.getOriginal (1);
        assertEquals ("It is the same", line, whereIsOriginalLineOne);
        assertEquals ("Line number is two", 2, whereIsOriginalLineOne.getLineNumber ());
        
        support.saveDocument ();
        
        Line currentLineOne = support.getLineSet ().getOriginal (1);
        assertEquals ("Well its line number is 1", 1, currentLineOne.getLineNumber ());
        assertFalse ("And it is not the same", currentLineOne.equals (line));
        
        Line currentLineTwo = support.getLineSet ().getOriginal (2);
        assertEquals ("This is our original line", line, currentLineTwo);
        assertGetOriginal ("Original number of the line was 1", set, line, 1);
        
        assertEquals ("Original set still has two lines", 2, set.getLines ().size ());
        assertEquals ("Index of current line 1 is 0 in old set", 1, set.getLines ().indexOf (line));
        assertEquals ("even the line number is two", 2, line.getLineNumber ());
        java.util.List newLines = support.getLineSet ().getLines ();
        assertEquals ("and the index in new set is two as well", 2, newLines.indexOf (line));
        
    }
    
    public void testWhenInsertedNewLineAtTheLineItStaysTheSame () throws Exception {
        content = "Ahoj\n";
        javax.swing.text.Document doc = support.openDocument ();

        Line.Set set = support.getLineSet ();
        
        Line line = set.getOriginal (0);
        doc.insertString (0, "Hi\n", null);
        
        assertEquals ("First line still stays the first", 0, line.getLineNumber ());
    }

    public void testWhenInsertedNewLineAtTheLineItStaysTheSame2 () throws Exception {
        content = "Ahoj\nJak se mas?\nChlape?\n";
        javax.swing.text.Document doc = support.openDocument ();

        Line.Set set = support.getLineSet ();
        
        Line line = set.getOriginal (1);
        assertEquals("Text of second line", "Jak se mas?\n", line.getText ());
        assertEquals ("Line number is one", 1, line.getLineNumber());
        assertEquals ("New line at forth position", "\n", doc.getText (4, 1));
        doc.insertString (6, "Jarda\n", null);
        
        assertEquals ("Second line still stays the second", 1, line.getLineNumber ());
    }
    
    // Mila claims that this is better behaviour, I do not think so, 
    // but as it is the current one, I am at least going to test it
    public void testJoinedLinesWillNeverPart () throws Exception {
        content = "111\n2\n";
        javax.swing.text.Document doc = support.openDocument ();
        
        Line.Set set = support.getLineSet ();
        
        Line one = set.getOriginal (0);
        Line two = set.getOriginal (1);
        
        assertEquals ("New line after first line", "\n", doc.getText (3, 1));
        
        doc.remove (3, 1);
        
        assertEquals ("Joined", one, two);
        
        doc.insertString (2, "\n", null);
        
        assertEquals ("They will not part", one, two);
        
        assertEquals ("Line number is 0", 0, one.getLineNumber ());
    }
    
    public void testGetLinesIndexOfDoesNotCreateAllLines () throws Exception {
        content = "0\n1\n2\n3\n4\n";
        javax.swing.text.Document doc = support.openDocument ();
        
        Line.Set set = support.getLineSet ();
        Line two = set.getOriginal (2);
        
        assertEquals ("Line index is two", 2, set.getLines ().indexOf (two));
        assertNumberOfLines (1, set);
        
        assertEquals ("Really two", 2, new ArrayList<Line>(set.getLines ()).indexOf (two));
    }
    
    public void testLinesAreNonMutable () throws Exception {
        content = "0\n1\n2\n3\n4\n";
        javax.swing.text.Document doc = support.openDocument ();
        
        assertNonmutable (support.getLineSet ().getLines ());
    }

    public void testGetLinesIndexWorksAfterModifications () throws Exception {
        content = "0\n1\n2\n3\n4\n";
        javax.swing.text.Document doc = support.openDocument ();
        
        Line.Set set = support.getLineSet ();
        
        int offset = 4;
        assertEquals ("2 is on the second line", "2", doc.getText (4, 1));
        doc.insertString (offset, "x\n", null);
        assertEquals ("x\n is on the second line", "x\n", doc.getText (4, 2));
        
        
        Line two = set.getOriginal (2);
        assertEquals ("2\n is the line text", "2\n", two.getText ());
        
        assertEquals ("Line index is two", 2, set.getLines ().indexOf (two));
        assertNumberOfLines (1, set);
        
        assertEquals ("Really two", 2, new ArrayList<Line>(set.getLines ()).indexOf (two));
    }
    
    public void testWhatHappensWhenAskingForLineOutOfBounds () throws Exception {
        content = "0";
        javax.swing.text.Document doc = support.openDocument ();
        
        Line.Set set = support.getLineSet ();

        try {
            Line l = set.getCurrent (1);
            fail ("Should thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            // ok
        }
        
        try {
            Line n = set.getOriginal (1);
            fail ("Should thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            // ok
        }
        try {
            Line l = set.getCurrent (-1);
            fail ("Should thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            // ok
        }
        
        try {
            Line n = set.getOriginal (-1);
            fail ("Should thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            // ok
        }
        
        Line l = set.getCurrent (0);
        Line n = set.getOriginal (0);
        
        assertNotNull (l);
        assertNotNull (n);
        
        assertEquals ("Lines are the same", l, n);
        assertEquals ("Text is", "0", l.getText ());
    }

    
    public void testGetLinesIndexWorksForNewlyAddedLines () throws Exception {
        content = "0\n1\n2\n3\n4\n";
        javax.swing.text.Document doc = support.openDocument ();
        
        Line.Set set = support.getLineSet ();
        
        int offset = 4;
        assertEquals ("2 is on the second line", "2", doc.getText (4, 1));
        doc.insertString (offset, "x\n", null);
        assertEquals ("x\n is on the second line", "x\n", doc.getText (4, 2));
        
        
        Line two = set.getCurrent (2);
        assertEquals ("x\n is the line text", "x\n", two.getText ());
        
        assertEquals ("Line index is -1 as it is not present", -1, set.getLines ().indexOf (two));
        // two lines created as we need to verify that the current two line is not 
        // in the original set
        // of course that it can be one if somebody implements this query 
        // in better way
        assertNumberOfLines (2, set);
        assertEquals ("Query on set works and does not produce new lines", 2, set.getOriginalLineNumber (two));
        assertNumberOfLines (2, set);

        // now few additinal checks 
        assertGetOriginal ("However if one asks the line set it works", set, two, 2);
        assertEquals ("Really missing from the list", -1, new ArrayList<Line>(set.getLines ()).indexOf (two));
    }
    
    
    public void testGetOriginalForLineFromDifferentLineSet () throws Exception {
        content = "line 1\nline 2\n";
        javax.swing.text.Document doc = support.openDocument ();

        Line.Set lineSet1 = support.getLineSet ();
        Line line = lineSet1.getCurrent (1);
        assertEquals ("line number is 1", 1, line.getLineNumber ());
        assertGetOriginal ("original line number is 1", lineSet1, line, 1);
        doc.insertString (6, "\nnew line", null);

        assertEquals ("line number is 2", 2, line.getLineNumber ());
        assertGetOriginal ("original line number is 1", lineSet1, line, 1);

        support.saveDocument ();

        assertEquals ("line number is 2", 2, line.getLineNumber ());
        assertGetOriginal ("original line number is 1", lineSet1, line, 1);

        Line.Set lineSet2 = support.getLineSet ();
        assertEquals ("line number is 2", 2, line.getLineNumber ());
        assertGetOriginal ("original line number is 1", lineSet1, line, 1);
        assertGetOriginal ("original line number is 2", lineSet2, line, 2);
        doc.insertString (6, "\nnew line", null);
        assertEquals ("line number is 3", 3, line.getLineNumber ());
        assertGetOriginal ("original line number is 1", lineSet1, line, 1);
        assertGetOriginal ("original line number is 2", lineSet2, line, 2);
        support.saveDocument ();
        assertEquals ("line number is 3", 3, line.getLineNumber ());
        assertGetOriginal ("original line number is 1", lineSet1, line, 1);
        assertGetOriginal ("original line number is 2", lineSet2, line, 2);
    }
   
    
    public void testGetOriginalForLineFromDifferentLineSet2() throws Exception {
        content = "line 1\nline 2\n";
        javax.swing.text.Document doc =
        support.openDocument();
        
        Line.Set lineSet1 = support.getLineSet();
        
        doc.insertString(6, "\nnew line", null);
        support.saveDocument();
        Line.Set lineSet2 = support.getLineSet();
        
        Line line = lineSet2.getCurrent(2);
        
        assertEquals("line number is 2", 
            2,
            line.getLineNumber()
        );
        assertGetOriginal ("original line number is 1", lineSet1, line, 1);
        assertGetOriginal("original line number is 2", lineSet2, line, 2);
        
        doc.insertString(6, "\nnew line", null);
        
        assertEquals("line number is 3", 3, line.getLineNumber());
        assertGetOriginal("original line number is 1", lineSet1, line, 1);
        assertGetOriginal("original line number is 2", lineSet2, line, 2);
    }
    
    public void testThatNullIsNeverReturnedAsALine () throws Exception {
        content = "line 1\nline 2\n";
        javax.swing.text.Document doc =
        support.openDocument();
        
        Line.Set lineSet1 = support.getLineSet();

        support.close ();
        
        Line l = lineSet1.getCurrent (0);
        assertNotNull ("Some line needs to be returned", l);
    }

    public void testLineAndLinePartCannotGoOverTheEndOfLine () throws Exception {
        content = "line 1\nline 2\n";
        javax.swing.text.Document doc = support.openDocument();
        
        Line.Set lineSet1 = support.getLineSet();
        Line first = lineSet1.getCurrent (0);
        Line.Part part = first.createPart (0, 10);
        assertEquals ("Line text is the same as text of the part", first.getText (), part.getText ());
        
        doc.remove (2, 2);
        assertEquals ("Ends with \n", part.getText ().length () - 1, part.getText ().indexOf ("\n"));
        assertEquals ("Line as well", first.getText ().length () - 1, first.getText ().indexOf ("\n"));
        assertEquals ("Still the line text is the same as text of the part", first.getText (), part.getText ());
    }

    public void testLineAndLinePartDoesNotThrowISEIssue53504 () throws Exception {
        content = "line 1\nline 2\n";
        javax.swing.text.Document doc = support.openDocument();
        
        Line.Set lineSet1 = support.getLineSet();
        Line first = lineSet1.getCurrent (0);
        Line.Part part = first.createPart (0, 10);
        assertEquals ("Line text is the same as text of the part", first.getText (), part.getText ());
        
        doc.remove (2, doc.getLength () - 2);
        if (!"li".equals (part.getText ())) {
            fail ("part now contains just first two characters: " + part.getText ());
        }
        assertEquals ("Still the line text is the same as text of the part", first.getText (), part.getText ());
    }

    public void testCreatingLineLongerThanFileFailsIssue53504 () throws Exception {
        content = "line 1";
        javax.swing.text.Document doc = support.openDocument();
        
        Line.Set lineSet1 = support.getLineSet();
        Line first = lineSet1.getCurrent (0);
        Line.Part part = first.createPart (8, 10);
        
        assertNotNull ("Part is returned", part);
        assertEquals ("But it is of course empty", 0, part.getText ().length ());
    }
    
    public void testManyLineInstances() throws Exception {
        long tm = System.currentTimeMillis();
        int lineCount = 1000;
        StringBuilder contentBuilder = new StringBuilder(lineCount * 10);
        for (int i = 0; i < lineCount; i++) {
            contentBuilder.append("Line ").append(i).append("\n");
        }
        content = contentBuilder.toString();
        javax.swing.text.Document doc = support.openDocument();

        // Create and hold a line instance on each line
        List<Line> lines = new ArrayList<Line>();
        Line.Set lineSet = support.getLineSet();
        for (int i = 0; i < lineCount; i++) {
            lines.add(lineSet.getCurrent(i));
        }
        
//        System.err.println("testManyLineInstances lineCount=" + lineCount + ", elapsed time: " + (System.currentTimeMillis() - tm));
        
        // Test searching for the line instances in a backward direction
        for (int i = lineCount - 1; i >= 0; i--) {
            assertSame("Line instances differ at index=" + i, lines.get(i), lineSet.getCurrent(i));
        }
        
        // Original impl with weak hash map will roughly do N^2 line comparisons :-(
        //   Each comparison needs a binary search todetermine the line number.
        assertTrue("Line.equals() count=" + DocumentLine.dlEqualsCounter + " too high",
                DocumentLine.dlEqualsCounter < 5000);

        // Release several lines at the end of list to test the binary search skipping the empty slots
        for (int i = lineCount - 3; i < lineCount; i++) {
            lines.set(i, null);
        }
        System.gc();
        lineSet.getCurrent(lineCount - 1);
    }
    
    private void assertNumberOfLines (int cnt, Line.Set set) throws Exception {
        class MF implements org.netbeans.junit.MemoryFilter {
            private java.util.HashSet<Line> counted = new java.util.HashSet<Line>();
            public int cnt;
            public boolean reject(Object obj) {
                if (obj instanceof Line) {
                    Line l = (Line)obj;
                    if (counted.add (l)) {
                        if (l.getLookup ().lookup (LineSetTest.class) == LineSetTest.this) {
                            cnt++;
                        }
                    }
                }
                return false;
            }
        }
        
        MF mf = new MF ();
        
        // just travel thru the memory
        assertSize (
            "Just one line",
            java.util.Collections.singleton (set), 
            Integer.MAX_VALUE,
            mf
        );

        if (mf.cnt > cnt) {
            fail ("Only given number of instance of line created (" + cnt + ") but was: " + mf.cnt);
        }
    }

    private static void assertGetOriginal (String s, Line.Set set, Line line, int expected) {
        assertEquals (s + " - Overriden DocumentLine.Set.getOriginal as well", expected, set.getOriginalLineNumber (line));
        assertEquals (s + " - The default Line.Set.computeOriginal method works", expected, Line.Set.computeOriginal (set, line));
    }
    
    private static void assertNonmutable (java.util.List<? extends Line> l) throws Exception {
        /*
        try {
            l.add (new Object ());
            fail ("add should fail"); 
        } catch (java.lang.UnsupportedOperationException ex) {
            // ok
        }
        try {
            l.add (0, new Object ());
            fail ("add should fail"); 
        } catch (java.lang.UnsupportedOperationException ex) {
            // ok
        }
         */
        
        try {
            Line x = null;
            l.remove (x);
            fail ("remove should fail"); 
        } catch (java.lang.UnsupportedOperationException ex) {
            // ok
        }

        /*
        try {
            l.addAll (java.util.Collections.EMPTY_LIST);
            fail ("addAll should fail"); 
        } catch (java.lang.UnsupportedOperationException ex) {
            // ok
        }
        try {
            l.addAll (0, java.util.Collections.EMPTY_LIST);
            fail ("addAll should fail"); 
        } catch (java.lang.UnsupportedOperationException ex) {
            // ok
        }
         */
        try {
            l.removeAll (java.util.Collections.EMPTY_LIST);
            fail ("removeAll should fail"); 
        } catch (java.lang.UnsupportedOperationException ex) {
            // ok
        }
        
        try {
            l.retainAll (java.util.Collections.EMPTY_LIST);
            fail ("retainAll should fail"); 
        } catch (java.lang.UnsupportedOperationException ex) {
            // ok
        }
        try {
            l.set (0, null);
            fail ("set should fail"); 
        } catch (java.lang.UnsupportedOperationException ex) {
            // ok
        }
    }
    
    //
    // Implementation of the CloneableEditorSupport.Env
    //
    
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        propL.add (l);
    }    
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        propL.remove (l);
    }
    
    public synchronized void addVetoableChangeListener(java.beans.VetoableChangeListener l) {
        assertNull ("This is the first veto listener", vetoL);
        vetoL = l;
    }
    public void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {
        assertEquals ("Removing the right veto one", vetoL, l);
        vetoL = null;
    }
    
    public org.openide.windows.CloneableOpenSupport findCloneableOpenSupport() {
        return support;
    }
    
    public String getMimeType() {
        return "text/plain";
    }
    
    public java.util.Date getTime() {
        return date;
    }
    
    public java.io.InputStream inputStream() throws java.io.IOException {
        return new java.io.ByteArrayInputStream (content.getBytes ());
    }
    public java.io.OutputStream outputStream() throws java.io.IOException {
        class ContentStream extends java.io.ByteArrayOutputStream {
            @Override
            public void close () throws java.io.IOException {
                super.close ();
                content = new String (toByteArray ());
            }
        }
        
        return new ContentStream ();
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public boolean isModified() {
        return modified;
    }

    public void markModified() throws java.io.IOException {
        modified = true;
    }
    
    public void unmarkModified() {
        modified = false;
    }

    /** Implementation of the CES */
    private static final class CES extends CloneableEditorSupport {
        public CES (Env env, Lookup l) {
            super (env, l);
        }
        
        protected String messageName() {
            return "Name";
        }
        
        protected String messageOpened() {
            return "Opened";
        }
        
        protected String messageOpening() {
            return "Opening";
        }
        
        protected String messageSave() {
            return "Save";
        }
        
        protected String messageToolTip() {
            return "ToolTip";
        }
        
    }
}
