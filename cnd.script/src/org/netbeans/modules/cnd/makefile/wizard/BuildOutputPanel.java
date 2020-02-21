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
