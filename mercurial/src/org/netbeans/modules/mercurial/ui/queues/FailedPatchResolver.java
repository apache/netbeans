/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.mercurial.ui.queues;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
class FailedPatchResolver {
    private boolean failure;
    
    private static final String PREFIX_APPLYING = "applying "; //NOI18N
    private static final String PREFIX_PATCHING_FILE = "patching file "; //NOI18N
    private static final String SAVING_REJECTS = "saving rejects to file "; //NOI18N
    private static final String MISSING_FILE = "unable to find "; //NOI18N
    private static final Pattern MISSING_FILE_PATTERN = Pattern.compile("unable to find \'(.*)\' for patching"); //NOI18N
    
    private String failedPatch;
    private Map<File, File> rejects;
    private final File root;
    private final OutputLogger logger;

    FailedPatchResolver (File root, List<String> output, OutputLogger logger) {
        this.root = root;
        this.logger = logger;
        initialize(output);
    }

    boolean isFailure () {
        return failure;
    }

    private void initialize (List<String> unfilteredOutput) {
        String patchedFile = null;
        for (String line : unfilteredOutput) {
            String normalizedLine = line.toLowerCase();
            if (normalizedLine.startsWith(PREFIX_APPLYING)) {
                patchedFile = null;
                failedPatch = line.substring(PREFIX_APPLYING.length());
            } else if (normalizedLine.startsWith(PREFIX_PATCHING_FILE)) {
                patchedFile = line.substring(PREFIX_PATCHING_FILE.length());
            } else if (normalizedLine.contains(MISSING_FILE)) {
                Matcher m = MISSING_FILE_PATTERN.matcher(line);
                if (m.matches()) {
                    patchedFile = m.group(1);
                }
            } else if (normalizedLine.contains(SAVING_REJECTS)) {
                failure = true;
                if (patchedFile != null) {
                    addToRejects(patchedFile, line.substring(normalizedLine.indexOf(SAVING_REJECTS) + SAVING_REJECTS.length()));
                }
            }
        }
    }

    private void addToRejects (String patchedFilePath, String rejectedFilePath) {
        if (rejects == null) {
            rejects = new LinkedHashMap<File, File>(5);
        }
        rejects.put(new File(root, patchedFilePath.replace("/", File.separator)), //NOI18N
                new File(root, rejectedFilePath.replace("/", File.separator))); //NOI18N
    }

    void resolveFailure () {
        if (isFailure()) {
            HgUtils.openOutput(logger);
            if (JOptionPane.showConfirmDialog(null, NbBundle.getMessage(FailedPatchResolver.class, "MSG_FailedPatchResolver_patchApplyFailed", failedPatch), //NOI18N
                    NbBundle.getMessage(FailedPatchResolver.class, "LBL_FailedPatchResolver_patchApplyFailed"), //NOI18N
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                for (Map.Entry<File, File> e : rejects.entrySet()) {
                    Utils.openFile(e.getKey());
                    Utils.openFile(e.getValue());
                }
            }
        }
    }
    
}
