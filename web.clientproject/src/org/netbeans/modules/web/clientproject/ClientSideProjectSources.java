/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.clientproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.clientproject.env.CommonProjectHelper;
import org.netbeans.modules.web.clientproject.env.Values;
import org.openide.util.ChangeSupport;
import org.openide.util.Mutex;

/**
 *
 */
public class ClientSideProjectSources implements Sources, ChangeListener, PropertyChangeListener {

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final ClientSideProject project;
    private final CommonProjectHelper helper;
    private final Values evaluator;

    // @GuardedBy("this")
    private boolean dirty;
    // @GuardedBy("this")
    private Sources delegate;


    public ClientSideProjectSources(ClientSideProject project, CommonProjectHelper helper, Values evaluator) {
        this.project = project;
        this.helper = helper;
        this.evaluator = evaluator;
        this.evaluator.addPropertyChangeListener(this);
    }

    @Override
    public SourceGroup[] getSourceGroups(final String type) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<SourceGroup[]>() {
            @Override
            public SourceGroup[] run() {
                Sources delegateCopy;
                synchronized (ClientSideProjectSources.this) {
                    assert Thread.holdsLock(ClientSideProjectSources.this);
                    if (delegate == null) {
                        delegate = project.is.initSources(project, helper, evaluator);
                        delegate.addChangeListener(ClientSideProjectSources.this);
                    }
                    if (dirty) {
                        delegate.removeChangeListener(ClientSideProjectSources.this);
                        delegate = project.is.initSources(project, helper, evaluator);
                        delegate.addChangeListener(ClientSideProjectSources.this);
                        dirty = false;
                    }
                    delegateCopy = delegate;
                }
                return delegateCopy.getSourceGroups(type);
            }
        });
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private void fireChange() {
        synchronized (this) {
            dirty = true;
        }
        changeSupport.fireChange();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChange();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (ClientSideProjectConstants.PROJECT_SOURCE_FOLDER.equals(propertyName)
                || ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER.equals(propertyName)
                || ClientSideProjectConstants.PROJECT_TEST_FOLDER.equals(propertyName)
                || ClientSideProjectConstants.PROJECT_TEST_SELENIUM_FOLDER.equals(propertyName)) {
            fireChange();
        }
    }

}
