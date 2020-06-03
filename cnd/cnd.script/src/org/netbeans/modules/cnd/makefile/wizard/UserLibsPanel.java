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
import java.io.File;
import java.util.LinkedList;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.netbeans.modules.cnd.makefile.wizard.EnterItemsPanel.ErrorInfo;
import org.netbeans.modules.cnd.makefile.wizard.EnterItemsPanel.ListItem;
import org.openide.util.NbBundle;

/**
 * Create the user libraries panel in the Makefile wizard.
 */

public class UserLibsPanel extends EnterItemsPanel {

    /** Serial version number */
    static final long serialVersionUID = 3971722083122307369L;

    private int key;
    private boolean initialized;

    private JPanel stdLibPanel = null;
    private JTextField stdLibsText;
    private JLabel stdLibsLabel = null;
    private boolean stdLibPanelAdded = false;


    /**
     * Constructor for the Makefile sources panel. Remember, most of the panel
     * is inherited from WizardDescriptor.
     */
    UserLibsPanel(MakefileWizard wd) {
	super(wd);
	String subtitle = new String(getString("LBL_UserLibsPanel")); // NOI18N
	setSubTitle(subtitle);
	this.getAccessibleContext().setAccessibleDescription(subtitle);
	initialized = false;
    }


    /** Defer widget creation until the panel needs to be displayed */
    private void create() {
	create(getString("LBL_UserLibs"), getString("MNEM_UserLibs").charAt(0), // NOI18N
		    DYNAMIC_DEFAULT_BUTTONS | ADD_BEGINNING);

	// Create (read-only) system libs textfield. Dynamically add it if target is complex exeutable.
        GridBagConstraints gridBagConstraints;

        stdLibPanel = new javax.swing.JPanel();
        stdLibPanel.setLayout(new java.awt.GridBagLayout());

	stdLibsLabel = new javax.swing.JLabel();
	gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
	stdLibPanel.add(stdLibsLabel, gridBagConstraints);

	stdLibsText = new javax.swing.JTextField();
	stdLibsLabel.setLabelFor(stdLibsText);
	stdLibsText.setEditable(false);
	stdLibsText.setFocusable(false);
	gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
	gridBagConstraints.gridheight = 1;
	gridBagConstraints.gridx = 0;
	gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
	gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
	gridBagConstraints.weightx = 1.0;
	stdLibPanel.add(stdLibsText, gridBagConstraints);
    }

    /**
     * Dynamically add stdLibs panel (only for complex executables)
     */
    private void addStdLibPanel() {
	if (!stdLibPanelAdded) {
	    GridBagConstraints grid = new GridBagConstraints();
	    grid.anchor = GridBagConstraints.NORTHWEST;
	    grid.gridx = 0;
	    grid.gridwidth = GridBagConstraints.REMAINDER;
	    grid.gridheight = 1;
	    grid.weightx = 1.0;
	    grid.weighty = 0.0;
	    grid.fill = java.awt.GridBagConstraints.HORIZONTAL;
	    grid.insets.top = 11;

	    addComponent(stdLibPanel, grid);
	    stdLibPanelAdded = true;
	}
    }

    /**
     * Dynamically remove stdLibs panel (only for complex executables)
     */
    private void removeStdLibPanel() {
	if (stdLibPanelAdded) {
	    remove(stdLibPanel);
	    stdLibPanelAdded = false;
	}
    }


    /** Set the label for the Source List */
    protected String getListLabel() {
	return getString("LBL_LibraryList");				// NOI18N
    }


    /** Set the mnemonic for the Source List */
    protected char getListMnemonic() {
	return getString("MNEM_LibraryList").charAt(0);			// NOI18N
    }


    /**
     *  Check the input and remove any invalid syntax. If the text starts with
     *  any option other than -L, -l, or -B ignore the option. Any token which
     *  doesn't start with a - is treated as a file and acceted.
     *
     *  @param token	The raw input as typed by the user
     *  @return		The validated (and possibly modified) string or null
     */
    protected String validateInput(String token) {

	if (token.charAt(0) == '-') {
	    char c = token.charAt(1);

	    if (c == 'L' || c == 'l' || c == 'B') {
		return token;
	    } else {
		return null;
	    }
	} else {
	    return token;
	}
    }


