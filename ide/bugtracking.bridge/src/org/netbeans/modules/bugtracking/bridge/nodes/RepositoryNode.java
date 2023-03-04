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

package org.netbeans.modules.bugtracking.bridge.nodes;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.Util;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class RepositoryNode extends AbstractNode implements PropertyChangeListener {
    private final Repository repository;

    public RepositoryNode(Repository repository) {
        super(Children.LEAF);
        this.repository = repository;
        setName(repository.getDisplayName());
        repository.addPropertyChangeListener(this);
    }

    @Override
    public Image getIcon(int type) {
        return repository.getIcon();
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            new AbstractAction(NbBundle.getMessage(BugtrackingRootNode.class, "CTL_QueryAction")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Util.createNewQuery(repository);
                }
            },
            new AbstractAction(NbBundle.getMessage(BugtrackingRootNode.class, "CTL_IssueAction")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Util.createNewIssue(repository);
                }
            },
            new AbstractAction(NbBundle.getMessage(BugtrackingRootNode.class, "LBL_EditRepository")) { // NOI18N
                @Override
                public void actionPerformed(ActionEvent e) {
                    Util.edit(repository);
                }
            },
            new AbstractAction(NbBundle.getMessage(BugtrackingRootNode.class, "LBL_RemoveRepository")) { // NOI18N
                @Override
                public void actionPerformed(ActionEvent e) {
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(RepositoryNode.class, "MSG_RemoveRepository", new Object[] { repository.getDisplayName() }), // NOI18N
                        NbBundle.getMessage(RepositoryNode.class, "CTL_RemoveRepository"),      // NOI18N
                        NotifyDescriptor.OK_CANCEL_OPTION);

                    if(DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
                        RequestProcessor.getDefault().post(new Runnable() {
                            @Override
                            public void run() {
                                repository.remove();
                            }
                        });
                    }
                }
            }
        };
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Repository.EVENT_ATTRIBUTES_CHANGED)) {
            super.setDisplayName(repository.getDisplayName());
        }
    }

}
