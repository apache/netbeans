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

package org.netbeans.modules.merge.builtin.visualizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.*;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.openide.actions.CopyAction;
import org.openide.actions.SaveAction;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.diff.builtin.visualizer.LinesComponent;
import org.openide.text.CloneableEditorSupport;

/**
 * This class displays two editor panes with two files and marks the differences
 * by a different color.
 * @author  Martin Entlicher
 */
public class MergePanel extends javax.swing.JPanel implements java.awt.event.ActionListener, javax.swing.event.CaretListener {
    
    public static final String ACTION_FIRST_CONFLICT = "firstConflict"; // NOI18N
    public static final String ACTION_LAST_CONFLICT = "lastConflict"; // NOI18N
    public static final String ACTION_PREVIOUS_CONFLICT = "previousConflict"; // NOI18N
    public static final String ACTION_NEXT_CONFLICT = "nextConflict"; // NOI18N
    public static final String ACTION_ACCEPT_RIGHT = "acceptRight"; // NOI18N
    public static final String ACTION_ACCEPT_LEFT_RIGHT = "acceptLeftRight"; // NOI18N
    //public static final String ACTION_ACCEPT_RIGHT_AND_NEXT = "acceptRightAndNext"; // NOI18N
    public static final String ACTION_ACCEPT_LEFT = "acceptLeft"; // NOI18N
    public static final String ACTION_ACCEPT_RIGHT_LEFT = "acceptRightLeft"; // NOI18N
    //public static final String ACTION_ACCEPT_LEFT_AND_NEXT = "acceptLeftAndNext"; // NOI18N
    
    public static final String PROP_CAN_BE_SAVED = "canBeSaved"; // NOI18N
    public static final String PROP_CAN_NOT_BE_SAVED = "canNotBeSaved"; // NOI18N
    
//    private AbstractDiff diff = null;
    private int totalHeight = 0;
    private int totalLines = 0;

    private int horizontalScroll1ChangedValue = -1;
    private int horizontalScroll2ChangedValue = -1;
    private int horizontalScroll3ChangedValue = -1;
    private int verticalScroll1ChangedValue = -1;
    private int verticalScroll3ChangedValue = -1;
    
    private LinesComponent linesComp1;
    private LinesComponent linesComp2;
    private LinesComponent linesComp3;
    
    /**
     * Line numbers in the result document. The indexes are "physical" document line numbers,
     * and values are "logical" document line numbers. If there is a space inserted (a conflict),
     * the corresponding document content is not defined and logical document line numbers
     * do not grow.
     * If the conflict starts from the beginning of the file, the logical line numbers are '0',
     * if the conflict is in the middle of the file, the logical line numbers are euqal to
     * the last logical line before this conflict.
     * The line numbers start from '1'.
     */
    private int[] resultLineNumbers;
    
    private int numConflicts;
    private int numUnresolvedConflicts;
    private int currentConflictPos;
    private final List<Integer> resolvedLeftConflictsLineNumbers = new ArrayList<Integer>();
    private final List<Integer> resolvedRightConflictsLineNumbers = new ArrayList<Integer>();
    private final List<Integer> resolvedLeftRightConflictsLineNumbers = new ArrayList<Integer>();
    private final List<Integer> resolvedRightLeftConflictsLineNumbers = new ArrayList<Integer>();

    private ArrayList<ActionListener> controlListeners = new ArrayList<ActionListener>();
    
    private SystemAction[] systemActions = new SystemAction[] { SaveAction.get(SaveAction.class),
                                                                null,
                                                                CloseMergeViewAction.get(CloseMergeViewAction.class) };

    static final long serialVersionUID =3683458237532937983L;
    private static final String PLAIN_TEXT_MIME = "text/plain";
    private Difference[] conflicts;

