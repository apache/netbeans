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
package org.netbeans.modules.cnd.makefile.wizard;

import java.io.File;
import java.util.ArrayList;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cnd.utils.CndPathUtilities;

/**
 * Create the second panel in the Makefile wizard.
 */
public class BaseDirectoryPanel extends DirectoryChooserPanel {

    /** Is the base directory a valid (existing) directory? */
    private boolean baseIsValid;
    /** Serial version number */
    static final long serialVersionUID = -4831717621793094L;
    private boolean initialized;

    /**
     * Constructor for the Directory panel. Remember, most of the panel is
     * inherited from WizardDescriptor.
     */
    public BaseDirectoryPanel(MakefileWizard wd) {
        super(wd);
        String subtitle = getString("LBL_BaseDirectoryPanel"); // NOI18N
        setSubTitle(subtitle);
        this.getAccessibleContext().setAccessibleDescription(subtitle);
        initialized = false;
        baseIsValid = false;
    }

    /** Validate the requested directory. Warn the user if it doesn't exist */
    @Override
    public void validateData(ArrayList<String> msgs, int key) {
        File file = new File(getMakefileData().getBaseDirectory(MakefileData.EXPAND));

        if (file.exists()) {
            if (!file.isDirectory()) {
                warn(msgs, WARN_CWD_NOT_DIR, file.getPath());
            }
        } else {
            warn(msgs, WARN_CWD_DOES_NOT_EXIST, file.getPath());
        }
    }

    /**
     *  The default validation method. Most panels don't do validation so don't
     *  need to override this.
     */
    @Override
    public boolean isPanelValid() {
        return baseIsValid;
    }

    /** Override the defualt and do some validation */
    @Override
    protected final void onOk() {
        checkit();
    }

    /**
     *  Validate the base directory currently typed into the text field. This method should
     *  not be confused with validateData(), which is called during Makefile generation.
     *  This validation occurs while the panel is posted. The validateData() occurs much
     *  later.
     */
    private void validateCurrentBase() {
        String text;
        JTextField tf = getText();
        File dir = null;

        baseIsValid = false;
        text = CndPathUtilities.expandPath(tf.getText());
        if (text.length() > 0) {
            if (text.charAt(0) == File.separatorChar) {
                dir = new File(text);
            } else {
                dir = new File(".", text);  // NOI18N
            }
        }

        if (dir != null && !dir.isFile()) {
            baseIsValid = true;
            MakefileWizard.getMakefileWizard().updateState();
        }
    }

    /**
     *  Create the panel. Do the superclasss first and then some panel-specific stuff
     *  afterwards.
     */
    private void create() {

        create(getString("LBL_BaseDirectory"), // NOI18N
                FileChooserPanel.ABSOLUTE_PATH,
                getString("HLP_BaseDirectory"));		// NOI18N

        JTextField tf = getText();
        tf.getDocument().addDocumentListener(new DocumentListener() {

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

        getLabel().setLabelFor(tf);
        getLabel().setDisplayedMnemonic(
                getString("MNEM_BaseDirectory").charAt(0));	// NOI18N
    }

    private final void checkit() {
        boolean oldVal = baseIsValid;

        validateCurrentBase();
        if (baseIsValid != oldVal) {
            MakefileWizard.getMakefileWizard().updateState();
        }
    }

    /** Set initial data in dialog */
    @Override
    public void addNotify() {
        if (!initialized) {
            initialized = true;
            MakefileWizard.getMakefileWizard().initDirPaths();
            create();
        }
        super.addNotify();


        getText().setText(getMakefileData().getBaseDirectory());
        validateCurrentBase();
    }

    /** Update MakefileData if the data was changed */
    @Override
    public void removeNotify() {
        super.removeNotify();
        JTextField tf = getText();
        if (tf != null) {
            String base = tf.getText();
            if (!base.equals(getMakefileData().getBaseDirectory())) {
                getMakefileData().setBaseDirectory(base);
            }
            MakefileWizard.getMakefileWizard().initMakefileName();
        }
    }
}
