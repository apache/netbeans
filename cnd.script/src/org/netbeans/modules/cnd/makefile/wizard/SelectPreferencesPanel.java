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
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.cnd.utils.ui.CndUIUtilities;

/**
 * Create the third panel in the Makefile wizard.
 */

public class SelectPreferencesPanel extends MakefileWizardPanel {

    /** Serial version number */
    static final long serialVersionUID = -8883172608978315976L;

    // the fields in the first panel...
    private JLabel	    compilerMsg;
    private boolean	    initialized;
    private javax.swing.JPanel panel2;
    private javax.swing.JTextArea compilerMsg2;
    private javax.swing.JCheckBox debugCheckBox;
    private javax.swing.JCheckBox optimizeCheckBox;


    /**
     * Constructor for the Makefile name panel. Remember, most of the panel is
     * inherited from WizardDescriptor.
     */
    SelectPreferencesPanel(MakefileWizard wd) {
	super(wd);
	String subtitle = getString("LBL_SelectPreferencesPanel"); // NOI18N
	setSubTitle(subtitle);
	this.getAccessibleContext().setAccessibleDescription(subtitle);
	initialized = false;
    }


    /** Defer widget creation until the panel needs to be displayed */
    private void create() {

        setLayout(new GridBagLayout());
	GridBagConstraints gridBagConstraints;
	JPanel panel = new JPanel(new GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
	add(panel, gridBagConstraints);

	panel2 = new JPanel();
        compilerMsg = new JLabel();
        debugCheckBox = new javax.swing.JCheckBox();
        optimizeCheckBox = new javax.swing.JCheckBox();
        compilerMsg2 = new javax.swing.JTextArea();

	gridBagConstraints.weightx = 1.0;
	gridBagConstraints.weighty = 1.0;
	gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
	gridBagConstraints.gridheight = GridBagConstraints.REMAINDER;
	gridBagConstraints.fill = GridBagConstraints.BOTH;
	add(new JLabel(""), gridBagConstraints); // NOI18N

        panel2.setLayout(new java.awt.GridBagLayout());

        compilerMsg.setText(getString("LBL_CompilerFlagsMsg"));		// NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        panel2.add(compilerMsg, gridBagConstraints);

        debugCheckBox.setText(getString("RB_DebugMsg"));			// NOI18N
	debugCheckBox.setMnemonic(getString("MNEM_DebugMsg").charAt(0));	// NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        panel2.add(debugCheckBox, gridBagConstraints);

        optimizeCheckBox.setText(getString("RB_OptMsg"));			// NOI18N
	optimizeCheckBox.setMnemonic(getString("MNEM_OptMsg").charAt(0));	// NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        panel2.add(optimizeCheckBox, gridBagConstraints);

        compilerMsg2.setBackground(panel.getBackground());
        compilerMsg2.setEditable(false);
        compilerMsg2.setLineWrap(true);
        compilerMsg2.setWrapStyleWord(true);
        compilerMsg2.setFocusable(false);
        compilerMsg2.setText(getString("LBL_CompilerFlagsMsg2"));		// NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        panel2.add(compilerMsg2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        panel.add(panel2, gridBagConstraints);
    }


    /** Create the widgets if not initialized. Also set the RadioButtons */
    public void addNotify() {
	CompilerFlags copts = getMakefileData().getCompilerFlags();

	if (!initialized) {
	    create();
	    copts.setOptionSource(OptionSource.SIMPLE);
	    copts.setSimpleDebug(true);
	    initialized = true;
	}

	super.addNotify();
	debugCheckBox.setSelected(copts.isSimpleDebug());
	optimizeCheckBox.setSelected(copts.isSimpleOptimize());
	// Try to get focus (two different methods ...)
	debugCheckBox.requestFocus();
	CndUIUtilities.requestFocus(debugCheckBox);
    }


    public void removeNotify() {
	super.removeNotify();

	CompilerFlags copts = getMakefileData().getCompilerFlags();

	copts.setSimpleDebug(debugCheckBox.isSelected());
	copts.setSimpleOptimize(optimizeCheckBox.isSelected());
    }

}
