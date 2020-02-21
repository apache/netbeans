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

package org.netbeans.modules.cnd.makefile.wizard;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import org.netbeans.modules.cnd.utils.ui.CndUIUtilities;

/**
 * Create the basic compiler flags panel in the Makefile wizard.
 */

public class BasicFlagsPanel extends MakefileWizardPanel {

    /** Serial version number */
    static final long serialVersionUID = -2105207605584107631L;

    /** Code Development flags are active */
    private JRadioButton    develBuildRB;

    /** Code Development subflag: Just compile with optimization */
    private JRadioButton    develDebugCodeRB;

    /** Code Development subflag: Compile with optimization and debugging */
    private JRadioButton    develOptDebugCodeRB;

    ///** Code Development subflag: Compile with SourceBrowser information */
    //private JCheckBox	    sbrowseTB;

    /** Final Code flags are active */
    private JRadioButton    finalBuildRB;

    /** Final Code subflag: Build optimized code */
    private JCheckBox	    finalOptTB;

    /** Final Code subflag: Strip symbolic information */
    private JCheckBox	    finalStripTB;

    /** The application should be compiled as a 64 bit binary */
    private JCheckBox	    create64BitTB;

    /** Do f90 module reordering in generated Makefile if set */
    private JCheckBox	    moduleSupportTB;

    /** The gui has been initialized */
    private boolean	    initialized;


    /**
     * Constructor for the Standard Libraries panel.
     */
    public BasicFlagsPanel(MakefileWizard wd) {
	super(wd);
	String subtitle = getString("LBL_BasicFlagsPanel"); // NOI18N
	setSubTitle(subtitle);
	this.getAccessibleContext().setAccessibleDescription(subtitle);
	initialized = false;
    }


    /** Defer widget creation until the panel needs to be displayed */
    private void create() {

        setLayout(new java.awt.GridBagLayout());
	GridBagConstraints grid = new GridBagConstraints();
	grid.anchor = GridBagConstraints.NORTHWEST;
	grid.fill = GridBagConstraints.HORIZONTAL;
	grid.gridx = 0;
	grid.gridy = GridBagConstraints.RELATIVE;
	grid.gridwidth = GridBagConstraints.REMAINDER;
	grid.weightx = 1.0;

	// Create the Development/Final code build section
	add(new JLabel(getString("LBL_BuildMode")), grid);		// NOI18N

	develBuildRB = new JRadioButton(getString("RB_DevelBuild"));	// NOI18N
	develBuildRB.setSelected(true);
	develBuildRB.setMnemonic(
			getString("MNEM_DevelBuild").charAt(0));	// NOI18N
	add(develBuildRB, grid);

	develDebugCodeRB = new JRadioButton(getString("RB_DebugFlag"));	// NOI18N
	develDebugCodeRB.setSelected(true);
	develDebugCodeRB.setMnemonic(
			getString("MNEM_DebugFlag").charAt(0));		// NOI18N
	grid.insets = new Insets(0, 17, 0, 0);
	add(develDebugCodeRB, grid);

	develOptDebugCodeRB =
			new JRadioButton(getString("RB_OptDebugFlag"));	// NOI18N
	develOptDebugCodeRB.setMnemonic(
			getString("MNEM_OptDebugFlag").charAt(0));	// NOI18N
	add(develOptDebugCodeRB, grid);

	ButtonGroup develGroup = new ButtonGroup();
	develGroup.add(develDebugCodeRB);
	develGroup.add(develOptDebugCodeRB);

	/* Not currently part of Krakatoa. I'm leaving the code here in case
	 * it gets added later (grp - 4/01).
	sbrowseTB = new JCheckBox(getString("TB_SourceBrowseFlag"));	// NOI18N
	sbrowseTB.setMnemonic(
			getString("MNEM_SourceBrowseFlag").charAt(0));	// NOI18N
	add(sbrowseTB, grid);
	 */

	finalBuildRB = new JRadioButton(getString("RB_FinalBuild"));	// NOI18N
	finalBuildRB.setMnemonic(
			getString("MNEM_FinalBuild").charAt(0));	// NOI18N
	grid.insets.top = 5;
	grid.insets.left = 0;
	add(finalBuildRB, grid);

	finalOptTB = new JCheckBox(getString("TB_OptimizeFlag"));	// NOI18N
	finalOptTB.setMnemonic(
			getString("MNEM_OptimizeFlag").charAt(0));	// NOI18N
	finalOptTB.setEnabled(false);
	grid.insets.top = 0;
	grid.insets.left = 17;
	add(finalOptTB, grid);

	finalStripTB = new JCheckBox(getString("TB_StripFlag"));	// NOI18N
	finalStripTB.setMnemonic(getString("MNEM_StripFlag").charAt(0));// NOI18N
	finalStripTB.setEnabled(false);
	add(finalStripTB, grid);

	// Group the Code Development and Final Code RadioButtons
	ButtonGroup buildGroup = new ButtonGroup();
	buildGroup.add(develBuildRB);
	buildGroup.add(finalBuildRB);

	create64BitTB = new JCheckBox(getString("TB_64BitFlag"));	// NOI18N
	create64BitTB.setMnemonic(getString("MNEM_64BitFlag").charAt(0));	// NOI18N
	grid.insets.top = 11;
	grid.insets.left = 0;
	add(create64BitTB, grid);

	moduleSupportTB = new JCheckBox(getString("TB_EnableModuleSupport"));	// NOI18N
	moduleSupportTB.setMnemonic(
			    getString("MNEM_EnableModuleSupport").charAt(0)); // NOI18N
	grid.insets.top = 5;
	add(moduleSupportTB, grid);

//xxx
	/*
	grid.insets.top = 11;
	grid.insets.left = 0;
	JLabel conformanceLabel = new JLabel("Compiler Conformance Level:");
	add(conformanceLabel, grid);

	JLabel cLabel = new JLabel(getString("LBL_C"));
	cLabel.setDisplayedMnemonic(getString("MNEM_C").charAt(0));
	grid = new GridBagConstraints();
	grid.anchor = GridBagConstraints.NORTHWEST;
	grid.fill = GridBagConstraints.NONE;
	grid.insets.top = 6;
	grid.insets.left = 0;
	grid.gridx = 0;
	grid.gridy = 10;
	add(cLabel, grid);

	JComboBox cComboBox = new JComboBox(new String[] {"ISO C (-Xc)", "ISO C Plus K&R C (-Xa)"});
	grid.anchor = GridBagConstraints.NORTHWEST;
	grid.fill = GridBagConstraints.NONE;
	grid.insets.top = 0;
	grid.insets.left = 0;
	grid.gridx = 1;
	grid.gridy = 10;
	add(cComboBox, grid);

	JLabel cppLabel = new JLabel(getString("LBL_Cpp"));
	cppLabel.setDisplayedMnemonic(getString("MNEM_Cpp").charAt(0));
	grid = new GridBagConstraints();
	grid.anchor = GridBagConstraints.NORTHWEST;
	grid.fill = GridBagConstraints.NONE;
	grid.insets.top = 6;
	grid.insets.left = 0;
	grid.gridx = 0;
	grid.gridy = 11;
	add(cppLabel, grid);

	JComboBox cppComboBox = new JComboBox(new String[] {"ISO C (-Xc)", "ISO C Plus K&R C (-Xa)"});
	grid.anchor = GridBagConstraints.NORTHWEST;
	grid.fill = GridBagConstraints.NONE;
	grid.insets.top = 0;
	grid.insets.left = 0;
	grid.gridx = 1;
	grid.gridy = 11;
	add(cppComboBox, grid);
	*/

	grid.gridwidth = GridBagConstraints.REMAINDER;
	grid.gridheight = GridBagConstraints.REMAINDER;
	grid.weightx = 1.0;
	grid.weighty = 1.0;
	add(new JLabel(""), grid); // NOI18N

	develBuildRB.addItemListener(new ItemListener() {
	    public void itemStateChanged(ItemEvent event) {
		if (develBuildRB.isSelected()) {
		    develDebugCodeRB.setEnabled(true);
		    develOptDebugCodeRB.setEnabled(true);
		    //sbrowseTB.setEnabled(true);
		} else {
		    develDebugCodeRB.setEnabled(false);
		    develOptDebugCodeRB.setEnabled(false);
		    //sbrowseTB.setEnabled(false);
		}
	    }
	});
    
	finalBuildRB.addItemListener(new ItemListener() {
	    public void itemStateChanged(ItemEvent event) {
		if (finalBuildRB.isSelected()) {
		    finalOptTB.setEnabled(true);
		    finalStripTB.setEnabled(true);
		} else {
		    finalOptTB.setEnabled(false);
		    finalStripTB.setEnabled(false);
		}
	    }
	});
    }


