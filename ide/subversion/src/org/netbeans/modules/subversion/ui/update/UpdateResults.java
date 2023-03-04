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
package org.netbeans.modules.subversion.ui.update;

import org.netbeans.modules.versioning.util.NoContentPanel;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.text.DateFormat;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Displays files that have been updated.
 * 
 * @author Maros Sandor
 */
class UpdateResults extends JComponent {
        
    private final List<FileUpdateInfo> results;
    
    public UpdateResults(List<FileUpdateInfo> results, SVNUrl url, String contextDisplayName) {
        this.results = results;
        String time = DateFormat.getTimeInstance().format(new Date());
        setName(NbBundle.getMessage(UpdateResults.class, "CTL_UpdateResults_Title", SvnUtils.decodeToString(url), contextDisplayName, time)); // NOI18N
        setLayout(new BorderLayout());
        if (results.size() == 0) {
            add(new NoContentPanel(NbBundle.getMessage(UpdateResults.class, "MSG_NoFilesUpdated"))); // NOI18N
        } else {
            final UpdateResultsTable urt = new UpdateResultsTable();
            Subversion.getInstance().getRequestProcessor().post(new Runnable () {
                public void run() {
                    final UpdateResultNode[] nodes = createNodes();
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            urt.setTableModel(nodes);
                            add(urt.getComponent());
                        }
                    });
                }
            });
        }
    }

    private UpdateResultNode[] createNodes() {
        UpdateResultNode [] nodes = new UpdateResultNode[results.size()];
        int idx = 0;
        for (FileUpdateInfo info : results) {
            nodes[idx++] = new UpdateResultNode(info);
        }
        return nodes;
    }   
    
}
