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
import java.awt.*;
//import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.modules.diff.builtin.DiffPresenter;

import org.openide.actions.CopyAction;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.ErrorManager;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.ImageUtilities;
//import org.openide.windows.Workspace;
//import org.openide.windows.Mode;

/**
 * This class displays two editor panes with two files and marks the differences
 * by a different color.
 * @author  Martin Entlicher
 */
public class DiffPanel extends javax.swing.JPanel implements javax.swing.event.CaretListener {

//    private AbstractDiff diff = null;
    private int totalHeight = 0;
    private int totalLines = 0;

    //private java.awt.Color numBackgroundColor = new java.awt.Color(224, 224, 224);
    //private java.awt.Color numForegroundColor = new java.awt.Color(128, 64, 64);

    private int horizontalScroll1ChangedValue = -1;
    private int horizontalScroll2ChangedValue = -1;
    
    private LinesComponent linesComp1;
    private LinesComponent linesComp2;

    static final long serialVersionUID =3683458237532937983L;

    /** Creates new DiffComponent from AbstractDiff object*/
    public DiffPanel() {
//        this.diff = diff;
        initComponents ();
        aquaBackgroundWorkaround();
        
        // my init components that radically modifies initComponents()
        // so all (including this toolbar) is clickable in form editor
        commandPanel.remove(prevButton);
        commandPanel.remove(nextButton);
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.TRAILING, 5, 0));
        toolbar.setBorder(BorderFactory.createEmptyBorder());
        toolbar.add(prevButton);
        toolbar.add(nextButton);
        remove(commandPanel);
        putClientProperty(DiffPresenter.PROP_TOOLBAR, toolbar);
        
        //setTitle(org.openide.util.NbBundle.getBundle(DiffComponent.class).getString("DiffComponent.title"));
        setName(org.openide.util.NbBundle.getMessage(DiffPanel.class, "DiffComponent.title"));
        //HelpCtx.setHelpIDString (getRootPane (), DiffComponent.class.getName ());
        initActions();
        jSplitPane1.setResizeWeight(0.5);
        putClientProperty("PersistenceType", "Never");
        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DiffPanel.class, "ACS_DiffPanelA11yName"));  // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DiffPanel.class, "ACS_DiffPanelA11yDesc"));  // NOI18N
        jEditorPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DiffPanel.class, "ACS_EditorPane1A11yName"));  // NOI18N
        jEditorPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DiffPanel.class, "ACS_EditorPane1A11yDescr"));  // NOI18N
        jEditorPane2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DiffPanel.class, "ACS_EditorPane2A11yName"));  // NOI18N
        jEditorPane2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DiffPanel.class, "ACS_EditorPane2A11yDescr"));  // NOI18N
    }

    private void aquaBackgroundWorkaround() {
        if( "Aqua".equals( UIManager.getLookAndFeel().getID() ) ) {             // NOI18N
            Color color = UIManager.getColor("NbExplorerView.background");      // NOI18N
            setBackground(color); 
            filePanel1.setBackground(color); 
            filePanel2.setBackground(color); 
        }
    }
        
    public void addNotify() {
        super.addNotify();

        jEditorPane1.putClientProperty("HighlightsLayerExcludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$"); //NOI18N
        jEditorPane2.putClientProperty("HighlightsLayerExcludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$"); //NOI18N
        
        JComponent parent = (JComponent) getParent();
        
        Action nextAction = new ButtonAction(nextButton);
        Action prevAction = new ButtonAction(prevButton);
        parent.getActionMap().put("jumpNext", nextAction);  // NOI18N
        parent.getActionMap().put("jumpPrev", prevAction); // NOI18N
    }

    /** Reverse mapping button => action */
    private static class ButtonAction extends AbstractAction {

        final JButton button;

        public ButtonAction(JButton button) {
            this.button = button;
        }

        public void actionPerformed(ActionEvent e) {
            button.doClick();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        setLayout(new java.awt.GridBagLayout());

        commandPanel.setLayout(new java.awt.GridBagLayout());

        prevButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/diff/builtin/visualizer/prev.gif", true));
        prevButton.setToolTipText(org.openide.util.NbBundle.getBundle(DiffPanel.class).getString("DiffComponent.prevButton.toolTipText"));
        prevButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        commandPanel.add(prevButton, gridBagConstraints);

        nextButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/diff/builtin/visualizer/next.gif", true));
        nextButton.setToolTipText(org.openide.util.NbBundle.getBundle(DiffPanel.class).getString("DiffComponent.nextButton.toolTipText"));
        nextButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        commandPanel.add(nextButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(commandPanel, gridBagConstraints);

        editorPanel.setLayout(new java.awt.GridBagLayout());

        editorPanel.setPreferredSize(new java.awt.Dimension(700, 600));
        jSplitPane1.setDividerSize(4);
        filePanel1.setLayout(new java.awt.GridBagLayout());

        jEditorPane1.addCaretListener(this);

        jScrollPane1.setViewportView(jEditorPane1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        filePanel1.add(jScrollPane1, gridBagConstraints);

        fileLabel1.setText("jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        filePanel1.add(fileLabel1, gridBagConstraints);

        jSplitPane1.setLeftComponent(filePanel1);

        filePanel2.setLayout(new java.awt.GridBagLayout());

        jEditorPane2.addCaretListener(this);

        jScrollPane2.setViewportView(jEditorPane2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        filePanel2.add(jScrollPane2, gridBagConstraints);

        fileLabel2.setText("jLabel2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        filePanel2.add(fileLabel2, gridBagConstraints);

        jSplitPane1.setRightComponent(filePanel2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        editorPanel.add(jSplitPane1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(editorPanel, gridBagConstraints);

    }

    // Code for dispatching events from components to event handlers.

    public void caretUpdate(javax.swing.event.CaretEvent evt) {
        if (evt.getSource() == jEditorPane1) {
            DiffPanel.this.jEditorPane1CaretUpdate(evt);
        }
        else if (evt.getSource() == jEditorPane2) {
            DiffPanel.this.jEditorPane2CaretUpdate(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

  private void jEditorPane1CaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jEditorPane1CaretUpdate
// Add your handling code here:
/*      int pos = evt.getDot();
      int line = org.openide.text.NbDocument.findLineNumber((StyledDocument) jEditorPane1.getDocument(), pos);
      StyledDocument linesDoc = (StyledDocument)jEditorPane1.getDocument();
      int numLines = org.openide.text.NbDocument.findLineNumber(linesDoc, linesDoc.getEndPosition().getOffset());
      if (line <= numLines) {
          jEditorPane1.setCaretPosition(org.openide.text.NbDocument.findLineOffset(linesDoc, line));
      }
 */
  }//GEN-LAST:event_jEditorPane1CaretUpdate

  private void jEditorPane2CaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jEditorPane2CaretUpdate
// Add your handling code here:
/*      int pos = evt.getDot();
      int line = org.openide.text.NbDocument.findLineNumber((StyledDocument) jEditorPane2.getDocument(), pos);
      StyledDocument linesDoc = (StyledDocument)jEditorPane2.getDocument();
      int numLines = org.openide.text.NbDocument.findLineNumber(linesDoc, linesDoc.getEndPosition().getOffset());
      if (line <= numLines) {
          jEditorPane2.setCaretPosition(org.openide.text.NbDocument.findLineOffset(linesDoc, line));
      }
 */
  }//GEN-LAST:event_jEditorPane2CaretUpdate

  public void setCurrentLine(int line, int diffLength) {
      if (line > 0) showLine(line, diffLength);
  }

  public void addPrevLineButtonListener(java.awt.event.ActionListener listener) {
      prevButton.addActionListener(listener);
  }
  public void addNextLineButtonListener(java.awt.event.ActionListener listener) {
      nextButton.addActionListener(listener);
  }
  
  /*
  public void goToNextLine(int line, int diffLength) {
      if (line > 0) showLine(line, diffLength);      
  }
   */
  
    private void jScrollBar1AdjustmentValueChanged (java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_jScrollBar1AdjustmentValueChanged
        // Add your handling code here:
    }//GEN-LAST:event_jScrollBar1AdjustmentValueChanged

    private void closeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        // Add your handling code here:
        exitForm(null);
    }//GEN-LAST:event_closeButtonActionPerformed

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
/*        try {
            org.netbeans.editor.Settings.setValue(null, org.netbeans.editor.SettingsNames.LINE_NUMBER_VISIBLE, lineNumbersVisible);
        } catch (Throwable exc) {
            // editor module not found
        }
        //System.out.println("exitForm() called.");
        //diff.closing();
        //close();
        //dispose ();
        for(Iterator it = closeListeners.iterator(); it.hasNext(); ) {
            ((TopComponentCloseListener) it.next()).closing();
        }
 */
    }//GEN-LAST:event_exitForm

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
            actions = new Hashtable<Object, Action> (a.length);
            int k = a.length;
            for (int i = 0; i < k; i++)
                actions.put (a[i].getValue (Action.NAME), a[i]);
            kitActions.put(editor, actions);
        }
        return actions.get (s);
    }
    
    private void editorActivated(final JEditorPane editor) {
        //System.out.println("editor("+editor+") activated.");
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
        //System.out.println("editorDeactivated ("+editor+")");
        Action copy = getAction (DefaultEditorKit.copyAction, editor);
        PropertyChangeListener copyListener;
        if (editor.equals(jEditorPane1)) copyListener = copyL;
        else copyListener = copyP;
        if (copy != null) {
            copy.removePropertyChangeListener(copyListener);
        }
    }
    

    public void open() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                jSplitPane1.setDividerLocation(0.5);
                openPostProcess();
            }
        });
    }

    protected void openPostProcess() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                expandFolds();
                initGlobalSizes();
                //showLine(1, 0);
                addChangeListeners();
/*                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        initGlobalSizes(); // do that again to be sure that components are initialized.
                        javax.swing.SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                syncFont(); // Components have to be fully initialized before font syncing
                                addChangeListeners();
                            }
                        });
                    }
                });
 */
            }
        });
    }
    
    private void expandFolds() {
        FoldHierarchy fh = FoldHierarchy.get(jEditorPane1);
        FoldUtilities.expandAll(fh);
        fh = FoldHierarchy.get(jEditorPane2);
        FoldUtilities.expandAll(fh);
    }

    /*
    public void removeNotify() {
        System.out.println("removeNotify() called");
        exitForm(null);
        super.removeNotify();
    }
     */
    private void initGlobalSizes() {
        StyledDocument doc1 = (StyledDocument) jEditorPane1.getDocument();
        StyledDocument doc2 = (StyledDocument) jEditorPane2.getDocument();
        int numLines1 = org.openide.text.NbDocument.findLineNumber(doc1, doc1.getEndPosition().getOffset());
        int numLines2 = org.openide.text.NbDocument.findLineNumber(doc2, doc2.getEndPosition().getOffset());
        int numLines = Math.max(numLines1, numLines2);
        if (numLines < 1) numLines = 1;
        this.totalLines = numLines;
        //        int totHeight = editorPanel1.getSize().height;
        int totHeight = jEditorPane1.getSize().height;
        //        int value = editorPanel2.getSize().height;
        int value = jEditorPane2.getSize().height;
        if (value > totHeight) totHeight = value;
        this.totalHeight = totHeight;
    }

    private void showLine(int line, int diffLength) {
        //System.out.println("showLine("+line+", "+diffLength+")");
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
//                    System.out.println("setting v2=" + value);
//                    Thread.dumpStack();
                }
            }
        });
        //jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        // The vertical scroll bar must be there for mouse wheel to work correctly.
        // However it's not necessary to be seen (but must be visible so that the wheel will work).
        jScrollPane1.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollBarV2.getModel().addChangeListener(new javax.swing.event.ChangeListener()  {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int value = scrollBarV2.getValue();
                int oldValue = scrollBarV1.getValue();
                if (oldValue != value) {
                    scrollBarV1.setValue(value);
//                    System.out.println("setting v1 to=" + value);
                }
            }
        });
        scrollBarH1.getModel().addChangeListener(new javax.swing.event.ChangeListener()  {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int value = scrollBarH1.getValue();
                //                System.out.println("stateChangedH1:value = "+value+", horizontalScroll1ChangedValue = "+horizontalScroll1ChangedValue);
                if (value == horizontalScroll1ChangedValue) return;
                int max1 = scrollBarH1.getMaximum();
                int max2 = scrollBarH2.getMaximum();
                int ext1 = scrollBarH1.getModel().getExtent();
                int ext2 = scrollBarH2.getModel().getExtent();
                if (max1 == ext1) horizontalScroll2ChangedValue = 0;
                else horizontalScroll2ChangedValue = (value*(max2 - ext2))/(max1 - ext1);
                horizontalScroll1ChangedValue = -1;
                //                System.out.println("H1 value = "+value+" => H2 value = "+horizontalScroll2ChangedValue+"\t\tmax1 = "+max1+", max2 = "+max2);
                scrollBarH2.setValue(horizontalScroll2ChangedValue);
            }
        });
        scrollBarH2.getModel().addChangeListener(new javax.swing.event.ChangeListener()  {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int value = scrollBarH2.getValue();
                //                System.out.println("stateChangedH2:value = "+value+", horizontalScroll2ChangedValue = "+horizontalScroll2ChangedValue);
                if (value == horizontalScroll2ChangedValue) return;
                int max1 = scrollBarH1.getMaximum();
                int max2 = scrollBarH2.getMaximum();
                int ext1 = scrollBarH1.getModel().getExtent();
                int ext2 = scrollBarH2.getModel().getExtent();
                if (max2 == ext2) horizontalScroll1ChangedValue = 0;
                else horizontalScroll1ChangedValue = (value*(max1 - ext1))/(max2 - ext2);
                horizontalScroll2ChangedValue = -1;
                //                System.out.println("H2 value = "+value+" => H1 value = "+horizontalScroll1ChangedValue+"\t\tmax1 = "+max1+", max2 = "+max2);
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
        EditorKit kit = editor.getEditorKit();
        /*
        try {
            org.netbeans.editor.Settings.setValue(null, org.netbeans.editor.SettingsNames.LINE_NUMBER_VISIBLE, Boolean.FALSE);
        } catch (Throwable exc) {
            // editor module not found
        }
         */
        StyledDocument doc;
        Document document = editor.getDocument();
/*        StyledDocument docLines = new DefaultStyledDocument();
        textLines.setStyledDocument(docLines);
 */
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
        int lastOffset = doc.getEndPosition().getOffset();
        int numLines = org.openide.text.NbDocument.findLineNumber(doc, lastOffset);
        int numLength = Integer.toString(numLines).length();
        //        textLines.setForeground(numForegroundColor);
        //        textLines.setBackground(numBackgroundColor);
        for (int line = 0; line <= numLines; line++) {
            int offset = org.openide.text.NbDocument.findLineOffset(doc, line);
            String lineStr = Integer.toString(line+1);
            if (lineStr.length() < numLength) lineStr = strCharacters(' ', numLength - lineStr.length()) + lineStr;
            //lineStr = " "+lineStr+" "; // NOI18N
/*            try {
                if (line < numLines) lineStr += "\n"; // NOI18N
                docLines.insertString(docLines.getLength(), lineStr, null);
            } catch (BadLocationException e) {
                E.deb("Internal ERROR: "+e.getMessage()); // NOI18N
            }
 */
        }
        //        joinScrollBars();
    }
    
/*    private void syncFont(JEditorPane editorPane, JTextPane numLineText) {
        //editorPane.getToolkit().sync();
        /*
        try {
            //System.out.println("editor size = "+editorPane.getSize()+", last pos = "+editorPane.modelToView(editorPane.getDocument().getEndPosition().getOffset()));
            java.awt.Rectangle viewRect = editorPane.modelToView(editorPane.getDocument().getEndPosition().getOffset());
            if (viewRect.y <= totalLines && viewRect.height == 1) return; // probably not complete or empty document
            editorPane.setSize(editorPane.getSize().width, viewRect.y + viewRect.height);
            totalHeight = viewRect.y + viewRect.height;
            //System.out.println("  => new editor size = "+editorPane.getSize());
        } catch (javax.swing.text.BadLocationException exc) {
            // ignored
        }
 */
/*        //editorPane.getToolkit().sync();
        java.awt.Font f = editorPane.getFont();
        //numLineText.setFont(f);
        float fontSize = f.getSize();
        java.awt.Font nlf = numLineText.getFont();
        StyledDocument doc = (StyledDocument) editorPane.getDocument();
        Element root = org.openide.text.NbDocument.findLineRootElement(doc);
        Element element = root.getElement(0);
        //System.out.println("fontSize = "+fontSize);
        javax.swing.text.View v = this.jEditorPane2.getUI().getRootView(this.jEditorPane2);
        if (v.getViewCount() == 1)
            v = v.getView (0);
        int rh = (int) v.getPreferredSpan (View.Y_AXIS);
        int lineHeight = rh/(org.openide.text.NbDocument.findLineNumber(doc, doc.getEndPosition().getOffset())+1);
        int spacingGap = 0;
        if (element != null) {
            javax.swing.text.ViewFactory viewFactory = editorPane.getEditorKit().getViewFactory();
            if (viewFactory != null) {
                javax.swing.text.View view = viewFactory.create(element);
                fontSize = view.getMaximumSpan(javax.swing.text.View.Y_AXIS);
            } else {
                //System.out.println("size = "+editorPane.getSize().height+", num lines = "+org.openide.text.NbDocument.findLineNumber(doc, doc.getEndPosition().getOffset()));
                //int lineHeight = editorPane.getSize().height/org.openide.text.NbDocument.findLineNumber(doc, doc.getEndPosition().getOffset());
                //System.out.println("lineHeight = "+lineHeight);
                int fmh;
                do {
                    java.awt.Font nlf1 = nlf.deriveFont(fontSize);
                    fmh = numLineText.getFontMetrics(nlf1).getHeight();
                    //System.out.println("fontSize = "+fontSize+" => fmh = "+fmh);
                    if (fmh < lineHeight) fontSize += 1;
                } while (fmh < lineHeight);
                int maxfmh = fmh;
                do {
                    java.awt.Font nlf1 = nlf.deriveFont(fontSize);
                    fmh = numLineText.getFontMetrics(nlf1).getHeight();
                    //System.out.println("fontSize = "+fontSize+" => fmh = "+fmh);
                    if (fmh > lineHeight) fontSize -= 1;
                } while (fmh > lineHeight && fontSize > 1);
                int minfmh = fmh;
                spacingGap = maxfmh - minfmh;
                //                if (minfmh != maxfmh && (lineHeight - minfmh)/(maxfmh - lineHeight) > 1) fontSize += 1;
            }
            //System.out.println("recalculated fontSize = "+fontSize);
        }
        nlf = nlf.deriveFont(fontSize);
        int nlfLineHeight = numLineText.getFontMetrics(nlf).getHeight();
        numLineText.setFont(nlf);
        int numLines = root.getElementCount();
        StyledDocument docNl = numLineText.getStyledDocument();
        Element rootNl = docNl.getDefaultRootElement();
        if (numLines < rootNl.getElementCount()) numLines = rootNl.getElementCount();
        //for(int i = 0; i < numLines; i++) {
        Element elementNl = rootNl;//.getElement(i);
        AttributeSet attrNl =  elementNl.getAttributes();
        Style s = docNl.getLogicalStyle(elementNl.getStartOffset());
        //System.out.println("style at "+elementNl.getStartOffset()+": "+s.getAttribute(StyleConstants.FontConstants.FontSize));
/*        StyleConstants.setLineSpacing(s, ((float) StyleConstants.getLineSpacing(s)lineHeight)/nlfLineHeight);
/*        StyleConstants.setFontSize(s, nlf.getSize());
        if (spacingGap > 0) {
            float currentGap = StyleConstants.getSpaceAbove(s);
            currentGap = currentGap + spacingGap;
            StyleConstants.setSpaceAbove(s, currentGap);
        }
        //StyleConstants.setSpaceAbove(s, lineHeight - nlf.getSize());
/*        docNl.setLogicalStyle(elementNl.getStartOffset(), s);
        //}
        numLineText.repaint();
    }
 */
    
    /**
     * Synchronize the font of line numbers with the editor's font.
     */
/*    private void syncFont() {
        initGlobalSizes();
//        syncFont(jEditorPane1, jTextPane1);
//        syncFont(jEditorPane2, jTextPane2);
        setScrollBarsIncrements();
    }
 */
    
    private void addChangeListeners() {
        jEditorPane1.addPropertyChangeListener("font", new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                //System.out.println("1:evt = "+evt+", Property NAME = "+evt.getPropertyName());
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
                //System.out.println("2:evt = "+evt+", Property NAME = "+evt.getPropertyName());
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
        //D.deb("setFile("+url+")"); // NOI18N
        //System.out.println("setFile1("+url+")");
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
        //jEditorPane1.setPage(url);
        jEditorPane1.setEditable(false);
        customizeEditor(jEditorPane1);
        linesComp1 = new LinesComponent(jEditorPane1);
        jScrollPane1.setRowHeaderView(linesComp1);
        jViewport1 = jScrollPane1.getViewport();
    }
    
    public void setSource2(Reader r) throws IOException {
        //D.deb("setFile("+url+")"); // NOI18N
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
        //jEditorPane2.setPage(url);
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
        //Document doc = jEditorPane1.getDocument();
        //if (!(doc instanceof StyledDocument)) jEditorPane1.setDocument(new DefaultStyledDocument());
    }
    
    public void setMimeType2(String mime) {
        EditorKit kit = CloneableEditorSupport.getEditorKit(mime);
        jEditorPane2.setEditorKit(kit);
        //Document doc = jEditorPane2.getDocument();
        //if (!(doc instanceof StyledDocument)) jEditorPane2.setDocument(new DefaultStyledDocument());
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
        //System.out.println("setHighlight(): <"+line1+", "+line2+">, color = "+color); // NOI18N
        //Style s = doc.addStyle("diff-style("+color+"):1500", null); // NOI18N
        //      SimpleAttributeSet attrSet = new SimpleAttributeSet();
        //      attrSet.addAttribute(StyleConstants.ColorConstants.Background, java.awt.Color.green);
        //s.addAttribute(StyleConstants.ColorConstants.Background, color);
        for(int line = line1-1; line < line2; line++) {
            if (line < 0) continue;
            try {
                int offset = org.openide.text.NbDocument.findLineOffset(doc, line);
                //System.out.println("setHighlight(): I got offset = "+offset); // NOI18N
                if (offset >= 0) {
                    Style s = doc.getLogicalStyle(offset);
                    if (s == null) {
                        //System.out.println("setHighlight(): logical style is NULL"); // NOI18N
                        s = doc.addStyle("diff-style("+color+"):1500", null); // NOI18N
                    }
                    s.addAttribute(StyleConstants.ColorConstants.Background, color);
                    doc.setLogicalStyle(offset, s);
                    //doc.setParagraphAttributes(offset, 1, s, false);
                }
            } catch (IndexOutOfBoundsException ex) {
                // diagnostics
                ErrorManager.getDefault().annotate(ex,  "#67631 reappreared. Please reopen with details.");   // NOI18N
                ErrorManager.getDefault().notify(ex);
            }
        }
        //doc.setParagraphAttributes(offset, 100, s, true);
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
        //int totLines = doc.getDefaultRootElement().getElementIndex(lastOffset);
        int offset = lastOffset-1;
        if (line <= totLines) {
            offset = org.openide.text.NbDocument.findLineOffset(doc, line);
            //offset = doc.getDefaultRootElement().getElement(line).getStartOffset();
        }
        //int endOffset = doc.getEndPosition().getOffset();
        //if (offset > endOffset) offset = endOffset;
        String insStr = strCharacters('\n', numLines);
        try {
            doc.insertString(offset, insStr, null);
        } catch (BadLocationException e) {
            org.openide.ErrorManager.getDefault().notify(e);
        }
        //initScrollBars();
    }
    
    public void addEmptyLines1(int line, int numLines) {
        StyledDocument doc = (StyledDocument) jEditorPane1.getDocument();
        //System.out.println("addEmptyLines1: line = "+line+", numLines = "+numLines); // NOI18N
        addEmptyLines(doc, line, numLines);
        linesComp1.addEmptyLines(line, numLines);
    }
    
    public void addEmptyLines2(int line, int numLines) {
        StyledDocument doc = (StyledDocument) jEditorPane2.getDocument();
        //System.out.println("addEmptyLines2: line = "+line+", numLines = "+numLines); // NOI18N
        addEmptyLines(doc, line, numLines);
        linesComp2.addEmptyLines(line, numLines);
    }
    
    
    private javax.swing.JViewport jViewport1;
    private javax.swing.JViewport jViewport2;
    //private javax.swing.JScrollBar jScrollBar1 = new javax.swing.JScrollBar();
    //private javax.swing.JScrollBar jScrollBar2 = new javax.swing.JScrollBar();
    //private javax.swing.JScrollBar jScrollBar3 = new javax.swing.JScrollBar();
    //private javax.swing.JEditorPane jEditorPane1 = new JEditorPane();
    //private javax.swing.JEditorPane jEditorPane2 = new JEditorPane();
    //    private Boolean lineNumbersVisible = Boolean.FALSE;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JPanel commandPanel = new javax.swing.JPanel();
    final javax.swing.JPanel editorPanel = new javax.swing.JPanel();
    final javax.swing.JLabel fileLabel1 = new javax.swing.JLabel();
    final javax.swing.JLabel fileLabel2 = new javax.swing.JLabel();
    final javax.swing.JPanel filePanel1 = new javax.swing.JPanel();
    final javax.swing.JPanel filePanel2 = new javax.swing.JPanel();
    final org.netbeans.modules.diff.builtin.visualizer.DEditorPane jEditorPane1 = new DEditorPane();
    final org.netbeans.modules.diff.builtin.visualizer.DEditorPane jEditorPane2 = new DEditorPane();
    final javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
    final javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
    final javax.swing.JSplitPane jSplitPane1 = new javax.swing.JSplitPane();
    final javax.swing.JButton nextButton = new javax.swing.JButton();
    final javax.swing.JButton prevButton = new javax.swing.JButton();
    // End of variables declaration//GEN-END:variables

    /* scroll pane with unvisible scroll bar
     * if necessary can be solved this way, however easier approach was chosen:
     * jScrollPane1.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
     * If the easier approach stop to work, this class can be used instead.
    private static final class JScrollPaneWithUnvisibleVerticalScrollBar extends JScrollPane {
        
        public JScrollBar createVerticalScrollBar() {
            return new UnvisibleScrollBar(JScrollBar.VERTICAL);
        }
        
        private final class UnvisibleScrollBar extends JScrollPane.ScrollBar {
            
            public UnvisibleScrollBar(int orientation) {
                super(orientation);
            }
            
            public boolean contains(int x, int y) {
                return false;
            }
            
            public Rectangle getBounds() {
                return new Rectangle(0, 0, 0, 0);
            }
            
            public Rectangle getBounds(Rectangle rv) {
                if (rv == null) rv = new Rectangle(0, 0, 0, 0);
                else rv.setBounds(0, 0, 0, 0);
                return rv;
            }
            
            public int getHeight() {
                return 0;
            }
            
            public int getWidth() {
                return 0;
            }
            
            public Dimension getSize() {
                return new Dimension(0, 0);
            }
            
            public Dimension getSize(Dimension rv) {
                if (rv == null) rv = new Dimension(0, 0);
                else rv.setSize(0, 0);
                return rv;
            }
            
            public Dimension getPreferredSize() {
                return new Dimension(0, 0);
            }
        }
    }
     */
    
}
