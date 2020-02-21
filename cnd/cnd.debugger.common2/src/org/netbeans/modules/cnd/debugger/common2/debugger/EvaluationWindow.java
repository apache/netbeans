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

package org.netbeans.modules.cnd.debugger.common2.debugger;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.accessibility.AccessibleContext;
import javax.swing.*;
import org.netbeans.api.editor.DialogBinding;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.FormatOption;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.netbeans.spi.viewmodel.Models;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public final class EvaluationWindow extends TopComponent {

    /** generated Serialized Version UID */
    private static final String preferredID = "EvaluationWindow"; // NOI18N
    private static EvaluationWindow DEFAULT;

    private transient JComponent tree = null;
    private String name;
    private String view_name;
    private volatile NativeDebugger debugger = null;
    private JMenuItem menuItemClear;
    private JPopupMenu popup;
    private JTextArea ta;
    private JScrollPane ta_sp;
    private JPanel hp;
    private JPanel cp;
    //private ArrayList<String> current_addrs;
    private PopupListener popupListener;
    private String expr;

    private JComboBox format_jcb;
    private FormatListener format_listener;
    private FormatOption format;
    private JComboBox exprList;
    private String selected_text = null;

    public static synchronized EvaluationWindow getDefault() {
        if (DEFAULT == null) {
            DEFAULT = (EvaluationWindow) WindowManager.getDefault().findTopComponent(preferredID);
            if (DEFAULT == null) {
                DEFAULT = new EvaluationWindow();
            }
        }
        return DEFAULT;
    }
    
    public EvaluationWindow() {
	name = Catalog.get("TITLE_EvaluationWindow");    //NOI18N
	view_name = Catalog.get("TITLE_EvaluationView"); //NOI18N
	super.setName(name);
	final String iconDir = "org/netbeans/modules/cnd/debugger/common2/icons/";//NOI18N
	setIcon(org.openide.util.ImageUtilities.loadImage
	    (iconDir + "evaluate_expression.png")); // NOI18N
    }

    @Override
    protected String preferredID() {
        return this.getClass().getName();
    }
    
    @Override
    public void requestActive() {
        super.requestActive();
        if (exprList != null) {
            exprList.requestFocusInWindow();
        }
    }

    @Override
    protected void componentHidden () {
	if (exprList != null)
            exprList.setSelectedIndex(0);
	if (debugger != null) {
	    debugger.registerEvaluationWindow(null);
        }
    }
    
    @Override
    public void componentShowing () {
        super.componentShowing ();
	boolean update = connectToDebugger(NativeDebuggerManager.get().currentDebugger());
        if (update) {
            updateWindow();
            updateFormats();
        }
    }

    @Override
    protected void componentClosed () {
        super.componentClosed();
	if (debugger != null) {
	    debugger.registerEvaluationWindow(null);
	    //tree = null;
	    //current_addrs.clear();
            ta.setText(null);
            ta.setCaretPosition(0);
	    //exprList.removeAllItems();
            exprList.setSelectedIndex(0);
	    //exprList = null;
	    //format_jcb.removeAllItems();
            //format_jcb.removeActionListener(format_listener);
            invalidate();
	}
    }

    private boolean connectToDebugger (NativeDebugger debugger) {
        boolean res = this.debugger != debugger;
	this.debugger = debugger;
	if (debugger != null) {
            debugger.registerEvaluationWindow(this);
        }
        return res;
    }
    
    private void updateFormats() {
        FormatOption[] evalFormats = (debugger != null) ? debugger.getEvalFormats() : null;
        if (evalFormats != null) {
            format_jcb.setModel(new DefaultComboBoxModel(evalFormats));
            format_jcb.setEnabled(true);
        } else {
            format_jcb.setModel(new DefaultComboBoxModel(new FormatOption[]{FormatOption.EMPTY}));
            format_jcb.setEnabled(false);
        }
        updateSelectedFormat();
    }

    @Override
    public int getPersistenceType () {
        return PERSISTENCE_ALWAYS;
    }
            
    @Override
    public String getName () {
        return (name);
    }
    
    @Override
    public String getToolTipText () {
        return (view_name);
    }
    
    private void evaluateTypedExpression() {
        exprEval(exprList.getEditor().getItem().toString());
    }
    
    public void exprEval(String expr) {
        this.expr = expr;
        format = (FormatOption)format_jcb.getSelectedItem();
	if (expr != null && !expr.equals("")) {
            //see bz#248470
            debugger.exprEval(format == null ? FormatOption.EMPTY : format, expr);
        }
    }

    private int exprMap(String expr) {
        for (int i = 0; i < exprList.getItemCount(); i++ ) {
            if (expr.equals((String)exprList.getItemAt(i))) {
                return i;
            }
        }
        return -1; // not found
    }

    public void evalResult(String result) {
        if (result == null) 
	    return; 
        if (result.length() == 0) 
	    result = " "; // NOI18N

	int i = result.indexOf(" ="); // NOI18N
	if (i == -1)
	    // 6574458
	    // non-expr, constant
	    i = result.length()-1;

	// 6708564 expr = result.substring(0, i);
	// Add expr to drop-down list
	int index = exprMap(expr);
	if (index == -1) {
	    // not found
	    // Add expr to drop-down list
	    /*
	    if (expr.length() < 40)
		// 6754292
		*/
		exprList.addItem(expr);
	}

	//exprList.setSelectedIndex(index);
        result = result.replace("\\n", "\n"); //NOI18N
        ta.append(result);
        // fix for #218111 - always scroll on update
        ta.setCaretPosition(ta.getDocument().getLength());
        updateWindow();
    }
    
    private void updateWindow () {
        int i, k;
        
        if (tree == null) {
            ta = new JTextArea();
            ta_sp = new JScrollPane(ta);
            //current_addrs = new ArrayList<String>();
            setLayout (new BorderLayout ());
            tree = Models.createView (Models.EMPTY_MODEL);
            tree.setName (view_name);
            
            ta.setEditable(false);
            ta.setWrapStyleWord(false);
            Font f = ta.getFont();
            ta.setFont(new Font("Monospaced", f.getStyle(), f.getSize())); //NOI18N
            hp = new JPanel(new BorderLayout());
/*
            JLabel hp_name = new JLabel("        Expression                       "); 
            JLabel hp_value = new JLabel("                                Value"); 
            hp_name.setToolTipText("Expression to be evaluated"); 
            hp_value.setToolTipText("Expression value");             
            hp.add(hp_name, BorderLayout.WEST);
            hp.add(hp_value, BorderLayout.CENTER);
  */          

            // Default settings
            expr = ""; //NOI18N
            
            //cp = new JPanel(new FlowLayout());
	    cp = new JPanel(new java.awt.GridBagLayout());

            cp.setToolTipText("Control panel to specify Expression. Use pop-up menu to specify output format."); // NOI18N
            JLabel cp_text1 = new JLabel(Catalog.get("LBL_Expression")); // NOI18N
            cp_text1.setToolTipText(Catalog.get("HINT_Expression")); // NOI18N
            exprList = new JComboBox();
	    exprList.setMaximumSize(cp.getPreferredSize());
            exprList.addItem(expr);
            exprList.setEditable(true);
            exprList.setEditor(new ComboBoxEditor() {
                class ExpressionEditorPane extends JEditorPane {
                    private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

                    public ExpressionEditorPane() {
                        super(MIMENames.CPLUSPLUS_MIME_TYPE, ""); //NOI18N
                        restrictEvents(
                                getInputMap(),
                                KeyEvent.VK_ENTER, KeyEvent.VK_ESCAPE, KeyEvent.VK_TAB
                        );              
                    }
                    
                    @Override
                    protected void processKeyEvent(KeyEvent e) {
                        super.processKeyEvent(e);
                        KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);
                        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
                        if (KeyEvent.KEY_PRESSED == e.getID()) {
                            if (enter.equals(ks) && !e.isConsumed()) {
                                for (ActionListener actionListener : listeners) {
                                    actionListener.actionPerformed(new ActionEvent(this, 0, e.toString()));
                                }
                                return;
                            }
                        }
                    }
                    
                    public void addActionListener(ActionListener l) {
                        listeners.add(l);
                    }
                    
                    public void removeActionListener(ActionListener l) {
                        listeners.remove(l);
                    }
                    
                }
                
                private ExpressionEditorPane pane = new ExpressionEditorPane();

                @Override
                public Component getEditorComponent() {
                    FileObject file = EditorContextDispatcher.getDefault().getMostRecentFile();
                    int line = EditorContextDispatcher.getDefault().getMostRecentLineNumber();
                    if (file != null && line != -1) {
                        DialogBinding.bindComponentToFile(file, line, 0, 0, pane);
                    }
                    return pane;
                }

                @Override
                public void setItem(Object anObject) {
                    if (anObject != null) {
                        pane.setText(anObject.toString());
                    } else {
                        pane.setText(""); //NOI18N
                    }
                }

                @Override
                public Object getItem() {
                    return pane.getText();
                }

                @Override
                public void selectAll() {
                    pane.selectAll();
                }

                @Override
                public void addActionListener(ActionListener l) {
                    pane.addActionListener(l);
                }

                @Override
                public void removeActionListener(ActionListener l) {
                    pane.removeActionListener(l);
                }
            });
                 
            exprList.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if ("comboBoxChanged".equals(e.getActionCommand()) && exprList.isPopupVisible()) { //NOI18N
                        evaluateTypedExpression();
                    }
                }
            });
            exprList.getEditor().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    evaluateTypedExpression();
                }
            });

            JLabel cp_text3 = new JLabel(Catalog.get("LBL_Format")); // NOI18N
            cp_text3.setToolTipText(Catalog.get("HINT_Output_format")); // NOI18N
	    format_listener = new FormatListener();
            format_jcb = new JComboBox();
            updateFormats();
            format_jcb.addActionListener(format_listener);

	    java.awt.GridBagConstraints gridBagConstraints ;
	    int gridx = 0;
	    
	    gridBagConstraints = new java.awt.GridBagConstraints();
	    gridBagConstraints.gridx = gridx++;
	    gridBagConstraints.gridy = 0;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	    gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
            cp.add(cp_text1, gridBagConstraints);

	    gridBagConstraints = new java.awt.GridBagConstraints();
	    gridBagConstraints.gridx = gridx++;
	    gridBagConstraints.gridy = 0;
	    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	    gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
	    gridBagConstraints.weightx = 1.0;
            cp.add(exprList, gridBagConstraints);

	    gridBagConstraints = new java.awt.GridBagConstraints();
	    gridBagConstraints.gridx = gridx++;
	    gridBagConstraints.gridy = 0;
	    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	    gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
            cp.add(cp_text3, gridBagConstraints);

	    gridBagConstraints = new java.awt.GridBagConstraints();
	    gridBagConstraints.gridx = gridx++;
	    gridBagConstraints.gridy = 0;
	    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	    gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 0);
            cp.add(format_jcb, gridBagConstraints);
            
            tree.add(hp, BorderLayout.NORTH);
            tree.add(ta_sp, BorderLayout.CENTER);
            tree.add(cp, BorderLayout.SOUTH);
            AccessibleContext ac = tree.getAccessibleContext();
            ac.setAccessibleDescription("Window to view  expression"); // NOI18N
            ac.setAccessibleName(Catalog.get("TITLE_EvaluationView")); // NOI18N
            add (tree, "Center");  //NOI18N

            //Create the popup menu.
            popup = new JPopupMenu();

            //Create listener
            popupListener = new PopupListener(popup);

            //Add Clear
            menuItemClear = new JMenuItem(new ClearBufferAction());
            popup.add(menuItemClear);

            //Add FollowSelectedPointer
	    /*
            menuItemFollowSelectedPointer = new JMenuItem(new FollowSelectedPointerAction());
            popup.add(menuItemFollowSelectedPointer);
	    */
            
            //Add listener
            ta.addMouseListener(popupListener);
            ta.setText(null);
            ta.setCaretPosition(0);
            
            restrictEvents(
                    exprList.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT),
                    KeyEvent.VK_ENTER, KeyEvent.VK_ESCAPE, KeyEvent.VK_TAB
            );            
        }
