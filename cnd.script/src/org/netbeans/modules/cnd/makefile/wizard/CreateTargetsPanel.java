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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.ui.CndUIUtilities;

/**
 * Create the third panel in the Makefile wizard.
 */
public class CreateTargetsPanel extends MakefileWizardPanel implements FocusListener {

    /** Serial version number */
    static final long serialVersionUID = -8649616207466524363L;

    // the fields in the first panel...
    private JLabel nameLabel;
    private JTextField nameText;
    private JButton nameChooser;
    private JRadioButton executable;
    private JRadioButton archive;
    private JRadioButton sharedLib;
    private JRadioButton recursiveMake;
    private JRadioButton customTarget;
    private ButtonGroup typeBG;
    private JButton addBtn;
    private JButton removeBtn;
    private JButton changeBtn;
    private JLabel listLabel;
    private JScrollPane listSP;
    private JList list;
    private boolean initialized;
    /** The nextButton is set if we are dynamically setting the default */
    private JButton nextButton;
    private int newKey;
    /** Keep a pointer to the MakefileWizard */
    private MakefileWizard wiz;
    /** Store the file (possibly customized) file chooser */
    protected JFileChooser fc;

    /**
     * Constructor for the Makefile name panel. Remember, most of the panel is
     * inherited from WizardDescriptor.
     */
    CreateTargetsPanel(MakefileWizard wiz) {
        super(wiz);
        this.wiz = wiz;
        String subtitle = getString("LBL_CreateTargetsPanel"); // NOI18N
        setSubTitle(subtitle);
        this.getAccessibleContext().setAccessibleDescription(subtitle);

        nextButton = null;
        initialized = false;
    }

    /** Defer widget creation until the panel needs to be displayed */
    private void create() {

        setLayout(new GridBagLayout());
        GridBagConstraints grid = new GridBagConstraints();
        grid.anchor = GridBagConstraints.WEST;
        grid.gridx = 0;
        grid.gridy = 0;
        Insets defaultInsets = grid.insets;

        nextButton = MakefileWizard.getMakefileWizard().getNextButton();

        // Create the components.
        nameLabel = new JLabel(getString("LBL_TargetName"));		// NOI18N
        nameLabel.setDisplayedMnemonic(
                getString("MNEM_TargetName").charAt(0));	// NOI18N
        add(nameLabel, grid);

        nameText = new JTextField();
        grid.anchor = GridBagConstraints.WEST;
        grid.gridx = 1;
        grid.gridy = GridBagConstraints.RELATIVE;
        grid.weightx = 100.0;
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.insets = new Insets(0, 11, 0, 5);
        add(nameText, grid);
        nameLabel.setLabelFor(nameText);

        nameChooser = new JButton(getString("BTN_Chooser"));		// NOI18N
        nameChooser.setMnemonic(getString("MNEM_Chooser").charAt(0));	// NOI18N
        grid.gridx = 2;
        grid.weightx = 0.0;
        grid.fill = GridBagConstraints.NONE;
        grid.insets = new Insets(0, 0, 0, 0);
        add(nameChooser, grid);
        createChooser(nameChooser);

        // Create the RadioButtons
        grid.gridx = 0;
        grid.gridwidth = GridBagConstraints.REMAINDER;
        grid.insets = new Insets(12, 0, 0, 0);
        executable = new JRadioButton(getString("RB_Executable"));	// NOI18N
        executable.setSelected(true);
        executable.setMnemonic(getString("MNEM_Executable").charAt(0));	// NOI18N
        add(executable, grid);

        grid.insets = defaultInsets;
        archive = new JRadioButton(getString("RB_StaticLibrary"));	// NOI18N
        archive.setMnemonic(getString("MNEM_StaticLibrary").charAt(0));	// NOI18N
        add(archive, grid);

        sharedLib = new JRadioButton(getString("RB_SharedLibrary"));	// NOI18N
        sharedLib.setMnemonic(
                getString("MNEM_SharedLibrary").charAt(0));	// NOI18N
        add(sharedLib, grid);

        recursiveMake = new JRadioButton(getString("RB_RecursiveMake"));// NOI18N
        recursiveMake.setMnemonic(
                getString("MNEM_RecursiveMake").charAt(0));	// NOI18N
        add(recursiveMake, grid);

        customTarget = new JRadioButton(getString("RB_CustomTarget"));	// NOI18N
        customTarget.setMnemonic(
                getString("MNEM_CustomTarget").charAt(0));	// NOI18N
        add(customTarget, grid);

        typeBG = new ButtonGroup();
        typeBG.add(executable);
        typeBG.add(archive);
        typeBG.add(sharedLib);
        typeBG.add(recursiveMake);
        typeBG.add(customTarget);

        JPanel bpanel = createButtonPanel();
        grid.insets = new Insets(5, 0, 0, 0);
        add(bpanel, grid);

        listLabel = new JLabel(getString("LBL_List"));			// NOI18N
        listLabel.setDisplayedMnemonic(
                getString("MNEM_List").charAt(0));		// NOI18N
        grid.insets = new Insets(11, 0, 0, 0);
        add(listLabel, grid);

        list = new JList(new DefaultListModel());
        listSP = new JScrollPane(list);
        grid.weightx = 100.0;
        grid.weighty = 100.0;
        grid.fill = GridBagConstraints.BOTH;
        grid.insets = defaultInsets;
        add(listSP, grid);
        listLabel.setLabelFor(list);

        setupListeners();
        newKey = 0;

        // a11y
        list.getAccessibleContext().setAccessibleDescription(
                getString("ACSD_List") // NOI18N
                );
        nameText.getAccessibleContext().setAccessibleDescription(
                getString("ACSD_TargetNameTF") // NOI18N
                );
        addBtn.getAccessibleContext().setAccessibleDescription(
                getString("ACSD_CreateTargetsPanelAddBtn") // NOI18N
                );
        removeBtn.getAccessibleContext().setAccessibleDescription(
                getString("ACSD_CreateTargetsPanelRemoveBtn") // NOI18N
                );
        changeBtn.getAccessibleContext().setAccessibleDescription(
                getString("ACSD_CreateTargetsPanelChangeBtn") // NOI18N
                );
        nameChooser.getAccessibleContext().setAccessibleDescription(
                getString("ACSD_CreateTargetsPanelBrowseBtn") // NOI18N
                );
    }

