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
package org.netbeans.modules.git.ui.rebase;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.ui.repository.Revision;
import org.netbeans.modules.git.ui.repository.RevisionPicker;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Vrabec
 */
abstract class RebaseKind {
    
    private final String id;
    private final PropertyChangeSupport support;
    private boolean valid;
    
    public RebaseKind (String id) {
        this.id = id;
        support = new PropertyChangeSupport(this);
    }
    
    public abstract String getDisplayName ();
    
    public abstract String getDescription ();
    
    public abstract JPanel getPanel ();

    public abstract String getUpstream ();

    public abstract String getSource ();

    public abstract String getDest ();

    public final String getId () {
        return id;
    }

    public final void addPropertyChangeListener (PropertyChangeListener list) {
        support.addPropertyChangeListener(list);
    }
    
    protected final void setValid (boolean valid) {
        boolean oldValid = this.valid;
        this.valid = valid;
        support.firePropertyChange(Rebase.PROP_VALID, oldValid, valid);
    }

    public final boolean isValid () {
        return valid;
    }

    protected static void setRevision (JTextField tf, GitBranch branch) {
        tf.setText(branch.getName() + " (" + branch.getId().substring(0, 7) + ")"); //NOI18N
    }
    
    protected static void setRevision (JTextField tf, Revision revision) {
        tf.setText(revision.toString(true));
    }
    
    public static class BasicKind extends RebaseKind {
        
        private final BasicPanel basicPanel;
        private final String currentBranch;
        private String dest;

        public BasicKind (GitBranch currentBranch) {
            super(BasicKind.class.getName());
            basicPanel = new BasicPanel();
            this.currentBranch = currentBranch.getName();
            initialize(currentBranch);
        }

        @Override
        @NbBundle.Messages("LBL_RebaseKind.basic.displayName=Current Branch To Tracked Branch's Head")
        public String getDisplayName () {
            return Bundle.LBL_RebaseKind_basic_displayName();
        }

        @Override
        @NbBundle.Messages("LBL_RebaseKind.basic.description=Moves current branch "
                + "to the head of its tracked branch")
        public String getDescription () {
            return Bundle.LBL_RebaseKind_basic_description();
        }

        @Override
        public JPanel getPanel () {
            return basicPanel;
        }

        @Override
        public String getUpstream () {
            return dest;
        }

        @Override
        public String getSource () {
            return currentBranch;
        }

        @Override
        public String getDest () {
            return dest;
        }

        @NbBundle.Messages({
            "MSG_RebaseKind.Basic.unknownTrackedBranch=No tracked branch"
        })
        private void initialize (GitBranch currentBranch) {
            setRevision(basicPanel.jTextField1, currentBranch);
            boolean valid = false;
            if (currentBranch.getTrackedBranch() == null) {
                dest = null;
                basicPanel.jTextField2.setText(Bundle.MSG_RebaseKind_Basic_unknownTrackedBranch());
            } else {
                dest = currentBranch.getTrackedBranch().getName();
                setRevision(basicPanel.jTextField2, currentBranch.getTrackedBranch());
                valid = !currentBranch.getId().equals(currentBranch.getTrackedBranch().getId());
            }
            setValid(valid);
        }
        
    }

    public static class SelectDestinationKind extends RebaseKind implements ActionListener {
        
        private final SelectDestPanel panel;
        private final GitBranch currentBranch;
        private Revision dest;
        private final File repository;

        public SelectDestinationKind (File repository, GitBranch currentBranch) {
            super(SelectDestinationKind.class.getName());
            this.repository = repository;
            panel = new SelectDestPanel();
            this.currentBranch = currentBranch;
            initialize(currentBranch);
            attachListeners();
        }

        @Override
        @NbBundle.Messages("LBL_RebaseKind.destination.displayName=Current Branch To Any Destination")
        public String getDisplayName () {
            return Bundle.LBL_RebaseKind_destination_displayName();
        }

        @Override
        @NbBundle.Messages("LBL_RebaseKind.destination.description=Moves Current Branch commits "
                + "to any destination you select")
        public String getDescription () {
            return Bundle.LBL_RebaseKind_destination_description();
        }

        @Override
        public JPanel getPanel () {
            return panel;
        }

        @Override
        public String getUpstream () {
            return dest.getRevision();
        }

        @Override
        public String getSource () {
            return currentBranch.getName();
        }

        @Override
        public String getDest () {
            return dest.getRevision();
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            if (e.getSource() == panel.btnBrowse) {
                RevisionPicker picker = new RevisionPicker(repository, new File[0]);
                if (picker.open()) {
                    dest = picker.getRevision();
                    updateLabels();
                }
            }
        }

        private void initialize (GitBranch currentBranch) {
            setRevision(panel.tfBaseRevision, currentBranch);
            boolean valid = false;
            GitBranch trackedBranch = currentBranch.getTrackedBranch();
            if (trackedBranch == null) {
                dest = null;
                panel.tfDestinationRevision.setText(Bundle.MSG_RebaseKind_Basic_unknownTrackedBranch());
            } else {
                dest = new Revision.BranchReference(trackedBranch);
                setRevision(panel.tfDestinationRevision, currentBranch.getTrackedBranch());
                valid = !currentBranch.getId().equals(currentBranch.getTrackedBranch().getId());
            }
            setValid(valid);
        }
        
        private void updateLabels () {
            setRevision(panel.tfDestinationRevision, dest);
            setValid(!currentBranch.getId().equals(dest.getCommitId()));
        }

        private void attachListeners () {
            panel.btnBrowse.addActionListener(this);
        }
        
    }

}
