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