    private JPanel createButtonPanel() {
        JPanel bpanel = new JPanel();
        bpanel.setLayout(new GridLayout(1, 3, 6, 0));

        addBtn = new JButton(getString("BTN_Add"));			// NOI18N
        addBtn.setMnemonic(getString("MNEM_Add").charAt(0));		// NOI18N
        addBtn.setEnabled(false);
        bpanel.add(addBtn);

        removeBtn = new JButton(getString("BTN_Remove"));		// NOI18N
        removeBtn.setMnemonic(getString("MNEM_Remove").charAt(0));	// NOI18N
        removeBtn.setEnabled(false);
        bpanel.add(removeBtn);

        changeBtn = new JButton(getString("BTN_Change"));		// NOI18N
        changeBtn.setMnemonic(getString("MNEM_Change").charAt(0));	// NOI18N
        changeBtn.setEnabled(false);
        bpanel.add(changeBtn);

        return bpanel;
    }

    /**
     *  Create a FileChoose for the text field.
     */
    protected void createChooser(JButton chooser) {

        chooser.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {

                if (fc == null) {
                    fc = new JFileChooser();
                    fc.setApproveButtonText(getString("BTN_Approve"));	// NOI18N
                    fc.setDialogTitle(
                            getString("DLG_FILE_CHOOSER_TITLE"));	// NOI18N
                }

                // See if the user has already typed a directory. If so use it.
                File f = null;
                String cur = nameText.getText();
                if (cur.length() > 0) {
                    f = new File(cur);
                }

                if (f != null && f.isDirectory()) {
                    fc.setCurrentDirectory(f);
                } else {
                    fc.setCurrentDirectory(new File(
                            getMakefileData().getBaseDirectory(MakefileData.EXPAND)));
                }
                int returnVal = fc.showDialog(CreateTargetsPanel.this, null);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    //String cwd = getMakefileData().getBaseDirectory(MakefileData.EXPAND);
                    //String path = CndPathUtilities.getRelativePath(cwd, file.getPath());
                    //addTarget(path);
                    nameText.setText(file.getName());
                    setupButtons();
                /*
                addTarget(file.getName()); // Add only name (?)
                nameText.setText(null);
                addBtn.setEnabled(false);
                 */
                }
            }
        });
    }

    /** The valid state is anytime we have at least a single target */
    @Override
    public boolean isPanelValid() {

        if (!initialized || ((DefaultListModel) list.getModel()).size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public void updatePanels() {
        wiz.updatePanels(MakefileData.COMPLEX_MAKEFILE_TYPE);
    }

    private int getCurrentType() {

        if (executable.isSelected()) {
            return TargetData.COMPLEX_EXECUTABLE;
        } else if (archive.isSelected()) {
            return TargetData.COMPLEX_ARCHIVE;
        } else if (sharedLib.isSelected()) {
            return TargetData.COMPLEX_SHAREDLIB;
        } else if (recursiveMake.isSelected()) {
            return TargetData.COMPLEX_MAKE_TARGET;
        } else if (customTarget.isSelected()) {
            return TargetData.COMPLEX_CUSTOM_TARGET;
        } else {
            return 0;
        }
    }

    /**
     *  Add the target to both the JList and target list. Create the new steps
     *  and add them to the steps list.
     */
    private void addTarget(String name) {
        List<TargetData> tlist = getMakefileData().getTargetList();
        TargetData target;
        String dir;
        int type;

        // Get the data for the target
        type = getCurrentType();
        if (tlist.isEmpty()) {
            dir = getMakefileData().defaultOutputDirectory();
        } else {
            // Use the outputDirectory from the last target as a default
            dir = (tlist.get(tlist.size() - 1)).getOutputDirectory();
        }

        // Get the target name, unless it was passed in
        if (name == null) {
            name = nameText.getText().trim();
        }

        // Now make sure its not aready a defined target
        if (!((DefaultListModel) list.getModel()).contains(name)) {
            // Add the target to the target list in MakefileData
            target = new TargetData(type, name, dir, newKey);
            tlist.add(target);

            // Now add the target to the JList in the panel.
            ((DefaultListModel) list.getModel()).addElement(name);
            list.clearSelection();

            // Finally, add the target panels to MakefileWizard.
            wiz.addTarget(type, name, newKey++);
        }
    /*
    if (UsageTracking.enabled) {
    String tstring;

    if (type == TargetData.COMPLEX_EXECUTABLE) {
    tstring = new String("COMPLEX_EXECUTABLE");		// NOI18N
    } else if (type == TargetData.COMPLEX_ARCHIVE) {
    tstring = new String("COMPLEX_ARCHIVE");		// NOI18N
    } else if (type == TargetData.COMPLEX_SHAREDLIB) {
    tstring = new String("COMPLEX_SHAREDLIB");		// NOI18N
    } else if (type == TargetData.COMPLEX_MAKE_TARGET) {
    tstring = new String("COMPLEX_MAKE_TARGET");		// NOI18N
    } else if (type == TargetData.COMPLEX_CUSTOM_TARGET) {
    tstring = new String("COMPLEX_CUSTOM_TARGET");		// NOI18N
    } else {
    tstring = new String("<Unknown>");			// NOI18N
    }
    UsageTracking.sendAction("Create Target: Type " +		// NOI18N
    tstring, null);
    }
     */
    }

    /** Remove 1 or more targets, including associated steps */
    private void deleteTargets(int[] indices) {

        List<TargetData> tlist = getMakefileData().getTargetList();
        int sel;
        DefaultListModel model = (DefaultListModel) list.getModel();
        while (indices.length > 0) {
            sel = indices[0];
            wiz.deleteTarget((tlist.get(sel)).getKey());
            tlist.remove(sel);
            model.removeElementAt(sel);
            indices = list.getSelectedIndices();
        }

    /*
    if (UsageTracking.enabled) {
    UsageTracking.sendAction("Remove Target", null);		// NOI18N
    }
     */
    }

    /** Change a target, either name or type */
    private void changeTarget(int idx, String name) {
        List<TargetData> tlist = getMakefileData().getTargetList();
        TargetData target = tlist.get(idx);

        if (name.charAt(name.length() - 1) == File.separatorChar) {
            name = CndPathUtilities.trimpath(name);
            nameText.setText(name);
            ((DefaultListModel) list.getModel()).set(idx, name);
        }
        wiz.changeTarget((tlist.get(idx)).getKey(),
                name, getCurrentType());
        int ctype = getCurrentType();
        if (ctype != target.getTargetType()) {
            target = new TargetData(ctype, name, target.getOutputDirectory(), target.getKey());
            tlist.remove(idx);
            tlist.add(idx, target);
        } else {
            target.setName(name);
        }

    /*
    if (UsageTracking.enabled) {
    UsageTracking.sendAction("Change Target", null);		// NOI18N
    }
     */
    }

    /**
     *  Setup listeners for nameText and list. This needs to be done after all
     *  components are created so its not done in the create* methods.
     */
    private void setupListeners() {
        final DefaultListModel model = (DefaultListModel) list.getModel();

        nameText.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
                setupButtons();
            }

            public void insertUpdate(DocumentEvent e) {
                setupButtons();
            }

            public void removeUpdate(DocumentEvent e) {
                setupButtons();
            }
        });

        addBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String name = nameText.getText().trim();
                if (getMakefileData().validateTargetName(name, getCurrentType())) {
                    addTarget(name);
                    nameText.setText(null);
                    setupButtons();
                } else {
                    // FIXUP What to do?
                }
            }
        });

        changeBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String name = nameText.getText().trim();
                if (getMakefileData().validateTargetName(name, getCurrentType())) {
                    model.set(list.getMinSelectionIndex(), name);
                    changeTarget(list.getMinSelectionIndex(), name);
                } else {
                    // FIXUP What to do?
                }
            }
        });

        removeBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int min = list.getMinSelectionIndex();
                int max = list.getMaxSelectionIndex();
                int[] indices = list.getSelectedIndices();

                deleteTargets(indices);
                if (model.isEmpty()) {
                    nextButton.setEnabled(false);
                } else if (min >= 0 && min < model.getSize()) {
                    list.setSelectedIndex(min);
                } else {
                    int newSel = min;
                    if (min >= model.getSize()) {
                        newSel = model.getSize() - 1;
                    }
                    list.setSelectedIndex(newSel);
                }
                setupButtons();
            }
        });

        list.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                // Ignore any calls while value is being adjusted (ie, dragged)
                if (!e.getValueIsAdjusting()) {
                    int min = list.getMinSelectionIndex();
                    int max = list.getMaxSelectionIndex();

                    if (min >= 0 && max >= 0 && list.getModel().getSize() > 0) {
                        if (min == max) {
                            // Single line selections:

                            // Set selected value in the text field and enable
                            // the change button.
                            nameText.setText(model.get(min).toString());
                            setTargetType(model.get(min).toString());
                        }
                    }
                    setupButtons();
                }
            }
        });

        list.addFocusListener(this);
    }

    private void setupButtons() {
        int min = list.getMinSelectionIndex();
        int max = list.getMaxSelectionIndex();
        String name = nameText.getText().trim();
        addBtn.setEnabled(name.length() > 0);
        if (min < 0) {
            changeBtn.setEnabled(false);
            removeBtn.setEnabled(false);
        } else if (min == max) {
            changeBtn.setEnabled(name.length() > 0);
            removeBtn.setEnabled(true);
        } else {
            changeBtn.setEnabled(false);
            removeBtn.setEnabled(true);
        }
        if (inFocus == list || name.length() == 0) {
            getRootPane().setDefaultButton(nextButton);
        } else {
            getRootPane().setDefaultButton(addBtn);
        }
    }

