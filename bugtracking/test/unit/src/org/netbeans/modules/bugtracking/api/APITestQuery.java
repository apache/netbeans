/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.bugtracking.TestQuery;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.openide.util.HelpCtx;

/**
 *
 * @author tomas
 */
public class APITestQuery extends TestQuery {

    public static final String FIRST_QUERY_NAME = "First Query";
    public static final String SECOND_QUERY_NAME = "Second Query";
    
    static final String TOOLTIP_SUF = " - tooltip";
    
    private String name;
    boolean isSaved;
    boolean wasRefreshed;
    boolean wasOpened;
    boolean wasRemoved;
    QueryController.QueryMode openedMode;
    private final APITestRepository repo;
    private QueryController controller;
    private QueryProvider.IssueContainer<APITestIssue> issueContainer;

    public APITestQuery(String name, APITestRepository repo) {
        this.name = name;
        this.repo = repo;
        this.isSaved = name != null;
    }
    
    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getTooltip() {
        return getDisplayName() + TOOLTIP_SUF;
    }

    @Override
    public QueryController getController() {
        if(controller == null) {
            controller = new QueryController() {
                @Override
                public void opened() {
                    wasOpened = true;
                }
                JPanel panel;
                @Override
                public JComponent getComponent(QueryMode mode) {
                    openedMode = mode;
                    if(panel == null) {
                        panel = new JPanel();
                    }
                    return panel;
                }
                
                @Override public void closed() {  }                
                @Override public HelpCtx getHelpCtx() { return null; }
                @Override
                public boolean providesMode(QueryController.QueryMode mode) {
                    return true;
                }
                @Override
                public boolean saveChanges(String name) {
                    return true;
                }
                @Override
                public boolean discardUnsavedChanges() {
                    return true;
                }
                @Override public void addPropertyChangeListener(PropertyChangeListener l) { }
                @Override public void removePropertyChangeListener(PropertyChangeListener l) { }
                 @Override
                public boolean isChanged() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            }; 
        }
        return controller;
    }

    @Override
    public boolean isSaved() {
        return isSaved;
    }

    @Override
    public void remove() {
        wasRemoved = true;
    }

    @Override
    public void refresh() {
        issueContainer.refreshingStarted();
        issueContainer.add(repo.getIssues(APITestIssue.ID_1).iterator().next());
        wasRefreshed = true;
        issueContainer.refreshingFinished();
    }

    @Override
    public boolean canRename() {
        return true;
    }

    @Override
    public void rename(String name) {
        this.name = name;
    }

    boolean canRemove() {
        return true;
    }

    void setIssueContainer(QueryProvider.IssueContainer<APITestIssue> c) {
        issueContainer = c;
    }
    
}
