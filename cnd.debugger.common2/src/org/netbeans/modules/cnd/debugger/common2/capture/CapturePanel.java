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

package org.netbeans.modules.cnd.debugger.common2.capture;

import java.awt.Color;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.UIManager;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.border.LineBorder;
import javax.swing.border.EtchedBorder;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.project.ProjectUtils;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.utils.CndPathUtilities;


/**
 * Panel for content of capture dialog.
 * The project connection is modelled on
 * ...debugger.actions.ExecutableProjectPanel which cannot be reused directly
 * as it provides controls for choosing executables.
 */

class CapturePanel extends JPanel {

    // passed-in info
    private final CaptureInfo captureInfo;

    // UI elements as in ExecutableProjectPanel
    private JLabel alertTitleLabel;
    private JTextArea textArea;
    private JLabel projectLabel;
    private JComboBox projectComboBox;

    private JLabel errorLabel;

    // sentinels
    public final String PROJECT_NONE = Catalog.get("NO_PROJECT");// NOI18N
    public final String PROJECT_NEW  = Catalog.get("NEW_PROJECT");// NOI18N

    // misc.
    private Project[] projectChoices;
    private static Project lastSelectedProject;

    CapturePanel(CaptureInfo captureInfo) {
	this.captureInfo = captureInfo;
	initComponents();
	if (! NativeDebuggerManager.isStandalone())
	    populateProjectMenu();
    }


    /**
     * Return the project selected by this dialog. (may be null)
     */
    public Project getSelectedProject() {
	if (NativeDebuggerManager.isStandalone())
	    return null;

	int index = projectComboBox.getSelectedIndex();
	if (index == 0) {
	    // PROJECT_NONE is selected
	    return null;
	} else if (index == 1) {
	    // PROJECT_NEW is selected
	    return null;
	} else {
	    lastSelectedProject = projectChoices[index-2];
	    return lastSelectedProject;
	}
    }