/*
       	k = current_addrs.size();
	if (k > 0)
            ta.append(current_addrs.get(k-1));
	    */

        invalidate();
    }
    
    private final void restrictEvents(InputMap im, int ... events) {
        final String NO_ACTION = "no-action"; // NOI18N
        for (int event : events) {
            final KeyStroke ks = KeyStroke.getKeyStroke(event, 0);
            im.put(ks, NO_ACTION);
        }
    }

    private class FormatListener implements ActionListener {

        // implement ActionListener
        @Override
        public void actionPerformed(java.awt.event.ActionEvent ev) {

            String ac = ev.getActionCommand();
            if (ac.equals("comboBoxChanged")) { // NOI18N
                evaluateTypedExpression();
            }
        }
    }
    
    private void updateSelectedFormat() {
        if (format != null) {
            format_jcb.setSelectedItem(format);
            if (format_jcb.getSelectedItem() != null) {
                return;
            }
        }
        format_jcb.setSelectedIndex(0);
    }

    
    class PopupListener extends MouseAdapter {
        JPopupMenu popup;

        PopupListener(JPopupMenu popupMenu) {
            popup = popupMenu;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
	    /*
                selected_text = ta.getSelectedText();
                if (selected_text == null) {
                    menuItemFollowSelectedPointer.setEnabled(false);
                } else {
                    menuItemFollowSelectedPointer.setEnabled(true);
                }
	    */
                menuItemClear.setEnabled(true);
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
        }
    }

    class ClearBufferAction extends AbstractAction {
        public ClearBufferAction() {
            super("Clear", // NOI18N
                new ImageIcon("org/netbeans/modules/cnd/debugger/common2/icons/Pointers.gif")); // NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            ta.setText(null);
            ta.setCaretPosition(0);
        }
    }
    
    class FollowSelectedPointerAction extends AbstractAction
    {
        public FollowSelectedPointerAction() {
            super("Follow Selected Pointer", // NOI18N
                new ImageIcon("paste.gif")); // NOI18N
        }
        @Override
        public void actionPerformed(ActionEvent ev) {
            FollowSelectedPointer(selected_text);
        }
    }
