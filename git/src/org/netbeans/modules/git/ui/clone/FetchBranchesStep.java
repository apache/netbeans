/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.clone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.ui.selectors.ItemSelector;
import org.netbeans.modules.git.ui.selectors.ItemSelector.Item;
import org.netbeans.modules.git.ui.wizards.AbstractWizardPanel;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class FetchBranchesStep extends AbstractWizardPanel implements ChangeListener {
    private final ItemSelector<Branch> branches;

    public FetchBranchesStep () {
        branches = new ItemSelector<Branch>(NbBundle.getMessage(FetchBranchesStep.class, "LBL_RemoteBranchesTitle"));
        branches.addChangeListener(this);
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run () {
                validateBeforeNext();
            }
        });
        getJComponent().setName(NbBundle.getMessage(FetchBranchesStep.class, "LBL_FetchBranches.remoteBranches")); //NOI18N
    }
    
    @Override
    @NbBundle.Messages({
        "MSG_FetchRefsPanel.errorNoBranchSelected=No branch selected",
        "MSG_FetchRefsPanel.warningNoBranchInRepository=No branch in the repository yet"
    })
    protected final boolean validateBeforeNext () {
        boolean valid = true;
        setValid(true, null);
        if (branches.isEmpty()) {
            setValid(true, new Message(Bundle.MSG_FetchRefsPanel_warningNoBranchInRepository(), true));
        } else if(branches.getSelectedBranches().isEmpty()) {
            valid = false;
            setValid(false, new Message(Bundle.MSG_FetchRefsPanel_errorNoBranchSelected(), true));
        } else {
            setValid(true, null);
        }
        return valid;
    }

    @Override
    protected final JComponent getJComponent () {
        return branches.getPanel();
    }

    public void fillRemoteBranches (Collection<GitBranch> remoteBranches) {
        List<Branch> l = new ArrayList<Branch>(remoteBranches.size());
        boolean preselected = remoteBranches.size() == 1;
        for (GitBranch gitBranch : remoteBranches) {
            l.add(new Branch(gitBranch, preselected || GitUtils.MASTER.equals(gitBranch.getName())));
        }
        branches.setBranches(l);
        validateBeforeNext();
    }
    
    public List<GitBranch> getSelectedBranches () {
        List<Branch> selected = branches.getSelectedBranches();
        List<GitBranch> l = new ArrayList<GitBranch>(selected.size());
        for (Branch b : selected) {
            l.add(b.branch);
        }
        return l;
    }
    
    public List<String> getSelectedBranchNames () {
        List<String>  selectedBranchNames;
        if (branches.isEmpty()) {
            selectedBranchNames = Collections.singletonList("*"); //NOI18N
        } else {
            List<Branch> selectedBranches = branches.getSelectedBranches();
            selectedBranchNames = new ArrayList<String>(selectedBranches.size());
            for (Branch b : selectedBranches) {
                selectedBranchNames.add(b.branch.getName());
            }
        }
        return selectedBranchNames;
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        validateBeforeNext();
    }

    @Override
    public HelpCtx getHelp () {
        return new HelpCtx(FetchBranchesStep.class);
    }
    
    private static class Branch extends ItemSelector.Item {
        private final GitBranch branch;

        public Branch(GitBranch branch, boolean selected) {
            super(selected, false);
            this.branch = branch;
        }
        
        @Override
        public String getText() {
            return branch.getName() + (branch.isActive() ? "*" : "");
        }
        @Override
        public String getTooltipText() {
            return getText();
        }

        public String getName() {
            return branch.getName();
        }

        public String getId() {
            return branch.getId();
        }

        public boolean isRemote() {
            return branch.isRemote();
        }

        public boolean isActive() {
            return branch.isActive();
        }
        @Override
        public int compareTo(Item t) {
            if(t == null) {
                return 1;
            }
            if(t instanceof Branch) {
                return getName().compareTo(((Branch)t).getName());
            }
            return 0;
        }
    }

}
