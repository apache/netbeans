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

package org.netbeans.modules.bugtracking.issuetable;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;

/**
 *
 * @author Tomas Stupka
 */
class FindInQuerySupport {
    private final FindInQueryBar bar;
    private final AncestorListener ancestorListener;
    private final IssueTable issueTable;

    private FindInQuerySupport(IssueTable issueTable) {
        this.issueTable = issueTable;
        bar = new FindInQueryBar(this);
        bar.setVisible(false);
        
        ancestorListener = new AncestorListener() {
            private Action sfa;
            @Override
            public void ancestorAdded(AncestorEvent ae) {
                TopComponent tc = findTC(ae.getComponent());
                if(tc != null) {
                    ActionMap actionMap = tc.getActionMap();
                    CallbackSystemAction a = SystemAction.get(org.openide.actions.FindAction.class);
                    sfa = actionMap.get(a.getActionMapKey());
                    actionMap.put(a.getActionMapKey(), new FindAction());                
                }
            }
            @Override 
            public void ancestorRemoved(AncestorEvent ae) { 
                TopComponent tc = findTC(ae.getComponent());
                if(tc != null && sfa != null) {
                    ActionMap actionMap = tc.getActionMap();
                    CallbackSystemAction a = SystemAction.get(org.openide.actions.FindAction.class);
                    actionMap.put(a.getActionMapKey(), sfa);                
                    sfa = null;
                }
            }
            @Override 
            public void ancestorMoved(AncestorEvent ae) { }

            private TopComponent findTC(Component cmp) {
                Container parent;
                while((parent = cmp.getParent()) != null) {
                    cmp = parent;
                    if(parent instanceof TopComponent) {
                        return (TopComponent) cmp;
                    }
                }
                return null;
            }
        };
    }

    static FindInQuerySupport create(IssueTable issueTable) {
        return new FindInQuerySupport(issueTable);
    }

    FindInQueryBar getFindBar() {
        return bar;
    }

    public AncestorListener getAncestorListener() {
        return ancestorListener;
    }

    void reset() {
        if(issueTable != null) {
            issueTable.resetFilterBySummary();
        }
    }

    protected void updatePattern() {        
        if(issueTable != null) {
            issueTable.setFilterBySummary(bar.getText(), bar.getRegularExpression(), bar.getWholeWords(), bar.getMatchCase());
        }
    }

    protected void cancel() {
        reset();
        bar.setVisible(false);
    }

    protected void switchHighlight(boolean on) {
        if(issueTable != null) {
            issueTable.switchFilterBySummaryHighlight(on);
        }
    }

    private class FindAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(issueTable == null) {
                return; 
            }
            if (bar.isVisible()) {
                updatePattern();
            } else {
                bar.setVisible(true);
                updatePattern();
            }
            bar.requestFocusInWindow();
        }
    }

}
