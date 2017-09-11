/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