// --------------------------
// implemetns FocusListener
// --------------------------
    private Component inFocus = null;

    public void focusGained(FocusEvent evt) {
        Component comp = evt.getComponent();
        inFocus = comp;
        if (inFocus == list) {
            DefaultListModel model = (DefaultListModel) list.getModel();
            if (!model.isEmpty() && list.getLeadSelectionIndex() < 0) {
                list.setSelectedIndex(0);
            }
        }
    }

    public void focusLost(FocusEvent evt) {
        inFocus = null;
    }

    /** set the target type radio button */
    private void setTargetType(String tname) {
        List<TargetData> tlist = getMakefileData().getTargetList();
        TargetData target;

        for (int i = 0; i < tlist.size(); i++) {
            target = tlist.get(i);

            if (tname.equals(target.getName())) {
                setTargetType(target.getTargetType());
                return;
            }
        }
    }

    /** set the target type radio button */
    private void setTargetType(int type) {

        switch (type) {
            case TargetData.COMPLEX_EXECUTABLE:
                typeBG.setSelected(executable.getModel(), true);
                break;

            case TargetData.COMPLEX_ARCHIVE:
                typeBG.setSelected(archive.getModel(), true);
                break;

            case TargetData.COMPLEX_SHAREDLIB:
                typeBG.setSelected(sharedLib.getModel(), true);
                break;

            case TargetData.COMPLEX_MAKE_TARGET:
                typeBG.setSelected(recursiveMake.getModel(), true);
                break;

            case TargetData.COMPLEX_CUSTOM_TARGET:
                typeBG.setSelected(customTarget.getModel(), true);
                break;
        }
        repaint();
    }

    /** Create the widgets if not initialized. Also update the panel fields */
    @Override
    public void addNotify() {
        List<TargetData> tlist = getMakefileData().getTargetList();
        TargetData target;

        // Create the gui and initialize the fields
        if (!initialized) {
            create();

            if (tlist.size() > 0) {
                target = tlist.get(tlist.size() - 1);
                setTargetType(target.getTargetType());

                DefaultListModel model = (DefaultListModel) list.getModel();
                for (int i = 0; i < tlist.size(); i++) {
                    model.addElement((tlist.get(i)).getName());
                }
            } else {
                executable.setEnabled(true);
            }
            initialized = true;
        }

        // If we have an existing target, does it need converting to a new type?
        // Is the target a SIMPLE_*? If so try and convert it.
        if (tlist.size() > 0 &&
                (target = tlist.get(0)).getTargetType() <
                MakefileData.COMPLEX_MAKEFILE_TYPE) {
            target.convert();
            if (newKey == 0) {
                newKey++;			    // so the next key isn't 0
            }

            // Does a Create step exist? If it does it means this target
            // has been converted more than once.
            if (!wiz.targetExists(target.getKey())) {
                wiz.addTarget(target.getTargetType(),
                        target.getName(), target.getKey());
            }
        }

        super.addNotify();
        nameText.selectAll();
        CndUIUtilities.requestFocus(nameText);
    }

    /** Remove the file chooser if its showing */
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
