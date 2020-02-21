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
package org.netbeans.modules.mercurial.remote.ui.queues;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import org.netbeans.modules.mercurial.remote.OutputLogger;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
class FailedPatchResolver {
    private boolean failure;
    
    private static final String PREFIX_APPLYING = "applying "; //NOI18N
    private static final String PREFIX_PATCHING_FILE = "patching file "; //NOI18N
    private static final String SAVING_REJECTS = "saving rejects to file "; //NOI18N
    private static final String MISSING_FILE = "unable to find "; //NOI18N
    private static final Pattern MISSING_FILE_PATTERN = Pattern.compile("unable to find \'(.*)\' for patching"); //NOI18N
    
    private String failedPatch;
    private Map<VCSFileProxy, VCSFileProxy> rejects;
    private final VCSFileProxy root;
    private final OutputLogger logger;

    FailedPatchResolver (VCSFileProxy root, List<String> output, OutputLogger logger) {
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
            String normalizedLine = line.toLowerCase(Locale.getDefault());
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
            rejects = new LinkedHashMap<>(5);
        }
        rejects.put(VCSFileProxy.createFileProxy(root, patchedFilePath),
                VCSFileProxy.createFileProxy(root, rejectedFilePath));
    }

    void resolveFailure () {
        if (isFailure()) {
            HgUtils.openOutput(logger);
            if (JOptionPane.showConfirmDialog(null, NbBundle.getMessage(FailedPatchResolver.class, "MSG_FailedPatchResolver_patchApplyFailed", failedPatch), //NOI18N
                    NbBundle.getMessage(FailedPatchResolver.class, "LBL_FailedPatchResolver_patchApplyFailed"), //NOI18N
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                for (Map.Entry<VCSFileProxy, VCSFileProxy> e : rejects.entrySet()) {
                    VCSFileProxySupport.openFile(e.getKey());
                    VCSFileProxySupport.openFile(e.getValue());
                }
            }
        }
    }
    
}
