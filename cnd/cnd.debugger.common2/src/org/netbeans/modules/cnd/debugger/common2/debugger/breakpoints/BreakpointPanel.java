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


package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

import java.awt.Window;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.DefaultComboBoxModel;
import javax.swing.border.*;
import javax.swing.event.*;

import org.openide.*;
import org.openide.util.HelpCtx;

import org.netbeans.spi.debugger.ui.Controller;

import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.debugger.common2.values.Action;
import org.netbeans.modules.cnd.debugger.common2.values.CountLimit;
import org.netbeans.modules.cnd.debugger.common2.values.AId;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

// for ActionsPanel
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.EditWatchPanel;


/**
 * Common breakpoint customizer panel.
 * Abstract base class for per-type panels.
 * Contained in an EditBreakpointPanel.
 */

abstract public class BreakpointPanel extends javax.swing.JPanel
    implements DocumentListener, ItemListener, HelpCtx.Provider, ControllerProvider {

    
    protected NativeBreakpoint breakpoint;
    protected final JPanel panel_settings;
    private final ActionsPanel panel_actions;

    protected boolean customizing = false;

    /**
     * Override the verbose swing toString
     */
    // interface Object
    @Override
    public String toString() {
	String className = getClass().getName();
	int dotx = className.lastIndexOf('.');
	if (dotx != -1)
	    className = className.substring(dotx);
	return className + '@' + Integer.toHexString(hashCode());
    }

    private static Border makeBorder(String title) {
	Insets insets = new Insets(0, 8, 5, 7);
	return new CompoundBorder(new TitledBorder(new EtchedBorder(), title),
				  new EmptyBorder(insets));
    }

    /*
     * was: parts of IpeStopAction
     */
    private static class ActionsPanel extends JPanel {
	private DefaultComboBoxModel model;

	// scriptText is static so static getScript(), which is called on
	// ok() can retrieve the script.

	private static JTextArea scriptText = null;
	private JLabel scriptLabel = null;
	private JComboBox actionCombo;
	private JTextArea script;

	private static String lastActionScript = null;      // history
	private Action actionType = Action.STOP;

	Action getAction() {
	    return actionType;
	}

	// this SHOULD not be static:
	static String getScript() {
	    return rememberLastScript();
	}

	/**
	 * User is committing to this script, remember it for seeding next time.
	 */
	static String rememberLastScript() {
	    // ... and return current script
	    String text = null;
	    if (scriptText != null) {
		text = scriptText.getText();
		if (text.trim().length() == 0)
		    text = null;
	    }
	    lastActionScript = text;
	    return text;
	}

	ActionsPanel() {
	    model = new DefaultComboBoxModel(Action.getTags());

	    setBorder(makeBorder(Catalog.get("BORDER_Actions"))); // NOI18N
	    setLayout(new GridBagLayout());

	    GridBagConstraints gbc;

	    JLabel comboLabel = new JLabel(Catalog.get("LBL_ActionsCombo")); // NOI18N
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new java.awt.Insets(0, 0, 5, 10);
		gbc.anchor = java.awt.GridBagConstraints.WEST;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;

		add(comboLabel, gbc);

	    actionCombo = new JComboBox(model);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.insets = new java.awt.Insets(0, 0, 5, 0);
		gbc.anchor = java.awt.GridBagConstraints.WEST;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;

		add(actionCombo, gbc);

	    comboLabel.setLabelFor(actionCombo);

	    scriptLabel = new JLabel(Catalog.get("LBL_ActionsScript")); // NOI18N
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new java.awt.Insets(0, 0, 5, 10);
		gbc.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;

		add(scriptLabel, gbc);

	    scriptText = new JTextArea();
	    script = scriptText;
	    JScrollPane scrollPane = new JScrollPane(script);
		script.setColumns(30);
		script.setEditable(false);
		script.setEnabled(false);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.insets = new java.awt.Insets(0, 0, 0, 0);
		gbc.anchor = java.awt.GridBagConstraints.WEST;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = java.awt.GridBagConstraints.BOTH;
		gbc.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		gbc.gridheight = java.awt.GridBagConstraints.REMAINDER;

		add(scrollPane, gbc);

	    scriptLabel.setLabelFor(script);

	    // seed initial values
	    actionCombo.setSelectedItem(actionType.toString());

	    if (lastActionScript != null) {
		script.setText(lastActionScript);
	    } else {
		script.setText(Catalog.get("StopExample"));	// NOI18N
	    }
	    script.setCaretPosition(0);

	    adjustScript();

	    actionCombo.addActionListener(new java.awt.event.ActionListener() {
                @Override
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    String value = actionCombo.getSelectedItem().toString();
		    if (value == null)
			return;
		    actionType = Action.byTag(value);
		    adjustScript();
		}
	    });

	    // a11y
	    Catalog.setAccessibleDescription(actionCombo,
					     "ACSD_ActionsCombo");// NOI18N
	    comboLabel.setDisplayedMnemonic(
		Catalog.getMnemonic("MNEM_Actions"));		// NOI18N
	    Catalog.setAccessibleDescription(scriptText,
					     "ACSD_ActionsScript");// NOI18N
	    scriptLabel.setDisplayedMnemonic(
		Catalog.getMnemonic("MNEM_ActionsScript"));	// NOI18N
	}

	/**
	 * Set the enabledness of the Script text component based on the action
	 */
	private void adjustScript() {
	    if (actionType == Action.WHEN ||
		actionType == Action.WHENINSTR) {
		scriptLabel.setEnabled(true);
		scriptText.setEnabled(true);
		scriptText.setEditable(true);
	    } else {
		scriptLabel.setEnabled(false);
		scriptText.setEnabled(false);
		scriptText.setEditable(false);
	    }
	}

	void seed(NativeBreakpoint b) {
	    // assert customizing
	    // seed initial values
	    actionType = b.getAction();
	    actionCombo.setSelectedItem(actionType.toString());
	    script.setText(b.getScript());
	    script.setCaretPosition(0);
	    adjustScript();
	}
    }


    /** Creates new form LineBreakpointPanel */
    protected BreakpointPanel(NativeBreakpoint newBreakpoint,
			   boolean customizing) {

	this.customizing = customizing;
	breakpoint = newBreakpoint;

	setLayout(new GridBagLayout());

	GridBagConstraints gbc;

	/*
	 * Arrange so that ...
	 * horizontally: both panels expand to fill.
	 * vertically: the settings panel clings to it's contents
	 * while the actions panel expands vertically to give a bigger
	 * script area.
	 */

	panel_settings = new JPanel();
	    panel_settings.setBorder(makeBorder(Catalog.get("BORDER_Settings"))); // NOI18N

	    gbc = new GridBagConstraints();
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.weightx = 1.0;
	    gbc.weighty = 0.0;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.NORTHWEST;

	    add(panel_settings, gbc);

        CommonPanel panel_common = new CommonPanel();

	    gbc = new GridBagConstraints();
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.weightx = 1.0;
	    gbc.weighty = 0.0;
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.NORTHWEST;

	    add(panel_common, gbc);

	panel_actions = new ActionsPanel();

	    gbc = new GridBagConstraints();
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.weightx = 1.0;
	    gbc.weighty = 0.0;
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.anchor = GridBagConstraints.NORTHWEST;

	    add(panel_actions, gbc);
    }    


    /**
     * For a given text field, if it contains more than
     * space, returned the text (without surrounding whitespace),
     * otherwise return null.
     */

    private String trim(javax.swing.text.JTextComponent tf) {
	String s = tf.getText();
	if ((s != null) && (s.trim().length() > 0)) {
	    return s.trim();
  	} else {
	    return null;
	}
    }
    
    
    /*
     * @param	tf	translated field
     */
    protected boolean badField(String tf, String err) {
	Exception ex = new IllegalArgumentException();
	// OLD String fname = NbBundle.getMessage(BreakpointPanel.class, f);
	String msg = NbBundle.getMessage(BreakpointPanel.class,
				         "ERR_invalid_field",	// NOI18N
				         err,
				         tf);
	ErrorManager.getDefault().annotate(ex, msg);
	ErrorManager.getDefault().notify(ErrorManager.USER, ex);
	return false;
    }

    protected abstract void seed(NativeBreakpoint initialBpt);

    public abstract void setDescriptionEnabled(boolean enabled);

    // delegations of Controller interface to subclass
    abstract protected void assignProperties();
    abstract protected boolean propertiesAreValid();

    protected final void seedCommonComponents(NativeBreakpoint initialBpt) {
	assert breakpoint == initialBpt;

	javaCombo.setSelectedItem(initialBpt.getJava());
	whileField.setText(initialBpt.getWhileIn());
	conditionPane.setText(initialBpt.getCondition());
	threadField.setText(initialBpt.getThread());
	lwpField.setText(initialBpt.getLwp());
	tempCheckBox.setSelected(initialBpt.getTemp());

	if (initialBpt.hasCountLimit()) {
	    if (initialBpt.getCountLimit() == -1) {
		countLimitCombo.getEditor().
		    setItem(CountLimit.Keyword_INFINITY);
	    } else {
		countLimitCombo.getEditor().
		    setItem("" + initialBpt.getCountLimit());
	    }
	} else {
	    // We don't want Action_DISABLE to appear literally
	    countLimitCombo.getEditor().setItem("");
	}

	if (customizing) {
	    countField.setText("" + initialBpt.getCount());
	}

	panel_actions.seed(initialBpt);
    }


    /**
     * Validate generic fields like event modifiers.
     */

    private boolean validateFields() {

	if (javaCombo != null) {
	    String javaText = (String) javaCombo.getSelectedItem();
	    if (javaText != null) {
		if (javaText.equals(Catalog.get("Sessions_Table_ModeJava"))) { //NOI18N
		    breakpoint.setJava(true);
		} else {
		    breakpoint.setJava(false);
		}
	    }
	}

	String wf = trim(whileField);
	if (!IpeUtils.sameString(wf, breakpoint.getWhileIn())) {
	    breakpoint.setWhileIn(wf);
	    breakpoint.setQwhileIn(null);
	}

	String cf = trim(conditionPane);
	if (!IpeUtils.sameString(cf, breakpoint.getCondition())) {
	    breakpoint.setCondition(cf);
	    breakpoint.setQcondition(null);
	} 

	AId tid = new AId(threadField.getText(), false, false);
	if (tid.errorMessage != null) {
	    return badField(Catalog.get("PROP_thread"),	// NOI18N
			    tid.errorMessage);
	}
	breakpoint.setThread(tid.toString());


	AId lid = new AId(lwpField.getText(), true, false);
	if (lid.errorMessage != null) {
	    return badField(Catalog.get("PROP_lwp"),	// NOI18N
			    lid.errorMessage);
	}
	breakpoint.setLwp(lid.toString());

	breakpoint.setTemp(tempCheckBox.isSelected());


	String countLimitText = (String) countLimitCombo.getEditor().getItem();
	CountLimit countLimit = new CountLimit(countLimitText);
	if (countLimit.errorMessage() != null) {
	    return badField(Catalog.get("PROP_count_limit"),	// NOI18N
			    countLimit.errorMessage());
	}
	if (countLimit.isEnabled()) {
	    breakpoint.setCountLimit(countLimit.count(), countLimit.isEnabled());
	} else {
	    breakpoint.setCountLimit(1, countLimit.isEnabled());
	}

	return true;
    }

    ///////////////////////////////////////////////////////////////////////
    // Implements Controller
    ///////////////////////////////////////////////////////////////////////

    protected class BptController implements Controller {

	private final BreakpointPanel owner;

	private final PropertyChangeSupport pcs =
	    new PropertyChangeSupport(this);

	BptController(BreakpointPanel owner) {
	    this.owner = owner;
	}

	/**
	 * Called when "Ok" button is pressed.
	 *
	 * @return whether customizer can be closed
	 */

	// interface Controller
        @Override
	public boolean ok() {
	    if (!validateFields())
		return false;

	    breakpoint.setAction(panel_actions.getAction());
	    breakpoint.setScript(ActionsPanel.getScript());
	    
	    /* OLD
	    // return to subclasses ok() which is supposed to validate
	    // and eventually call post()
	    return true;
	    */

	    // Used to be in subclass ok() implementation. Now factored out:
	    if (!isValid())
		return false;

	    owner.assignProperties();
	    post();
	    return true;
	}

	/**
	 * Called when "Cancel" button is pressed.
	 *
	 * @return whether customizer can be closed
	 */

	// interface Controller
        @Override
	final public boolean cancel () {
	    return true;
	}

	// interface Controller
        @Override
	final public boolean isValid() {
	    return owner.propertiesAreValid();
	}
	
        // interface Controller
        @Override
        final public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }

        // interface Controller
        @Override
        final public void removePropertyChangeListener(PropertyChangeListener l)
 {
            pcs.removePropertyChangeListener(l);
        }

        protected void validChanged() {
            pcs.firePropertyChange(Controller.PROP_VALID, null, null);
        }
    }

    private final BptController controller = new BptController(this);

    @Override
    public Controller getController() {
	return controller;
    }

    /**
     * To be called by subclass to send the breakpoint to the engine.
     */
    private void post() {
	// This will cause a message to be sent to the engine to create
	// a breakpoint.
	// System.out.println("BreakpointPanel.post()");
	if (customizing) {
	    breakpoint.original().postChange(breakpoint, Gen.primary(null));
	} else {
	    breakpoint.postCreate();
	}
    }
    


    /** True iff we'return showing the details panel for the breakpoints */
    private static boolean showingDetails = false;
    
    private javax.swing.JSeparator separator;