    /** Creates new DiffComponent from AbstractDiff object*/
    public MergePanel() {
//        this.diff = diff;
        initComponents ();
        // TODO Get icons for these buttons
        firstConflictButton.setVisible(false);
        lastConflictButton.setVisible(false);
        prevConflictButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/diff/builtin/visualizer/prev.gif", true));
        nextConflictButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/diff/builtin/visualizer/next.gif", true));
        //setTitle(org.openide.util.NbBundle.getBundle(DiffComponent.class).getString("DiffComponent.title"));
        //setName(org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.title"));
        //HelpCtx.setHelpIDString (getRootPane (), DiffComponent.class.getName ());
        initActions();
        diffSplitPane.setResizeWeight(0.5);
        mergeSplitPane.setResizeWeight(0.5);
        putClientProperty("PersistenceType", "Never");
        jEditorPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MergePanel.class, "ACS_EditorPane1A11yName"));  // NOI18N
        jEditorPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MergePanel.class, "ACS_EditorPane1A11yDescr"));  // NOI18N
        jEditorPane1.putClientProperty(MergeHighlightsLayerFactory.HIGHLITING_LAYER_ID, jEditorPane1);
        jEditorPane2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MergePanel.class, "ACS_EditorPane2A11yName"));  // NOI18N
        jEditorPane2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MergePanel.class, "ACS_EditorPane2A11yDescr"));  // NOI18N
        jEditorPane2.putClientProperty(MergeHighlightsLayerFactory.HIGHLITING_LAYER_ID, jEditorPane2);
        jEditorPane3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MergePanel.class, "ACS_EditorPane3A11yName"));  // NOI18N
        jEditorPane3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MergePanel.class, "ACS_EditorPane3A11yDescr"));  // NOI18N
        jEditorPane3.putClientProperty(MergeHighlightsLayerFactory.HIGHLITING_LAYER_ID, jEditorPane3);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        jEditorPane1.putClientProperty("HighlightsLayerExcludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$"); //NOI18N
        jEditorPane2.putClientProperty("HighlightsLayerExcludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$"); //NOI18N
        jEditorPane3.putClientProperty("HighlightsLayerExcludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$"); //NOI18N
        jEditorPane3.getDocument().addDocumentListener((MergePane)jEditorPane3);
    }

    @Override
    public void removeNotify() {
        jEditorPane3.getDocument().removeDocumentListener((MergePane)jEditorPane3);
        super.removeNotify();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        acceptLeftButton = new javax.swing.JButton();
        acceptLeftRightButton = new javax.swing.JButton();
        acceptAndNextLeftButton = new javax.swing.JButton();
        acceptRightButton = new javax.swing.JButton();
        acceptRightLeftButton = new javax.swing.JButton();
        acceptAndNextRightButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        commandPanel.setLayout(new java.awt.GridBagLayout());

        firstConflictButton.setPreferredSize(new java.awt.Dimension(24, 24));
        firstConflictButton.addActionListener(this);
        commandPanel.add(firstConflictButton, new java.awt.GridBagConstraints());

        prevConflictButton.setToolTipText(org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.prevButton.toolTipText")); // NOI18N
        prevConflictButton.setMargin(new java.awt.Insets(1, 1, 0, 1));
        prevConflictButton.setMaximumSize(new java.awt.Dimension(24, 24));
        prevConflictButton.setMinimumSize(new java.awt.Dimension(24, 24));
        prevConflictButton.setPreferredSize(new java.awt.Dimension(24, 24));
        prevConflictButton.addActionListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 1);
        commandPanel.add(prevConflictButton, gridBagConstraints);

        nextConflictButton.setToolTipText(org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.nextButton.toolTipText")); // NOI18N
        nextConflictButton.setMargin(new java.awt.Insets(1, 1, 0, 1));
        nextConflictButton.setMaximumSize(new java.awt.Dimension(24, 24));
        nextConflictButton.setMinimumSize(new java.awt.Dimension(24, 24));
        nextConflictButton.setPreferredSize(new java.awt.Dimension(24, 24));
        nextConflictButton.addActionListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 1);
        commandPanel.add(nextConflictButton, gridBagConstraints);

        lastConflictButton.setPreferredSize(new java.awt.Dimension(24, 24));
        lastConflictButton.addActionListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 2);
        commandPanel.add(lastConflictButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(statusLabel, "jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 1);
        commandPanel.add(statusLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(commandPanel, gridBagConstraints);

        editorPanel.setPreferredSize(new java.awt.Dimension(700, 600));
        editorPanel.setLayout(new java.awt.GridBagLayout());

        mergeSplitPane.setDividerSize(4);
        mergeSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        diffSplitPane.setDividerSize(4);

        filePanel1.setLayout(new java.awt.GridBagLayout());

        leftCommandPanel.setLayout(new javax.swing.BoxLayout(leftCommandPanel, javax.swing.BoxLayout.LINE_AXIS));

        org.openide.awt.Mnemonics.setLocalizedText(acceptLeftButton, org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.acceptLeftButton.text")); // NOI18N
        acceptLeftButton.setToolTipText(org.openide.util.NbBundle.getBundle(MergePanel.class).getString("ACS_MergePanel.acceptLeftButton.textA11yDesc")); // NOI18N
        acceptLeftButton.addActionListener(this);
        leftCommandPanel.add(acceptLeftButton);

        org.openide.awt.Mnemonics.setLocalizedText(acceptLeftRightButton, org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.acceptLeftRightButton.text")); // NOI18N
        acceptLeftRightButton.setToolTipText(org.openide.util.NbBundle.getBundle(MergePanel.class).getString("MergePanel.acceptLeftRightButton.TTtext")); // NOI18N
        acceptLeftRightButton.setActionCommand(org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.declineLeftButton.text")); // NOI18N
        acceptLeftRightButton.addActionListener(this);
        leftCommandPanel.add(acceptLeftRightButton);

        org.openide.awt.Mnemonics.setLocalizedText(acceptAndNextLeftButton, org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.acceptAndNextLeftButton")); // NOI18N
        acceptAndNextLeftButton.setToolTipText(org.openide.util.NbBundle.getBundle(MergePanel.class).getString("ACS_MergePanel.acceptAndNextLeftButtonA11yDesc")); // NOI18N
        acceptAndNextLeftButton.addActionListener(this);
        leftCommandPanel.add(acceptAndNextLeftButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        filePanel1.add(leftCommandPanel, gridBagConstraints);

        jEditorPane1.addCaretListener(this);
        jScrollPane1.setViewportView(jEditorPane1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        filePanel1.add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(fileLabel1, "jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        filePanel1.add(fileLabel1, gridBagConstraints);

        diffSplitPane.setLeftComponent(filePanel1);

        filePanel2.setLayout(new java.awt.GridBagLayout());

        rightCommandPanel.setLayout(new javax.swing.BoxLayout(rightCommandPanel, javax.swing.BoxLayout.LINE_AXIS));

        org.openide.awt.Mnemonics.setLocalizedText(acceptRightButton, org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.acceptRightButton.text")); // NOI18N
        acceptRightButton.setToolTipText(org.openide.util.NbBundle.getBundle(MergePanel.class).getString("ACS_MergePanel.acceptRightButton.textA11yDesc")); // NOI18N
        acceptRightButton.addActionListener(this);
        rightCommandPanel.add(acceptRightButton);

        org.openide.awt.Mnemonics.setLocalizedText(acceptRightLeftButton, org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.acceptRightLeftButton.text")); // NOI18N
        acceptRightLeftButton.setToolTipText(org.openide.util.NbBundle.getBundle(MergePanel.class).getString("MergePanel.acceptRightLeftButton.TTtext")); // NOI18N
        acceptRightLeftButton.setActionCommand(org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.declineLeftButton.text")); // NOI18N
        acceptRightLeftButton.addActionListener(this);
        rightCommandPanel.add(acceptRightLeftButton);

        org.openide.awt.Mnemonics.setLocalizedText(acceptAndNextRightButton, org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.acceptAndNextRightButton")); // NOI18N
        acceptAndNextRightButton.setToolTipText(org.openide.util.NbBundle.getBundle(MergePanel.class).getString("ACS_MergePanel.acceptAndNextRightButtonA11yDesc")); // NOI18N
        acceptAndNextRightButton.addActionListener(this);
        rightCommandPanel.add(acceptAndNextRightButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        filePanel2.add(rightCommandPanel, gridBagConstraints);

        jEditorPane2.addCaretListener(this);
        jScrollPane2.setViewportView(jEditorPane2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        filePanel2.add(jScrollPane2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(fileLabel2, "jLabel2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        filePanel2.add(fileLabel2, gridBagConstraints);

        diffSplitPane.setRightComponent(filePanel2);

        mergeSplitPane.setLeftComponent(diffSplitPane);

        resultPanel.setLayout(new java.awt.GridBagLayout());

        resultScrollPane.setViewportView(jEditorPane3);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        resultPanel.add(resultScrollPane, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(resultLabel, "jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        resultPanel.add(resultLabel, gridBagConstraints);

        mergeSplitPane.setRightComponent(resultPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        editorPanel.add(mergeSplitPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(editorPanel, gridBagConstraints);
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == firstConflictButton) {
            MergePanel.this.firstConflictButtonActionPerformed(evt);
        }
        else if (evt.getSource() == prevConflictButton) {
            MergePanel.this.prevConflictButtonActionPerformed(evt);
        }
        else if (evt.getSource() == nextConflictButton) {
            MergePanel.this.nextConflictButtonActionPerformed(evt);
        }
        else if (evt.getSource() == lastConflictButton) {
            MergePanel.this.lastConflictButtonActionPerformed(evt);
        }
        else if (evt.getSource() == acceptLeftButton) {
            MergePanel.this.acceptLeftButtonActionPerformed(evt);
        }
        else if (evt.getSource() == acceptLeftRightButton) {
            MergePanel.this.acceptLeftRightButtonActionPerformed(evt);
        }
        else if (evt.getSource() == acceptAndNextLeftButton) {
            MergePanel.this.acceptAndNextLeftButtonActionPerformed(evt);
        }
        else if (evt.getSource() == acceptRightButton) {
            MergePanel.this.acceptRightButtonActionPerformed(evt);
        }
        else if (evt.getSource() == acceptRightLeftButton) {
            MergePanel.this.acceptRightLeftButtonActionPerformed(evt);
        }
        else if (evt.getSource() == acceptAndNextRightButton) {
            MergePanel.this.acceptAndNextRightButtonActionPerformed(evt);
        }
    }

    public void caretUpdate(javax.swing.event.CaretEvent evt) {
        if (evt.getSource() == jEditorPane1) {
            MergePanel.this.jEditorPane1CaretUpdate(evt);
        }
        else if (evt.getSource() == jEditorPane2) {
            MergePanel.this.jEditorPane2CaretUpdate(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void firstConflictButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firstConflictButtonActionPerformed
        // Add your handling code here:
        fireControlActionCommand(ACTION_FIRST_CONFLICT);
    }//GEN-LAST:event_firstConflictButtonActionPerformed

    private void prevConflictButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevConflictButtonActionPerformed
        // Add your handling code here:
        fireControlActionCommand(ACTION_PREVIOUS_CONFLICT);
    }//GEN-LAST:event_prevConflictButtonActionPerformed

    private void nextConflictButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextConflictButtonActionPerformed
        // Add your handling code here:
        fireControlActionCommand(ACTION_NEXT_CONFLICT);
    }//GEN-LAST:event_nextConflictButtonActionPerformed

    private void lastConflictButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lastConflictButtonActionPerformed
        // Add your handling code here:
        fireControlActionCommand(ACTION_LAST_CONFLICT);
    }//GEN-LAST:event_lastConflictButtonActionPerformed

    private void acceptRightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptRightButtonActionPerformed
        // Add your handling code here:
        fireControlActionCommand(ACTION_ACCEPT_RIGHT);
    }//GEN-LAST:event_acceptRightButtonActionPerformed

    private void acceptAndNextRightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptAndNextRightButtonActionPerformed
        // Add your handling code here:
        fireControlActionCommand(ACTION_ACCEPT_RIGHT);
        fireControlActionCommand(ACTION_NEXT_CONFLICT);
    }//GEN-LAST:event_acceptAndNextRightButtonActionPerformed

    private void acceptAndNextLeftButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptAndNextLeftButtonActionPerformed
        // Add your handling code here:
        fireControlActionCommand(ACTION_ACCEPT_LEFT);
        fireControlActionCommand(ACTION_NEXT_CONFLICT);
    }//GEN-LAST:event_acceptAndNextLeftButtonActionPerformed

    private void acceptLeftButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptLeftButtonActionPerformed
        // Add your handling code here:
        fireControlActionCommand(ACTION_ACCEPT_LEFT);
    }//GEN-LAST:event_acceptLeftButtonActionPerformed

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

  public void setConflicts (Difference[] diffs) {
      this.conflicts = diffs;
      this.numConflicts = diffs.length;
      this.numUnresolvedConflicts = numConflicts;
  }
    
  public int getNumUnresolvedConflicts(){
      return numUnresolvedConflicts;
  }

  /**
   * Instruct view to move to given line. It actually
   * moves when AWT processes posted event.
   */
  public void setCurrentLine(final int line, final int diffLength, final int conflictPos,
                             final int resultLine) {
      if (line > 0) {
          SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                  showLine12(line, diffLength);
                  showLine3(resultLine, diffLength);
                  if (conflictPos >= 0) MergePanel.this.currentConflictPos = conflictPos;
                  updateStatusLine();
                  updateAcceptButtons(line);
              }
          });
      }
  }
  
  public void setNeedsSaveState(boolean needsSave) {
      firePropertyChange((needsSave) ? PROP_CAN_BE_SAVED : PROP_CAN_NOT_BE_SAVED, null, null);
  }
  
  public synchronized void addControlActionListener(ActionListener listener) {
      controlListeners.add(listener);
  }
  
  public synchronized void removeControlActionListener(ActionListener listener) {
      controlListeners.remove(listener);
  }
  
  private void updateStatusLine() {
      statusLabel.setText(org.openide.util.NbBundle.getMessage(MergePanel.class,
          "MergePanel.statusLine", Integer.toString(currentConflictPos + 1),
          Integer.toString(numConflicts), Integer.toString(numUnresolvedConflicts)));
  }
  
  private void updateAcceptButtons(int linePos) {
      Integer conflictPos = Integer.valueOf(linePos);
      boolean left = resolvedLeftConflictsLineNumbers.contains(conflictPos);
      boolean right = resolvedRightConflictsLineNumbers.contains(conflictPos);
      boolean leftRight = resolvedLeftRightConflictsLineNumbers.contains(conflictPos);
      boolean rightLeft = resolvedRightLeftConflictsLineNumbers.contains(conflictPos);
      acceptLeftButton.setEnabled(!left);
      acceptLeftRightButton.setEnabled(conflicts[currentConflictPos].getType() == Difference.CHANGE && !leftRight);
      acceptAndNextLeftButton.setEnabled(!left);
      acceptRightButton.setEnabled(!right);
      acceptRightLeftButton.setEnabled(conflicts[currentConflictPos].getType() == Difference.CHANGE && !rightLeft);
      acceptAndNextRightButton.setEnabled(!right);
  }
  
  private void fireControlActionCommand(String command) {
      ArrayList<ActionListener> listeners;
      synchronized (this) {
          listeners = new ArrayList<ActionListener>(controlListeners);
      }
      ActionEvent evt = new ActionEvent(this, 0, command);
      for (ActionListener l: listeners) {
          l.actionPerformed(evt);
      }
  }

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

    private void acceptLeftRightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptLeftRightButtonActionPerformed
        fireControlActionCommand(ACTION_ACCEPT_LEFT_RIGHT);
    }//GEN-LAST:event_acceptLeftRightButtonActionPerformed

    private void acceptRightLeftButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptRightLeftButtonActionPerformed
        fireControlActionCommand(ACTION_ACCEPT_RIGHT_LEFT);
    }//GEN-LAST:event_acceptRightLeftButtonActionPerformed

    public void setSystemActions(SystemAction[] actions) {
        this.systemActions = actions;
    }
    
    public SystemAction[] getSystemActions() {
        return systemActions;
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
        jEditorPane3.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                editorActivated(jEditorPane3);
            }
            public void focusLost(FocusEvent e) {
                editorDeactivated(jEditorPane3);
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
                diffSplitPane.setDividerLocation(0.5);
                mergeSplitPane.setDividerLocation(0.5);
                openPostProcess();
            }
        });
    }

    protected void openPostProcess() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
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

    private void showLine12(int line, int diffLength) {
        assert SwingUtilities.isEventDispatchThread();
        //System.out.println("showLine("+line+", "+diffLength+")");
        this.linesComp1.setActiveLine(line);
        this.linesComp2.setActiveLine(line);
        linesComp1.repaint();
        linesComp2.repaint();
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
        int viewSize = jViewport1.getViewRect().y;
        if (ypos < p1.y || ypos + ((diffLength + padding)*totalHeight)/totalLines > p1.y + viewHeight) {
            //System.out.println("resetting posision=" + ypos);
            p1.y = ypos;
            p2.y = ypos;
            setViewPosition(p1, p2);
        }
        try {
            off1 = org.openide.text.NbDocument.findLineOffset((StyledDocument) jEditorPane1.getDocument(), line);
            jEditorPane1.setCaretPosition(off1);
        } catch (IndexOutOfBoundsException ex) { }
        try {
            off2 = org.openide.text.NbDocument.findLineOffset((StyledDocument) jEditorPane2.getDocument(), line);
            jEditorPane2.setCaretPosition(off2);
        } catch (IndexOutOfBoundsException ex) { }
        //D.deb("off1 = "+off1+", off2 = "+off2+", totalHeight = "+totalHeight+", totalLines = "+totalLines+", ypos = "+ypos);
        //System.out.println("off1 = "+off1+", off2 = "+off2+", totalHeight = "+totalHeight+", totalLines = "+totalLines+", ypos = "+ypos);
    }
    
    private void showLine3(int line, int diffLength) {
        linesComp3.setActiveLine(line);
        linesComp3.repaint();
    }
    
    private void setViewPosition(java.awt.Point p1, java.awt.Point p2) {
        assert SwingUtilities.isEventDispatchThread();
        jViewport1.setViewPosition(p1);
        jViewport1.repaint(jViewport1.getViewRect());
        jViewport2.setViewPosition(p2);
        jViewport2.repaint(jViewport2.getViewRect());
    }
    
    private void joinScrollBars() {
        final JScrollBar scrollBarH1 = jScrollPane1.getHorizontalScrollBar();
        final JScrollBar scrollBarV1 = jScrollPane1.getVerticalScrollBar();
        final JScrollBar scrollBarH2 = jScrollPane2.getHorizontalScrollBar();
        final JScrollBar scrollBarV2 = jScrollPane2.getVerticalScrollBar();
        final JScrollBar scrollBarH3 = resultScrollPane.getHorizontalScrollBar();
        final JScrollBar scrollBarV3 = resultScrollPane.getVerticalScrollBar();
        scrollBarV1.getModel().addChangeListener(new javax.swing.event.ChangeListener()  {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int value = scrollBarV1.getValue();
                int oldValue = scrollBarV2.getValue();
                if (oldValue != value) {
                    scrollBarV2.setValue(value);
//                    System.out.println("setting v2=" + value);
//                    Thread.dumpStack();
                }
                // TODO use a better algorithm to adjust scrollbars, if there are large changes, this will not work optimally.
                if (value == verticalScroll1ChangedValue) return ;
                int max1 = scrollBarV1.getMaximum();
                int max2 = scrollBarV3.getMaximum();
                int ext1 = scrollBarV1.getModel().getExtent();
                int ext2 = scrollBarV3.getModel().getExtent();
                if (max1 == ext1) verticalScroll3ChangedValue = 0;
                else verticalScroll3ChangedValue = (value*(max2 - ext2))/(max1 - ext1);
                verticalScroll1ChangedValue = -1;
                scrollBarV3.setValue(verticalScroll3ChangedValue);
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
        /* don't not let the result source vertical scrolling to influence the diff panels.
        scrollBarV3.getModel().addChangeListener(new javax.swing.event.ChangeListener()  {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int value = scrollBarV3.getValue();
                if (value == verticalScroll3ChangedValue) return ;
                int max1 = scrollBarV3.getMaximum();
                int max2 = scrollBarV1.getMaximum();
                int ext1 = scrollBarV3.getModel().getExtent();
                int ext2 = scrollBarV1.getModel().getExtent();
                if (max1 == ext1) verticalScroll1ChangedValue = 0;
                else verticalScroll1ChangedValue = (value*(max2 - ext2))/(max1 - ext1);
                verticalScroll3ChangedValue = -1;
                scrollBarV1.setValue(verticalScroll1ChangedValue);
            }
        });
         */
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
                int max3 = scrollBarH3.getMaximum();
                int ext1 = scrollBarH1.getModel().getExtent();
                int ext2 = scrollBarH2.getModel().getExtent();
                int ext3 = scrollBarH3.getModel().getExtent();
                if (max2 == ext2) {
                    horizontalScroll1ChangedValue = 0;
                    horizontalScroll3ChangedValue = 0;
                } else {
                    horizontalScroll1ChangedValue = (value*(max1 - ext1))/(max2 - ext2);
                    horizontalScroll3ChangedValue = (value*(max3 - ext3))/(max2 - ext2);
                }
                horizontalScroll2ChangedValue = -1;
                //                System.out.println("H2 value = "+value+" => H1 value = "+horizontalScroll1ChangedValue+"\t\tmax1 = "+max1+", max2 = "+max2);
                scrollBarH1.setValue(horizontalScroll1ChangedValue);
                scrollBarH3.setValue(horizontalScroll3ChangedValue);
            }
        });
        scrollBarH3.getModel().addChangeListener(new javax.swing.event.ChangeListener()  {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int value = scrollBarH3.getValue();
                //                System.out.println("stateChangedH1:value = "+value+", horizontalScroll1ChangedValue = "+horizontalScroll1ChangedValue);
                if (value == horizontalScroll3ChangedValue) return;
                int max1 = scrollBarH1.getMaximum();
                int max2 = scrollBarH2.getMaximum();
                int max3 = scrollBarH3.getMaximum();
                int ext1 = scrollBarH1.getModel().getExtent();
                int ext2 = scrollBarH2.getModel().getExtent();
                int ext3 = scrollBarH3.getModel().getExtent();
                if (max3 == ext3) {
                    horizontalScroll1ChangedValue = 0;
                    horizontalScroll2ChangedValue = 0;
                } else {
                    horizontalScroll1ChangedValue = (value*(max1 - ext1))/(max3 - ext3);
                    horizontalScroll2ChangedValue = (value*(max2 - ext2))/(max3 - ext3);
                }
                horizontalScroll3ChangedValue = -1;
                //                System.out.println("H1 value = "+value+" => H2 value = "+horizontalScroll2ChangedValue+"\t\tmax1 = "+max1+", max2 = "+max2);
                scrollBarH1.setValue(horizontalScroll1ChangedValue);
                scrollBarH2.setValue(horizontalScroll2ChangedValue);
            }
        });
        diffSplitPane.setDividerLocation(0.5);
        mergeSplitPane.setDividerLocation(0.5);
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
        //int lastOffset = doc.getEndPosition().getOffset();
        //int numLines = org.openide.text.NbDocument.findLineNumber(doc, lastOffset);
        //int numLength = Integer.toString(numLines).length();
        //        textLines.setForeground(numForegroundColor);
        //        textLines.setBackground(numBackgroundColor);
        /*
        for (int line = 0; line <= numLines; line++) {
            int offset = org.openide.text.NbDocument.findLineOffset(doc, line);
            String lineStr = Integer.toString(line+1);
            if (lineStr.length() < numLength) lineStr = strCharacters(' ', numLength - lineStr.length()) + lineStr;
            //lineStr = " "+lineStr+" "; // NOI18N
            try {
                if (line < numLines) lineStr += "\n"; // NOI18N
                docLines.insertString(docLines.getLength(), lineStr, null);
            } catch (BadLocationException e) {
                E.deb("Internal ERROR: "+e.getMessage()); // NOI18N
            }
        }
         */
        //        joinScrollBars();
    }
    
    private void addChangeListeners() {
        jEditorPane1.addPropertyChangeListener("font", new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                //System.out.println("1:evt = "+evt+", Property NAME = "+evt.getPropertyName());
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        initGlobalSizes();
                        linesComp1.changedAll();
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
                        linesComp2.changedAll();
                    }
                });
            }
        });
        jEditorPane3.addPropertyChangeListener("font", new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                //System.out.println("2:evt = "+evt+", Property NAME = "+evt.getPropertyName());
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        initGlobalSizes();
                        linesComp3.changedAll();
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
        jEditorPane1.putClientProperty(SimpleValueNames.CODE_FOLDING_ENABLE, false); //NOI18N
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
        jEditorPane2.putClientProperty(SimpleValueNames.CODE_FOLDING_ENABLE, false); //NOI18N
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
    
    public void setResultSource(Reader r) throws IOException {
        EditorKit kit = jEditorPane3.getEditorKit();
        if (kit == null) throw new IOException("Missing Editor Kit"); // NOI18N
        Document doc = kit.createDefaultDocument();
        if (!(doc instanceof StyledDocument)) {
            doc = new DefaultStyledDocument(new StyleContext());
            kit = new StyledEditorKit();
            jEditorPane3.setEditorKit(kit);
        }
        try {
            kit.read(r, doc, 0);
        } catch (javax.swing.text.BadLocationException e) {
            throw new IOException("Can not locate the beginning of the document."); // NOI18N
        } finally {
            r.close();
        }
        kit.install(jEditorPane3);
        jEditorPane3.putClientProperty(SimpleValueNames.CODE_FOLDING_ENABLE, false); //NOI18N
        jEditorPane3.setDocument(doc);
        //jEditorPane2.setPage(url);
        jEditorPane3.setEditable(false);
        customizeEditor(jEditorPane3);
        linesComp3 = new LinesComponent(jEditorPane3);
        resultScrollPane.setRowHeaderView(linesComp3);
        resultLineNumbers = new int[1];
        assureResultLineNumbersLength(
            org.openide.text.NbDocument.findLineNumber((StyledDocument) doc,
                                                       doc.getEndPosition().getOffset()) + 1);
        for (int i = 0; i < resultLineNumbers.length; i++) resultLineNumbers[i] = i;
    }
    
    private static final int EXTRA_CAPACITY = 5;
    private void assureResultLineNumbersLength(int length) {
        if (length > resultLineNumbers.length) {
            int[] newrln = new int[length + EXTRA_CAPACITY];
            System.arraycopy(resultLineNumbers, 0, newrln, 0, resultLineNumbers.length);
            resultLineNumbers = newrln;
        }
    }
    
    /**
     * Copy a part of first document into the result document.
     * @param line1 The starting line in the first source
     * @param line2 The ending line in the first source or <code>null</code>
     *              when the part ends at the end of the document
     * @param line3 The starting line in the result
     */
    public void copySource1ToResult(int line1, int line2, int line3) {
        StyledDocument doc1 = (StyledDocument) jEditorPane1.getDocument();
        StyledDocument doc2 = (StyledDocument) jEditorPane3.getDocument();
        try {
            copy(doc1, line1, line2, doc2, line3);
        } catch (BadLocationException e) {
            org.openide.ErrorManager.getDefault().notify(e);
        }
    }
    
    /**
     * Copy a part of second document into the result document.
     * @param line1 The starting line in the second source
     * @param line2 The ending line in the second source or <code>null</code>
     *              when the part ends at the end of the document
     * @param line3 The starting line in the result
     */
    public void copySource2ToResult(int line1, int line2, int line3) {
        StyledDocument doc1 = (StyledDocument) jEditorPane2.getDocument();
        StyledDocument doc2 = (StyledDocument) jEditorPane3.getDocument();
        try {
            copy(doc1, line1, line2, doc2, line3);
        } catch (BadLocationException e) {
            org.openide.ErrorManager.getDefault().notify(e);
        }
    }
    
    /** Copies a part of one document into another. */
    private void copy(StyledDocument doc1, int line1, int line2, StyledDocument doc2, int line3) throws BadLocationException {
        int offset1 = org.openide.text.NbDocument.findLineOffset(doc1, line1 - 1);
        int offset2 = (line2 >= 0) ? org.openide.text.NbDocument.findLineOffset(doc1, line2)
                                   : (doc1.getLength() - 1);
        if (offset1 >= offset2) return ;
        int offset3 = org.openide.text.NbDocument.findLineOffset(doc2, line3-1);
        int length = offset2 - offset1;
        if (line2 < 0) length++;
        String text = doc1.getText(offset1, length);
        //System.out.println("copy: offset1 = "+offset1+", offset2 = "+offset2);
        //System.out.println(">> copy text: at "+offset3+" <<\n"+text+">>  <<");
        doc2.insertString(offset3, text, null);
        // Adjust the line numbers
        if (line2 < 0) line2 = org.openide.text.NbDocument.findLineNumber(doc1, doc1.getLength());
        int numLines = line2 - line1 + 1;
        //System.out.println("copy("+line1+", "+line2+", "+line3+"): resultLineNumbers.length = "+resultLineNumbers.length);
        assureResultLineNumbersLength(line3 + numLines);
        if (resultLineNumbers[line3] == 0 && line3 > 0) resultLineNumbers[line3] = resultLineNumbers[line3 - 1] + 1;
        int resultLine = resultLineNumbers[line3];
        //System.out.println("resultLine = rln["+line3+"] = "+resultLine);
        //System.out.println("insertNumbers("+line3+", "+resultLine+", "+numLines+")");
        linesComp3.insertNumbers(line3 - 1, resultLine, numLines);
        linesComp3.changedAll();
        for (int i = 0; i < numLines; i++) resultLineNumbers[line3 + i] = resultLine + i;
    }
    
    /**
     * Replace a part of result with a part of the first source.
     * @param line1 The starting line in the first source
     * @param line2 The ending line in the first source or <code>null</code>
     *              when the part ends at the end of the document
     * @param line3 The starting line in the result
     * @param line4 The ending line in the result
     */
    public void replaceSource1InResult(int line1, int line2, int line3, int line4) {
        //System.out.println("replaceSource1InResult("+line1+", "+line2+", "+line3+", "+line4+")");
        Integer conflictLine = Integer.valueOf((line1 > 0) ? line1 : 1);
        // If trying to resolve the conflict twice simply return .
        if (resolvedLeftConflictsLineNumbers.contains(conflictLine)) return ;
        StyledDocument doc1 = (StyledDocument) jEditorPane1.getDocument();
        StyledDocument doc2 = (StyledDocument) jEditorPane3.getDocument();
        try {
            replace(doc1, line1, line2, doc2, line3, line4);
        } catch (BadLocationException e) {
            org.openide.ErrorManager.getDefault().notify(e);
        }
        if (resolvedRightConflictsLineNumbers.contains(conflictLine)) {
            resolvedRightConflictsLineNumbers.remove(conflictLine);
        } else if (resolvedLeftRightConflictsLineNumbers.contains(conflictLine)) {
            resolvedLeftRightConflictsLineNumbers.remove(conflictLine);
        } else if (resolvedRightLeftConflictsLineNumbers.contains(conflictLine)) {
            resolvedRightLeftConflictsLineNumbers.remove(conflictLine);
        } else {
            // We've resolved the conflict.
            numUnresolvedConflicts--;
            updateStatusLine();
        }
        resolvedLeftConflictsLineNumbers.add(conflictLine);
        updateAcceptButtons(line1);
    }
    
    /**
     * Replace a part of result with a part of the second source.
     * @param line1 The starting line in the second source
     * @param line2 The ending line in the second source or <code>null</code>
     *              when the part ends at the end of the document
     * @param line3 The starting line in the result
     * @param line4 The ending line in the result
     */
    public void replaceSource2InResult(int line1, int line2, int line3, int line4) {
        //System.out.println("replaceSource2InResult("+line1+", "+line2+", "+line3+", "+line4+")");
        Integer conflictLine = Integer.valueOf((line1 > 0) ? line1 : 1);
        // If trying to resolve the conflict twice simply return .
        if (resolvedRightConflictsLineNumbers.contains(conflictLine)) return ;
        StyledDocument doc1 = (StyledDocument) jEditorPane2.getDocument();
        StyledDocument doc2 = (StyledDocument) jEditorPane3.getDocument();
        try {
            replace(doc1, line1, line2, doc2, line3, line4);
        } catch (BadLocationException e) {
            org.openide.ErrorManager.getDefault().notify(e);
        }
        if (resolvedLeftConflictsLineNumbers.contains(conflictLine)) {
            resolvedLeftConflictsLineNumbers.remove(conflictLine);
        } else if (resolvedLeftRightConflictsLineNumbers.contains(conflictLine)) {
            resolvedLeftRightConflictsLineNumbers.remove(conflictLine);
        } else if (resolvedRightLeftConflictsLineNumbers.contains(conflictLine)) {
            resolvedRightLeftConflictsLineNumbers.remove(conflictLine);
        } else {
            // We've resolved the conflict.
            numUnresolvedConflicts--;
            updateStatusLine();
        }
        resolvedRightConflictsLineNumbers.add(conflictLine);
        updateAcceptButtons(line1);
    }
    
    public void replaceBothInResult (int line1, int line2,
            int line3, int line4,
            int line5, int line6, boolean right) {
        Integer conflictLine1 = line1 > 0 ? line1 : 1;
        Integer conflictLine2 = line3 > 0 ? line3 : 1;
        // If trying to resolve the conflict twice simply return .
        StyledDocument doc1 = (StyledDocument) jEditorPane1.getDocument();
        StyledDocument doc2 = (StyledDocument) jEditorPane2.getDocument();
        StyledDocument target = (StyledDocument) jEditorPane3.getDocument();
        try {
            if (right) {
                replace(doc2, line3, line4, doc1, line1, line2, target, line5, line6);
            } else {
                replace(doc1, line1, line2, doc2, line3, line4, target, line5, line6);
            }
        } catch (BadLocationException e) {
            org.openide.ErrorManager.getDefault().notify(e);
        }
        if (resolvedLeftConflictsLineNumbers.contains(conflictLine1)) {
            resolvedLeftConflictsLineNumbers.remove(conflictLine1);
        } else if (resolvedRightConflictsLineNumbers.contains(conflictLine2)) {
            resolvedRightConflictsLineNumbers.remove(conflictLine2);
        } else if (right && resolvedLeftRightConflictsLineNumbers.contains(conflictLine1)) {
            resolvedLeftRightConflictsLineNumbers.remove(conflictLine1);
        } else if (!right && resolvedRightLeftConflictsLineNumbers.contains(conflictLine2)) {
            resolvedRightLeftConflictsLineNumbers.remove(conflictLine2);
        } else {
            // We've resolved the conflict.
            numUnresolvedConflicts--;
            updateStatusLine();
        }
        if (right) {
            resolvedRightLeftConflictsLineNumbers.add(conflictLine2);
        } else {
            resolvedLeftRightConflictsLineNumbers.add(conflictLine1);
        }
        updateAcceptButtons(line1);
    }
    
    private void replace(StyledDocument doc1, int line1, int line2,
                         StyledDocument doc2, int line3, int line4) throws BadLocationException {
        //dumpResultLineNumbers();
        //System.out.println("replace("+line1+", "+line2+", "+line3+", "+line4+")");
        int offset1 = (line1 > 0) ? org.openide.text.NbDocument.findLineOffset(doc1, line1 - 1)
                                  : 0;
        int offset2 = (line2 >= 0) ? org.openide.text.NbDocument.findLineOffset(doc1, line2)
                                   : (doc1.getLength() - 1);
        int offset3 = (line3 > 0) ? org.openide.text.NbDocument.findLineOffset(doc2, line3 - 1)
                                  : 0;
        int offset4 = (line4 >= 0) ? org.openide.text.NbDocument.findLineOffset(doc2, line4)
                                   : (doc2.getLength() - 1);
        //System.out.println("replace: offsets = "+offset1+", "+offset2+", "+offset3+", "+offset4);
        int length = offset2 - offset1;
        if (line2 < 0) length++;
        String text = doc1.getText(offset1, length);
        doc2.remove(offset3, offset4 - offset3);
        doc2.insertString(offset3, text, null);
        // Adjust the line numbers
        assureResultLineNumbersLength(line4);
        //int lineDiff;
        int physicalLineDiff = line2 - line1 - (line4 - line3);
        if (physicalLineDiff > 0) {
            System.arraycopy(resultLineNumbers, line4 + 1,
                             resultLineNumbers, line4 + physicalLineDiff + 1,
                             resultLineNumbers.length - line4 - physicalLineDiff - 1);
            //System.out.println("arraycopy("+line4+", "+(line4 + physicalLineDiff)+")");
            //dumpResultLineNumbers();
        }
        int lineDiff = (resultLineNumbers[line3] <= resultLineNumbers[line3 - 1])
                       ? (line2 - line1 + 1)
                       : (line2 - line1 - (line4 - line3));
        //if (resultLineNumbers[line3] <= resultLineNumbers[line3 - 1]) {
            // There are no line numbers defined.
            //lineDiff = line2 - line1 + 1;
        int n = resultLineNumbers[line3 - 1];
        for (int i = line3; i <= line4 + physicalLineDiff; i++) {
            resultLineNumbers[i] = ++n;
        }
            /*
            for (int i = line4 + lineDiff + 1; i < resultLineNumbers.length; i++) {
                if (resultLineNumbers[i] != 0) resultLineNumbers[i] += lineDiff;
                else break;
            }
             */
        //lineDiff = line2 - line1 + 1;
        //System.out.println("insertNumbers("+line3+", "+resultLineNumbers[line3]+", "+(line2 - line1 + 1)+")");
        linesComp3.insertNumbers(line3 - 1, resultLineNumbers[line3], line2 - line1 + 1);
        linesComp3.changedAll();
        //dumpResultLineNumbers();
        //} else {
        //    lineDiff = line2 - line1 - (line4 - line3);
        //}
        if (physicalLineDiff < 0) {
            System.arraycopy(resultLineNumbers, line4 + 1,
            resultLineNumbers, line4 + physicalLineDiff + 1,
            resultLineNumbers.length - line4 - 1);
            //System.out.println("arraycopy("+line4+", "+(line4 + physicalLineDiff)+")");
            //dumpResultLineNumbers();
        }
        adjustLineNumbers(line4 + physicalLineDiff + 1, lineDiff);

        // #65970 workaround, resultLineNumbers content must be primitive only raising
        int line = -1;
        for (int i = 0; i< resultLineNumbers.length; i++) {
            if (resultLineNumbers[i] < line) {
                resultLineNumbers[i] = line;
            }
            line = resultLineNumbers[i];
        }
    }
    
    private void replace(StyledDocument doc1, int line1, int line2,
                         StyledDocument doc2, int line3, int line4,
                         StyledDocument target, int targetStart, int targetEnd) throws BadLocationException {
        //dumpResultLineNumbers();
        //System.out.println("replace("+line1+", "+line2+", "+line3+", "+line4+")");
        int offset1 = (line1 > 0) ? org.openide.text.NbDocument.findLineOffset(doc1, line1 - 1)
                                  : 0;
        int offset2 = (line2 >= 0) ? org.openide.text.NbDocument.findLineOffset(doc1, line2)
                                   : (doc1.getLength() - 1);
        int offset3 = (line3 > 0) ? org.openide.text.NbDocument.findLineOffset(doc2, line3 - 1)
                                  : 0;
        int offset4 = (line4 >= 0) ? org.openide.text.NbDocument.findLineOffset(doc2, line4)
                                   : (doc2.getLength() - 1);
        int offset5 = (targetStart > 0) ? org.openide.text.NbDocument.findLineOffset(target, targetStart - 1)
                                  : 0;
        int offset6 = (targetEnd >= 0) ? org.openide.text.NbDocument.findLineOffset(target, targetEnd)
                                   : (target.getLength() - 1);
        //System.out.println("replace: offsets = "+offset1+", "+offset2+", "+offset3+", "+offset4);
        int length = offset4 - offset3;
        if (line4 < 0) length++;
        String text = doc2.getText(offset3, length);
        target.remove(offset5, offset6 - offset5);
        target.insertString(offset5, text, null);
        
        length = offset2 - offset1;
        if (line2 < 0) length++;
        text = doc1.getText(offset1, length);
        target.insertString(offset5, text, null);
        // Adjust the line numbers
        assureResultLineNumbersLength(targetEnd);
        //int lineDiff;
        int physicalLineDiff = line2 - line1 + line4 - line3 + 1 - (targetEnd - targetStart);
        if (physicalLineDiff > 0) {
            System.arraycopy(resultLineNumbers, targetEnd + 1,
                             resultLineNumbers, targetEnd + physicalLineDiff + 1,
                             resultLineNumbers.length - targetEnd - physicalLineDiff - 1);
            //System.out.println("arraycopy("+line4+", "+(line4 + physicalLineDiff)+")");
            //dumpResultLineNumbers();
        }
        int lineDiff = (resultLineNumbers[targetStart] <= resultLineNumbers[targetStart - 1])
                       ? (line2 - line1 + 1 + line4 - line3 + 1)
                       : physicalLineDiff;
        //if (resultLineNumbers[line3] <= resultLineNumbers[line3 - 1]) {
            // There are no line numbers defined.
            //lineDiff = line2 - line1 + 1;
        int n = resultLineNumbers[targetStart - 1];
        for (int i = targetStart; i <= targetEnd + physicalLineDiff; i++) {
            resultLineNumbers[i] = ++n;
        }
            /*
            for (int i = line4 + lineDiff + 1; i < resultLineNumbers.length; i++) {
                if (resultLineNumbers[i] != 0) resultLineNumbers[i] += lineDiff;
                else break;
            }
             */
        //lineDiff = line2 - line1 + 1;
        //System.out.println("insertNumbers("+line3+", "+resultLineNumbers[line3]+", "+(line2 - line1 + 1)+")");
        linesComp3.insertNumbers(targetStart - 1, resultLineNumbers[targetStart], line2 - line1 + 1 + line4 - line3 + 1);
        linesComp3.changedAll();
        //dumpResultLineNumbers();
        //} else {
        //    lineDiff = line2 - line1 - (line4 - line3);
        //}
        if (physicalLineDiff < 0) {
            System.arraycopy(resultLineNumbers, targetEnd + 1,
            resultLineNumbers, targetEnd + physicalLineDiff + 1,
            resultLineNumbers.length - targetEnd - 1);
            //System.out.println("arraycopy("+line4+", "+(line4 + physicalLineDiff)+")");
            //dumpResultLineNumbers();
        }
        adjustLineNumbers(targetEnd + physicalLineDiff + 1, lineDiff);

        // #65970 workaround, resultLineNumbers content must be primitive only raising
        int line = -1;
        for (int i = 0; i< resultLineNumbers.length; i++) {
            if (resultLineNumbers[i] < line) {
                resultLineNumbers[i] = line;
            }
            line = resultLineNumbers[i];
        }
    }
    
    private void adjustLineNumbers(int startLine, int shift) {
        //System.out.println("adjustLineNumbers("+startLine+", "+shift+")");
        int end = resultLineNumbers.length;
        while (end > 0 && resultLineNumbers[end - 1] == 0) end--;
        int startSetLine = -1;
        int endSetLine = -1;
        //resultLineNumbers[startLine] += shift;
        for (int i = startLine; i < end; i++) {
            resultLineNumbers[i] += shift;
            if (resultLineNumbers[i] <= resultLineNumbers[i - 1]) {
                if (startSetLine > 0) {
                    //System.out.println("insertNumbers("+startSetLine+", "+resultLineNumbers[startSetLine]+", "+(i - startSetLine)+")");
                    linesComp3.insertNumbers(startSetLine - 1, resultLineNumbers[startSetLine], i - startSetLine);
                    linesComp3.changedAll();
                    //dumpResultLineNumbers();
                    startSetLine = -1;
                }
                if (endSetLine < 0) {
                    endSetLine = i;
                }
            } else {
                if (endSetLine > 0) {
                    //System.out.println("removeNumbers("+endSetLine+", "+(i - endSetLine)+")");
                    linesComp3.removeNumbers(endSetLine - 1, i - endSetLine);
                    linesComp3.changedAll();
                    //dumpResultLineNumbers();
                    endSetLine = -1;
                }
                if (startSetLine < 0) {
                    startSetLine = i;
                }
            }
        }
        if (startSetLine > 0) {
            //System.out.println("insertNumbers("+startSetLine+", "+resultLineNumbers[startSetLine]+", "+(end - startSetLine)+" (END))");
            linesComp3.insertNumbers(startSetLine - 1, resultLineNumbers[startSetLine], end - startSetLine);
            linesComp3.shrink(end - 1);
            linesComp3.changedAll();
            //dumpResultLineNumbers();
        }
        if (endSetLine > 0) {
            //System.out.println("removeNumbers("+endSetLine+", "+(end - endSetLine)+" (END))");
            linesComp3.removeNumbers(endSetLine - 1, end - endSetLine);
            linesComp3.shrink(end - 1);
            linesComp3.changedAll();
            //dumpResultLineNumbers();
        }
    }
    
    public void setSource1Title(String title) {
        fileLabel1.setText(title);
    }
    
    public void setSource2Title(String title) {
        fileLabel2.setText(title);
    }
    
    public void setResultSourceTitle(String title) {
        resultLabel.setText(title);
    }
    
    public void setStatusLabel(String status) {
        statusLabel.setText(status);
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
    
    public void setMimeType3(String mime) {
        EditorKit kit = CloneableEditorSupport.getEditorKit(mime);
        jEditorPane3.setEditorKit(kit);
    }
    
    /*
    public void setDocument1(Document doc) {
        if (doc != null) {
            jEditorPane1.setDocument(doc);
        }
    }
     */
    
    public void setResultDocument(Document doc) {
        if (doc != null) {
            jEditorPane3.setDocument(doc);
            jEditorPane3.setEditable(false);
            linesComp3 = new LinesComponent(jEditorPane3);
            resultScrollPane.setRowHeaderView(linesComp3);
        }
    }
    
    /*
     * Whether all conflicts are resolved and the panel can be closed.
     * @return <code>true</code> when the panel can be closed, <code>false</code> otherwise.
     *
    public boolean canClose() {
        return true;
    }
     */
    
    /**
     * Write the result content into the given writer. Skip all unresolved conflicts.
     * @param w The writer to write the result into.
     * @throws IOException When the writing process fails.
     */
    public void writeResult(Writer w, boolean stripLastNewline) throws IOException {
        //System.out.println("writeResult()");
        /*
        try {
            jEditorPane3.getEditorKit().write(w, jEditorPane3.getDocument(),
                                              0, jEditorPane3.getDocument().getLength());
        } catch (BadLocationException blex) {
            throw new IOException(blex.getLocalizedMessage());
        }
         */
        int end = resultLineNumbers.length;
        while (end > 0 && resultLineNumbers[end - 1] == 0) end--;
        int startSetLine = -1;
        StyledDocument doc = (StyledDocument) jEditorPane3.getDocument();
        try {
            for (int i = 1; i < end; i++) {
                if (resultLineNumbers[i] <= resultLineNumbers[i - 1]) {
                    if (startSetLine > 0) {
                    try {
                        //System.out.println("write("+startSetLine+", "+i+")");
                        int offsetStart = org.openide.text.NbDocument.findLineOffset(doc, startSetLine - 1);
                        int offsetEnd = org.openide.text.NbDocument.findLineOffset(doc, i - 1);
                        //System.out.println("  Have text(<l="+(startSetLine-1)+",off="+offsetStart+";l="+(i-1)+",off="+offsetEnd+">), length = "+doc.getLength());
                        try {
                            //System.out.println("'"+doc.getText(offsetStart, offsetEnd - offsetStart)+"'");
                            writeText(w, doc.getText(offsetStart, offsetEnd - offsetStart));
                        } catch (BadLocationException blex) {
                            throw new IOException(blex.getLocalizedMessage());
                        }
                        //dumpResultLineNumbers();
                        startSetLine = -1;
                    } catch (IndexOutOfBoundsException ex) {
                        Logger.getLogger(MergePanel.class.getName()).log(Level.SEVERE, "Invalid position "
                                + startSetLine + "[" + i + "]: " + Arrays.toString(resultLineNumbers), ex);
                    }
                    }
                } else {
                    if (startSetLine < 0) {
                        startSetLine = i;
                    }
                }
            }
            if (startSetLine > 0) {
                //System.out.println("write("+startSetLine+", "+end+" (END))");
                int offsetStart = org.openide.text.NbDocument.findLineOffset(doc, startSetLine - 1);
                int offsetEnd = doc.getLength();
                try {
                    String text = doc.getText(offsetStart, offsetEnd - offsetStart);
                    if (stripLastNewline && text.endsWith("\n")) {
                        text = text.substring(0, text.length() - 1);
                    }
                    writeText(w, text);
                } catch (BadLocationException blex) {
                    throw new IOException(blex.getLocalizedMessage());
                }
                //dumpResultLineNumbers();
            }
        } finally {
            w.close();
        }
    }
    
    private void writeText(Writer w, String text) throws IOException {
        text = text.replace("\n", System.getProperty("line.separator"));
        w.write(text);
    }

    public void highlightRegion1(int line1, int line2, java.awt.Color color) {
        StyledDocument doc = (StyledDocument) jEditorPane1.getDocument();
        ((MergePane)jEditorPane1).addHighlight(doc, line1, line2, color);
    }
    
    public void highlightRegion2(int line1, int line2, java.awt.Color color) {
        StyledDocument doc = (StyledDocument) jEditorPane2.getDocument();
        ((MergePane)jEditorPane2).addHighlight(doc, line1, line2, color);
    }
    
    public void highlightRegion3(int line1, int line2, java.awt.Color color) {
        StyledDocument doc = (StyledDocument) jEditorPane3.getDocument();
        ((MergePane)jEditorPane3).addHighlight(doc, line1, line2, color);
    }
    
    public void unhighlightRegion3(int line1, int line2) {
        StyledDocument doc = (StyledDocument) jEditorPane3.getDocument();
        ((MergePane)jEditorPane3).removeHighlight(doc, line1, line2);
    }
    
    private void addEmptyLines(StyledDocument doc, int line, int numLines) {
        int lastOffset = doc.getEndPosition().getOffset();
        int totLines = org.openide.text.NbDocument.findLineNumber(doc, lastOffset);
        //int totLines = doc.getDefaultRootElement().getElementIndex(lastOffset);
        int offset = lastOffset;
        if (line <= totLines) {
            offset = org.openide.text.NbDocument.findLineOffset(doc, line);
            //offset = doc.getDefaultRootElement().getElement(line).getStartOffset();
        } else {
            offset = lastOffset - 1;
            Logger logger = Logger.getLogger(MergePanel.class.getName());
            logger.log(Level.WARNING, "line({0}) > totLines({1}): final offset({2})", new Object[] {line, totLines, offset}); //NOI18N
            logger.log(Level.INFO, null, new Exception());
        }
        //int endOffset = doc.getEndPosition().getOffset();
        //if (offset > endOffset) offset = endOffset;
        String insStr = strCharacters('\n', numLines);
        //System.out.println("addEmptyLines = '"+insStr+"'");
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
    
    public void addEmptyLines3(int line, int numLines) {
        StyledDocument doc = (StyledDocument) jEditorPane3.getDocument();
        //System.out.println("addEmptyLines3: line = "+line+", numLines = "+numLines); // NOI18N
        addEmptyLines(doc, line - 1, numLines);
        linesComp3.addEmptyLines(line - 1, numLines);
        assureResultLineNumbersLength(line + numLines);
        if (resultLineNumbers[line] == 0 && line > 0) resultLineNumbers[line] = resultLineNumbers[line - 1];
        int resultLine = resultLineNumbers[line];
        for (int i = 1; i < numLines; i++) resultLineNumbers[line + i] = resultLine;
    }
    
    
    private javax.swing.JViewport jViewport1;
    private javax.swing.JViewport jViewport2;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton acceptAndNextLeftButton;
    private javax.swing.JButton acceptAndNextRightButton;
    private javax.swing.JButton acceptLeftButton;
    private javax.swing.JButton acceptLeftRightButton;
    private javax.swing.JButton acceptRightButton;
    private javax.swing.JButton acceptRightLeftButton;
    final javax.swing.JPanel commandPanel = new javax.swing.JPanel();
    final javax.swing.JSplitPane diffSplitPane = new javax.swing.JSplitPane();
    final javax.swing.JPanel editorPanel = new javax.swing.JPanel();
    final javax.swing.JLabel fileLabel1 = new javax.swing.JLabel();
    final javax.swing.JLabel fileLabel2 = new javax.swing.JLabel();
    final javax.swing.JPanel filePanel1 = new javax.swing.JPanel();
    final javax.swing.JPanel filePanel2 = new javax.swing.JPanel();
    final javax.swing.JButton firstConflictButton = new javax.swing.JButton();
    final javax.swing.JEditorPane jEditorPane1 = new MergePane();
    final javax.swing.JEditorPane jEditorPane2 = new MergePane();
    final javax.swing.JEditorPane jEditorPane3 = new MergePane();
    final javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
    final javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
    final javax.swing.JButton lastConflictButton = new javax.swing.JButton();
    final javax.swing.JPanel leftCommandPanel = new javax.swing.JPanel();
    final javax.swing.JSplitPane mergeSplitPane = new javax.swing.JSplitPane();
    final javax.swing.JButton nextConflictButton = new javax.swing.JButton();
    final javax.swing.JButton prevConflictButton = new javax.swing.JButton();
    final javax.swing.JLabel resultLabel = new javax.swing.JLabel();
    final javax.swing.JPanel resultPanel = new javax.swing.JPanel();
    final javax.swing.JScrollPane resultScrollPane = new javax.swing.JScrollPane();
    final javax.swing.JPanel rightCommandPanel = new javax.swing.JPanel();
    final javax.swing.JLabel statusLabel = new javax.swing.JLabel();
    // End of variables declaration//GEN-END:variables

}
