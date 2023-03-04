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
package org.netbeans.modules.git.ui.diff;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import javax.swing.JPanel;
import org.netbeans.modules.git.ui.repository.Revision;
import org.netbeans.modules.git.ui.repository.RevisionDialogController;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Vrabec
 */
abstract class DiffToRevisionKind {

    private final String id;
    private final PropertyChangeSupport support;
    private boolean valid;
    
    public DiffToRevisionKind (String id) {
        this.id = id;
        support = new PropertyChangeSupport(this);
    }
    
    public abstract String getDisplayName ();
    
    public abstract String getDescription ();
    
    public abstract JPanel getPanel ();

    public abstract Revision getTreeFirst ();

    public abstract Revision getTreeSecond ();

    public final String getId () {
        return id;
    }

    public final void addPropertyChangeListener (PropertyChangeListener list) {
        support.addPropertyChangeListener(list);
    }
    
    protected final void setValid (boolean valid) {
        boolean oldValid = this.valid;
        this.valid = valid;
        support.firePropertyChange(DiffToRevision.PROP_VALID, oldValid, valid);
    }

    public final boolean isValid () {
        return valid;
    }
    
    public static class LocalToBaseKind extends DiffToRevisionKind {
        
        private final JPanel basicPanel;

        public LocalToBaseKind () {
            super(LocalToBaseKind.class.getName());
            basicPanel = new JPanel();
            setValid(true);
        }

        @Override
        @NbBundle.Messages("LBL_DiffToRevisionKind.localToBase.displayName=Local To Base")
        public String getDisplayName () {
            return Bundle.LBL_DiffToRevisionKind_localToBase_displayName();
        }

        @Override
        @NbBundle.Messages("LBL_DiffToRevisionKind.localToBase.description=Compares"
                + " your local files against HEAD or Index")
        public String getDescription () {
            return Bundle.LBL_DiffToRevisionKind_localToBase_description();
        }

        @Override
        public JPanel getPanel () {
            return basicPanel;
        }

        @Override
        public Revision getTreeFirst () {
            return Revision.HEAD;
        }

        @Override
        public Revision getTreeSecond () {
            return Revision.LOCAL;
        }
        
    }
    
    public static class LocalToRevisionKind extends DiffToRevisionKind {
        private final RevisionDialogController revisionPicker;
        
        public LocalToRevisionKind (File repository, Revision preselectedRevision) {
            super(LocalToRevisionKind.class.getName());
            revisionPicker = new RevisionDialogController(repository, new File[0], preselectedRevision.getRevision());
            revisionPicker.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange (PropertyChangeEvent evt) {
                    if (evt.getPropertyName() == RevisionDialogController.PROP_VALID) {
                        setValid(Boolean.TRUE.equals(evt.getNewValue()));
                    }
                }
            });
            setValid(true);
        }

        @Override
        @NbBundle.Messages("LBL_DiffToRevisionKind.localToRevision.displayName=Local To Revision")
        public String getDisplayName () {
            return Bundle.LBL_DiffToRevisionKind_localToRevision_displayName();
        }

        @Override
        @NbBundle.Messages("LBL_DiffToRevisionKind.localToRevision.description=Compares"
                + " your local files against any revision")
        public String getDescription () {
            return Bundle.LBL_DiffToRevisionKind_localToRevision_description();
        }

        @Override
        public JPanel getPanel () {
            return revisionPicker.getPanel();
        }

        @Override
        public Revision getTreeFirst () {
            return revisionPicker.getRevision();
        }

        @Override
        public Revision getTreeSecond () {
            return Revision.LOCAL;
        }
        
    }
    
    public static class BaseToRevisionKind extends DiffToRevisionKind {
        private final RevisionDialogController revisionPicker;
        
        public BaseToRevisionKind (File repository, Revision preselectedRevision) {
            super(BaseToRevisionKind.class.getName());
            revisionPicker = new RevisionDialogController(repository, new File[0], preselectedRevision.getRevision());
            revisionPicker.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange (PropertyChangeEvent evt) {
                    if (evt.getPropertyName() == RevisionDialogController.PROP_VALID) {
                        setValid(Boolean.TRUE.equals(evt.getNewValue()));
                    }
                }
            });
            setValid(true);
        }

        @Override
        @NbBundle.Messages("LBL_DiffToRevisionKind.baseToRevision.displayName=Base To Revision")
        public String getDisplayName () {
            return Bundle.LBL_DiffToRevisionKind_baseToRevision_displayName();
        }

        @Override
        @NbBundle.Messages("LBL_DiffToRevisionKind.baseToRevision.description=Compares"
                + " the current HEAD against any revision")
        public String getDescription () {
            return Bundle.LBL_DiffToRevisionKind_baseToRevision_description();
        }

        @Override
        public JPanel getPanel () {
            return revisionPicker.getPanel();
        }

        @Override
        public Revision getTreeFirst () {
            return revisionPicker.getRevision();
        }

        @Override
        public Revision getTreeSecond () {
            return Revision.HEAD;
        }
        
    }
}
