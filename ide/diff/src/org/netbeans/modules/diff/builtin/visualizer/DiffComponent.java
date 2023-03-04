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

package org.netbeans.modules.diff.builtin.visualizer;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.BorderLayout;

import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.windows.*;

import org.netbeans.api.diff.Difference;
import org.netbeans.modules.diff.builtin.DiffPresenter;

//import org.netbeans.modules.vcscore.util.Debug;
//import org.netbeans.modules.vcscore.util.TopComponentCloseListener;

/**
 * This class displays two editor panes with two files and marks the differences
 * by a different color.
 *
 * @author  Martin Entlicher
 */
public class DiffComponent extends org.openide.windows.TopComponent {

    public static final java.awt.Color COLOR_MISSING = new java.awt.Color(255, 160, 180);
    public static final java.awt.Color COLOR_ADDED = new java.awt.Color(180, 255, 180);
    public static final java.awt.Color COLOR_CHANGED = new java.awt.Color(160, 200, 255);
    
    //private AbstractDiff diff = null;
    private Difference[] diffs = null;
    /** The shift of differences */
    private int[][] diffShifts;
    private DiffPanel diffPanel = null;
    
    private java.awt.Color colorMissing = COLOR_MISSING;
    private java.awt.Color colorAdded = COLOR_ADDED;
    private java.awt.Color colorChanged = COLOR_CHANGED;
    
    //private ArrayList closeListeners = new ArrayList();
    private int currentDiffLine = -1;
    
    /**
     * Used for deserialization.
     */
    private boolean diffSetSuccess = true;

    static final long serialVersionUID =3683458237532937983L;
    
    /**
     * An empty constructor needed by deserialization process.
     */
    public DiffComponent() {
        putClientProperty("PersistenceType", "Never");
    }
    
    /** Creates new DiffComponent from list of Difference objects */
    public DiffComponent(final Difference[] diffs, final String mainTitle, final String mimeType,
                         final String sourceName1, final String sourceName2,
                         final String title1, final String title2,
                         final Reader r1, final Reader r2) {
        this(diffs, mainTitle, mimeType, sourceName1, sourceName2, title1, title2,
             r1, r2, null);
    }
    
    /** Creates new DiffComponent from list of Difference objects */
    public DiffComponent(final Difference[] diffs, final String mainTitle, final String mimeType,
                         final String sourceName1, final String sourceName2,
                         final String title1, final String title2,
                         final Reader r1, final Reader r2, java.awt.Color[] colors) {
        this.diffs = diffs;
        diffShifts = new int[diffs.length][2];
        setLayout(new BorderLayout());
        diffPanel = new DiffPanel();
        putClientProperty(DiffPresenter.PROP_TOOLBAR, diffPanel.getClientProperty(DiffPresenter.PROP_TOOLBAR));
        diffPanel.addPrevLineButtonListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (diffs.length == 0) return ;
                currentDiffLine--;
                if (currentDiffLine < 0) currentDiffLine = diffs.length - 1;
                showCurrentLine();
            }
        });
        diffPanel.addNextLineButtonListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (diffs.length == 0) return ;
                currentDiffLine++;
                if (currentDiffLine >= diffs.length) currentDiffLine = 0;
                showCurrentLine();
            }
        });
        add(diffPanel, BorderLayout.CENTER);
        
        if (colors != null && colors.length >= 3) {
            colorMissing = colors[0];
            colorAdded = colors[1];
            colorChanged = colors[2];
        }
