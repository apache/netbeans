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

import java.io.File;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import org.netbeans.modules.cnd.makefile.wizard.EnterItemsPanel.ErrorInfo;

public class MakefileIncludesPanel extends EnterItemsPanel {

    /** Serial version number */
    static final long serialVersionUID = -3932940292545539665L;

    private int key;
    private boolean initialized;


    /**
     * Constructor for the Makefile sources panel. Remember, most of the panel
     * is inherited from WizardDescriptor.
     */
    MakefileIncludesPanel(MakefileWizard wd) {
        super(wd);
        String subtitle = getString("LBL_MakefileIncludesPanel"); // NOI18N
        setSubTitle(subtitle);
        this.getAccessibleContext().setAccessibleDescription(subtitle);
        initialized = false;
    }


    /** Defer widget creation until the panel needs to be displayed */
    private void create() {
        create(getString("LBL_IncDir"), getString("MNEM_IncDir").charAt(0), // NOI18N
                DIR_CHOOSER | DYNAMIC_DEFAULT_BUTTONS);
    }

    /** Set the label for the Source List */
    @Override
    protected String getListLabel() {
        return getString("LBL_IncludesList"); // NOI18N
    }

    /** Set the mnemonic for the Source List */
    @Override
    protected char getListMnemonic() {
        return getString("MNEM_IncludesList").charAt(0); // NOI18N
    }

    /** Validate the include directories */
    @Override
    public void validateData(ArrayList<String> msgs, int key) {
        TargetData target = getMakefileData().getTarget(key);

        String[] ilist = target.getIncludesList();
        if (ilist == null) {
            warn(msgs, WARN_NO_INC_DIRS, target.getName());
        } else {
            String cwd = getMakefileData().getBaseDirectory(MakefileData.EXPAND);
            ArrayList<String> dne = new ArrayList<String>();
            ArrayList<String> notDir = new ArrayList<String>();

            for (int i = 0; i < ilist.length; i++) {
                String incDir = ilist[i];

                File dir;
                if (incDir.startsWith(File.separator)) {
                    dir = new File(incDir);
                } else {
                    dir = new File(cwd, incDir);
                }
                if (dir != null) {
                    if (!dir.exists()) {
                        dne.add("\t" + dir.getPath() + "\n"); // NOI18N
                    } else if (!dir.isDirectory()) {
                        notDir.add("\t" + dir.getPath() + "\n"); // NOI18N
                    }
                }
            }

            if (dne.size() > 0) {
                if (dne.size() < MAX_ITEMS_TO_SHOW) {
                    warn(msgs, WARN_DNE_INCDIR, target.getName());
                    for (int i = 0; i < dne.size(); i++) {
                        msgs.add(dne.get(i));
                    }
                    msgs.add("\n"); // NOI18N
                } else {
                    warn(msgs, WARN_DNE_COUNT, target.getName(),
                            String.valueOf(dne.size()));
                }
            }

            if (notDir.size() > 0) {
                if (notDir.size() < MAX_ITEMS_TO_SHOW) {
                    warn(msgs, WARN_INC_NOT_DIR, target.getName());
                    for (int i = 0; i < notDir.size(); i++) {
                        msgs.add(notDir.get(i));
                    }
                    msgs.add("\n"); // NOI18N
                } else {
                    warn(msgs, WARN_INC_NOT_DIR_COUNT, target.getName(),
                            String.valueOf(notDir.size()));
                }
            }
        }
    }


    /** Get the title and message for the error dialog */
    protected ErrorInfo getErrorInfo() {
        return new ErrorInfo(getString("DLG_MIP_EmptyRE"), // NOI18N
                getString("MSG_NoFilesMatched")); // NOI18N
    }


    /**
     *  Check the input and remove any invalid syntax. If the text starts with
     *  any option other than -I ignore completely and return null.
     *
     *  @param text The raw input as typed by the user
     *  @return	    The validated (and possibly modified) string or null
     */
    @Override
    protected String validateInput(String text) {
        if (text.startsWith("-I")) { // NOI18N
            return text.substring(2);
        } else if (text.charAt(0) == '-') { // NOI18N
            return null;
        } else {
            return text;
        }
    }


    /** Create the widgets if first time */
    @Override
    public void addNotify() {
        TargetData target = getMakefileData().getCurrentTarget();
        key = target.getKey();

        if (!initialized) {
            create();
            initialized = true;
        }

        // Initialize the list. First, remove any from the JList. Then, add any
        // entries from the target into the JList.
        DefaultListModel model = (DefaultListModel) getList().getModel();
        model.removeAllElements();
        String[] ilist = target.getIncludesList();
        if (ilist != null) {
            for (int i = 0; i < ilist.length; i++) {
                model.addElement(ilist[i]);
            }
        }

        super.addNotify();
    }


    /** Get the data from the panel and update the target */
    @Override
    public void removeNotify() {
        super.removeNotify();
        TargetData target = getMakefileData().getTarget(key);
        target.setIncludesList(getListItems());
    }
}
