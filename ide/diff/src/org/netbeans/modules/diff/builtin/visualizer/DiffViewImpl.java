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

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;
import javax.swing.text.*;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.modules.diff.DiffModuleConfig;

import org.openide.actions.CopyAction;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.ErrorManager;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.text.CloneableEditorSupport;

/**
 * Panel that shows differences between two files. The code here was originally distributed among DiffPanel and
 * DiffComponent classes.
 * 
 * @author Maros Sandor
 */
public class DiffViewImpl extends javax.swing.JPanel implements org.netbeans.api.diff.DiffView, javax.swing.event.CaretListener {

    private Difference[] diffs = null;
    
    /** The shift of differences */
    private int[][] diffShifts;
    
    private java.awt.Color colorMissing;
    private java.awt.Color colorAdded;
    private java.awt.Color colorChanged;
    
    private int currentDiffLine = -1;
    
    private int totalHeight = 0;
    private int totalLines = 0;

    private int horizontalScroll1ChangedValue = -1;
    private int horizontalScroll2ChangedValue = -1;
    
    private LinesComponent linesComp1;
    private LinesComponent linesComp2;

    private String source1;
    private String source2;
    
    private int onLayoutLine;
    private int onLayoutLength;

    public DiffViewImpl() {
    }

    private static void copyStreamsCloseAll(Writer out, Reader in) throws IOException {
        char[] buff = new char[4096];
        int n;
        while ((n = in.read(buff)) > 0) {
            out.write(buff, 0, n);
        }
        in.close();
        out.close();
    }
    
    public DiffViewImpl(StreamSource ss1, StreamSource ss2) throws IOException {
        colorMissing = DiffModuleConfig.getDefault().getDeletedColor();
        colorAdded = DiffModuleConfig.getDefault().getAddedColor(); 
        colorChanged = DiffModuleConfig.getDefault().getChangedColor();
        Reader r1 = ss1.createReader();
        Reader r2 = ss2.createReader();
        String title1 = ss1.getTitle();
        String title2 = ss2.getTitle();
        String mimeType1 = ss1.getMIMEType();
        String mimeType2 = ss2.getMIMEType();
        if (mimeType1 == null) mimeType1 = mimeType2;
        if (mimeType2 == null) mimeType2 = mimeType1;
        
        saveSources(r1, r2);
        initComponents ();
        aquaBackgroundWorkaround();
        
        setName(org.openide.util.NbBundle.getMessage(DiffViewImpl.class, "DiffComponent.title"));
        initActions();
        jSplitPane1.setResizeWeight(0.5);
        putClientProperty("PersistenceType", "Never");
        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DiffViewImpl.class, "ACS_DiffPanelA11yName"));  // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DiffViewImpl.class, "ACS_DiffPanelA11yDesc"));  // NOI18N
        jEditorPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DiffViewImpl.class, "ACS_EditorPane1A11yName"));  // NOI18N
        jEditorPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DiffViewImpl.class, "ACS_EditorPane1A11yDescr"));  // NOI18N
        jEditorPane2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DiffViewImpl.class, "ACS_EditorPane2A11yName"));  // NOI18N
        jEditorPane2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DiffViewImpl.class, "ACS_EditorPane2A11yDescr"));  // NOI18N

        if (source1 == null) jEditorPane1.setVisible(false);
        if (source2 == null) jEditorPane2.setVisible(false);

        if (r1 != null && r2 != null) {
            DiffProvider provider = DiffModuleConfig.getDefault().getDefaultDiffProvider();
            diffs = provider.computeDiff(new StringReader(source1), new StringReader(source2));
        } else {
            diffs = new Difference[0];
        }
        diffShifts = new int[diffs.length][2];

        setSource1Title(title1);
        setSource2Title(title2);
        