//        initComponents ();
        //setTitle(org.openide.util.NbBundle.getBundle(DiffComponent.class).getString("DiffComponent.title"));
        if (mainTitle == null) {
            setName(org.openide.util.NbBundle.getBundle(DiffComponent.class).getString("DiffComponent.title"));
        } else {
            setName(mainTitle);
        }
        setIcon(ImageUtilities.loadImage("org/netbeans/modules/diff/diffSettingsIcon.gif", true));
        initContent(mimeType, sourceName1, sourceName2, title1, title2, r1, r2);
        //HelpCtx.setHelpIDString (getRootPane (), DiffComponent.class.getName ());
        putClientProperty("PersistenceType", "Never");
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(DiffComponent.class);
    }



    private void showCurrentLine() {
        if (currentDiffLine >= diffs.length) return;
        Difference diff = diffs[currentDiffLine];
        int line = diff.getFirstStart() + diffShifts[currentDiffLine][0];
        if (diff.getType() == Difference.ADD) line++;
        int lf1 = diff.getFirstEnd() - diff.getFirstStart() + 1;
        int lf2 = diff.getSecondEnd() - diff.getSecondStart() + 1;
        int length = Math.max(lf1, lf2);
        diffPanel.setCurrentLine(line, length);
    }
    
    private void initContent(String mimeType, String sourceName1, String sourceName2,
                             String title1, String title2, Reader r1, Reader r2) {
        setMimeType1(mimeType);
        setMimeType2(mimeType);
        try {
            setSource1(r1);
            setSource2(r2);
        } catch (IOException ioex) {
            org.openide.ErrorManager.getDefault().notify(ioex);
        }
        setSource1Title(title1);
        setSource2Title(title2);
        insertEmptyLines(true);
        setDiffHighlight(true);
        insertEmptyLinesNotReported();
    }

    @Deprecated
    public void open(Workspace workspace) {
        super.open(workspace);
        diffPanel.open();
        if (currentDiffLine < 0) {
            currentDiffLine = 0;
            showCurrentLine();
        }
    }
    
    /**
     * Transfer the focus to the diff panel.
     */
    @Deprecated
    public void requestFocus() {
        super.requestFocus();
        diffPanel.requestFocus();
    }
    
    /**
     * Transfer the focus to the diff panel.
     */
    @Deprecated
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return diffPanel.requestFocusInWindow();
    }
    
    public void addNotify() {
        super.addNotify();
        if (currentDiffLine < 0) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    diffPanel.open();
                    javax.swing.SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            java.awt.Toolkit.getDefaultToolkit().sync();
                            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    if (currentDiffLine < 0) {
                                        currentDiffLine = 0;
                                        showCurrentLine();
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }
    }
    
    /**
     * Override for clean up reasons.
     * Will be moved to the appropriate method when will be made.
     */
    @Deprecated
    public boolean canClose(Workspace workspace, boolean last) {
        boolean can = super.canClose(workspace, last);
        if (last && can) {
            exitForm(null);
        }
        return can;
    }
    /*
    public void removeNotify() {
        System.out.println("removeNotify() called");
        exitForm(null);
        super.removeNotify();
    }
     */
    
    public void setSource1(Reader r) throws IOException {
        diffPanel.setSource1(r);
    }
    
    public void setSource2(Reader r) throws IOException {
        diffPanel.setSource2(r);
    }
    
    public void setSource1Title(String title) {
        diffPanel.setSource1Title(title);
    }
    
    public void setSource2Title(String title) {
        diffPanel.setSource2Title(title);
    }
    
    public void setMimeType1(String mime) {
        diffPanel.setMimeType1(mime);
    }
    
    public void setMimeType2(String mime) {
        diffPanel.setMimeType2(mime);
    }
    
    public void setDocument1(Document doc) {
        diffPanel.setDocument1(doc);
    }
    
    public void setDocument2(Document doc) {
        diffPanel.setDocument2(doc);
    }
    
    public void unhighlightAll() {
        diffPanel.unhighlightAll();
    }
    
    public void highlightRegion1(int line1, int line2, java.awt.Color color) {
        //D.deb("Highlight region 1"); // NOI18N
        diffPanel.highlightRegion1(line1, line2, color);
    }
    
    public void highlightRegion2(int line1, int line2, java.awt.Color color) {
        //D.deb("Highlight region 2"); // NOI18N
        diffPanel.highlightRegion2(line1, line2, color);
    }
    
    public void addEmptyLines1(int line, int numLines) {
        diffPanel.addEmptyLines1(line, numLines);
    }
    
    public void addEmptyLines2(int line, int numLines) {
        diffPanel.addEmptyLines2(line, numLines);
    }
        
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        //try {
        out.writeObject(diffs);
        /*
        } catch (IOException exc) {
            System.out.println("exc = "+exc);
            exc.printStackTrace();
            throw exc;
        }
         */
    }
    
    private void insertEmptyLines(boolean updateActionLines) {
        int n = diffs.length;
        //int ins1 = 0;
        //int ins2 = 0;
        //D.deb("insertEmptyLines():"); // NOI18N
        for(int i = 0; i < n; i++) {
            Difference action = diffs[i];
            int n1 = action.getFirstStart() + diffShifts[i][0];
            int n2 = action.getFirstEnd() + diffShifts[i][0];
            int n3 = action.getSecondStart() + diffShifts[i][1];
            int n4 = action.getSecondEnd() + diffShifts[i][1];
            //System.out.println("Action = "+action);
            //System.out.println("ins1 = "+diffShifts[i][0]+", ins2 = "+diffShifts[i][1]);
            if (updateActionLines && i < n - 1) {
                diffShifts[i + 1][0] = diffShifts[i][0];
                diffShifts[i + 1][1] = diffShifts[i][1];
            }
            switch (action.getType()) {
                case Difference.DELETE:
                    addEmptyLines2(n3, n2 - n1 + 1);
                    if (updateActionLines && i < n - 1) {
                        diffShifts[i+1][1] += n2 - n1 + 1;
                    }
                    //ins2 += n2 - n1 + 1;
                    break;
                case Difference.ADD:
                    addEmptyLines1(n1, n4 - n3 + 1);
                    if (updateActionLines && i < n - 1) {
                        diffShifts[i+1][0] += n4 - n3 + 1;
                    }
                    //ins1 += n4 - n3 + 1;
                    break;
                case Difference.CHANGE:
                    int r1 = n2 - n1;
                    int r2 = n4 - n3;
                    if (r1 < r2) {
                        addEmptyLines1(n2, r2 - r1);
                        if (updateActionLines && i < n - 1) {
                            diffShifts[i+1][0] += r2 - r1;
                        }
                        //ins1 += r2 - r1;
                    } else if (r1 > r2) {
                        addEmptyLines2(n4, r1 - r2);
                        if (updateActionLines && i < n - 1) {
                            diffShifts[i+1][1] += r1 - r2;
                        }
                        //ins2 += r1 - r2;
                    }
                    break;
            }
        }
    }
    
    private void setDiffHighlight(boolean set) {
        int n = diffs.length;
        //D.deb("Num Actions = "+n); // NOI18N
        for(int i = 0; i < n; i++) {
            Difference action = diffs[i];
            int n1 = action.getFirstStart() + diffShifts[i][0];
            int n2 = action.getFirstEnd() + diffShifts[i][0];
            int n3 = action.getSecondStart() + diffShifts[i][1];
            int n4 = action.getSecondEnd() + diffShifts[i][1];
            //D.deb("Action: "+action.getAction()+": ("+n1+","+n2+","+n3+","+n4+")"); // NOI18N
            switch (action.getType()) {
            case Difference.DELETE:
                if (set) highlightRegion1(n1, n2, colorMissing);
                else highlightRegion1(n1, n2, java.awt.Color.white);
                break;
            case Difference.ADD:
                if (set) highlightRegion2(n3, n4, colorAdded);
                else highlightRegion2(n3, n4, java.awt.Color.white);
                break;
            case Difference.CHANGE:
                if (set) {
                    highlightRegion1(n1, n2, colorChanged);
                    highlightRegion2(n3, n4, colorChanged);
                } else {
                    highlightRegion1(n1, n2, java.awt.Color.white);
                    highlightRegion2(n3, n4, java.awt.Color.white);
                }
                break;
            }
        }
    }
    
    /**
     * Read one line from the given text, from the given position. It returns
     * the end position of this line and the beginning of the next one.
     * @param begin Contains just one value - IN: the beginning of the line to read.
     *              OUT: the start of the next line.
     * @param end Contains just one value - OUT: the end of the line.
     * @param text The text to read.
     * @return The line.
     */
    private static String readLine(int[] begin, int[] end, String text) {
        int n = text.length();
        for (int i = begin[0]; i < n; i++) {
            char c = text.charAt(i);
            if (c == '\n' || c == '\r') {
                end[0] = i;
                break;
            }
        }
        if (end[0] < begin[0]) end[0] = n;
        String line = text.substring(begin[0], end[0]);
        begin[0] = end[0] + 1;
        if (begin[0] < n && text.charAt(end[0]) == '\r' && text.charAt(begin[0]) == '\n') begin[0]++;
        return line;
    }
    
    /**
     * Find the first diff, that is on or below the given line number.
     * @return The index of the desired difference in the supplied array or
     *         a value, that is bigger then the array size if such a diff does
     *         not exist.
     */
    private static int findDiffForLine(int lineNumber, int diffIndex, Difference[] diffs, int[][] diffShifts) {
        while (diffIndex < diffs.length) {
            if ((diffs[diffIndex].getFirstEnd() + diffShifts[diffIndex][0]) >= lineNumber ||
                (diffs[diffIndex].getSecondEnd() + diffShifts[diffIndex][1]) >= lineNumber) break;
            diffIndex++;
        }
        return diffIndex;
    }
    
    /**
     * Find out whether the line lies in the difference.
     * @param lineNumber The number of the line.
     * @param diff The difference
     * @param diffShifts The shifts of the difference in the current document
     * @return true if the line lies in the difference, false if does not.
     */
    private static boolean isLineInDiff(int lineNumber, Difference diff, int[] diffShifts) {
        int l1 = diff.getFirstStart() + diffShifts[0];
        int l2 = diff.getFirstEnd() + diffShifts[0];
        int l3 = diff.getSecondStart() + diffShifts[1];
        int l4 = diff.getSecondEnd() + diffShifts[1];
        return (l1 <= lineNumber && ((l2 >= l1) ? (l2 >= lineNumber) : false)) ||
               (l3 <= lineNumber && ((l4 >= l3) ? (l4 >= lineNumber) : false));
    }
    
    private static int numEmptyLines(int beginLine, String text, int endLine) {
        if (endLine >= 0 && endLine <= beginLine) return 0;
        int numLines = 0;
        int[] begin = { beginLine };
        int[] end = { 0 };
        do {
            String line = readLine(begin, end, text);
            if (line.trim().length() > 0) break;
            numLines++;
        } while ((endLine < 0 || beginLine + numLines < endLine) && begin[0] < text.length());
        return numLines;
    }
    
    /**
     * We have to keep the balance of lines from the first and the second document,
     * so that corresponding lines will have the same line number in the document.
     * Because some diff providers can be set not to report changes in empty lines,
     * we have to use heuristics to balance the corresponding lines manually
     * if they do not match.
     * <p>
     * This method goes through both documents and finds unreported differences
     * in empty lines. Whenever it encounters an empty line with a corresponding
     * non-empty line, which in not inside a difference (== unreported),
     * it checks whether the following lines match (because there can be unreported
     * difference in the amount of space rather than a missing or added line).
     * If following lines "match", then silently add an empty line to the other
     * document. This added line is not highlighted, since it was not reported
     * as a difference.
     */
    private void insertEmptyLinesNotReported() {
        String docText1 = diffPanel.getDocumentText1();
        String docText2 = diffPanel.getDocumentText2();
        int[] begin1 = { 0 };
        int[] end1 = { -1 };
        int[] begin2 = { 0 };
        int[] end2 = { -1 };
        int n1 = docText1.length();
        int n2 = docText2.length();
        int lineNumber = 1;
        int diffIndex = 0;
        do {
            int lastBegin1 = begin1[0];
            int lastBegin2 = begin2[0];
            String line1 = readLine(begin1, end1, docText1);
            String line2 = readLine(begin2, end2, docText2);
            if (line1.length() == 0 && line2.length() > 0) {
                //System.out.println("Detected empty line LEFT "+lineNumber);
                diffIndex = findDiffForLine(lineNumber, diffIndex, diffs, diffShifts);
                if (diffIndex >= diffs.length || !isLineInDiff(lineNumber, diffs[diffIndex], diffShifts[diffIndex])) {
                    boolean addMissingLine;
                    if (line2.trim().length() == 0) {
                        int emptyLines1 = numEmptyLines(begin1[0], docText1, (diffIndex < diffs.length) ? diffs[diffIndex].getFirstStart() : -1);
                        int emptyLines2 = numEmptyLines(begin2[0], docText2, (diffIndex < diffs.length) ? diffs[diffIndex].getSecondStart() : -1);
                        addMissingLine = emptyLines1 > emptyLines2;
                        //System.out.println("emptyLines1 = "+emptyLines1+", emptyLines2 = "+emptyLines2);
                    } else {
                        addMissingLine = true;
                    }
                    if (addMissingLine) {
                        addEmptyLines2(lineNumber - 1, 1);
                        //highlightRegion2(lineNumber, lineNumber, colorAdded);
                        shiftDiffs(false, lineNumber);
                        begin2[0] = lastBegin2;
                        end2[0] = lastBegin2 - 1;
                    }
                }
            } else if (line2.length() == 0 && line1.length() > 0) {
                //System.out.println("Detected empty line RIGHT "+lineNumber);
                diffIndex = findDiffForLine(lineNumber, diffIndex, diffs, diffShifts);
                if (diffIndex >= diffs.length || !isLineInDiff(lineNumber, diffs[diffIndex], diffShifts[diffIndex])) {
                    boolean addMissingLine;
                    if (line1.trim().length() == 0) {
                        int emptyLines1 = numEmptyLines(begin1[0], docText1, (diffIndex < diffs.length) ? diffs[diffIndex].getFirstStart() : -1);
                        int emptyLines2 = numEmptyLines(begin2[0], docText2, (diffIndex < diffs.length) ? diffs[diffIndex].getSecondStart() : -1);
                        addMissingLine = emptyLines2 > emptyLines1;
                        //System.out.println("emptyLines1 = "+emptyLines1+", emptyLines2 = "+emptyLines2);
                    } else {
                        addMissingLine = true;
                    }
                    if (addMissingLine) {
                        addEmptyLines1(lineNumber - 1, 1);
                        //highlightRegion1(lineNumber, lineNumber, colorMissing);
                        shiftDiffs(true, lineNumber);
                        begin1[0] = lastBegin1;
                        end1[0] = lastBegin1 - 1;
                    }
                }
            }
            lineNumber++;
        } while (begin1[0] < n1 && begin2[0] < n2);
    }
    
    /**
     * Shift the differences by one in the first or the second document from the given line.
     * @param inFirstDoc True to shift differences the first document, false for the second.
     * @param fromLine The starting line. Shift all differences after this line.
     */
    private void shiftDiffs(boolean inFirstDoc, int fromLine) {
        int n = diffs.length;
        for(int i = 0; i < n; i++) {
            Difference action = diffs[i];
            if (inFirstDoc) {
                if (action.getFirstStart() + diffShifts[i][0] >= fromLine) {
                    diffShifts[i][0]++;
                }
            } else {
                if (action.getSecondStart() + diffShifts[i][1] >= fromLine) {
                    diffShifts[i][1]++;
                }
            }
        }        
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        Object obj = in.readObject();
        diffs = (Difference[]) obj;
        diffPanel = new DiffPanel();
        //this.diffSetSuccess = diff.setDiffComponent(this);
    }
    
    private Object readResolve() throws ObjectStreamException {
        if (this.diffSetSuccess) return this;
        else return null;
    }
    
    /**
     * Disable serialization.
     */
    protected Object writeReplace () throws java.io.ObjectStreamException {
        exitForm(null);
        return null;
    }
    
       /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                diffPanel = null;
                diffs = null;
                removeAll();
            }
        });
        /*
        try {
            org.netbeans.editor.Settings.setValue(null, org.netbeans.editor.SettingsNames.LINE_NUMBER_VISIBLE, lineNumbersVisible);
        } catch (Throwable exc) {
            // editor module not found
        }
         */
        //System.out.println("exitForm() called.");
        //diff.closing();
        //close();
        //dispose ();
        /*
        for(Iterator it = closeListeners.iterator(); it.hasNext(); ) {
            ((TopComponentCloseListener) it.next()).closing();
        }
         */
 
    }

}
