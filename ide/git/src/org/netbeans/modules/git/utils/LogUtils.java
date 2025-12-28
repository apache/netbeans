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

package org.netbeans.modules.git.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.git.ui.diff.DiffAction;
import org.netbeans.modules.git.ui.history.SearchHistoryAction;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.netbeans.modules.git.ui.repository.Revision;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 * @author Ondrej Vrabec
 */
public class LogUtils {
    
    private LogUtils () {
        
    }
    
    @NbBundle.Messages({
        "# {0} - [history view]", "# {1} - [diff]",
        "MSG_LogUtils.updateBranch.actions=Show changes in {0} or as {1}\n",
        "MSG_LogUtils.updateBranch.actions.history=[Search History]",
        "MSG_LogUtils.updateBranch.actions.diff=[Diff]"
    })
    public static void logBranchUpdateReview (File repository, String branchName, String oldId, String newId, OutputLogger logger) {
        if (oldId != null && newId != null
                && !oldId.equals(newId)) {
            String line = Bundle.MSG_LogUtils_updateBranch_actions("{0}", "{1}");
            int historyPos = line.indexOf("{0}");
            int diffPos = line.indexOf("{1}");
            List<String> segments = new ArrayList<>();
            OutputListener list1, list2;
            if (historyPos < diffPos) {
                segments.add(line.substring(0, historyPos));
                segments.add(Bundle.MSG_LogUtils_updateBranch_actions_history());
                list1 = new ShowHistoryListener(repository, oldId, newId);
                segments.add(line.substring(historyPos + 3, diffPos));
                segments.add(Bundle.MSG_LogUtils_updateBranch_actions_diff());
                list2 = new DiffListener(repository, branchName, oldId, newId);
                segments.add(line.substring(diffPos + 3));
            } else {
                segments.add(line.substring(0, diffPos));
                segments.add(Bundle.MSG_LogUtils_updateBranch_actions_diff());
                list1 = new DiffListener(repository, branchName, oldId, newId);
                segments.add(line.substring(diffPos + 3, historyPos));
                segments.add(Bundle.MSG_LogUtils_updateBranch_actions_history());
                list2 = new ShowHistoryListener(repository, oldId, newId);
                segments.add(line.substring(historyPos + 3));
            }
            logger.output(segments.get(0), null);
            logger.output(segments.get(1), list1);
            logger.output(segments.get(2), null);
            logger.output(segments.get(3), list2);
            logger.output(segments.get(4), null);
        }
        logger.outputLine("");
    }
    
    private static class ShowHistoryListener implements OutputListener {
        private final File repository;
        private final String from;
        private final String to;

        public ShowHistoryListener (File repository, String from, String to) {
            this.repository = new File(repository.getAbsolutePath());
            this.from = from.length() > 7 ? from.substring(0, 7) : from;
            this.to = to.length() > 7 ? to.substring(0, 7) : to;
        }

        @Override
        public void outputLineAction (OutputEvent ev) {
            SearchHistoryAction.openSearch(repository, repository, repository.getName(), from, to);
        }

    }
    
    @NbBundle.Messages({
        "# {0} - branch name", "# {1} - commit id",
        "MSG_LogUtils.updateBranch.actions.diff.previous={0} - {1}",
        "# {0} - branch name", "# {1} - commit id",
        "MSG_LogUtils.updateBranch.actions.diff.updated={0} - {1}"
    })
    private static class DiffListener implements OutputListener {
        private final File repository;
        private final String branchName;
        private final String from;
        private final String to;

        public DiffListener (File repository, String branchName, String from, String to) {
            this.repository = new File(repository.getAbsolutePath());
            this.branchName = branchName;
            this.from = from.length() > 7 ? from.substring(0, 7) : from;
            this.to = to.length() > 7 ? to.substring(0, 7) : to;
        }

        @Override
        public void outputLineAction (OutputEvent ev) {
            SystemAction.get(DiffAction.class).diff(GitUtils.getContextForFile(repository),
                    new Revision(from, Bundle.MSG_LogUtils_updateBranch_actions_diff_previous(branchName, from)),
                    new Revision(to, Bundle.MSG_LogUtils_updateBranch_actions_diff_previous(branchName, to)));
        }
        
    }
    
}
