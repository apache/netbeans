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
