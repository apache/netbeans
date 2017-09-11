/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.persistence.spi.support;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopesFactory;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopesImplementation;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;

/**
 * Helper class for implementing
 * {@link org.netbeans.modules.j2ee.persistence.spi.PersistenceScopesProvider}.
 * It creates and maintains a {@link org.netbeans.modules.j2ee.persistence.api.PersistenceScopes}
 * instance containing a single {@link org.netbeans.modules.j2ee.persistence.api.PersistenceScope}
 * or an empty array of <code>PersistenceScope</code> depending on whether the persistence.xml
 * file corresponding to that <code>PersistenceScope</code> exists or not, firing property changes
 * as the persistence.xml file is created/deleted.
 *
 * @author Andrei Badea
 */
public final class PersistenceScopesHelper {

    private static final Logger LOG = Logger.getLogger(PersistenceScopesHelper.class.getName());

    private final PersistenceScopes persistenceScopes = PersistenceScopesFactory.createPersistenceScopes(new PersistenceScopesImpl());
    private final PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);
    private final FileChangeListener fileChangeListener = new FileListener();

    private PersistenceScope persistenceScope;
    private File persistenceXml;
    private boolean persistenceExists;

    public PersistenceScopesHelper() {
    }

    /**
     * Call this method in order to change the persistence scope returned by the
     * <code>PersistenceScopes</code> instance returned by {@link #getPersistenceScopes}
     * or the corresponding persistence.xml file.
     *
     * @param  newPersistenceScope the new persistence scope; can be null, but in this case
     *         the <code>newPersistenceXml</code> parameter must be null too.
     * @param  newPersistenceXml the new persistence.xml file; can be null.
     *
     * @throws IllegalArgumentException if <code>newPersistenceScope</code> is null
     *         and <code>newPersistenceXml</code> is not.
     */
    public void changePersistenceScope(PersistenceScope newPersistenceScope, File newPersistenceXml) {
        if (newPersistenceScope == null && newPersistenceXml != null) {
            throw new IllegalArgumentException("The persistenceScope parameter cannot be null when newPersistenceXml is non-null"); // NOI18N
        }

        boolean oldPersistenceExists, newPersistenceExists;
        PersistenceScope oldPersistenceScope;

        synchronized (this) {
            oldPersistenceExists = persistenceExists;
            oldPersistenceScope = persistenceScope;

            LOG.fine("changePersistenceScope: newPersistenceXml=" + newPersistenceXml); // NOI18N

            if (persistenceXml != null) {
                FileUtil.removeFileChangeListener(fileChangeListener, persistenceXml);
            }
            if (newPersistenceXml != null) {
                persistenceXml = newPersistenceXml;
                FileUtil.addFileChangeListener(fileChangeListener, persistenceXml);
            } else {
                persistenceXml = null;
            }

            persistenceScope = newPersistenceScope;
            persistenceXml = newPersistenceXml;

            change();

            newPersistenceExists = persistenceExists;

            LOG.fine("changePersistenceScope: oldPersistenceExists=" + oldPersistenceExists + ", newPersistenceExists=" + newPersistenceExists); // NOI18N
        }

        if (oldPersistenceExists != newPersistenceExists || (oldPersistenceScope != newPersistenceScope && newPersistenceExists)) {
            LOG.fine("changePersistenceScope: firing PROP_PERSISTENCE_SCOPES change"); // NOI18N
            propChangeSupport.firePropertyChange(PersistenceScopes.PROP_PERSISTENCE_SCOPES, null, null);
        }
    }

    /**
     * Returns the <code>PersistenceScopes</code> created by this helper. Usually
     * an implementor of <code>PersistenceScopesProvider</code> will delegate
     * its <code>getPersistenceScopes</code> method to this method.
     *
     * @return a <code>PersistenceScopes</code> instance; never null.
     */
    public PersistenceScopes getPersistenceScopes() {
        return persistenceScopes;
    }

    /**
     * Called when anything has changed (the persistence scope has changed, the path of the
     * persistence.xml file has changes, the persistence.xml file has been created or deleted).
     */
    private void change() {
        synchronized (this) {
            persistenceExists = false;
            if (persistenceXml != null) {
                persistenceExists = FileUtil.toFileObject(persistenceXml) != null;
            }
        }
    }

    /**
     * Called when something happened to persistence.xml (created, deleted).
     */
    private void fileEvent() {
        boolean oldPersistenceExists, newPersistenceExists;

        synchronized (this) {
            oldPersistenceExists = persistenceExists;
            change();
            newPersistenceExists = persistenceExists;
        }

        LOG.fine("fileEvent: oldPersistenceExists=" + oldPersistenceExists + ", newPersistenceExists=" + newPersistenceExists); // NOI18N

        if (oldPersistenceExists != newPersistenceExists) {
            LOG.fine("fileEvent: firing PROP_PERSISTENCE_SCOPES change"); // NOI18N
            propChangeSupport.firePropertyChange(PersistenceScopes.PROP_PERSISTENCE_SCOPES, oldPersistenceExists, newPersistenceExists);
        }
    }

    private PersistenceScope[] getPersistenceScopeList() {
        synchronized (this) {
            if (persistenceExists) {
                return new PersistenceScope[] { persistenceScope };
            } else {
                return new PersistenceScope[0];
            }
        }
    }

    private void addPropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.addPropertyChangeListener(listener);
    }

    private void removePropertyChangeListener(PropertyChangeListener listener) {
        propChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Listener on the persistence.xml file.
     */
    private class FileListener implements FileChangeListener {

        @Override
        public void fileFolderCreated(FileEvent fe) {

        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            LOG.fine("fileCreated: " + fe.getFile().getPath());
            fileEvent();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            LOG.fine("fileModified: " + fe.getFile().getPath());
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            LOG.fine("fileDeleted: " + fe.getFile().getPath());
            fileEvent();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {

        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {

        }
    }

    /**
     * Implementation of <code>PersistenceScopesImplementation</code>.
     * The <code>PersistenceScopes</code> instance maintained by the helper
     * delegates to this.
     */
    private class PersistenceScopesImpl implements PersistenceScopesImplementation {

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            PersistenceScopesHelper.this.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            PersistenceScopesHelper.this.removePropertyChangeListener(listener);
        }

        @Override
        public PersistenceScope[] getPersistenceScopes() {
            return PersistenceScopesHelper.this.getPersistenceScopeList();
        }
    }
}
