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
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.bugtracking.TestIssue;
import org.netbeans.modules.bugtracking.spi.IssueController;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.openide.util.HelpCtx;

/**
 *
 * @author tomas
 */
public class APITestIssue extends TestIssue {
    static final String ID_1 = "1";
    static final String ID_2 = "2";
    static final String ID_SUB_3 = "3";
    static final String ID_NEW = "1000";

    static final String SUMMARY_SUF = " - summary";
    static final String TOOLTIP_SUF = " - tooltip";
    
    private final String id;
    private final boolean isNew;
    boolean wasOpened;
    boolean wasRefreshed;
    boolean wasClosedOnComment;
    String addedComment;
    String attachedPatchDesc;
    boolean idFinished;
    File attachedFile;
    private IssueController controller;
    private final APITestRepository repo;
    private String summary;
    private String description;
    private boolean isPatch;

    public APITestIssue(String id, APITestRepository repo) {
        this(id, repo, false);
    }
    
    public APITestIssue(String id, APITestRepository repo, boolean isNew) {
        this(id, repo, isNew, id + SUMMARY_SUF, null);
    }
    
    public APITestIssue(String id, APITestRepository repo, boolean isNew, String summary, String description) {
        this.id = id;
        this.isNew = isNew;
        this.repo = repo;
        this.summary = summary;
        this.description = description;
    }
    
    @Override
    public String getDisplayName() {
        return "Issue : " + id + getSummary();
    }

    @Override
    public String getTooltip() {
        return id + TOOLTIP_SUF;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getSummary() {
        return summary;
    }
    
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public boolean refresh() {
        wasRefreshed = true;
        support.firePropertyChange(IssueProvider.EVENT_ISSUE_DATA_CHANGED, null, null);
        return true;
    }

    @Override
    public void addComment(String comment, boolean closeAsFixed) {
        wasClosedOnComment = closeAsFixed;
        addedComment = comment;
    }

    @Override
    public void attachFile(File file, String description, boolean isPatch) {
        this.attachedPatchDesc = description;
        this.attachedFile = file;
        this.isPatch = isPatch;
    }

    @Override
    public IssueController getController() {
        if(controller == null) {
            controller = new IssueController() {
                @Override
                public void opened() {
                    wasOpened = true;
                }
                private JPanel panel;
                @Override
                public JComponent getComponent() {
                    if(panel == null) {
                        panel = new JPanel();
                    }
                    return panel;
                }
                @Override public HelpCtx getHelpCtx() { return null; }
                @Override public void closed() { }
                @Override public boolean saveChanges() { return true; }
                @Override public boolean discardUnsavedChanges() { return true; }
                @Override public boolean isChanged() { return false; }
                @Override public void addPropertyChangeListener(PropertyChangeListener l) { }
                @Override public void removePropertyChangeListener(PropertyChangeListener l) { }
            };
        }
        return controller;
    }

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    @Override
    public Collection<String> getSubtasks() {
        return Arrays.asList(new String[] {APITestIssue.ID_SUB_3});
    }

    @Override
    public boolean isFinished() {
        return idFinished;
    }

    void discardOutgoing() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    boolean submit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