/*
    class RefreshEvaluationAction extends AbstractAction
    {
        public RefreshEvaluationAction() {
            super("Refresh", 
                new ImageIcon("paste.gif"));
        }
        public void actionPerformed(ActionEvent ev) {
            String s=(String)((exprList.getEditor()).getItem());
            if (s.length() > 0) {
                expr = s;
            }
            exprEval();
        }
    }

    class HideTextAction extends AbstractAction
    {
        public HideTextAction() {
            super("Hide Text", 
                new ImageIcon("cut.gif"));
        }
        public void actionPerformed(ActionEvent ev) {
        }
    }

*/
    protected void FollowSelectedPointer(String s) {
        int i;
        // Remove all spaces and tabs at the beginning
        for (i=0; i < s.length(); i++) {
            if (s.charAt(i) == ' ') continue;
            if (s.charAt(i) == '\t') continue;
            break;
        }
        if (i > 0) 
            if (i < s.length())
                s=s.substring(i);
                
        // Remove everything after address
        for (i=0; i < s.length(); i++) {
            if (s.charAt(i) == ':') break;
            if (s.charAt(i) == ';') break;
            if (s.charAt(i) == ' ') break;
            if (s.charAt(i) == '\t') break;
        }
        if (i > 0) 
            if (i < s.length())
                s=s.substring(0, i);

        if (s.length() > 0) {
            expr = s;
        }
        evaluateTypedExpression();
    }

    @Override
    public HelpCtx getHelpCtx() {
	return new HelpCtx("EvaluationWindow");
    }

}