//        java.lang.AssertionError: BaseKit.install() incorrectly called from non-AWT thread.
//                at org.netbeans.editor.BaseKit.install(BaseKit.java:503)
//                at org.netbeans.modules.diff.builtin.visualizer.DiffViewImpl.setSource1(DiffViewImpl.java:642)
//                at org.netbeans.modules.diff.builtin.visualizer.DiffViewImpl.<init>(DiffViewImpl.java:173)
//                at org.netbeans.modules.diff.builtin.DefaultDiff.createDiff(DefaultDiff.java:112)
//                at org.netbeans.modules.versioning.system.cvss.ui.actions.diff.DiffMainPanel$DiffPrepareTask.run(DiffMainPanel.java:634)

        final String f1 = mimeType1;
        final String f2 = mimeType2;
        try {
            Runnable awtTask = new Runnable() {
                public void run() {
                    setMimeType1(f1);
                    setMimeType2(f2);
                    try {
                        if (source1 != null) setSource1(new StringReader(source1));
                        if (source2 != null) setSource2(new StringReader(source2));
                    } catch (IOException ioex) {
                        org.openide.ErrorManager.getDefault().notify(ioex);
                    }
                    insertEmptyLines(true);
                    setDiffHighlight(true);
                    insertEmptyLinesNotReported();

                    Color borderColor = UIManager.getColor("scrollpane_border");
                    if (borderColor == null) borderColor = UIManager.getColor("controlShadow");
                    jScrollPane1.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
                    jScrollPane2.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
                    jSplitPane1.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                awtTask.run();
            } else {
                 SwingUtilities.invokeAndWait(awtTask);
            }
        } catch (InterruptedException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.notify(e);
        } catch (InvocationTargetException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.notify(e);
        }

    }

    private void aquaBackgroundWorkaround() {
        if( "Aqua".equals( UIManager.getLookAndFeel().getID() ) ) {             // NOI18N
            Color color = UIManager.getColor("NbExplorerView.background");      // NOI18N
            setBackground(color); 
            filePanel1.setBackground(color); 
            filePanel2.setBackground(color); 
        }
    }
        
    private void saveSources(Reader r1, Reader r2) throws IOException {
        if (r1 != null) {
            StringWriter sw = new StringWriter();
            copyStreamsCloseAll(sw, r1);
            source1 = sw.toString();
        }
        if (r2 != null) {
            StringWriter sw = new StringWriter();
            copyStreamsCloseAll(sw, r2);
            source2 = sw.toString();
        }
    }

    public boolean requestFocusInWindow() {
        return jEditorPane1.requestFocusInWindow();
    }

    public Component getComponent() {
        return this;
    }

    public int getDifferenceCount() {
        return diffs.length;
    }

    public boolean canSetCurrentDifference() {
        return true;
    }

    public void setCurrentDifference(int diffNo) throws UnsupportedOperationException {


        if (diffNo < -1 || diffNo >= diffs.length) throw new IllegalArgumentException("Illegal difference number: " + diffNo);

        if (diffNo == -1) {
            if (linesComp1 != null) {
                this.linesComp1.setActiveLine(-1);
                linesComp1.repaint();
            }

            if (linesComp2 != null) {
                this.linesComp2.setActiveLine(-1);
                linesComp2.repaint();
            }
        } else {
            currentDiffLine = diffNo;
            showCurrentLine();
        }
    }

    public int getCurrentDifference() throws UnsupportedOperationException {
        int firstVisibleLine;
        int lastVisibleLine;
        int candidate = currentDiffLine;
        if (jViewport1 != null) {
            int viewHeight = jViewport1.getViewSize().height;
            java.awt.Point p1;
            initGlobalSizes(); // The window might be resized in the mean time.
            p1 = jViewport1.getViewPosition();
            int HALFLINE_CEILING = 2;  // compensation for rounding error and partially visible lines
            float firstPct = ((float)p1.y / (float)viewHeight);
            firstVisibleLine =  (int) (firstPct * totalLines) + HALFLINE_CEILING;
            float lastPct = ((float)(jViewport1.getHeight() + p1.y) / (float)viewHeight);
            lastVisibleLine = (int) (lastPct * totalLines) - HALFLINE_CEILING;

            for (int i = 0; i<diffs.length; i++) {
                int startLine = diffShifts[i][0] + diffs[i].getFirstStart();
                int endLine = diffShifts[i][0] + diffs[i].getFirstEnd();  // there no add changes in left pane  
                if (firstVisibleLine < startLine && startLine < lastVisibleLine
                || firstVisibleLine < endLine && endLine < lastVisibleLine) {
                    if (i == currentDiffLine) {
                        return currentDiffLine; // current is visible, eliminate hazards use it.
                    }
                    candidate = i;  // takes last visible, optimalized for Next>
                }
            }
        }

        return candidate;
    }

    public JToolBar getToolBar() {
        return null;
    }

    private void showCurrentLine() {
        Difference diff = diffs[currentDiffLine];
        int line = diff.getFirstStart() + diffShifts[currentDiffLine][0];
        if (diff.getType() == Difference.ADD) line++;
        int lf1 = diff.getFirstEnd() - diff.getFirstStart() + 1;
        int lf2 = diff.getSecondEnd() - diff.getSecondStart() + 1;
        int length = Math.max(lf1, lf2);
        setCurrentLine(line, length);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerSize(4);
        filePanel1.setLayout(new java.awt.GridBagLayout());

        jEditorPane1.addCaretListener(this);

        jScrollPane1.setViewportView(jEditorPane1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        filePanel1.add(jScrollPane1, gridBagConstraints);

        fileLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        fileLabel1.setLabelFor(jEditorPane1);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        filePanel1.add(fileLabel1, gridBagConstraints);

        jSplitPane1.setLeftComponent(filePanel1);

        filePanel2.setLayout(new java.awt.GridBagLayout());

        jEditorPane2.addCaretListener(this);

        jScrollPane2.setViewportView(jEditorPane2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        filePanel2.add(jScrollPane2, gridBagConstraints);

        fileLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        fileLabel2.setLabelFor(jEditorPane2);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        filePanel2.add(fileLabel2, gridBagConstraints);

        jSplitPane1.setRightComponent(filePanel2);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);

    }

    // Code for dispatching events from components to event handlers.

    public void caretUpdate(javax.swing.event.CaretEvent evt) {
        if (evt.getSource() == jEditorPane1) {
            DiffViewImpl.this.jEditorPane1CaretUpdate(evt);
        }
        else if (evt.getSource() == jEditorPane2) {
            DiffViewImpl.this.jEditorPane2CaretUpdate(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void jEditorPane1CaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jEditorPane1CaretUpdate
    // Add your handling code here:
    }//GEN-LAST:event_jEditorPane1CaretUpdate
    
    private void jEditorPane2CaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jEditorPane2CaretUpdate
    // Add your handling code here:
    }//GEN-LAST:event_jEditorPane2CaretUpdate
    
    public void setCurrentLine(int line, int diffLength) {
        if (line > 0) showLine(line, diffLength);
        onLayoutLine = line;
        onLayoutLength = diffLength;
    }

    private void initActions() {
        jEditorPane1.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                editorActivated(jEditorPane1);
            }
            public void focusLost(FocusEvent e) {
                editorDeactivated(jEditorPane1);
            }
        });
        jEditorPane2.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                editorActivated(jEditorPane2);
            }
            public void focusLost(FocusEvent e) {
                editorDeactivated(jEditorPane2);
            }
        });
    }

    public void addNotify() {
        super.addNotify();

        jEditorPane1.putClientProperty("HighlightsLayerExcludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$"); //NOI18N
        jEditorPane2.putClientProperty("HighlightsLayerExcludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$"); //NOI18N
        
        List<Action> actions = new ArrayList<Action>(2);
        actions.add(getActionMap().get("jumpNext"));  // NOI18N
        actions.add(getActionMap().get("jumpPrev"));  // NOI18N
        jEditorPane1.setPopupActions(actions);
        jEditorPane2.setPopupActions(actions);

        expandFolds();
        initGlobalSizes();
        addChangeListeners();
    }

    public void doLayout() {
        super.doLayout();
        setCurrentLine(onLayoutLine, onLayoutLength);
        onLayoutLine = 0;
    }

    private Hashtable<JEditorPane, Hashtable<Object, Action>> kitActions;
            /** Listener for copy action enabling  */
    private PropertyChangeListener copyL;
    private PropertyChangeListener copyP;
    
    private Action getAction (String s, JEditorPane editor) {
        if (kitActions == null) {
            kitActions = new Hashtable<JEditorPane, Hashtable<Object, Action>>();
        }
        Hashtable<Object, Action> actions = kitActions.get(editor);
        if (actions == null) {
            EditorKit kit = editor.getEditorKit();
            if (kit == null) {
                return null;
            }
            
            Action[] a = kit.getActions ();
            actions = new Hashtable<Object, Action>(a.length);
            int k = a.length;
            for (int i = 0; i < k; i++)
                actions.put (a[i].getValue (Action.NAME), a[i]);
            kitActions.put(editor, actions);
        }
        return actions.get (s);
    }
    
    private void editorActivated(final JEditorPane editor) {
        final Action copy = getAction (DefaultEditorKit.copyAction, editor);
        if (copy != null) {
            final CallbackSystemAction sysCopy
            = ((CallbackSystemAction) SystemAction.get (CopyAction.class));
            final ActionPerformer perf = new ActionPerformer () {
                public void performAction (SystemAction action) {
                    copy.actionPerformed (new ActionEvent (editor, 0, "")); // NOI18N
                }
            };
            sysCopy.setActionPerformer(copy.isEnabled() ? perf : null);
            PropertyChangeListener copyListener;
            copy.addPropertyChangeListener(copyListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("enabled".equals(evt.getPropertyName())) { // NOI18N
                        if (((Boolean)evt.getNewValue()).booleanValue()) {
                            sysCopy.setActionPerformer(perf);
                        } else if (sysCopy.getActionPerformer() == perf) {
                            sysCopy.setActionPerformer(null);
                        }
                    }
                }
            });
            if (editor.equals(jEditorPane1)) copyL = copyListener;
            else copyP = copyListener;
        }
    }
    
    private void editorDeactivated(JEditorPane editor) {
        Action copy = getAction (DefaultEditorKit.copyAction, editor);
        PropertyChangeListener copyListener;
        if (editor.equals(jEditorPane1)) copyListener = copyL;
        else copyListener = copyP;
        if (copy != null) {
            copy.removePropertyChangeListener(copyListener);
        }
    }

    private void expandFolds() {
        FoldHierarchy fh = FoldHierarchy.get(jEditorPane1);
        FoldUtilities.expandAll(fh);
        fh = FoldHierarchy.get(jEditorPane2);
        FoldUtilities.expandAll(fh);
    }

    private void initGlobalSizes() {
        StyledDocument doc1 = (StyledDocument) jEditorPane1.getDocument();
        StyledDocument doc2 = (StyledDocument) jEditorPane2.getDocument();
        int numLines1 = org.openide.text.NbDocument.findLineNumber(doc1, doc1.getEndPosition().getOffset());
        int numLines2 = org.openide.text.NbDocument.findLineNumber(doc2, doc2.getEndPosition().getOffset());
        int numLines = Math.max(numLines1, numLines2);
        if (numLines < 1) numLines = 1;
        this.totalLines = numLines;
        int totHeight = jEditorPane1.getSize().height;
        int value = jEditorPane2.getSize().height;
        if (value > totHeight) totHeight = value;
        this.totalHeight = totHeight;
    }

    private void showLine(int line, int diffLength) {
        this.linesComp1.setActiveLine(line);
        this.linesComp2.setActiveLine(line);
        linesComp2.repaint();
        linesComp1.repaint();
        int padding = 5;
        if (line <= 5) padding = line/2;
        int off1, off2;
        int ypos;
        int viewHeight = jViewport1.getExtentSize().height;
        java.awt.Point p1, p2;
        initGlobalSizes(); // The window might be resized in the mean time.
        p1 = jViewport1.getViewPosition();
        p2 = jViewport2.getViewPosition();
        ypos = (totalHeight*(line - padding - 1))/(totalLines + 1);

        try {
            off1 = org.openide.text.NbDocument.findLineOffset((StyledDocument) jEditorPane1.getDocument(), line - 1);
            off2 = org.openide.text.NbDocument.findLineOffset((StyledDocument) jEditorPane2.getDocument(), line - 1);

            jEditorPane1.setCaretPosition(off1);
            jEditorPane2.setCaretPosition(off2);
        } catch (IndexOutOfBoundsException ex) {
            ErrorManager.getDefault().notify(ex);
        }

        if (ypos < p1.y || ypos + ((diffLength + padding)*totalHeight)/totalLines > p1.y + viewHeight) {
            p1.y = ypos;
            jViewport1.setViewPosition(p1);  // joinScrollBar will move paired view
        }
    }

    private void joinScrollBars() {
        final JScrollBar scrollBarH1 = jScrollPane1.getHorizontalScrollBar();
        final JScrollBar scrollBarV1 = jScrollPane1.getVerticalScrollBar();
        final JScrollBar scrollBarH2 = jScrollPane2.getHorizontalScrollBar();
        final JScrollBar scrollBarV2 = jScrollPane2.getVerticalScrollBar();
        scrollBarV1.getModel().addChangeListener(new javax.swing.event.ChangeListener()  {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int value = scrollBarV1.getValue();
                int oldValue = scrollBarV2.getValue();
                if (oldValue != value) {
                    scrollBarV2.setValue(value);
                }
            }
        });
        // The vertical scroll bar must be there for mouse wheel to work correctly.
        // However it's not necessary to be seen (but must be visible so that the wheel will work).
        jScrollPane1.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollBarV2.getModel().addChangeListener(new javax.swing.event.ChangeListener()  {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int value = scrollBarV2.getValue();
                int oldValue = scrollBarV1.getValue();
                if (oldValue != value) {
                    scrollBarV1.setValue(value);
                }
            }
        });
        scrollBarH1.getModel().addChangeListener(new javax.swing.event.ChangeListener()  {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int value = scrollBarH1.getValue();
                if (value == horizontalScroll1ChangedValue) return;
                int max1 = scrollBarH1.getMaximum();
                int max2 = scrollBarH2.getMaximum();
                int ext1 = scrollBarH1.getModel().getExtent();
                int ext2 = scrollBarH2.getModel().getExtent();
                if (max1 == ext1) horizontalScroll2ChangedValue = 0;
                else horizontalScroll2ChangedValue = (value*(max2 - ext2))/(max1 - ext1);
                horizontalScroll1ChangedValue = -1;
                scrollBarH2.setValue(horizontalScroll2ChangedValue);
            }
        });
        scrollBarH2.getModel().addChangeListener(new javax.swing.event.ChangeListener()  {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int value = scrollBarH2.getValue();
                if (value == horizontalScroll2ChangedValue) return;
                int max1 = scrollBarH1.getMaximum();
                int max2 = scrollBarH2.getMaximum();
                int ext1 = scrollBarH1.getModel().getExtent();
                int ext2 = scrollBarH2.getModel().getExtent();
                if (max2 == ext2) horizontalScroll1ChangedValue = 0;
                else horizontalScroll1ChangedValue = (value*(max1 - ext1))/(max2 - ext2);
                horizontalScroll2ChangedValue = -1;
                scrollBarH1.setValue(horizontalScroll1ChangedValue);
            }
        });
        jSplitPane1.setDividerLocation(0.5);
    }
    
    private String strCharacters(char c, int num) {
        StringBuffer s = new StringBuffer();
        while(num-- > 0) {
            s.append(c);
        }
        return s.toString();
    }
    
    private void customizeEditor(JEditorPane editor) {
        StyledDocument doc;
        Document document = editor.getDocument();
        try {
            doc = (StyledDocument) editor.getDocument();
        } catch(ClassCastException e) {
            doc = new DefaultStyledDocument();
            try {
                doc.insertString(0, document.getText(0, document.getLength()), null);
            } catch (BadLocationException ble) {
                // leaving the document empty
            }
            editor.setDocument(doc);
        }
    }
    
    private void addChangeListeners() {
        jEditorPane1.addPropertyChangeListener("font", new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        initGlobalSizes();
                        linesComp1.repaint();
                    }
                });
            }
        });
        jEditorPane2.addPropertyChangeListener("font", new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        initGlobalSizes();
                        linesComp2.repaint();
                    }
                });
            }
        });
    }

    public void setSource1(Reader r) throws IOException {
        EditorKit kit = jEditorPane1.getEditorKit();
        if (kit == null) throw new IOException("Missing Editor Kit"); // NOI18N
        Document doc = kit.createDefaultDocument();
        if (!(doc instanceof StyledDocument)) {
            doc = new DefaultStyledDocument(new StyleContext());
            kit = new StyledEditorKit();
            jEditorPane1.setEditorKit(kit);
        }
        try {
            kit.read(r, doc, 0);
        } catch (javax.swing.text.BadLocationException e) {
            throw new IOException("Can not locate the beginning of the document."); // NOI18N
        } finally {
            r.close();
        }
        kit.install(jEditorPane1);
        jEditorPane1.setDocument(doc);
        jEditorPane1.setEditable(false);
        customizeEditor(jEditorPane1);
        linesComp1 = new LinesComponent(jEditorPane1);
        jScrollPane1.setRowHeaderView(linesComp1);
        jViewport1 = jScrollPane1.getViewport();
    }
    
    public void setSource2(Reader r) throws IOException {
        EditorKit kit = jEditorPane2.getEditorKit();
        if (kit == null) throw new IOException("Missing Editor Kit"); // NOI18N
        Document doc = kit.createDefaultDocument();
        if (!(doc instanceof StyledDocument)) {
            doc = new DefaultStyledDocument(new StyleContext());
            kit = new StyledEditorKit();
            jEditorPane2.setEditorKit(kit);
        }
        try {
            kit.read(r, doc, 0);
        } catch (javax.swing.text.BadLocationException e) {
            throw new IOException("Can not locate the beginning of the document."); // NOI18N
        } finally {
            r.close();
        }
        kit.install(jEditorPane2);
        jEditorPane2.setDocument(doc);
        jEditorPane2.setEditable(false);
        
        customizeEditor(jEditorPane2);
        linesComp2 = new LinesComponent(jEditorPane2);
        jScrollPane2.setRowHeaderView(linesComp2);
        jViewport2 = jScrollPane2.getViewport();
        // add scrollbar listeners..
        joinScrollBars();
    }
    
    public void setSource1Title(String title) {
        fileLabel1.setText(title);
        // Set the minimum size in 'x' direction to a low value, so that the splitter can be moved to corner locations
        fileLabel1.setMinimumSize(new Dimension(3, fileLabel1.getMinimumSize().height));
    }
    
    public void setSource2Title(String title) {
        fileLabel2.setText(title);
        // Set the minimum size in 'x' direction to a low value, so that the splitter can be moved to corner locations
        fileLabel2.setMinimumSize(new Dimension(3, fileLabel2.getMinimumSize().height));
    }
    
    public void setMimeType1(String mime) {
        EditorKit kit = CloneableEditorSupport.getEditorKit(mime);
        jEditorPane1.setEditorKit(kit);
    }
    
    public void setMimeType2(String mime) {
        EditorKit kit = CloneableEditorSupport.getEditorKit(mime);
        jEditorPane2.setEditorKit(kit);
    }
    
    public void setDocument1(Document doc) {
        if (doc != null) {
            jEditorPane1.setDocument(doc);
        }
    }
    
    public void setDocument2(Document doc) {
        if (doc != null) {
            jEditorPane2.setDocument(doc);
        }
    }
    
    String getDocumentText1() {
        return jEditorPane1.getText();
    }
    
    String getDocumentText2() {
        return jEditorPane2.getText();
    }
    
    private void setHighlight(StyledDocument doc, int line1, int line2, java.awt.Color color) {
        for(int line = line1-1; line < line2; line++) {
            if (line < 0) continue;
            int offset = org.openide.text.NbDocument.findLineOffset(doc, line);
            if (offset >= 0) {
                Style s = doc.getLogicalStyle(offset);
                if (s == null) {
                    s = doc.addStyle("diff-style("+color+"):1500", null); // NOI18N
                }
                s.addAttribute(StyleConstants.ColorConstants.Background, color);
                doc.setLogicalStyle(offset, s);
            }
        }
    }
    
    private void unhighlight(StyledDocument doc) {
        int endOffset = doc.getEndPosition().getOffset();
        int endLine = org.openide.text.NbDocument.findLineNumber(doc, endOffset);
        Style s = doc.addStyle("diff-style(white):1500", null); // NOI18N
        s.addAttribute(StyleConstants.ColorConstants.Background, java.awt.Color.white);
        for(int line = 0; line <= endLine; line++) {
            int offset = org.openide.text.NbDocument.findLineOffset(doc, line);
            doc.setLogicalStyle(offset, s);
        }
    }
    
    public void unhighlightAll() {
        unhighlight((StyledDocument) jEditorPane1.getDocument());
        unhighlight((StyledDocument) jEditorPane2.getDocument());
    }
    
    public void highlightRegion1(int line1, int line2, java.awt.Color color) {
        StyledDocument doc = (StyledDocument) jEditorPane1.getDocument();
        setHighlight(doc, line1, line2, color);
    }
    
    public void highlightRegion2(int line1, int line2, java.awt.Color color) {
        StyledDocument doc = (StyledDocument) jEditorPane2.getDocument();
        setHighlight(doc, line1, line2, color);
    }
    
    private void addEmptyLines(StyledDocument doc, int line, int numLines) {
        int lastOffset = doc.getEndPosition().getOffset();
        int totLines = org.openide.text.NbDocument.findLineNumber(doc, lastOffset);
        int offset = lastOffset-1;
        if (line <= totLines) {
            offset = org.openide.text.NbDocument.findLineOffset(doc, line);
        }
        String insStr = strCharacters('\n', numLines);
        try {
            doc.insertString(offset, insStr, null);
        } catch (BadLocationException e) {
            org.openide.ErrorManager.getDefault().notify(e);
        }
    }
    
    public void addEmptyLines1(int line, int numLines) {
        StyledDocument doc = (StyledDocument) jEditorPane1.getDocument();
        addEmptyLines(doc, line, numLines);
        linesComp1.addEmptyLines(line, numLines);
    }
    
    public void addEmptyLines2(int line, int numLines) {
        StyledDocument doc = (StyledDocument) jEditorPane2.getDocument();
        addEmptyLines(doc, line, numLines);
        linesComp2.addEmptyLines(line, numLines);
    }
    
    
    private javax.swing.JViewport jViewport1;
    private javax.swing.JViewport jViewport2;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JLabel fileLabel1 = new javax.swing.JLabel();
    final javax.swing.JLabel fileLabel2 = new javax.swing.JLabel();
    final javax.swing.JPanel filePanel1 = new javax.swing.JPanel();
    final javax.swing.JPanel filePanel2 = new javax.swing.JPanel();
    final org.netbeans.modules.diff.builtin.visualizer.DEditorPane jEditorPane1 = new DEditorPane();
    final org.netbeans.modules.diff.builtin.visualizer.DEditorPane jEditorPane2 = new DEditorPane();
    final javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
    final javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
    final javax.swing.JSplitPane jSplitPane1 = new javax.swing.JSplitPane();
    // End of variables declaration//GEN-END:variables

    
    
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
        String docText1 = getDocumentText1();
        String docText2 = getDocumentText2();
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
    
}
