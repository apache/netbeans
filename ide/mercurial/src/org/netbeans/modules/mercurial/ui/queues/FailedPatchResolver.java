/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
import org.openide.util.Utilities;

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
            if (JOptionPane.showConfirmDialog(Utilities.findDialogParent(), NbBundle.getMessage(FailedPatchResolver.class, "MSG_FailedPatchResolver_patchApplyFailed", failedPatch), //NOI18N
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