//    private javax.swing.JButton moreButton;
    private javax.swing.JLabel whileLabel;
    private javax.swing.JTextField whileField;
    private javax.swing.JLabel conditionLabel;
    private JTextComponent conditionPane;
    private javax.swing.JLabel lwpLabel;
    private javax.swing.JTextField lwpField;
    private javax.swing.JLabel threadLabel;
    private javax.swing.JTextField threadField;
    private javax.swing.JLabel countLimitLabel;
    private javax.swing.JComboBox countLimitCombo;
    private javax.swing.JLabel countLabel;
    private javax.swing.JTextField countField;
    /* LATER
    private javax.swing.JLabel persistenceLabel;
    private javax.swing.JComboBox persistenceCombo;
    */
    // OLD private javax.swing.JLabel tempLabel;
    private javax.swing.JCheckBox tempCheckBox;
    private javax.swing.JLabel javaLabel;
    private javax.swing.JComboBox javaCombo = null;    


    /*
     * Called from subclass (usually constructor)
     * Subclass is also supposed to set the layout to GridBagLayout
     */
    protected void addCommonComponents(int y) {
        // does nothing as we decided to drop More/Less button
    }

    private class CommonPanel extends JPanel {

        protected CommonPanel() {
            setBorder(makeBorder(Catalog.get("BORDER_Common"))); // NOI18N
	    setLayout(new GridBagLayout());
            
            int y = 0;
    //	moreButton = new javax.swing.JButton();
            separator = new javax.swing.JSeparator();
            whileLabel = new javax.swing.JLabel();
            whileField = new javax.swing.JTextField();
            conditionLabel = new javax.swing.JLabel();
            lwpLabel = new javax.swing.JLabel();
            lwpField = new javax.swing.JTextField();
            threadLabel = new javax.swing.JLabel();
            threadField = new javax.swing.JTextField();
            countLimitLabel = new javax.swing.JLabel();
            countLimitCombo = new javax.swing.JComboBox();
            countLabel = new javax.swing.JLabel();
            countField = new javax.swing.JTextField();
            // OLD tempLabel = new javax.swing.JLabel();
            tempCheckBox = new javax.swing.JCheckBox(" "); // NOI18N
            /* LATER
            persistenceLabel = new javax.swing.JLabel();
            persistenceCombo = new javax.swing.JComboBox();
            */
            javaLabel = new javax.swing.JLabel();
            javaCombo = new javax.swing.JComboBox();

            //Add JEditorPane and context
            JComponent [] editorComponents = EditWatchPanel.createEditorComponent();
            conditionPane = (JTextComponent) editorComponents[1];
            JScrollPane conditionSP = (JScrollPane)editorComponents[0];

            java.awt.GridBagConstraints gridBagConstraints2;

    //        moreButton.setText(Catalog.get("More"));// NOI18N
    //        moreButton.setMnemonic(
    //	    Catalog.getMnemonic("MNEM_More"));	// NOI18N
    //        moreButton.addActionListener(new java.awt.event.ActionListener() {
    //            public void actionPerformed(java.awt.event.ActionEvent evt) {
    //                onLessMore();
    //            }
    //        });

    //        gridBagConstraints2 = new java.awt.GridBagConstraints();
    //        gridBagConstraints2.insets = new java.awt.Insets(5, 0, 5, 0);
    //        gridBagConstraints2.gridx = 0;
    //        gridBagConstraints2.gridy = y;
    //        // gridBagConstraints2.anchor = java.awt.GridBagConstraints.EAST;
    //        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
    //        panel_settings.add(moreButton, gridBagConstraints2);

    //        y++;

//            gridBagConstraints2 = new java.awt.GridBagConstraints();
//            gridBagConstraints2.insets = new java.awt.Insets(10, 0, 10, 0);
//            gridBagConstraints2.gridx = 0;
//            gridBagConstraints2.gridy = y;
//            gridBagConstraints2.gridwidth = java.awt.GridBagConstraints.REMAINDER;
//            gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
//            gridBagConstraints2.anchor = java.awt.GridBagConstraints.EAST;
//            add(separator, gridBagConstraints2);

//            y++;

            // Language mode field
            javaLabel.setText(Catalog.get("JavaBptMode"));	// NOI18N
            javaLabel.setDisplayedMnemonic(
                Catalog.getMnemonic("MNEM_JavaBptMode"));	// NOI18N
            javaLabel.setLabelFor(javaCombo);
            gridBagConstraints2 = new java.awt.GridBagConstraints();
            gridBagConstraints2.gridy = y;
            gridBagConstraints2.insets = new java.awt.Insets(0, 0, 0, 10);
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
    //        add(javaLabel, gridBagConstraints2);

            gridBagConstraints2 = new java.awt.GridBagConstraints();
            gridBagConstraints2.gridy = y;
            gridBagConstraints2.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
    //        add(javaCombo, gridBagConstraints2);

    //        y++;

            // Condition field
            conditionLabel.setText(Catalog.get("Condition"));	// NOI18N
            conditionLabel.setDisplayedMnemonic(
                Catalog.getMnemonic("MNEM_Condition"));		// NOI18N
            conditionLabel.setLabelFor(conditionSP);
            gridBagConstraints2 = new java.awt.GridBagConstraints();
            gridBagConstraints2.gridy = y;
            gridBagConstraints2.insets = new java.awt.Insets(0, 0, 0, 10);
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
            add(conditionLabel, gridBagConstraints2);

            gridBagConstraints2 = new java.awt.GridBagConstraints();
            gridBagConstraints2.gridy = y;
            gridBagConstraints2.gridwidth = 4;
            gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints2.insets = new java.awt.Insets(5, 0, 0, 0);
            add(conditionSP, gridBagConstraints2);

            y++;

            // Count fields
            countLimitLabel.setText(Catalog.get("CountLimit"));	// NOI18N
            countLimitLabel.setDisplayedMnemonic(
                Catalog.getMnemonic("MNEM_CountLimit"));		// NOI18N
            countLimitLabel.setLabelFor(countLimitCombo);
            gridBagConstraints2 = new java.awt.GridBagConstraints();
            gridBagConstraints2.gridy = y;
            gridBagConstraints2.insets = new java.awt.Insets(0, 0, 0, 10);
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
            add(countLimitLabel, gridBagConstraints2);

            countLimitCombo.setEditable(true);
            countLimitCombo.addItem(CountLimit.Action_INFINITY);
            if (customizing) {
                // Hmmm Action_DISABLE ends up appearing literally as opposed to
                // us getting an empty field.
                countLimitCombo.addItem(CountLimit.Action_DISABLE);
                countLimitCombo.addItem(CountLimit.Action_CURRENT);
            }
            countLimitCombo.getEditor().setItem("");
            gridBagConstraints2 = new java.awt.GridBagConstraints();
            gridBagConstraints2.gridy = y;
            gridBagConstraints2.insets = new java.awt.Insets(5, 0, 0, 0);
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
            add(countLimitCombo, gridBagConstraints2);

            Dimension ref_dim = threadField.getPreferredSize();
            Dimension combo_dim = countLimitCombo.getPreferredSize();
            combo_dim.height = ref_dim.height;
            countLimitCombo.setPreferredSize(combo_dim);

            javax.swing.JPanel ccountPanel = new javax.swing.JPanel();
            ccountPanel.setLayout(new java.awt.BorderLayout());
            ccountPanel.add(countLabel, java.awt.BorderLayout.WEST);
            ccountPanel.add(countField, java.awt.BorderLayout.CENTER);
            countLabel.setText(Catalog.get("CurrentCount")); // NOI18N
            countLabel.setLabelFor(countField);
            countLabel.setBorder(new javax.swing.border.EmptyBorder(
                new java.awt.Insets(0, 0, 0, 10))
            );
            countField.setEditable(false);
            countLabel.setDisplayedMnemonic(
                Catalog.getMnemonic("MNEM_CurrentCount"));	// NOI18N
            countField.setColumns(5);
            gridBagConstraints2 = new java.awt.GridBagConstraints();
            gridBagConstraints2.gridx = 2;
            gridBagConstraints2.gridy = y;
            gridBagConstraints2.insets = new java.awt.Insets(0, 20, 0, 0);
            gridBagConstraints2.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints2.weightx = 0.1;
            add(ccountPanel, gridBagConstraints2);

            y++;

            // While in field
            whileLabel.setText(Catalog.get("WhileIn"));	// NOI18N
            whileLabel.setDisplayedMnemonic(
                Catalog.getMnemonic("MNEM_WhileIn"));	// NOI18N
            whileLabel.setLabelFor(whileField);
            gridBagConstraints2 = new java.awt.GridBagConstraints();
            gridBagConstraints2.gridy = y;
            gridBagConstraints2.insets = new java.awt.Insets(0, 0, 0, 10);
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
            add(whileLabel, gridBagConstraints2);

            gridBagConstraints2 = new java.awt.GridBagConstraints();
            gridBagConstraints2.gridy = y;
            gridBagConstraints2.gridwidth = 4;
            gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints2.insets = new java.awt.Insets(5, 0, 0, 0);
            gridBagConstraints2.weightx = 1.0;
            add(whileField, gridBagConstraints2);

            y++;

            // LWP field
            lwpLabel.setText(Catalog.get("LWP"));	// NOI18N
            lwpLabel.setDisplayedMnemonic(
                Catalog.getMnemonic("MNEM_LWP"));	// NOI18N
            lwpLabel.setLabelFor(lwpField);
            gridBagConstraints2 = new java.awt.GridBagConstraints();
            gridBagConstraints2.gridy = y;
            gridBagConstraints2.insets = new java.awt.Insets(0, 0, 0, 10);
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
    //        add(lwpLabel, gridBagConstraints2);

            lwpField.setColumns(6);
            javax.swing.JPanel lwpPanel = new javax.swing.JPanel();
            lwpPanel.setLayout(new java.awt.BorderLayout());
            lwpPanel.add(lwpField, java.awt.BorderLayout.WEST);
            gridBagConstraints2 = new java.awt.GridBagConstraints();
            gridBagConstraints2.gridy = y;
            gridBagConstraints2.insets = new java.awt.Insets(5, 0, 0, 0);
            gridBagConstraints2.weightx = 1.0;
            gridBagConstraints2.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
            //add(lwpPanel, gridBagConstraints2);

            //y++;

            // Thread field
            threadLabel.setText(Catalog.get("Thread"));	// NOI18N
            threadLabel.setDisplayedMnemonic(
                Catalog.getMnemonic("MNEM_Thread"));	// NOI18N
            threadLabel.setLabelFor(threadField);
            gridBagConstraints2 = new java.awt.GridBagConstraints();
            gridBagConstraints2.gridy = y;
            gridBagConstraints2.insets = new java.awt.Insets(0, 0, 0, 10);
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
            add(threadLabel, gridBagConstraints2);

            threadField.setColumns(6);
            javax.swing.JPanel threadPanel = new javax.swing.JPanel();
            threadPanel.setLayout(new java.awt.BorderLayout());
            threadPanel.add(threadField, java.awt.BorderLayout.WEST);
            gridBagConstraints2 = new java.awt.GridBagConstraints();
            gridBagConstraints2.gridy = y;
            gridBagConstraints2.insets = new java.awt.Insets(5, 0, 0, 0);
            gridBagConstraints2.weightx = 1.0;
            gridBagConstraints2.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
            add(threadPanel, gridBagConstraints2);

            y++;

            // Temporary checkbox
            tempCheckBox.setText(Catalog.get("Temp"));	// NOI18N
            tempCheckBox.setMnemonic(
                Catalog.getMnemonic("MNEM_Temp"));		// NOI18N
            gridBagConstraints2 = new java.awt.GridBagConstraints();
            gridBagConstraints2.gridx = 1;
            gridBagConstraints2.gridy = y;
            gridBagConstraints2.insets = new java.awt.Insets(5, 0, 0, 0);
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
    //        add(tempCheckBox, gridBagConstraints2);

    //	y++;

            /* LATER
            persistenceLabel.setText(Catalog.get("Persistence")); // NOI18N
            gridBagConstraints2 = new java.awt.GridBagConstraints();
            gridBagConstraints2.gridy = y;
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
            add(persistenceLabel, gridBagConstraints2);

            gridBagConstraints2 = new java.awt.GridBagConstraints();
            gridBagConstraints2.gridy = y;
            gridBagConstraints2.insets = new java.awt.Insets(5, 0, 0, 0);
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
            add(persistenceCombo, gridBagConstraints2);
            */

            /* LATER
            gridBagConstraints1 = new java.awt.GridBagConstraints();
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridy = 2;
            gridBagConstraints1.gridwidth = 2;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints1.weightx = 0.8;
            gridBagConstraints1.weighty = 0.5;
            add(advancedPanel, gridBagConstraints1);
            */

            // 'showingDetails' is static, so as we switch eventtypes it doesn't
            // change
    //	adjustModifiers();


            /* LATER
            DefaultComboBoxModel model = new DefaultComboBoxModel(
                  new String[] {
                      Catalog.get("Configuration"), // NOI18N
                      Catalog.get("ConfigurationAcrossExec"), // NOI18N
                      Catalog.get("ConfigurationDebugeeOnly"), // NOI18N
                      Catalog.get("UntilFired") // NOI18N
                  });
            persistenceCombo.setModel(model);
            */

            if (javaCombo != null) {
                String def = Catalog.get("Default");// NOI18N
                String nat = Catalog.get("Sessions_Table_ModeNative");// NOI18N
                String jv = Catalog.get("Sessions_Table_ModeJava"); // NOI18N
                DefaultComboBoxModel model = new DefaultComboBoxModel(
                              new String[] { def, nat, jv });
                javaCombo.setModel(model);
            }



            whileField.getDocument().addDocumentListener(BreakpointPanel.this);
            conditionPane.getDocument().addDocumentListener(BreakpointPanel.this);
            lwpField.getDocument().addDocumentListener(BreakpointPanel.this);
            threadField.getDocument().addDocumentListener(BreakpointPanel.this);

            countLimitCombo.addItemListener(BreakpointPanel.this);
            javaCombo.addItemListener(BreakpointPanel.this);

            // Do the a11y stuff
            initA11y();
        }
    }

    private void initA11y() {
//            moreButton.getAccessibleContext().setAccessibleDescription(
//                moreButton.getText()
//            );
            whileField.getAccessibleContext().setAccessibleDescription(
                Catalog.get("ACSD_WhileIn") // NOI18N
            );
            if (conditionPane.getCaret() != null) {
                conditionPane.getAccessibleContext().setAccessibleDescription(
                    Catalog.get("ACSD_Condition") // NOI18N
                );
            }
            lwpField.getAccessibleContext().setAccessibleDescription(
                Catalog.get("ACSD_LWP") // NOI18N
            );
            threadField.getAccessibleContext().setAccessibleDescription(
                Catalog.get("ACSD_Thread") // NOI18N
            );
            countLimitCombo.getAccessibleContext().setAccessibleDescription(
                Catalog.get("ACSD_CountLimit") // NOI18N
            );
            countField.getAccessibleContext().setAccessibleDescription(
                Catalog.get("ACSD_CurrentCount") // NOI18N
            );
            tempCheckBox.getAccessibleContext().setAccessibleDescription(
                Catalog.get("ACSD_Temp") // NOI18N
            );
            javaCombo.getAccessibleContext().setAccessibleDescription(
                Catalog.get("ACSD_JavaBptMode") // NOI18N
            );
    }

    private void onLessMore() {
        showingDetails = !showingDetails;
	adjustModifiers();
    }

    private void adjustModifiers() {
	separator.setVisible(showingDetails);
	whileLabel.setVisible(showingDetails);
	whileField.setVisible(showingDetails);
	conditionLabel.setVisible(showingDetails);
	conditionPane.setVisible(showingDetails);
	lwpLabel.setVisible(showingDetails);
	lwpField.setVisible(showingDetails);
	threadLabel.setVisible(showingDetails);
	threadField.setVisible(showingDetails);
	countLimitLabel.setVisible(showingDetails);
	countLimitCombo.setVisible(showingDetails);
	countLabel.setVisible(showingDetails);
	countField.setVisible(showingDetails);
	// OLD tempLabel.setVisible(showingDetails);
	tempCheckBox.setVisible(showingDetails);
	/* LATER
	persistenceLabel.setVisible(showingDetails);
	persistenceCombo.setVisible(showingDetails);
	*/
	javaLabel.setVisible(showingDetails);
	javaCombo.setVisible(showingDetails);

//        if (showingDetails) {
//            moreButton.setText(Catalog.get("Less"));	// NOI18N
//            moreButton.setMnemonic(
//		Catalog.getMnemonic("MNEM_Less"));	// NOI18N
//        } else {
//            moreButton.setText(Catalog.get("More"));	// NOI18N
//	    moreButton.setMnemonic(
//		Catalog.getMnemonic("MNEM_More"));	// NOI18N
//        }

        // Change accessible description according to current state
//        moreButton.getAccessibleContext().setAccessibleDescription(
//	    moreButton.getText()
//        );
            
        revalidate();
        Window w = SwingUtilities.windowForComponent(this);
        if (w == null) return;
        w.pack();
    }

    /** When true, we've scheduled a check for the next iteration which
	has not yet been run. Used to avoid duplicate checks, since combo
	box events seem to throw a large number of updates */
    private boolean checkPlanned = false;
    
    /** Add a prop-valid check the -next- iteration through the event loop.
	This is necessary because when we're dealing with combo boxes,
	lots of weirdness occurs if we do an immediate valid check. For example,
	as part of leaving focus (which happens when we try to read the value
	out of the combo box), it "sets" the text of the text field which
	calls remove, which in turn causes a text update which finally results
	in valid=false.  This temporary setting the button to false, then
	seems to interact with the action callback on the ok button..
     */

    private void checkValidSoon() {
	synchronized(this) {
	    if (checkPlanned) {
		return;
	    } else {
		checkPlanned = true;
	    } 
	}
	SwingUtilities.invokeLater(new Runnable() {
                @Override
		public void run() {
		    synchronized(BreakpointPanel.this) {
			checkPlanned = false;
		    }
		    // OLD firePropertyChange(Controller.PROP_VALID, null, null);
		    controller.validChanged();
		}
	    });
    }


    // Implements DocumentListener
    @Override
    public void changedUpdate(DocumentEvent e) {
	checkValidSoon();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
	checkValidSoon();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
	checkValidSoon();
    }


    // interface ItemListener
    @Override
    public void itemStateChanged(ItemEvent e) {
	checkValidSoon();
    }

    // Implements HelpCtx.Provider
    @Override
    public HelpCtx getHelpCtx () {
	return new HelpCtx ("Breakpoints");	// NOI18N
    }
}
