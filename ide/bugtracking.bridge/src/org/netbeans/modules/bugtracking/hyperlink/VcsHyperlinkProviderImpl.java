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

package org.netbeans.modules.bugtracking.hyperlink;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.bugtracking.api.Util;
import org.netbeans.modules.versioning.util.VCSHyperlinkProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provides hyperlink functionality on issue reference in VCS artefacts as e.g. log messages in Search History
 *
 * @author Tomas Stupka
 */
@ServiceProvider(service=VCSHyperlinkProvider.class, position = 100)
public class VcsHyperlinkProviderImpl extends VCSHyperlinkProvider {

    @Override
    public int[] getSpans(String text) {
        return Util.getIssueSpans(text);
    }

    @Override
    public String getTooltip(String text, int offsetStart, int offsetEnd) {
        return NbBundle.getMessage(VcsHyperlinkProviderImpl.class, "LBL_OpenIssue", new Object[] { Util.getIssueId(text.substring(offsetStart, offsetEnd))});
    }

    @Override
    public void onClick(final File file, final String text, int offsetStart, int offsetEnd) {
        final String issueId = Util.getIssueId(text.substring(offsetStart, offsetEnd));
        if(issueId == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "No issue found for {0}", text.substring(offsetStart, offsetEnd));
            return;
        }
        RequestProcessor.getDefault().post(() -> {
            Util.openIssue(FileUtil.toFileObject(file), issueId);
        });
    }

}
