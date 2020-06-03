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

package  org.netbeans.modules.cnd.makefile.wizard;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.netbeans.modules.cnd.makefile.utils.IpeFileSystemView;
import org.netbeans.modules.cnd.utils.ui.CndUIUtilities;

/**
 * Create the basic compiler flags panel in the Makefile wizard.
 */
/* Implements FocusListener because if you selected something in a text field,
 * then clicked in a different text field, your selection would persist in the
 * previous text field. Need to lose the selection when you lose the focus.
 * This issue is when using jdk 1.4.0.
 */
public class CompilerPathPanel extends MakefileWizardPanel implements FocusListener {
    /** Serial version number */
    static final long serialVersionUID = 1334257510688903149L;

    // the fields in the panel...
    private JTextField	    c;
    private JTextField	    cpp;
    private JTextField	    f95;
    private JTextField	    asm;

    private boolean	    initialized;
    private JFileChooser    fc;

    /** Save the chooser directory for subsequent choosers calls */
    private File	    chooserDir;

    /**
     * Constructor for the compiler paths panel.
     */
    public CompilerPathPanel(MakefileWizard wd) {
	super(wd);
	String subtitle = getString("LBL_CompilerPathPanel"); // NOI18N
	setSubTitle(subtitle);
	this.getAccessibleContext().setAccessibleDescription(subtitle);
	initialized = false;
    }


    /** Defer widget creation until the panel needs to be displayed */
    private void create() {

        setLayout(new java.awt.GridBagLayout());
	GridBagConstraints grid = new GridBagConstraints();
	grid.fill = GridBagConstraints.HORIZONTAL;

	JLabel pathLabel = new JLabel(getString("LBL_CompilerPaths"));	// NOI18N
	grid.anchor = GridBagConstraints.NORTHWEST;
	grid.gridx = 0;
	grid.gridy = 0;
	grid.gridwidth = GridBagConstraints.REMAINDER;
	add(pathLabel, grid, -1);

	c = createLine(1, getString("LBL_C"), getString("MNEM_C"));	    // NOI18N
	cpp = createLine(2, getString("LBL_Cpp"), getString("MNEM_Cpp"));   // NOI18N
	f95 = createLine(4, getString("LBL_F95"), getString("MNEM_F95"));   // NOI18N
	asm = createLine(5, getString("LBL_Asm"), getString("MNEM_Asm"));   // NOI18N

	c.addFocusListener(this);
	cpp.addFocusListener(this);
	f95.addFocusListener(this);
	asm.addFocusListener(this);

	grid.gridx = 0;
	grid.gridy = 7;
	grid.gridwidth = GridBagConstraints.REMAINDER;
	grid.gridheight = GridBagConstraints.REMAINDER;
	grid.weightx = 1.0;
	grid.weighty = 1.0;
	add(new JLabel(""), grid);					// NOI18N
    }

    private JTextField createLine(int lnum, String label, String mnemonic) {
	GridBagConstraints grid = new GridBagConstraints();
	grid.gridy = lnum;

	JLabel nueLabel = new JLabel(label);
	nueLabel.setDisplayedMnemonic(mnemonic.charAt(0));
	grid.anchor = GridBagConstraints.WEST;
	grid.gridx = 0;
	grid.gridwidth = 1;
	if (lnum == 1) {
	    grid.insets = new Insets(5, 0, 0, 0);
	} else {
	    grid.insets = new Insets(11, 0, 0, 0);
	}
	add(nueLabel, grid);

	JTextField nueText = new JTextField();
	nueLabel.setLabelFor(nueText);
	grid.anchor = GridBagConstraints.WEST;
	grid.gridx = 1;
	grid.gridwidth = GridBagConstraints.RELATIVE;
	grid.fill = GridBagConstraints.HORIZONTAL;
	grid.weightx = 1.0;
	grid.insets = new Insets(11, 12, 0, 0);
	add(nueText, grid);

	JButton chooser = new JButton(getString("BTN_Chooser"));	// NOI18N
	String mnem = "MNEM_Chooser" + lnum; // NOI18N
	chooser.setMnemonic(getString(mnem).charAt(0));
	grid.anchor = GridBagConstraints.WEST;
	grid.gridx = 2;
	grid.gridwidth = 1;
	grid.fill = GridBagConstraints.NONE;
	grid.weightx = 0.0;
	grid.insets = new Insets(11, 5, 0, 0);
	add(chooser, grid);
	createChooser(nueText, chooser);

	return nueText;
    }


    /**
     *  Create a FileChoose for the text field.
     */
    protected void createChooser(final JTextField text, final JButton chooser) {

	chooser.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
	    
		if (fc == null) {
		    fc = new JFileChooser();
		    fc.setApproveButtonText(getString("BTN_Approve"));	// NOI18N
		    fc.setFileSystemView(new
				IpeFileSystemView(fc.getFileSystemView()));
		    fc.setDialogTitle(
				getString("DLG_FILE_CHOOSER_TITLE"));	// NOI18N
		}

		if (chooserDir == null) {
		    chooserDir = new File(getMakefileData().getBaseDirectory());
		}
		fc.setCurrentDirectory(chooserDir);
		int returnVal = fc.showDialog(CompilerPathPanel.this, null);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    chooserDir = fc.getCurrentDirectory();
		    text.setText(fc.getSelectedFile().getAbsolutePath());
		}
	    }
	});
    }


    /** Create the widgets if not initialized */
    @Override
    public void addNotify() {

	if (!initialized) {
	    create();
	    initialized = true;
	}

	MakefileData md = getMakefileData();

	c.setText(md.getCCompiler(md.getToolset()));
	cpp.setText(md.getCppCompiler(md.getToolset()));
	f95.setText(md.getFCompiler(md.getToolset()));
	asm.setText(md.getAsmPath());

	super.addNotify();
	c.selectAll();

	CndUIUtilities.requestFocus(c);
    }


    /** Get the data from the panel and update the target */
    @Override
    public void removeNotify() {
	super.removeNotify();

	if (fc != null && fc.isShowing()) {
	    Object o = fc.getTopLevelAncestor();
	    if (o != null && o instanceof JDialog) {
		((JDialog) o).dispose();
	    }
	}

	MakefileData md = getMakefileData();
	int i;

	if (c.getText().length() > 0) {
	    md.setCCompiler(md.getToolset(), c.getText());
	}

	if (cpp.getText().length() > 0) {
	    md.setCppCompiler(md.getToolset(), cpp.getText());
	}

	if (f95.getText().length() > 0) {
	    md.setFCompiler(md.getToolset(), f95.getText());
	}

	if (asm.getText().length() > 0) {
	    md.setAsmPath(asm.getText());
	}
    }

    public void focusGained(FocusEvent evt) {
    }

    public void focusLost(FocusEvent evt) {
	((JTextField) evt.getComponent()).setSelectionEnd(0);
    }
}