    /**
     *  Check for an error. Its not an error as long as any items are in the list. We
     *  don't care if something ``exists'' because it might be an alternative library
     *  specification.
     *
     *  @param tcount	The number of tokens
     *  @param list	The list of token matches
     *  @param nefiles	True if non existant files were specified or matched
     */
    protected boolean checkErrorConditions(int tcount, LinkedList list, boolean nefiles) {
	return tcount == 1 && list.size() == 0;
    }


    /** Get the title and message for the error dialog */
    protected ErrorInfo getErrorInfo() {
	return new ErrorInfo(getString("DLG_ULP_EmptyRE"),		// NOI18N
			getString("MSG_NoFilesMatched"));		// NOI18N
    }

    /**
     *  Overridden from EnterItemsPanel
     *  Scan all entered items, and add -l to the ones that looks like system libraries
     */
    protected void addMultipleFiles(Object[] objects) {
	for (int i = 0; i < objects.length; i++) {
	    if (!(objects[i] instanceof ListItem))
		continue;
	    ListItem item = (ListItem)objects[i];
	    String name = item.getName();
	    if (name.length() == 0)
		continue;
	    if (name.charAt(0) == '-')
		continue;
	    if (name.charAt(0) == '$')
		continue;
	    if (name.indexOf(File.separator) >= 0)
		continue;
	    if (name.endsWith(".a") || name.endsWith(".so") || name.endsWith(".dylib") || name.endsWith(".dll")) // NOI18N
		continue;

	    // it is most likely a standard libray. Prefix it with "-l".
	    item.setName("-l" + name); // NOI18N
	}
	super.addMultipleFiles(objects);
    }



    /** Create and initialized the widgets */
    public void addNotify() {
	TargetData target = getMakefileData().getCurrentTarget();
	int targetType = target.getTargetType();
	key = target.getKey();

	if (!initialized) {
	    create();
	    initialized = true;
	}

	if (targetType == TargetData.COMPLEX_EXECUTABLE) {
	}
	
	// Set text in read-only stdlibs textfield and change the label
	if (targetType == TargetData.COMPLEX_EXECUTABLE) {
	    MakeVarName var = new MakeVarName();
	    var.setTargetName(target.getName());
	    String s = var.makeRef("SYSLIBS_");		// NOI18N

	    // If new, add $(SYSLIBS_...) to list
	    if (target.getUserLibsList() == null) {
		target.setUserLibsList(new String[] {s});
	    }

	    // Get set of sys libraries, and set the text in the read-only text field
	    StdLibFlags flags = target.getStdLibFlags();
	    int os = getMakefileData().getMakefileOS();
	    int toolset = getMakefileData().getToolset();
	    String txt = flags.getSysLibFlags(toolset, os, getMakefileData().getCompilerFlags().is64Bit(), target);
	    stdLibsText.setText(txt);
	    stdLibsText.setToolTipText(txt);

	    // Change the label to include correct target
	    String ltxt = NbBundle.getMessage(UserLibsPanel.class, "LBL_SYSLIB", s); // NOI18N
	    stdLibsLabel.setText(ltxt);

	    addStdLibPanel();
	}
	else {
	    removeStdLibPanel();
	}

	// Initialize the list. First, remove any from the JList. Then, add any
	// entries from the target into the JList.
	DefaultListModel model = (DefaultListModel) getList().getModel();
	model.removeAllElements();
	String[] slist = target.getUserLibsList();
	if (slist != null) {
	    for (int i = 0; i < slist.length; i++) {
		model.addElement(slist[i]);
	    }
	}

	super.addNotify();
    }


    /** Get the data from the panel and update the target */
    public void removeNotify() {
	super.removeNotify();

	TargetData target = getMakefileData().getTarget(key);
	target.setUserLibsList(getListItems());
    }
}
