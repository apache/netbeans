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

import java.io.File;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import org.netbeans.modules.cnd.makefile.wizard.EnterItemsPanel.ErrorInfo;

/**
 * Create the Sources panel in the Makefile wizard.
 */

public class MakefileSourcesPanel extends EnterItemsPanel {

    /** Save the source filter rather than doing repeated lookups */
    private String srcFilter;

    /** Serial version number */
    static final long serialVersionUID = -6961895016031819992L;

    private boolean initialized;

    /** Store the target key */
    private int		key;

    /**
     * Constructor for the Makefile sources panel.
     */
    public MakefileSourcesPanel(MakefileWizard wd) {
        super(wd);
        String subtitle = getString("LBL_MakefileSourcesPanel"); // NOI18N
        setSubTitle(subtitle);
        this.getAccessibleContext().setAccessibleDescription(subtitle);
        initialized = false;
    }


    /** Defer widget creation until the panel needs to be displayed */
    private void create() {
        int flags;
        String msg;
        if (getMakefileData().getMakefileType() == MakefileData.COMPLEX_MAKEFILE_TYPE) {
            flags = EXPAND_DIRS | MSP_FILTER | DYNAMIC_DEFAULT_BUTTONS | DYNAMIC_LAST_BUTTON | ITEMS_REQUIRED | DIR_AND_FILE_CHOOSER;
            msg = getString("LBL_SourceNamesComplex"); // NOI18N
        } else {
            flags = EXPAND_DIRS | MSP_FILTER | DYNAMIC_DEFAULT_BUTTONS | ITEMS_REQUIRED;
            msg = getString("LBL_SourceNamesSimple"); // NOI18N
        }
        create(msg, getString("MNEM_SourceNames").charAt(0), flags); // NOI18N
    }

    /** Set the label for the Source List */
    @Override
    protected String getListLabel() {
        return getString("LBL_SourceList"); // NOI18N
    }

    /** Set the mnemonic for the Source List */
    @Override
    protected char getListMnemonic() {
        return getString("MNEM_SourceList").charAt(0); // NOI18N
    }


    /** Get the title and message for the error dialog */
    protected ErrorInfo getErrorInfo() {
        return new ErrorInfo(getString("DLG_NoFilesError"), // NOI18N
                getString("MSG_NoFilesMatched")); // NOI18N
    }


    /** Validate the source files */
    @Override
    public void validateData(ArrayList<String> msgs, int key) {
        TargetData target = getMakefileData().getTarget(key);

        String[] slist = target.getSourcesList();
        if (slist == null) {
            warn(msgs, WARN_NO_SRC_FILES, target.getName());
        } else {
            String cwd = getMakefileData().getBaseDirectory(MakefileData.EXPAND);
            ArrayList<String> dne = new ArrayList<String>();
            int absCount = 0;
            int hdrCount = 0;
            int i;

            for (i = 0; i < slist.length; i++) {
                String srcFile = slist[i];

                if (srcFile.startsWith("/")) { // NOI18N
                    absCount++;
                }

                if (srcFile.endsWith(".h")) { // NOI18N
                    hdrCount++;
                }

                File file;
                if (srcFile.startsWith(File.separator)) {
                    file = new File(srcFile);
                } else {
                    file = new File(cwd, srcFile);
                }
                if (!file.exists()) {
                    dne.add("\t" + file.getPath() + "\n"); // NOI18N
                }
            }

            if (absCount > 0) {
                warn(msgs, WARN_ABSPATH_SRC_COUNT, target.getName(),
                        String.valueOf(absCount));
            }

            if (hdrCount > 0) {
                warn(msgs, WARN_HDR_SRC_COUNT, target.getName(),
                        String.valueOf(hdrCount));
            }

            if (dne.size() > 0) {
                if (dne.size() < MAX_ITEMS_TO_SHOW) {
                    warn(msgs, WARN_DNE_FILES, target.getName());
                    for (i = 0; i < dne.size(); i++) {
                        msgs.add(dne.get(i));
                    }
                    msgs.add("\n"); // NOI18N
                } else {
                    warn(msgs, WARN_DNE_COUNT, target.getName(),
                            String.valueOf(dne.size()));
                }
            }
        }
    }


    /** Create the widgets if not initialized. Also initialize the text field */
    @Override
    public void addNotify() {
        TargetData target = getMakefileData().getCurrentTarget();
        key = target.getKey();

        if (!initialized) {
            create();
            srcFilter = getString("DFLT_SourceFilter"); // NOI18N
            initialized = true;
        }

        // Initialize the text field
        getEntryText().setText(srcFilter);

        // Initialize the list. First, remove any from the JList. Then, add any
        // entries from the target into the JList.
        DefaultListModel model = (DefaultListModel) getList().getModel();
        model.removeAllElements();
        String[] slist = target.getSourcesList();
        if (slist != null) {
            for (int i = 0; i < slist.length; i++) {
                model.addElement(slist[i]);
            }
        }

        super.addNotify();
    }


    /** Get the data from the panel and update the target */
    @Override
    public void removeNotify() {
        super.removeNotify();

        TargetData target = getMakefileData().getTarget(key);

        String[] slist = getListItems();
        target.setSourcesList(slist);
    }
}
