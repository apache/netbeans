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

package org.netbeans.modules.debugger.ui.eval;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.editor.EditorUI;
import org.netbeans.spi.debugger.ui.CodeEvaluator.EvaluatorService;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Daniel Prusa
 */
public class CodeEvaluatorUI extends TopComponent implements HelpCtx.Provider,
    DocumentListener, KeyListener, PropertyChangeListener {

    /** unique ID of <code>TopComponent</code> (singleton) */
    private static final String ID = "evaluatorPane"; //NOI18N
    private static final String PROP_RESULT_CHANGED = "resultChanged"; // NOI18N

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private static WeakReference<CodeEvaluatorUI> instanceRef;

    private JEditorPane codePane;
    // Text of the code pane, which is updated in AWT and can be read in any thread.
    // Solves the problem with calling getText() in non-AWT thread.
    private volatile String codeText = "";
    //private History history;
    private Reference<EvaluatorService> debuggerRef = new WeakReference<>(null);
    private final DbgManagerListener dbgManagerListener;
    //private TopComponent resultView;
    private final JButton dropDownButton;

    private static final RequestProcessor rp = new RequestProcessor("Debugger Evaluator", 1);  // NOI18N


    /** Creates new form CodeEvaluator */
    public CodeEvaluatorUI() {
        initComponents();
        codePane = new JEditorPaneWithHelp();
        codePane.setMinimumSize(new Dimension(0,0));
        // Do not highlight the current row
        codePane.putClientProperty(
            "HighlightsLayerExcludes", //NOI18N
            "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$" //NOI18N
        );
        EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(codePane);
        // Do not draw text limit line
        try {
            java.lang.reflect.Field textLimitLineField = EditorUI.class.getDeclaredField("textLimitLineVisible"); // NOI18N
            textLimitLineField.setAccessible(true);
            textLimitLineField.set(eui, false);
        } catch (Exception ex) {}
        //history = new History();

        dropDownButton = createDropDownButton();

        javax.swing.GroupLayout rightPanelLayout = new javax.swing.GroupLayout(rightPanel);
        rightPanel.setLayout(rightPanelLayout);
        rightPanelLayout.setHorizontalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(dropDownButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2))
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                .addComponent(evaluateButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2))
        );
        rightPanelLayout.setVerticalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addComponent(dropDownButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(evaluateButton))
        );
        Dimension preferredSize = rightPanel.getPreferredSize();
        rightPanel.setMinimumSize(preferredSize);
        
        //setupContext();
        editorScrollPane.setViewportView(codePane);
        invalidate();
        codePane.addKeyListener(this);
        dbgManagerListener = new DbgManagerListener (this);
        DebuggerManager.getDebuggerManager().addDebuggerListener(
                DebuggerManager.PROP_CURRENT_ENGINE,
                dbgManagerListener
        );
        checkDebuggerState();
    }
    
    public void pasteExpression(String expr) {
        codePane.setText(expr);
        codeText = expr;
        if (!isOpened()) {
            open();
        }
        requestActive();
    }

    private JButton createDropDownButton() {
        Icon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/debugger/resources/evaluator/drop_down_arrow.png", false);
        final JButton button = new DropDownButton();
        button.setIcon(icon);
        String tooltipText = NbBundle.getMessage(CodeEvaluatorUI.class, "CTL_Expressions_Dropdown_tooltip");
        button.setToolTipText(tooltipText);
        button.setEnabled(false);
        Dimension size = new Dimension(icon.getIconWidth() + 3, icon.getIconHeight() + 2);
        button.setPreferredSize(size);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setFocusable(false);
        AbstractAction action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ("pressed".equals(e.getActionCommand())) {
                    JComponent jc = (JComponent) e.getSource();
                    Point p = new Point(0, 0);
                    SwingUtilities.convertPointToScreen(p, jc);
                    if (!ButtonPopupSwitcher.isShown()) {
                        SwitcherTableItem[] items = createSwitcherItems();
                        ButtonPopupSwitcher.selectItem(jc, items, p.x, p.y);
                    }
                    //Other portion of issue 37487, looks funny if the
                    //button becomes pressed
                    if (jc instanceof AbstractButton) {
                        AbstractButton jb = (AbstractButton) jc;
                        jb.getModel().setPressed(false);
                        jb.getModel().setRollover(false);
                        jb.getModel().setArmed(false);
                        jb.repaint();
                    }
                }
            } // actionPerformed

            @Override
            public boolean isEnabled() {
                return !getEditItemsList().isEmpty();
            }

        };
        action.putValue(Action.SMALL_ICON, icon);
        action.putValue(Action.SHORT_DESCRIPTION, tooltipText);
        button.setAction(action);
        return button;
    }

    private RequestProcessor.Task setupContextTask;

    private void setupContext() {
        if (setupContextTask == null) {
            setupContextTask = rp.create(new Runnable() {
                @Override
                public void run() {
                    setupContextLazily();
                }
            });
        }
        // Setting up a context takes time.
        setupContextTask.schedule(500);
    }

    private void setupContextLazily() {
        final String text = codeText;
        final Document[] documentPtr = new Document[] { null };

        class ContextUpdated implements Runnable {
            @Override
            public void run() {
                if (codePane.getDocument() != documentPtr[0]) {
                    codePane.getDocument().addDocumentListener(CodeEvaluatorUI.this);
                    if (text != null) {
                        codePane.setText(text);
                    }
                }
                documentPtr[0] = codePane.getDocument();
            }
        }

        EvaluatorService es = debuggerRef.get();
        if (es != null) {
            ContextUpdated contextUpdated = new ContextUpdated();
            es.setupContext(codePane, contextUpdated);
            SwingUtilities.invokeLater(contextUpdated);
        }
    }
    
    private List<String> getEditItemsList() {
        EvaluatorService es = debuggerRef.get();
        if (es != null) {
            return es.getExpressionsHistory();
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    private SwitcherTableItem[] createSwitcherItems() {
        List<String> editItemsList = getEditItemsList();
        SwitcherTableItem[] items = new SwitcherTableItem[editItemsList.size()];
        int x = 0;
        for (String item : editItemsList) {
            items[x++] = new SwitcherTableItem(new MenuItemActivatable(item), item);
        }
        return items;
    }

    private void recomputeDropDownItems() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                List<String> editItemsList = getEditItemsList();
                for (String str : editItemsList) {
                    StringTokenizer tok = new StringTokenizer(str, "\n"); // NOI18N
                    String dispName = "";
                    while (dispName.trim().length() == 0 && tok.hasMoreTokens()) {
                        dispName = tok.nextToken();
                    }
                }
                dropDownButton.setEnabled(!editItemsList.isEmpty());
            }
        });
    }

    public static synchronized CodeEvaluatorUI getInstance() {
        CodeEvaluatorUI instance = (CodeEvaluatorUI) WindowManager.getDefault().findTopComponent(ID);
        if (instance == null) {
            instance = new CodeEvaluatorUI();
        }
        return instance;
    }

    private static CodeEvaluatorUI getDefaultInstance() {
        CodeEvaluatorUI evaluator = instanceRef != null ? instanceRef.get() : null;
        if (evaluator != null) {
            return evaluator;
        }
        final CodeEvaluatorUI result[] = new CodeEvaluatorUI[1];
        if (SwingUtilities.isEventDispatchThread()) {
            result[0] = getInstance();
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        result[0] = getInstance();
                    }
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        instanceRef = new WeakReference(result[0]);
        return result[0];
    }

    public static void addResultListener(final PropertyChangeListener listener) {
        rp.post(new Runnable() {
            @Override
            public void run() {
                CodeEvaluatorUI defaultInstance = getDefaultInstance();
                if (defaultInstance != null) {
                    synchronized(defaultInstance.pcs) {
                        defaultInstance.pcs.addPropertyChangeListener(listener);
                    }
                }
            }
        });
    }

    public static void removeResultListener(final PropertyChangeListener listener) {
        rp.post(new Runnable() {
            @Override
            public void run() {
                CodeEvaluatorUI defaultInstance = getDefaultInstance();
                if (defaultInstance != null) {
                    synchronized(defaultInstance.pcs) {
                        defaultInstance.pcs.removePropertyChangeListener(listener);
                    }
                }
            }
        });
    }

    private static void fireResultChange() {
        rp.post(new Runnable() {
            @Override
            public void run() {
                CodeEvaluatorUI defaultInstance = getDefaultInstance();
                if (defaultInstance != null) {
                    synchronized (defaultInstance.pcs) {
                        defaultInstance.pcs.firePropertyChange(PROP_RESULT_CHANGED, null, null);
                    }
                }
            }
        });
    }

    private synchronized void checkDebuggerState() {
        DebuggerEngine de = DebuggerManager.getDebuggerManager().getCurrentEngine();
        final EvaluatorService es;
        if (de != null) {
            es = de.lookupFirst(null, EvaluatorService.class);
        } else {
            es = null;
        }
        //final List<String> expressionsHistory = es.getExpressionsHistory();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                EvaluatorService lastEs = debuggerRef.get();
                debuggerRef = new WeakReference(es);
                if (es != lastEs) {
                    setupContext();
                    if (lastEs != null) {
                        lastEs.removePropertyChangeListener(CodeEvaluatorUI.this);
                    }
                    if (es != null) {
                        es.addPropertyChangeListener(CodeEvaluatorUI.this);
                    }
                    computeEvaluationButtonState();
                    recomputeDropDownItems();
                }
            }
        });
    }

    private void computeEvaluationButtonState() {
        EvaluatorService debugger = debuggerRef.get();
        boolean isEnabled = debugger != null && debugger.canEvaluate() &&
                            codePane.getDocument().getLength() > 0 &&
                            editorScrollPane.getViewport().getView() == codePane;
        evaluateButton.setEnabled(isEnabled);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        DebuggerManager.getDebuggerManager().removeDebuggerListener(
                DebuggerManager.PROP_CURRENT_ENGINE,
                dbgManagerListener);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        evaluateButton = new javax.swing.JButton();
        editorScrollPane = new javax.swing.JScrollPane();
        separatorPanel = new javax.swing.JPanel();
        rightPanel = new javax.swing.JPanel();

        evaluateButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/debugger/resources/evaluator/evaluate.png"))); // NOI18N
        evaluateButton.setText(org.openide.util.NbBundle.getMessage(CodeEvaluatorUI.class, "CodeEvaluatorUI.evaluateButton.text")); // NOI18N
        evaluateButton.setToolTipText(org.openide.util.NbBundle.getMessage(CodeEvaluatorUI.class, "HINT_Evaluate_Button")); // NOI18N
        evaluateButton.setEnabled(false);
        evaluateButton.setPreferredSize(new java.awt.Dimension(38, 22));
        evaluateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                evaluateButtonActionPerformed(evt);
            }
        });

        editorScrollPane.setBorder(null);

        separatorPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("Separator.foreground"));
        separatorPanel.setMaximumSize(new java.awt.Dimension(1, 32767));
        separatorPanel.setMinimumSize(new java.awt.Dimension(1, 10));
        separatorPanel.setPreferredSize(new java.awt.Dimension(1, 10));

        javax.swing.GroupLayout rightPanelLayout = new javax.swing.GroupLayout(rightPanel);
        rightPanel.setLayout(rightPanelLayout);
        rightPanelLayout.setHorizontalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        rightPanelLayout.setVerticalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 22, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(editorScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(separatorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(rightPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(editorScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
            .addComponent(separatorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
            .addComponent(rightPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void evaluateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_evaluateButtonActionPerformed
        evaluate();
    }//GEN-LAST:event_evaluateButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane editorScrollPane;
    private javax.swing.JButton evaluateButton;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JPanel separatorPanel;
    // End of variables declaration//GEN-END:variables

    public static void openEvaluator() {
        String selectedText = null;
        JEditorPane editor = EditorContextDispatcher.getDefault().getMostRecentEditor();
        if (editor != null) {
            selectedText = editor.getSelectedText();
        }
        CodeEvaluatorUI evaluator = getInstance();
        evaluator.open ();
        if (selectedText != null) {
            evaluator.codePane.setText(selectedText);
            evaluator.codeText = selectedText;
        }
        evaluator.codePane.selectAll();
        evaluator.requestActive ();
    }

    @Override
    public boolean requestFocusInWindow() {
        codePane.requestFocusInWindow(); // [TODO]
        return super.requestFocusInWindow();
    }

    @Override
    protected String preferredID() {
        return this.getClass().getName();
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage (CodeEvaluatorUI.class, "CTL_Code_Evaluator_name"); // NOI18N
    }

    @Override
    public String getToolTipText() {
        return NbBundle.getMessage (CodeEvaluatorUI.class, "CTL_Code_Evaluator_tooltip"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("EvaluateCode"); // NOI18N
    }

    // ..........................................................................

    public String getExpression() {
        return codeText;
    }

    public void evaluate() {
        //evalTask.schedule(10);
        EvaluatorService es = debuggerRef.get();
        if (es != null) {
            es.evaluate(codeText);
        }
    }

    /*
    private void displayResult(Variable var) {
        this.result = var;
        if (var == null) {
            fireResultChange();
            return ;
        }
        //DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(var.getValue()));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                boolean isMinimized = false;
                if (preferences.getBoolean("show_evaluator_result", true)) {
                    TopComponent view = WindowManager.getDefault().findTopComponent("localsView"); // NOI18N [TODO]
                    view.open();
                    isMinimized = WindowManager.getDefault().isTopComponentMinimized(view);
                    view.requestActive();
                } else {
                    if (resultView == null) {
                        resultView = getResultViewInstance();
                    }
                    if (result != null && resultView != null) {
                        resultView.open();
                        isMinimized = WindowManager.getDefault().isTopComponentMinimized(resultView);
                        resultView.requestActive();
                    }
                }
                if (!isMinimized) {
                    getInstance().requestActive();
                }
                fireResultChange();
            }
        });
    }
    */

    /*
    private void addResultToHistory(final String expr, Variable result) {
        if (lastEvaluationRecord != null) {
            history.addItem(lastEvaluationRecord.expr, lastEvaluationRecord.type,
                    lastEvaluationRecord.value, lastEvaluationRecord.toString);
        }
        if (result != null) { // 'result' can be null if debugger finishes
            String type = result.getType();
            String value = result.getValue();
            String toString = ""; // NOI18N
            if (result instanceof ObjectVariable) {
                try {
                    toString = ((ObjectVariable) result).getToStringValue ();
                } catch (InvalidExpressionException ex) {
                }
            } else {
                toString = value;
            }
            lastEvaluationRecord = new HistoryRecord(expr, type, value, toString);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String expr2 = expr.trim();
                if (editItemsSet.contains(expr2)) {
                    editItemsList.remove(expr2);
                    editItemsList.add(0, expr2);
                } else {
                    editItemsList.add(0, expr2);
                    editItemsSet.add(expr2);
                    if (editItemsList.size() > 20) { // [TODO] constant
                        String removed = editItemsList.remove(editItemsList.size() - 1);
                        editItemsSet.remove(removed);
                    }
                }
                recomputeDropDownItems();
                
                final ArrayList<String> itemsToStore = new ArrayList<String>(editItemsList);
                rp.post(new Runnable() {
                    @Override
                    public void run() {
                        storeEditItems(itemsToStore);
                    }
                });
            }
        });
    }
    */

    // KeyListener implementation ..........................................

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
            e.consume();
            if (debuggerRef.get() != null) {
                evaluate();
            }
        }
//        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
//            e.consume();
//            close();
//        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    // DocumentListener implementation ..........................................

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateWatch();
        codeText = codePane.getText();
    }

    // DocumentListener
    @Override
    public void removeUpdate(DocumentEvent e) {
        updateWatch();
        codeText = codePane.getText();
    }

    // DocumentListener
    @Override
    public void changedUpdate(DocumentEvent e) {
        updateWatch();
        codeText = codePane.getText();
    }

    private void updateWatch() {
        // Update this LAZILY to prevent from deadlocks!
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                computeEvaluationButtonState();
            }
        });
    }

    // PropertyChangeListener on current thread .................................

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();
        if (EvaluatorService.PROP_CAN_EVALUATE.equals(propertyName)) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    EvaluatorService debugger = debuggerRef.get();
                    if (debugger != null && debugger.canEvaluate()) {
                        setupContext();
                    }
                    computeEvaluationButtonState();
                }
            });
        } else if (EvaluatorService.PROP_EXPRESSIONS_HISTORY.equals(propertyName)) {
            recomputeDropDownItems();
        }
    }

    // ..........................................................................

    private synchronized TopComponent getResultViewInstance() {
        /** unique ID of <code>TopComponent</code> (singleton) */
        TopComponent instance = WindowManager.getDefault().findTopComponent("resultsView"); // NOI18N [TODO]
        // Can be null
        return instance;
    }

    private static class DbgManagerListener extends DebuggerManagerAdapter {

        private final Reference<CodeEvaluatorUI> codeEvaluatorRef;

        public DbgManagerListener(CodeEvaluatorUI evaluator) {
            codeEvaluatorRef = new WeakReference<CodeEvaluatorUI>(evaluator);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            CodeEvaluatorUI evaluator = (CodeEvaluatorUI) codeEvaluatorRef.get();
            if (evaluator != null) {
                evaluator.checkDebuggerState();
            }
        }

    }

    private class MenuItemActivatable implements SwitcherTableItem.Activatable {

        String text;

        MenuItemActivatable(String str) {
            text = str;
        }

        @Override
        public void activate() {
            codePane.setText(text);
            codeText = text;
        }

    }
    
    private class JEditorPaneWithHelp extends JEditorPane implements HelpCtx.Provider {

        @Override
        public HelpCtx getHelpCtx() {
            return CodeEvaluatorUI.this.getHelpCtx();
        }
        
    }

    private static class DropDownButton extends JButton {

        @Override
        protected void processMouseEvent(MouseEvent me) {
            super.processMouseEvent(me);
            if (isEnabled() && me.getID() == MouseEvent.MOUSE_PRESSED) {
                getAction().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "pressed"));
            }
        }

        protected String getTabActionCommand(ActionEvent e) {
            return null;
        }

        void performAction( ActionEvent e ) {
        }

    }

}
