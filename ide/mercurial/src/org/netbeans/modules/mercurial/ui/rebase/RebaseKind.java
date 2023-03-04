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
package org.netbeans.modules.mercurial.ui.rebase;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Collection;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.repository.HeadRevisionPicker;
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

    public abstract String getBase ();

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

    protected static void setRevision (JTextField tf, HgLogMessage message, String wcparentCSetId) {
        tf.setText(message.toAnnotatedString(wcparentCSetId));
    }
    
    public static class BasicKind extends RebaseKind {
        
        private final BasicPanel basicPanel;
        private final String wcParentCSet;
        private String dest;

        public BasicKind (Collection<HgLogMessage> branchHeads, HgLogMessage workingCopyParent) {
            super(BasicKind.class.getName());
            basicPanel = new BasicPanel();
            wcParentCSet = workingCopyParent.getCSetShortID();
            initialize(branchHeads, workingCopyParent);
        }

        @Override
        @NbBundle.Messages("LBL_RebaseKind.basic.displayName=Current Revision To Tipmost Head")
        public String getDisplayName () {
            return Bundle.LBL_RebaseKind_basic_displayName();
        }

        @Override
        @NbBundle.Messages("LBL_RebaseKind.basic.description=Moves Working Directory "
                + "parent and its ancestors to the tipmost current branch's head")
        public String getDescription () {
            return Bundle.LBL_RebaseKind_basic_description();
        }

        @Override
        public JPanel getPanel () {
            return basicPanel;
        }

        @Override
        public String getBase () {
            return wcParentCSet;
        }

        @Override
        public String getSource () {
            return null;
        }

        @Override
        public String getDest () {
            return dest;
        }

        private void initialize (Collection<HgLogMessage> branchHeads, HgLogMessage workingCopyParent) {
            HgLogMessage tipmostHead = branchHeads.iterator().next();
            for (HgLogMessage head : branchHeads) {
                if (head.getRevisionAsLong() > tipmostHead.getRevisionAsLong()) {
                    tipmostHead = head;
                }
            }
            dest = tipmostHead.getCSetShortID();
            setRevision(basicPanel.jTextField1, workingCopyParent, wcParentCSet);
            setRevision(basicPanel.jTextField2, tipmostHead, wcParentCSet);
            setValid(workingCopyParent.getRevisionAsLong() != tipmostHead.getRevisionAsLong());
        }
        
    }

    public static class SelectDestinationKind extends RebaseKind implements ActionListener {
        
        private final SelectDestPanel panel;
        private final String wcParentCSet;
        private HgLogMessage dest;
        private final File repository;

        public SelectDestinationKind (File repository, Collection<HgLogMessage> branchHeads, HgLogMessage workingCopyParent) {
            super(SelectDestinationKind.class.getName());
            this.repository = repository;
            panel = new SelectDestPanel();
            wcParentCSet = workingCopyParent.getCSetShortID();
            initialize(branchHeads, workingCopyParent);
            attachListeners();
        }

        @Override
        @NbBundle.Messages("LBL_RebaseKind.destination.displayName=Current Revision To Any Destination")
        public String getDisplayName () {
            return Bundle.LBL_RebaseKind_destination_displayName();
        }

        @Override
        @NbBundle.Messages("LBL_RebaseKind.destination.description=Moves Working Directory "
                + "parent and its ancestors to an arbitrary destination changeset")
        public String getDescription () {
            return Bundle.LBL_RebaseKind_destination_description();
        }

        @Override
        public JPanel getPanel () {
            return panel;
        }

        @Override
        public String getBase () {
            return wcParentCSet;
        }

        @Override
        public String getSource () {
            return null;
        }

        @Override
        public String getDest () {
            return dest.getCSetShortID();
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            if (e.getSource() == panel.btnBrowse) {
                HeadRevisionPicker picker = new HeadRevisionPicker(repository, null);
                if (picker.showDialog()) {
                    dest = picker.getSelectionRevision();
                    updateLabels();
                }
            }
        }

        private void initialize (Collection<HgLogMessage> branchHeads, HgLogMessage workingCopyParent) {
            dest = branchHeads.iterator().next();
            for (HgLogMessage head : branchHeads) {
                if (head.getRevisionAsLong() > dest.getRevisionAsLong()
                        || dest.getRevisionAsLong() == workingCopyParent.getRevisionAsLong()) {
                    dest = head;
                }
            }
            setRevision(panel.tfBaseRevision, workingCopyParent, wcParentCSet);
            updateLabels();
        }
        
        private void updateLabels () {
            setRevision(panel.tfDestinationRevision, dest, wcParentCSet);
            setValid(!wcParentCSet.equals(dest.getCSetShortID()));
        }

        private void attachListeners () {
            panel.btnBrowse.addActionListener(this);
        }
        
    }

    public static class SelectBaseKind extends RebaseKind implements ActionListener {
        
        private final SelectBasePanel panel;
        private HgLogMessage base;
        private HgLogMessage dest;
        private final File repository;
        private final String wcParentCSet;

        public SelectBaseKind (File repository, Collection<HgLogMessage> branchHeads, HgLogMessage workingCopyParent) {
            super(SelectBaseKind.class.getName());
            this.repository = repository;
            panel = new SelectBasePanel();
            wcParentCSet = workingCopyParent.getCSetShortID();
            base = workingCopyParent;
            initialize(branchHeads);
            attachListeners();
        }

        @Override
        @NbBundle.Messages("LBL_RebaseKind.base.displayName=Revision To Any Destination")
        public String getDisplayName () {
            return Bundle.LBL_RebaseKind_base_displayName();
        }

        @Override
        @NbBundle.Messages("LBL_RebaseKind.base.description=Moves any changeset "
                + "and its ancestors to an arbitrary destination changeset")
        public String getDescription () {
            return Bundle.LBL_RebaseKind_base_description();
        }

        @Override
        public JPanel getPanel () {
            return panel;
        }

        @Override
        public String getBase () {
            return base.getCSetShortID();
        }

        @Override
        public String getSource () {
            return null;
        }

        @Override
        public String getDest () {
            return dest.getCSetShortID();
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            if (e.getSource() == panel.btnBrowseDest) {
                HeadRevisionPicker picker = new HeadRevisionPicker(repository, null);
                if (picker.showDialog()) {
                    dest = picker.getSelectionRevision();
                    updateLabels();
                }
            } else if (e.getSource() == panel.btnBrowseBase) {
                HeadRevisionPicker picker = new HeadRevisionPicker(repository, null);
                if (picker.showDialog()) {
                    base = picker.getSelectionRevision();
                    updateLabels();
                }
            }
        }

        private void initialize (Collection<HgLogMessage> branchHeads) {
            dest = branchHeads.iterator().next();
            for (HgLogMessage head : branchHeads) {
                if (head.getRevisionAsLong() > dest.getRevisionAsLong()
                        || dest.getCSetShortID().equals(base.getCSetShortID())) {
                    dest = head;
                }
            }
            updateLabels();
        }
        
        private void updateLabels () {
            setRevision(panel.tfBaseRevision, base, wcParentCSet);
            setRevision(panel.tfDestinationRevision, dest, wcParentCSet);
            setValid(!base.getCSetShortID().equals(dest.getCSetShortID()));
        }

        private void attachListeners () {
            panel.btnBrowseBase.addActionListener(this);
            panel.btnBrowseDest.addActionListener(this);
        }
        
    }

    public static class SelectSourceKind extends RebaseKind implements ActionListener {
        
        private final SelectSourcePanel panel;
        private HgLogMessage source;
        private HgLogMessage dest;
        private final File repository;
        private final String wcParentCSet;

        public SelectSourceKind (File repository, Collection<HgLogMessage> branchHeads, HgLogMessage workingCopyParent) {
            super(SelectSourceKind.class.getName());
            this.repository = repository;
            panel = new SelectSourcePanel();
            wcParentCSet = workingCopyParent.getCSetShortID();
            source = workingCopyParent;
            initialize(branchHeads);
            attachListeners();
        }

        @Override
        @NbBundle.Messages("LBL_RebaseKind.source.displayName=Tree To Any Destination")
        public String getDisplayName () {
            return Bundle.LBL_RebaseKind_source_displayName();
        }

        @Override
        @NbBundle.Messages("LBL_RebaseKind.source.description=Moves any changeset "
                + "and all its descendants to an arbitrary destination changeset")
        public String getDescription () {
            return Bundle.LBL_RebaseKind_source_description();
        }

        @Override
        public JPanel getPanel () {
            return panel;
        }

        @Override
        public String getBase () {
            return null;
        }

        @Override
        public String getSource () {
            return source.getCSetShortID();
        }

        @Override
        public String getDest () {
            return dest.getCSetShortID();
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            if (e.getSource() == panel.btnBrowseDest) {
                HeadRevisionPicker picker = new HeadRevisionPicker(repository, null);
                if (picker.showDialog()) {
                    dest = picker.getSelectionRevision();
                    updateLabels();
                }
            } else if (e.getSource() == panel.btnBrowseSource) {
                HeadRevisionPicker picker = new HeadRevisionPicker(repository, null);
                if (picker.showDialog()) {
                    source = picker.getSelectionRevision();
                    updateLabels();
                }
            }
        }

        private void initialize (Collection<HgLogMessage> branchHeads) {
            dest = branchHeads.iterator().next();
            for (HgLogMessage head : branchHeads) {
                if (head.getRevisionAsLong() > dest.getRevisionAsLong()
                        || dest.getCSetShortID().equals(source.getCSetShortID())) {
                    dest = head;
                }
            }
            updateLabels();
        }
        
        private void updateLabels () {
            setRevision(panel.tfSourceRevision, source, wcParentCSet);
            setRevision(panel.tfDestinationRevision, dest, wcParentCSet);
            setValid(!source.getCSetShortID().equals(dest.getCSetShortID()));
        }

        private void attachListeners () {
            panel.btnBrowseSource.addActionListener(this);
            panel.btnBrowseDest.addActionListener(this);
        }
        
    }
    
}