    /** Create the widgets if not initialized */
    public void addNotify() {
	CompilerFlags copts = getMakefileData().getCompilerFlags();
	
	if (!initialized) {
	    create();
	    initialized = true;
	}

	// Initialize data from the current CompilerFlags
	if (copts.getOptionSource() == OptionSource.DEVELOPMENT) {
	    develBuildRB.setSelected(true);
	} else {
	    finalBuildRB.setSelected(true);
	}
	finalOptTB.setSelected(copts.isFinalOptimize());
	finalStripTB.setSelected(copts.isFinalStrip());
	develDebugCodeRB.setSelected(copts.isDevelDebug());

	// 64 bit compilation flag
	create64BitTB.setSelected(copts.is64Bit());

	// Fortran module support
	moduleSupportTB.setSelected(getMakefileData().isModuleEnabled());

	// Disable fortran module support for GNU compiler
	if (getMakefileData().getToolset() == MakefileData.GNU_TOOLSET_TYPE) {
	    getMakefileData().setModuleEnabled(false);
	    moduleSupportTB.setEnabled(false);
	}

	CndUIUtilities.requestFocus(develBuildRB);
	super.addNotify();
    }


    /** Get the data from the panel and update the target */
    public void removeNotify() {
	super.removeNotify();

	CompilerFlags copts = getMakefileData().getCompilerFlags();

	// Build flags
	if (develBuildRB.isSelected()) {
	    copts.setOptionSource(OptionSource.DEVELOPMENT);
	} else {
	    copts.setOptionSource(OptionSource.FINAL);
	}

	copts.setFinalOptimize(finalOptTB.isSelected());
	copts.setFinalStrip(finalStripTB.isSelected());
	copts.setDevelDebug(develDebugCodeRB.isSelected());

	// 64 bit flag
	copts.set64Bit(create64BitTB.isSelected());

	// Fortran module support
	getMakefileData().setModuleEnabled(moduleSupportTB.isSelected());
    }
}

