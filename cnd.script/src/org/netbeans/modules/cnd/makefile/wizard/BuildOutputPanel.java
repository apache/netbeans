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
import org.netbeans.modules.cnd.utils.CndPathUtilities;

/**
 * Create the BuildOutputPanel in the MakefileWizard.
 */
public class BuildOutputPanel extends DirectoryChooserPanel {

    /** Serial version number */
    static final long serialVersionUID = 2730227827286861351L;
    /** Store the target key */
    private int key;
    private boolean initialized;

    /**
     * Constructor for the BuildOutputPanel.
     */
    public BuildOutputPanel(MakefileWizard wd) {
        super(wd);
        String subtitle = getString("LBL_BuildOutputPanel"); // NOI18N
        setSubTitle(subtitle);
        this.getAccessibleContext().setAccessibleDescription(subtitle);
        initialized = false;
    }

    /** Defer widget creation until the panel needs to be displayed */
    private void create() {

        //create(getString("LBL_TargetDirectory"), NAME_ONLY);		// NOI18N
        create(getString("LBL_TargetDirectory"), RELATIVE_PATH);	// NOI18N
        getLabel().setLabelFor(getText());
        getLabel().setDisplayedMnemonic(
                getString("MNEM_TargetDirectory").charAt(0));	// NOI18N
    }

    /** Validate the output directory */
    @Override
    public void validateData(ArrayList<String> msgs, int key) {
        TargetData target = getMakefileData().getTarget(key);
        String cwd = getMakefileData().getBaseDirectory(MakefileData.EXPAND);
        String odir = CndPathUtilities.expandPath(target.getOutputDirectory());
        File outdir = null;
        File outpar = null;
        File cwf = null;

        if (odir.length() > 0 && !odir.equals(cwd)) {
            if (odir.startsWith(File.separator)) {
                outdir = new File(odir);
            } else {
                outdir = new File(cwd, odir);
            }
            outpar = outdir.getParentFile();
            cwf = new File(cwd);
        }

        if (outdir != null && !outdir.equals(cwf)) {
            if (!outdir.exists()) {
                if (outpar == null || !outpar.canWrite()) {
                    warn(msgs, WARN_CANNOT_CREATE_OUTPUT_DIR, outdir.getPath());
                }
            } else if (!outdir.canWrite()) {
                warn(msgs, WARN_CANNOT_WRITE_TO_OUTPUT_DIR, outdir.getPath());
            }
        }
    }

    /** Initialize the panel and update the values when displayed */
    @Override
    public void addNotify() {
        TargetData target = getMakefileData().getCurrentTarget();
        String od = target.getOutputDirectory();
        String real_od = od;
        key = target.getKey();

        if (!initialized) {
            create();
            initialized = true;
        }

        if (od.charAt(0) == '$') {
            od = CndPathUtilities.expandPath(real_od);
        }
        if (od.charAt(0) == '/' || od.charAt(0) == '~') {
            getText().setText(real_od);
        } else {
            //getText().setText(getMakefileData().getBaseDirectory() + File.separator + real_od);
            getText().setText(CndPathUtilities.getRelativePath(getMakefileData().getBaseDirectory(), real_od));
        }
        super.addNotify();
    }

    /** Update the MakefileData */
    @Override
    public void removeNotify() {
        super.removeNotify();
        JTextField tf = getText();
        if (tf != null) {
            TargetData target = getMakefileData().getTarget(key);
            String od = tf.getText();
            if (od == null) {
                od = "."; // NOI18N
            }
            od = od.trim();
            if (od.length() > 1) {
                od = CndPathUtilities.trimpath(od);
            }
            if (od.length() == 0) {
                od = "."; // NOI18N
            }
            target.setOutputDirectory(od); // FIXUP: no trim here ????
        }
    }
}
