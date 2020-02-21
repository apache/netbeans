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
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import org.netbeans.modules.cnd.makefile.utils.IpeFileSystemView;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.ui.CndUIUtilities;

/**
 *  Superclass for a MakefileWizard panel consisting of a directory textfield
 *  and a name textfield. The directory textfield has a directory chooser. There
 *  may also be optional help text below the textfields.
 */

public class ItemChooser extends MakefileWizardPanel 
                         implements FocusListener {

    /** Serial version number */
    static final long serialVersionUID = 6653452210904639697L;

    // the fields in the first panel...
    private JLabel	dirLabel;
    private JTextField	dirText;
    private JButton	dirChooser;
    private JLabel	label;
    private JTextField	text;

    private JFileChooser fc;

    /** Is the directory a valid (ie, existing) directory? */
    private boolean directoryValid;

    /** Is it OK for the directory to not exist? */
    private boolean uncreatedDirOK;

    /** Is directory field read-only? */
    private boolean dirReadOnly;

    /** Is the text field the name of a regular file? */
    private boolean textNotFile;


    /**
     *  Constructor for the Makefile binary panel.
     */
    protected ItemChooser(MakefileWizard wd) {
	this(wd, false, false);
    }


    /**
     *  Constructor for the Makefile binary panel.
     */
    protected ItemChooser(MakefileWizard wd, boolean uncreatedDirOK) {
	this(wd, uncreatedDirOK, false);
    }

    /**
     *  Constructor for the Makefile binary panel.
     */
    protected ItemChooser(MakefileWizard wd, boolean uncreatedDirOK, boolean dirReadOnly) {
	super(wd);
	this.uncreatedDirOK = uncreatedDirOK;
	this.dirReadOnly = dirReadOnly;
    }

    /**
     *  The default validation method. Most panels don't do validation so don't
     *  need to override this.
     */
    @Override
    public boolean isPanelValid() { 
	return (uncreatedDirOK && textNotFile) || directoryValid;
    }


    /** Override the defualt and do some validation */
    protected final void onOk() {
	checkit();
    }


    /**
     *  Validate the base directory currently typed into the text field. This method should
     *  not be confused with validateData(), which is called during Makefile generation.
     *  This validation occurs while the panel is posted. The validateData() occurs much
     *  later.
     */
    private void validateDirectory() {
	String dir = dirText.getText();

	if (dir.length() > 0) {
	    File file;

	    if (dir.charAt(0) == File.separatorChar) {
		file = new File(dir);
	    } else {
		file = new File(".", dir);  // NOI18N
	    }

	    if (uncreatedDirOK) {
		// for this case just check dir isn't a file
		boolean tnf = !file.isFile();

		if (tnf != textNotFile) {
		    textNotFile = tnf;
		    MakefileWizard.getMakefileWizard().updateState();
		}
	    } else {
		// for this case it must be a valid directory
		boolean isfile = file.isDirectory();
		if (isfile != directoryValid) {
		    directoryValid = isfile;
		    MakefileWizard.getMakefileWizard().updateState();
		}
	    }
	}
    }


    /** Check the directory and updateState if its changed */
    private final void checkit() {
	boolean oldVal = directoryValid;

	validateDirectory();
	if (directoryValid != oldVal) {
	    MakefileWizard.getMakefileWizard().updateState();
	}
    }


    /** Defer widget creation until the panel needs to be displayed */
    protected void create(String dlabel, char dmnem,
			    String nlabel, char nmnem) {
	int gridy = 0;

        setLayout(new GridBagLayout());
	GridBagConstraints grid = new GridBagConstraints();
	Insets defaults = grid.insets;

	// Create the components for the directory area.
	dirLabel = new JLabel(dlabel);
	dirLabel.setDisplayedMnemonic(dmnem);
	grid.anchor = GridBagConstraints.WEST;
	grid.gridx = 0;
	grid.gridy = gridy++;
	grid.gridwidth = GridBagConstraints.REMAINDER;
	add(dirLabel, grid);

	// The directory textfield
	dirText = new JTextField();		    // set value in addNotify()
	dirText.addFocusListener(this);
	dirLabel.setLabelFor(dirText);
	grid.gridy = gridy++;
	grid.gridwidth = GridBagConstraints.RELATIVE;
	grid.weightx = 1.0;
	grid.fill = GridBagConstraints.HORIZONTAL;
	grid.anchor = GridBagConstraints.WEST;
	grid.insets = defaults;
	add(dirText, grid);
	dirText.getDocument().addDocumentListener(new DocumentListener() {

	    public void changedUpdate(DocumentEvent ev) {
		checkit();
	    }

	    public void insertUpdate(DocumentEvent ev) {
		checkit();
	    }

	    public void removeUpdate(DocumentEvent ev) {
		checkit();
	    }
	});

	if (dirReadOnly) {
	    dirText.setEnabled(false);
	}
	else {
	// The directory chooser button
	dirChooser = new JButton(getString("BTN_Chooser"));		// NOI18N
	dirChooser.setMnemonic(getString("MNEM_Chooser").charAt(0));	// NOI18N
	grid.gridx = 2;
	grid.gridwidth = GridBagConstraints.REMAINDER;
	grid.fill = GridBagConstraints.NONE;
	grid.anchor = GridBagConstraints.WEST;
	grid.weightx = 0.0;
	grid.insets = new Insets(0, 5, 0, 0);
	add(dirChooser, grid);

	dirChooser.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		String cwd = getMakefileData().getBaseDirectory(MakefileData.EXPAND);
	    
		fc = new JFileChooser();
		fc.setApproveButtonText(getString("BTN_Approve"));	// NOI18N
		fc.setDialogTitle(getString("TITLE_DirChooser"));	// NOI18N
		fc.setCurrentDirectory(new File(cwd));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setFileSystemView(new IpeFileSystemView(fc.getFileSystemView()));

		int returnVal = fc.showDialog(ItemChooser.this, null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    String path = fc.getSelectedFile().getAbsolutePath();
		    dirText.setText(CndPathUtilities.getRelativePath(cwd, path));
		}
	    }
	});
	}

	// Now create and set the GridBagLayout constraints.
        label = new JLabel(nlabel);
	label.setDisplayedMnemonic(nmnem);
	grid.anchor = GridBagConstraints.NORTHWEST;
	grid.gridx = 0;
	grid.gridy = gridy++;
	grid.gridwidth = GridBagConstraints.REMAINDER;
	grid.insets = new Insets(10, 0, 0, 0);
	add(label, grid);

        text = new JTextField();		// set name in addNotify()
	text.addFocusListener(this);
	label.setLabelFor(text);
	grid.gridy = gridy++;
	grid.gridwidth = GridBagConstraints.RELATIVE;
	grid.weightx = 1.0;
	grid.insets = defaults;
	grid.fill = GridBagConstraints.HORIZONTAL;
	grid.anchor = GridBagConstraints.WEST;
	add(text, grid);
	Keymap km = text.addKeymap("ItemChooserKeymap",			// NOI18N
				text.getKeymap());
	km.addActionForKeyStroke(KeyStroke.getKeyStroke('/'),
		new DefaultEditorKit.BeepAction());
	text.setKeymap(km);


	grid.gridx = 0;
	grid.gridy = gridy++;
	grid.gridwidth = GridBagConstraints.REMAINDER;
	grid.gridheight = GridBagConstraints.REMAINDER;
	grid.weightx = 1.0;
	grid.weighty = 1.0;
	add(new JLabel(""), grid);                                      // NOI18N
    }

    final void convertLabel(String nlabel, char nmnem) {
	label.setText(nlabel);
	label.setDisplayedMnemonic(nmnem);
    }

    final JTextField getText() {
	return text;
    }

    final JTextField getDirText() {
	return dirText;
    }

    final JLabel getNameLabel() {
	return label;
    }

    public void focusGained(FocusEvent evt) {
    }

    public void focusLost(FocusEvent evt) {
	// help doesn't always get deselected so set total
	// selection length to zero.
	((JTextComponent) evt.getComponent()).setSelectionEnd(0);
    }


    @Override
    public void addNotify () {
	super.addNotify();
	dirText.selectAll();
	CndUIUtilities.requestFocus(dirText);
	validateDirectory();
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

