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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.cnd.makefile.utils.IpeFileSystemView;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.ui.CndUIUtilities;

/**
 * The FileChooserPanel is used to derive various panels in the Makefile
 * wizard which have a single text field and file chooser.
 */

public abstract class FileChooserPanel extends MakefileWizardPanel
                                       implements FocusListener {

    /** Serial version number */
    static final long serialVersionUID = -3638230469770222549L;

    /** The label of the file/path component */
    private JLabel	    label;

    /** The component for the file/path we are looking for */
    private JTextField	    text;

    /** The component that contains the help information */
    private JTextArea       helpText;

    /** Store the file (possibly customized) file chooser */
    protected JFileChooser  fc;

    /** The type of file/path information to return (see flags below) */
    private int pathType;

    /** The initial value for pathType */
    public static final int PATH_NONE = 0;

    /** Return the file/path as an absolute file */
    public static final int ABSOLUTE_PATH = 1;

    /** Return the file/path relative to the currentDirectory */
    public static final int RELATIVE_PATH = 2;

    /** Return the file/path from File.getName() */
    public static final int NAME_ONLY = 3;


    /** Default constructor */
    public FileChooserPanel(MakefileWizard wd) {
	super(wd);
	init();
    }

    /** Create a panel passing in a file chooser */
    public FileChooserPanel(MakefileWizard wd, JFileChooser fc) {
	super(wd);
	this.fc = fc;
	init();
    }

    protected void init() {
	pathType = PATH_NONE;
    }

    public void setPathType(int pathType) {
	if (pathType == ABSOLUTE_PATH || pathType == RELATIVE_PATH ||
			pathType == NAME_ONLY) {
	    this.pathType = pathType;
	}
    }


    public void create(String labelString) {
	create(labelString, pathType, null);
    }


    public void create(String labelString, int pathType) {
	create(labelString, pathType, null);
    }


    public void create(String labelString, String help) {
	create(labelString, pathType, help);
    }


    public void create(String labelString, final int pathType, String help) {
	int gridy = 0;

        setLayout(new GridBagLayout());
	GridBagConstraints grid = new GridBagConstraints();
	this.pathType = pathType;

	// Create the components.
	// First, the label
	label = new JLabel(labelString);
	grid.anchor = GridBagConstraints.NORTHWEST;
	grid.gridx = 0;
	grid.gridy = gridy++;
	grid.gridwidth = GridBagConstraints.REMAINDER;
	add(label, grid);

	// Next, the text field
        text = new JTextField();
	text.addFocusListener(this);
	grid.gridy = gridy++;
	grid.weightx = 1.0;
	grid.gridwidth = GridBagConstraints.RELATIVE;
	grid.fill = GridBagConstraints.HORIZONTAL;
	grid.anchor = GridBagConstraints.WEST;
	add(text, grid);

	// Next, the file chooser button
        JButton chooser = new JButton(getString("BTN_Chooser"));	// NOI18N
	chooser.setMnemonic(getString("MNEM_Chooser").charAt(0));	// NOI18N
	grid.gridx = 2;
	grid.weightx = 0.0;
	grid.gridwidth = GridBagConstraints.REMAINDER;
	grid.fill = GridBagConstraints.NONE;
	grid.insets = new Insets(0, 5, 0, 0);
	add(chooser, grid);
	chooser.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		File file;
		File cwd;
	    
		if (fc == null) {
		    fc = new JFileChooser();
		    fc.setApproveButtonText(getString("BTN_Approve"));	// NOI18N
		    fc.setFileSystemView(new
				IpeFileSystemView(fc.getFileSystemView()));
		    fc.setDialogTitle(
				getString("DLG_FILE_CHOOSER_TITLE"));	// NOI18N
		}

		// See if the user has already typed a directory. If so use it.
		String cur = CndPathUtilities.expandPath(text.getText());
		if (cur.length() > 0 && (file = new File(cur)).isDirectory()) {
		    if (cur.charAt(0) == '.') {
                        cwd = new File(getMakefileData().getBaseDirectory() + File.separator + cur);
                    } else {
                        cwd = file;
                    }
		} else {
		    cwd = new File(getMakefileData().getBaseDirectory());
		}
		fc.setCurrentDirectory(cwd);

		int returnVal = fc.showDialog(FileChooserPanel.this, null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    file = fc.getSelectedFile();
		    if (pathType == ABSOLUTE_PATH) {
			try {
			    text.setText(file.getCanonicalPath());
			} catch (IOException ex) {
			    text.setText(file.getAbsolutePath());
			}
		    } else if (pathType == NAME_ONLY) {
			text.setText(file.getName());
		    } else if (pathType == RELATIVE_PATH) {
			String path = null;
			String cwdpath;

			try {
			    path = file.getAbsolutePath();
			    cwdpath = cwd.getCanonicalPath();

			    if (path.equals(cwdpath)) {
				text.setText(".");  // NOI18N
			    } else if (path.startsWith(cwdpath)) {
				text.setText(path.substring(cwdpath.length() + 1));
			    } else {
				text.setText(path);
			    }
			} catch (IOException ex) {
			    if (path == null) {
				text.setText(file.getAbsolutePath());
			    } else {
				text.setText(path);
			    }
			}
		    }
		    onOk();
		}
	    }
	});

	if (help != null) {
	    grid.anchor = GridBagConstraints.NORTHWEST;
	    grid.gridx = 0;
	    grid.gridy = gridy++;
	    grid.weighty = 1.0;
	    grid.gridheight = GridBagConstraints.REMAINDER;
	    grid.fill = GridBagConstraints.BOTH;
	    grid.insets = new Insets(11, 0, 0, 0);
	    helpText = new JTextArea(help);
	    helpText.getAccessibleContext().setAccessibleName("ACSN_DirHelp"); // NOI18N
	    helpText.addFocusListener(this);
	    helpText.setEditable(false);
	    helpText.setFocusable(false);
	    helpText.setLineWrap(true);
	    helpText.setWrapStyleWord(true);
	    helpText.setBackground(label.getBackground());
	    add(helpText, grid);
	} else {
	    grid.gridx = 0;
	    grid.gridy = gridy++;
	    grid.gridwidth = GridBagConstraints.REMAINDER;
	    grid.gridheight = GridBagConstraints.REMAINDER;
	    grid.weightx = 1.0;
	    grid.weighty = 1.0;
	    add(new JLabel(""), grid); // NOI18N
	}
    }


    /** If something is to be done on OK then it must be in a derived class */
    protected void onOk() {
    }

    
    /** Return the label widget */
    protected JLabel getLabel() {
	return label;
    }


    /** Return the text widget so its value can be querried */
    protected JTextField getText() {
	return text;
    }

    public void focusGained(FocusEvent evt) {
	Component comp = evt.getComponent();
	if (comp == helpText || comp == text) {
	    ((JTextComponent)comp).selectAll();
	}
    }

    public void focusLost(FocusEvent evt) {
	// help doesn't always get deselected so set total
	// selection length to zero.
	((JTextComponent) evt.getComponent()).setSelectionEnd(0);
    }

    @Override
    public void addNotify() {
	super.addNotify();
	CndUIUtilities.requestFocus(text);
    }


    @Override
    public void removeNotify() {
	super.removeNotify();

	if (fc != null && fc.isShowing()) {
	    Object o = fc.getTopLevelAncestor();
	    if (o != null && o instanceof JDialog) {
		((JDialog) o).dispose();
	    }
	}
    }
}