    /**
     * Return true if "<noproject>" was chosen.
     */
    public boolean getNoProject() {
	if (NativeDebuggerManager.isStandalone())
	    return true;

	int index = projectComboBox.getSelectedIndex();
	if (index == 0) {
	    // PROJECT_NONE is selected
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Map Project 'target' into an index into 'projectComboBox'.
     */
    private int mapProject(Project target) {

	for (int px = 0; px < projectChoices.length; px++) {
	    if (target == projectChoices[px])
		return px + 2;		// + 2 to skip over sentinels
	}
	return 0;
    }

    private void populateProjectMenu() {

	//
	// populate
	//
	projectComboBox.removeAllItems();

	projectChoices = OpenProjects.getDefault().getOpenProjects();

	projectComboBox.addItem(PROJECT_NONE);
	projectComboBox.addItem(PROJECT_NEW);
	for (Project p : projectChoices) {
	    String projectName = ProjectUtils.getInformation(p).getName();
	    projectComboBox.addItem(projectName);
	}


	//
	// select a project
	//
	Project p = projectFromExecutable(captureInfo.executable);

	/* Don't do this for now. It's liable to generate more confusion if
	   amatching project isn't found, a previous project is chosen and
	   the user says Yes w/o realising that they're working with an
	   unrelated project.

	if (p == null)
	    p = lastSelectedProject;
	*/

	int chosenIndex = 0;
	if (p != null)
	    chosenIndex = mapProject(p);
	projectComboBox.setSelectedIndex(chosenIndex);
    }


    /**
     * Returns true if Project 'candidate' was previously created from
     * 'executable' (by ...actions.ProjectSupport).
     */

    private boolean match(Project candidate, String executable) {
	String baseName = CndPathUtilities.getBaseName(executable);
	String projectName = ProjectUtils.getInformation(candidate).getName();

	// LATER: strip possible serial number from project name

	return projectName.equals(baseName);
    }


    /**
     * Find a Project which matches this executable.
     *
     * Ideally we'd like to be able to look into a projects configurations and
     * the build-output property to find an exact match.
     * For now we only take advantage of the fact that automatically created
     * projects are named based on the executable name.
     */

    private Project projectFromExecutable(String executable) {
	/* LATER
	executable is not a full path!

	File file = new File(executable);
	FileObject fo = FileUtil.toFileObject(file);
	if (fo == null) {
	    return null;
	}
	DataObject dataObject = null;
	try {
	    dataObject = DataObject.find(fo);
	} catch(Exception e) {
	    return null;
	}
	if (!(dataObject instanceof ExeElfObject)) {
	    return null;
	}
	*/

	for (int px = 0; px < projectChoices.length; px++) {
	    Project candidate = projectChoices[px];
	    if (match(candidate, executable))
		return candidate;
	}

	return null;
    }


    private void initComponents() {
	final boolean debug = false;
	if (debug)
	    this.setBorder(new LineBorder(Color.black));

	Catalog.setAccessibleDescription(this, "ASCD_CapturePanel");// NOI18N

	setLayout(new GridBagLayout());
	GridBagConstraints gbc;

	// See JLF-II p66
	// But we deviate because we're embedded in a JOptionPane.
	final int dialogMargin = 0;	// 11;
	final int labelSpace = 11;
	final int titleSpace = 12;	// p74
	final int bottomMargin = 12;

	    alertTitleLabel = new JLabel();
	    /* OLD
	    String alertMsg = Catalog.format("FMT_Captured",	// NOI18N
					     captureInfo.executable)
	    		      + " " +
			      Catalog.format("FMT_Host",	// NOI18N
					     captureInfo.hostName);
	    */
	    String alertMsg;
	    if (captureInfo.hostName == null) {
		// localhost
		alertMsg = Catalog.format("FMT_Captured",// NOI18N
					  captureInfo.executable);
	    } else {
		// remote host
		alertMsg = Catalog.format("FMT_CapturedOnHost",	// NOI18N
					  captureInfo.executable,
					  captureInfo.hostName);
	    }

	    // make bold:
	    alertMsg = String.format("<html><b>%s", alertMsg);	// NOI18N
	    alertTitleLabel.setText(alertMsg);

	    gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.gridwidth = 3;
	    gbc.weightx = 1.0;
	    gbc.anchor = GridBagConstraints.WEST;
	    gbc.insets = new Insets(dialogMargin, dialogMargin, titleSpace, dialogMargin);
	    add(alertTitleLabel, gbc);

	    final int rows = 5;
	    final int cols = 60;
	    textArea = new JTextArea(captureInfo.messageString(), rows, cols);
	    textArea.setEditable(false);
	    textArea.setFocusable(false);
	    Color bgColor;

	    final boolean noBorder = false;
	    if (noBorder) {
		bgColor = (Color) UIManager.getDefaults().
		    get("Label.background");	// NOI18N
		if (debug)
		    textArea.setBorder(new LineBorder(Color.black));
	    } else {
		bgColor = (Color) UIManager.getDefaults().
		    get("TextArea.background");	// NOI18N
		textArea.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
	    }

	    textArea.setBackground(bgColor);
	    gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = 1;
	    gbc.gridwidth = 3;
	    gbc.weightx = 1.0;
	    gbc.fill = GridBagConstraints.BOTH;
	    gbc.anchor = GridBagConstraints.CENTER;
	    gbc.insets = new Insets(0, dialogMargin, 17, dialogMargin);
	    add(textArea, gbc);

	    if ( ! NativeDebuggerManager.isStandalone()) {
		projectComboBox = new JComboBox();
		Catalog.setAccessibleDescription(projectComboBox,
						 "ACSD_Project");	// NOI18N
		projectComboBox.addActionListener(new ActionListener() {
                    @Override
		    public void actionPerformed(ActionEvent e) {
			// different project was selected
			clearError();
			if (getSelectedProject() != null) {
			    setWarning(Catalog.get("MSG_overrideWarn"));// NOI18N
			}
		    }
		} );

		projectLabel = new JLabel();
		projectLabel.setText
		    (Catalog.get("ASSOCIATED_PROJECT_LBL"));	// NOI18N
		projectLabel.setDisplayedMnemonic
		    (Catalog.getMnemonic("MNM_Project"));	// NOI18N
		projectLabel.setLabelFor(projectComboBox);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, dialogMargin, 12, labelSpace);
		add(projectLabel, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 12, dialogMargin);
		add(projectComboBox, gbc);
	    }

	    errorLabel = new JLabel();
	    errorLabel.setForeground(UIManager.getColor("nb.errorForeground"));	// NOI18N
	    clearError();

	    gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = 3;
	    gbc.gridwidth = 3;
	    gbc.weightx = 1.0;
	    gbc.anchor = GridBagConstraints.WEST;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    gbc.insets = new Insets(0, dialogMargin, bottomMargin, dialogMargin);
	    add(errorLabel, gbc);
    }

    private void setWarning(String msg) {
	errorLabel.setText(msg);
    }

    private void clearError() {
	// Don't use "" because then the layout will jump as we set and
	// clear the text.
	errorLabel.setText(" "); // NOI18N
    }
}
