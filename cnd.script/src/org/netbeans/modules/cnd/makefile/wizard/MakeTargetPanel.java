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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *  Create a panel used for gathering the binary name and output directory for
 *  the simple application cases (all Makefile types other than complex).
 */
public class MakeTargetPanel extends MakefileWizardPanel
        implements FocusListener {

    /** Serial version number */
    static final long serialVersionUID = -8864738441088422274L;
    /** the constraints used in that panel */
    private GridBagConstraints grid;
    /** share the insets rather than create a bunch of them */
    Insets insets;

    // the fields in the first panel...
    private JTextField targetName;
    private JTextField dependsOn;
    private JTextField subdirectory;
    private JTextField makeFlags;
    private JTextArea commandDisplay;
    private boolean enableCommandSelection;
    private String tname;
    private String depends;
    private String subdir;
    private String mflags;
    private int row;
    private boolean initialized;
    /** Store the target key */
    private int key;

    /**
     *  Constructor for the make target panel.
     */
    MakeTargetPanel(MakefileWizard wd) {
        super(wd);
        String subtitle = new String(getString("LBL_MakeTargetPanel")); // NOI18N
        setSubTitle(subtitle);
        this.getAccessibleContext().setAccessibleDescription(subtitle);
        initialized = false;
    }

    /** Defer widget creation until the panel needs to be displayed */
    private void create() {

        setLayout(new GridBagLayout());

        insets = new Insets(10, 0, 0, 0);
        grid = new GridBagConstraints();
        grid.anchor = GridBagConstraints.NORTHWEST;
        grid.insets = insets;

        tname = new String("");						// NOI18N
        depends = new String("");					// NOI18N
        subdir = new String("");					// NOI18N
        mflags = new String("");					// NOI18N
        row = 0;

        JPanel tpanel = createTextFields();
        insets.top = 0;
        insets.left = 0;
        grid.gridx = 0;
        grid.gridy = 0;
        grid.insets = insets;
        add(tpanel, grid);
        Dimension tsize = tpanel.getPreferredSize();
        Dimension psize = getPreferredSize();
        Dimension csize =
                new Dimension(psize.width, psize.height - tsize.height);

        createCommandDisplay(csize,
                "LBL_CommandDisplay", "MNEM_CommandDisplay");		// NOI18N

        setupListeners();
    }

    /** Put the textfields in a JPanel */
    private JPanel createTextFields() {

        JPanel tpanel = new JPanel(new GridBagLayout());

        targetName = createTextField(tpanel,
                "LBL_TargetName", "MNEM_TargetName");		// NOI18N
        dependsOn = createTextField(tpanel,
                "LBL_DependsOn", "MNEM_DependsOn");		// NOI18N
        subdirectory = createTextField(tpanel,
                "LBL_Subdirectory", "MNEM_Subdirectory");	// NOI18N
        makeFlags = createTextField(tpanel,
                "LBL_MakeFlags", "MNEM_MakeFlags");		// NOI18N

        return tpanel;
    }

    /** Create a textfield and its label */
    private JTextField createTextField(JPanel tpanel, String label, String mnem) {

        // Create the textfield components.
        JLabel nueLabel = new JLabel(getString(label));
        JTextField nueText = new JTextField();

        // Set the GridBagLayout constraints.
        insets.left = 0;
        grid.gridx = 0;
        grid.gridy = row++;
        grid.gridwidth = 1;
        grid.weightx = 0.0;
        tpanel.add(nueLabel, grid);

        insets.left = 5;
        grid.gridx = 1;
        grid.gridwidth = GridBagConstraints.REMAINDER;
        grid.weightx = 1.0;
        grid.fill = GridBagConstraints.HORIZONTAL;
        tpanel.add(nueText, grid);

        nueLabel.setDisplayedMnemonic(getString(mnem).charAt(0));
        nueLabel.setLabelFor(nueText);

        return nueText;
    }

    private void createCommandDisplay(Dimension size, String label, String mnem) {

        // Create and add the components to the JPanel
        JLabel nueLabel = new JLabel(getString(label));
        nueLabel.setDisplayedMnemonic(getString(mnem).charAt(0));
        insets.top = 16;
        grid.gridx = 0;
        grid.gridy = 1;
        grid.gridwidth = 1;
        add(nueLabel, grid);
        size.setSize(size.getWidth(), size.getHeight() - insets.top -
                nueLabel.getPreferredSize().getHeight());

        commandDisplay = new JTextArea();
        commandDisplay.setEditable(false);
        commandDisplay.setBackground(getBackground());
        commandDisplay.addFocusListener(this);
        JScrollPane s = new JScrollPane(commandDisplay);
        s.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        s.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        size.height = size.height - insets.top -
                nueLabel.getPreferredSize().height;
        s.setPreferredSize(size);
        nueLabel.setLabelFor(commandDisplay);

        insets.top = 0;
        grid.gridy = 2;
        grid.gridwidth = GridBagConstraints.REMAINDER;
        grid.gridheight = GridBagConstraints.REMAINDER;
        add(s, grid);

        enableCommandSelection = false;
    }

    public void focusGained(FocusEvent evt) {
        // don't select text when panel is first displayed otherwise
        // the text appears to "flash"
        if (enableCommandSelection) {
            commandDisplay.selectAll();
        }
        enableCommandSelection = true;
    }

    public void focusLost(FocusEvent evt) {
        commandDisplay.setSelectionEnd(0);
    }

    /** Setup the document listeners for each textfield */
    private void setupListeners() {

        targetName.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
                tname = targetName.getText();
                commandDisplay.setText(getCommandDisplay());
                updateButtons();
            }

            public void insertUpdate(DocumentEvent e) {
                tname = targetName.getText();
                commandDisplay.setText(getCommandDisplay());
                updateButtons();
            }

            public void removeUpdate(DocumentEvent e) {
                tname = targetName.getText();
                commandDisplay.setText(getCommandDisplay());
                updateButtons();
            }
        });

        dependsOn.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
                depends = dependsOn.getText();
                commandDisplay.setText(getCommandDisplay());
                updateButtons();
            }

            public void insertUpdate(DocumentEvent e) {
                depends = dependsOn.getText();
                commandDisplay.setText(getCommandDisplay());
                updateButtons();
            }

            public void removeUpdate(DocumentEvent e) {
                depends = dependsOn.getText();
                commandDisplay.setText(getCommandDisplay());
                updateButtons();
            }
        });

        subdirectory.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
                subdir = subdirectory.getText();
                commandDisplay.setText(getCommandDisplay());
                updateButtons();
            }

            public void insertUpdate(DocumentEvent e) {
                subdir = subdirectory.getText();
                commandDisplay.setText(getCommandDisplay());
                updateButtons();
            }

            public void removeUpdate(DocumentEvent e) {
                subdir = subdirectory.getText();
                commandDisplay.setText(getCommandDisplay());
                updateButtons();
            }
        });

        makeFlags.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
                mflags = makeFlags.getText();
                commandDisplay.setText(getCommandDisplay());
                updateButtons();
            }

            public void insertUpdate(DocumentEvent e) {
                mflags = makeFlags.getText();
                commandDisplay.setText(getCommandDisplay());
                updateButtons();
            }

            public void removeUpdate(DocumentEvent e) {
                mflags = makeFlags.getText();
                commandDisplay.setText(getCommandDisplay());
                updateButtons();
            }
        });
    }

    private void updateButtons() {
        MakefileWizard mw = MakefileWizard.getMakefileWizard();

        if (tname.length() > 0 || depends.length() > 0 ||
                subdir.length() > 0 || mflags.length() > 0) {
            mw.getNextButton().setEnabled(true);
            if (mw.getMakefileData().isComplete(true)) {
                mw.getFinishButton().setEnabled(true);
            }
        } else {
            mw.getNextButton().setEnabled(false);
            mw.getFinishButton().setEnabled(false);
        }
    }

    @Override
    public boolean isPanelValid() {
        if (tname != null && depends != null && subdir != null && mflags != null) {
            return tname.length() > 0 || depends.length() > 0 ||
                    subdir.length() > 0 || mflags.length() > 0;
        } else {
            return false;
        }
    }

    /** Put together the command display string based on current input */
    private String getCommandDisplay() {
        return getCommandDisplay(-1);
    }

    /**
     *  Put together the command display string based on current input.
     *  This method can be called during target definition, in which case we
     *  get the target via getCurrentTarget(). It can also be called after
     *  target definition has been completed. In that case we must pass the
     *  target key.
     */
    private String getCommandDisplay(int key) {
        StringBuilder buf = new StringBuilder(512);
        TargetData target;
        String[] dirs;

        if (key < 0) {
            target = getMakefileData().getCurrentTarget();
        } else {
            target = getMakefileData().getTarget(key);
        }

        buf.append(target.getName());
        buf.append(": ");						// NOI18N
        if (depends.length() > 0) {
            buf.append(depends);
        }

        dirs = getSubDirList();
        for (int i = 0; i < dirs.length; i++) {
            buf.append("\n\t");						// NOI18N
            if (dirs[i].length() > 0) {
                buf.append("cd ").append(dirs[i]).append("; ");		// NOI18N
            }

            buf.append("$(MAKE) ");					// NOI18N
            if (mflags.length() > 0) {
                buf.append(mflags).append(' ');
            }
            if (tname.length() > 0) {
                buf.append(tname);
            }
        }

        return buf.toString();
    }

    private String[] getSubDirList() {
        String[] dirs;

        if (subdir.length() < 1 || subdir.equals(".")) {		// NOI18N
            dirs = new String[1];
            dirs[0] = new String(""); // NOI18N
        } else {
            StringTokenizer tok = new StringTokenizer(subdir, " ");	// NOI18N
            int count = tok.countTokens();
            int i = 0;

            dirs = new String[count];
            while (tok.hasMoreTokens()) {
                dirs[i++] = tok.nextToken();
            }
        }

        return dirs;
    }

    /** Validate the binary name and output directory */
    @Override
    public void validateData(ArrayList<String> msgs, int key) {
        String cwd = getMakefileData().getBaseDirectory(MakefileData.EXPAND);
        File sd = null;

        if (subdir.length() > 0 && !subdir.equals(".")) {		// NOI18N
            if (subdir.startsWith(File.separator)) {
                sd = new File(subdir);
            } else {
                sd = new File(cwd, subdir);
            }
            if (sd.getPath().equals(cwd)) {
                sd = null;		// Don't test it. Its a variation of '.'
            }
        }

        if (sd != null) {
            if (!sd.exists()) {
                // check subdir for nonexistant directory
                warn(msgs, WARN_SUBDIR_DOES_NOT_EXIST, subdir, tname);
            } else if (!sd.canWrite()) {
                // check subdir for unwritable directory
                warn(msgs, WARN_SUBDIR_NOT_WRITABLE, subdir, tname);
            }
        } else {
            // if subdir is blank check for infinite recursion
            List<TargetData> tlist = getMakefileData().getTargetList();
            for (int i = 0; i < tlist.size(); i++) {
                if (tname.equals((tlist.get(i)).getName())) {
                    warn(msgs, WARN_INFINITE_RECURSION, tname);
                }
            }
        }

        // check makeFlags for unmatched quotes
        if (!isValidMakeFlags()) {
            warn(msgs, WARN_INVALID_MAKEFLAGS);
        }
    }

    /** Verify the Make Flags. Currently we only check for unmatched quotes */
    private boolean isValidMakeFlags() {
        char c;
        char lastChar = 0;
        int squote = 0;
        int dquote = 0;

        for (int i = 0; i < mflags.length(); i++) {
            c = mflags.charAt(i);
            if (lastChar != '\\') {
                if (c == '\'') {
                    squote++;
                }
                if (c == '"') {
                    dquote++;
                }
            }
            lastChar = c;
        }

        if ((squote % 2) == 1 || (dquote % 2) == 1) {
            return false;
        } else {
            return true;
        }
    }

    /** Create and initialize the target */
    @Override
    public void addNotify() {
        TargetData target = getMakefileData().getCurrentTarget();
        int i;

        if (!initialized) {
            create();
            initialized = true;
        }

        key = target.getKey();
        targetName.setText(tname);
        dependsOn.setText(depends);
        subdirectory.setText(subdir);
        makeFlags.setText(mflags);
        commandDisplay.setText(getCommandDisplay());
        updateButtons();
        super.addNotify();
    }

    /** Get the data from the panel and update the target */
    @Override
    public void removeNotify() {
        super.removeNotify();

        TargetData target = getMakefileData().getTarget(key);
        target.setTargetName(tname);
        target.setDependsOn(depends);
        target.setSubdirectory(subdir);
        target.setMakeFlags(mflags);
    }
}

