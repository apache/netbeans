/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package  org.netbeans.modules.cnd.makefile.wizard;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.cnd.utils.ui.CndUIUtilities;

/**
 * Create the Sources panel in the Makefile wizard.
 */
/* Implements FocusListener because if you selected something in a text field,
 * then clicked in a different text field, your selection would persist in the
 * previous text field. Need to lose the selection when you lose the focus.
 * This issue is when using jdk 1.4.0.
 */
public final class CompilerOptionsPanel extends MakefileWizardPanel
                                        implements FocusListener {

    /** Serial version number */
    static final long serialVersionUID = 5260017369797781413L;

    private JTextArea topLabel;
    private JTextField cText;
    private JTextField cppText;
    private JTextField fortranText;
    private JTextField basicText;
    private CompilerFlags copts;
    private JLabel cppCompilerLabel;
    private JComboBox cConformLevelCB;
    private ActionListener cConformLevelCBActionListener;
    private JComboBox cppConformLevelCB;
    private ActionListener cppConformLevelCBActionListener;

    private boolean initialized;


    /** Constructor for the compiler options panel */
    public CompilerOptionsPanel(MakefileWizard wd) {
	super(wd);
	String subtitle = getString("LBL_CompilerOptionsPanel"); // NOI18N
	setSubTitle(subtitle);
	this.getAccessibleContext().setAccessibleDescription(subtitle);
	initialized = false;
    }


    /**
     *  Create the panel. Since this class is used as a superclass for several
     *  panels information is passed in to allow some customization at creation
     *  time.
     *
     *  @param label	The label for the text field at the top of the panel
     *  @param flags	Set some optional flags to override default behavior
     */
    protected void create() {
	int gridy = 0;

        setLayout(new GridBagLayout());
	GridBagConstraints grid = new GridBagConstraints();
	grid.anchor = GridBagConstraints.NORTHWEST;
	grid.gridx = 0;

	topLabel = new JTextArea(getString("LBL_COptsTopLabel"));    // NOI18N
	topLabel.addFocusListener(this);
	topLabel.setEditable(false);
	topLabel.setFocusable(false);
        topLabel.setBackground(new JPanel().getBackground());
	grid.gridy = gridy++;
	grid.fill = GridBagConstraints.HORIZONTAL;
	grid.gridwidth = GridBagConstraints.REMAINDER;
	grid.weightx = 1.0;
	add(topLabel, grid);

	grid.insets.top = 5;

	// C language fields
	JLabel cLabel = new JLabel(getString("LBL_COptsCCompiler"));   // NOI18N
	cLabel.setDisplayedMnemonic(getString("MNEM_COptsCCompiler").charAt(0));  // NOI18N
	grid.gridx = 0;
	grid.gridy = gridy++;
	grid.fill = GridBagConstraints.NONE;
	grid.gridwidth = 1;
	grid.insets.right = 11;
	grid.insets.top = 11;
	grid.weightx = 0.0;
	add(cLabel, grid);

	cText = new JTextField();
	cText.addFocusListener(this);
	cLabel.setLabelFor(cText);
	grid.gridx = 1;
	grid.fill = GridBagConstraints.HORIZONTAL;
	grid.gridwidth = GridBagConstraints.REMAINDER;
	grid.insets.right = 0;
	add(cText, grid);

        JLabel cCompilerLabel = new javax.swing.JLabel();
        cCompilerLabel.setText(getString("LBL_ConformLevel")); // NOI18N
	cCompilerLabel.setDisplayedMnemonic(getString("MNEM_ConformLevel1").charAt(0));    // NOI18N
        grid.gridx = 1;
        grid.gridy = ++gridy;
	grid.gridwidth = 1;
	grid.fill = GridBagConstraints.NONE;
        grid.insets = new java.awt.Insets(8, 0, 0, 0);
        grid.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(cCompilerLabel, grid);

        cConformLevelCB = new JComboBox();
        cCompilerLabel.setLabelFor(cConformLevelCB);
        grid.gridx = 2;
        grid.gridy = gridy++;
	grid.gridwidth = 1;
	grid.fill = GridBagConstraints.NONE;
        grid.insets = new java.awt.Insets(5, 5, 0, 0);
        grid.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(cConformLevelCB, grid);

	// C++ language fields
	JLabel cppLabel = new JLabel(getString("LBL_COptsCppCompiler"));   // NOI18N
	cppLabel.setDisplayedMnemonic(getString("MNEM_COptsCppCompiler").charAt(0));    // NOI18N
	grid.gridx = 0;
	grid.gridy = gridy++;
	grid.fill = GridBagConstraints.NONE;
	grid.gridwidth = 1;
        grid.insets = new java.awt.Insets(0, 0, 0, 0);
	grid.insets.right = 11;
	grid.insets.top = 11;
	grid.weightx = 0.0;
	add(cppLabel, grid);

	cppText = new JTextField();
	cppText.addFocusListener(this);
	cppLabel.setLabelFor(cppText);
	grid.gridx = 1;
	grid.fill = GridBagConstraints.HORIZONTAL;
	grid.gridwidth = GridBagConstraints.REMAINDER;
	grid.insets.right = 0;
	add(cppText, grid);

        cppCompilerLabel = new javax.swing.JLabel();
        grid.gridx = 1;
        grid.gridy = ++gridy;
	grid.gridwidth = 1;
	grid.fill = GridBagConstraints.NONE;
        grid.insets = new java.awt.Insets(8, 0, 0, 0);
        grid.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(cppCompilerLabel, grid);

        cppConformLevelCB = new JComboBox();
        cppCompilerLabel.setLabelFor(cppConformLevelCB);
        grid.gridx = 2;
        grid.gridy = gridy++;
	grid.gridwidth = 1;
	grid.fill = GridBagConstraints.NONE;
        grid.insets = new java.awt.Insets(5, 5, 0, 0);
        grid.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(cppConformLevelCB, grid);

	// Fortran language fields
	JLabel fortranLabel = new JLabel(getString("LBL_COptsFortranCompiler"));   // NOI18N
	fortranLabel.setDisplayedMnemonic(
			getString("MNEM_COptsFortranCompiler").charAt(0));    // NOI18N
	grid.gridx = 0;
	grid.gridy = gridy++;
	grid.fill = GridBagConstraints.NONE;
	grid.gridwidth = 1;
        grid.insets = new java.awt.Insets(0, 0, 0, 0);
	grid.insets.right = 11;
	grid.insets.top = 11;
	grid.weightx = 0.0;
	add(fortranLabel, grid);

	fortranText = new JTextField();
	fortranText.addFocusListener(this);
	fortranLabel.setLabelFor(fortranText);
	grid.gridx = 1;
	grid.fill = GridBagConstraints.HORIZONTAL;
	grid.gridwidth = GridBagConstraints.REMAINDER;
	grid.insets.right = 0;
	add(fortranText, grid);

	// Show a read-only image of Basic Options
	JLabel basicLabel = new JLabel(getString("LBL_BasicOptionsVar"));	// NOI18N
	grid.gridx = 0;
	grid.gridy = gridy++;
	grid.fill = GridBagConstraints.NONE;
	grid.gridwidth = 1;
	grid.insets.right = 11;
	grid.weightx = 0.0;
	add(basicLabel, grid);

	basicText = new JTextField();
	basicText.addFocusListener(this);
	basicText.setEditable(false);
	basicText.setFocusable(false);
	basicText.setBackground(basicLabel.getBackground());
	basicLabel.setLabelFor(basicText);
	grid.gridwidth = GridBagConstraints.REMAINDER;
	grid.gridx = 1;
	grid.fill = GridBagConstraints.HORIZONTAL;
	grid.gridwidth = GridBagConstraints.REMAINDER;
	grid.insets.right = 0;
	grid.weightx = 1.0;
	grid.weighty = 1.0;
	add(basicText, grid);

	// Fill in extra space below the last visible widget
	grid.gridy = gridy++;
	grid.gridheight = GridBagConstraints.REMAINDER;
	add(new JLabel(""), grid); // NOI18N

	topLabel.setBackground(cLabel.getBackground());

	// Add action and key listeners
        cConformLevelCB.addActionListener(cConformLevelCBActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cConformLevelCBActionPerformed(evt);
            }
        });

        cText.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                cTextKeyReleased(evt);
            }
        });

        cppConformLevelCB.addActionListener(cppConformLevelCBActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cppConformLevelCBActionPerformed(evt);
            }
        });

        cppText.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                cppTextKeyReleased(evt);
            }
        });
    }


    /** Initialize the display values as we enter the panel */
    public void addNotify() {

	if (!initialized) {
	    initialized = true;
	    
	    create();
	    copts = getMakefileData().getCompilerFlags();
	}

	super.addNotify();

	cText.setText(copts.getCFlags(getMakefileData().getToolset()));
	cppText.setText(copts.getCcFlags(getMakefileData().getToolset()));
	fortranText.setText(copts.getF90Flags());
	basicText.setText(copts.getBasicOptions(getMakefileData().getToolset()));
	cText.selectAll();
	CndUIUtilities.requestFocus(cText);

	// Compiler conformance level
	cConformLevelCB.removeActionListener(cConformLevelCBActionListener);
	cppConformLevelCB.removeActionListener(cppConformLevelCBActionListener);
	cConformLevelCB.removeAllItems();
	cppConformLevelCB.removeAllItems();
	if (getMakefileData().getToolset() == MakefileData.SUN_TOOLSET_TYPE) {
	    cConformLevelCB.addItem(getString("CSunConform0"));
	    cConformLevelCB.addItem(getString("CSunConform1"));
	    cConformLevelCB.addItem(getString("CSunConform2"));
	    cConformLevelCB.setSelectedIndex(getMakefileData().getConformLevelCSun());

	    cppCompilerLabel.setText(getString("LBL_Compatibility"));
	    cppCompilerLabel.setDisplayedMnemonic(getString("MNEM_Compatibility").charAt(0));    // NOI18N
	    cppConformLevelCB.addItem(getString("CppSunConform0"));
	    cppConformLevelCB.addItem(getString("CppSunConform1"));
	    cppConformLevelCB.setSelectedIndex(getMakefileData().getConformLevelCppSun());
	}
	else {
	    cConformLevelCB.addItem(getString("CGNUConform0"));
	    cConformLevelCB.addItem(getString("CGNUConform1"));
	    cConformLevelCB.setSelectedIndex(getMakefileData().getConformLevelCGNU());

	    cppCompilerLabel.setText(getString("LBL_ConformLevel"));
	    cppCompilerLabel.setDisplayedMnemonic(getString("MNEM_ConformLevel2").charAt(0));    // NOI18N
	    cppConformLevelCB.addItem(getString("CppGNUConform0"));
	    cppConformLevelCB.addItem(getString("CppGNUConform1"));
	    cppConformLevelCB.setSelectedIndex(getMakefileData().getConformLevelCppGNU());
	}
	cConformLevelCB.addActionListener(cConformLevelCBActionListener);
	cppConformLevelCB.addActionListener(cppConformLevelCBActionListener);
    }


    /** Check the display values as we leave the panel and update CompilerFlags if needed */
    public void removeNotify() {
	super.removeNotify();

	copts.setCFlags(getMakefileData().getToolset(), cText.getText());
	copts.setCcFlags(getMakefileData().getToolset(), cppText.getText());
	copts.setF90Flags(fortranText.getText());

	// Compiler conformance level
	if (getMakefileData().getToolset() == MakefileData.SUN_TOOLSET_TYPE) {
	    getMakefileData().setConformLevelCSun(cConformLevelCB.getSelectedIndex());
	    getMakefileData().setConformLevelCppSun(cppConformLevelCB.getSelectedIndex());
	}
	else {
	    getMakefileData().setConformLevelCGNU(cConformLevelCB.getSelectedIndex());
	    getMakefileData().setConformLevelCppGNU(cppConformLevelCB.getSelectedIndex());
	}
    }

    public void focusGained(FocusEvent evt) {
	if (evt.getComponent() == basicText ||
	    evt.getComponent() == topLabel) {
	    ((JTextComponent) evt.getComponent()).selectAll();
	}
    }
    public void focusLost(FocusEvent evt) {
	((JTextComponent) evt.getComponent()).setSelectionEnd(0);
    }

    /**
     * Track c conformance level combobox selection and transfer option to textfield
     */
    private void cConformLevelCBActionPerformed(ActionEvent evt) {
	String newTxt;
	String[] levels;
	if (getMakefileData().getToolset() == MakefileData.SUN_TOOLSET_TYPE) {
	    levels = MakefileData.conformLevelsCSun;
	}
	else {
	    levels = MakefileData.conformLevelsCGNU;
	}
	int i = cConformLevelCB.getSelectedIndex();

	newTxt = removeOptions(cText.getText(), levels);
	newTxt = addOption(newTxt, levels[i]);
	cText.setText(newTxt);
    }

    /**
     * Track cpp conformance level combobox selection and transfer option to textfield
     */
    private void cppConformLevelCBActionPerformed(ActionEvent evt) {
	String newTxt;
	String[] levels;
	if (getMakefileData().getToolset() == MakefileData.SUN_TOOLSET_TYPE) {
	    levels = MakefileData.conformLevelsCppSun;
	}
	else {
	    levels = MakefileData.conformLevelsCppGNU;
	}
	int i = cppConformLevelCB.getSelectedIndex();

	newTxt = removeOptions(cppText.getText(), levels);
	newTxt = addOption(newTxt, levels[i]);
	cppText.setText(newTxt);
    }

    /**
     * Track c conformance level textfield and transfer options to combobox selection
     */
    private void cTextKeyReleased(java.awt.event.KeyEvent evt) {
	String txt = cText.getText();
	String[] levels;
	if (getMakefileData().getToolset() == MakefileData.SUN_TOOLSET_TYPE) {
	    levels = MakefileData.conformLevelsCSun;
	}
	else {
	    levels = MakefileData.conformLevelsCGNU;
	}
	int def = 0;
	boolean foundOne = false;

	cConformLevelCB.removeActionListener(cConformLevelCBActionListener);
	for (int i = 0; i < levels.length; i++) {
	    if (levels[i].length() == 0) {
		def = i;// The defalut value if no options found
		continue;
	    }
	    if (containsOption(txt, levels[i]) >= 0) {
		cConformLevelCB.setSelectedIndex(i);
		foundOne = true;
		break;
	    }    
	}
	if (!foundOne) {
	    cConformLevelCB.setSelectedIndex(def);
	}
	cConformLevelCB.addActionListener(cConformLevelCBActionListener);
    }

    /**
     * Track cpp conformance level textfield and transfer options to combobox selection
     */
    private void cppTextKeyReleased(java.awt.event.KeyEvent evt) {
	String txt = cppText.getText();
	String[] levels;
	if (getMakefileData().getToolset() == MakefileData.SUN_TOOLSET_TYPE) {
	    levels = MakefileData.conformLevelsCppSun;
	}
	else {
	    levels = MakefileData.conformLevelsCppGNU;
	}
	int def = 0;
	boolean foundOne = false;

	cppConformLevelCB.removeActionListener(cppConformLevelCBActionListener);
	for (int i = 0; i < levels.length; i++) {
	    if (levels[i].length() == 0) {
		def = i;// The defalut value if no options found
		continue;
	    }
	    if (containsOption(txt, levels[i]) >= 0) {
		cppConformLevelCB.setSelectedIndex(i);
		foundOne = true;
		break;
	    }    
	}
	if (!foundOne) {
	    cppConformLevelCB.setSelectedIndex(def);
	}
	cppConformLevelCB.addActionListener(cppConformLevelCBActionListener);
    }

    /**
     * returns position of option 'option' in a text String. Returns -1 if not found.
     */
    private int containsOption(String txt, String option) {
        int i;
	int start = 0;
        int lTxt = txt.length();
        int lOpt = option.length();
        
	while (start <= (lTxt - lOpt)) {
	    i = txt.indexOf(option, start);
	    if (i < 0) {
		return -1;
	    }
	    if (i > 0 && !Character.isWhitespace(txt.charAt(i-1))) {
		start = i+1;
		continue;
	    }
	    if (i < (lTxt - lOpt) && !Character.isWhitespace(txt.charAt(i + lOpt))) {
		start = i+1;
		continue;
	    }
	    return i;
	}
        return -1;
    }

    /**
     * Removes an array of options 'options' from text String. Returns modified text string.
     */
    private String removeOptions(String txt, String[] options) {
        String newTxt = txt;
	for (int i = 0; i < options.length; i++) {
	    newTxt = removeOption(newTxt, options[i]);
	}
	return newTxt;
    }
    
    /**
     * Removes option 'option' from text String. Returns modified text string.
     */
    private String removeOption(String txt, String option) {
        int i;
        String newTxt;
        
	if (option == null || option.length() == 0)
	    return txt;

        i = containsOption(txt, option);
        if (i < 0)
            return txt;
        
        if (i == 0) {
            newTxt = txt.substring(i + option.length());
        }
        else {
            newTxt = txt.substring(0, i-1) + txt.substring(i + option.length());
        }
        return newTxt;
    }
    
    /**
     * Add an option 'option' to text String (and possible also a space). Returns modified text string.
     */
    private String addOption(String txt, String option) {
        int i;
	String white = ""; // NOI18N
        String newTxt;
        
        i = containsOption(txt, option);
        if (i >= 0)
            return txt;
        
	if (txt.length() > 0 && !Character.isWhitespace(txt.charAt(txt.length()-1)))
	    white = " "; // NOI18N
        newTxt = txt + white + option;
        
        return newTxt;
    }
}
