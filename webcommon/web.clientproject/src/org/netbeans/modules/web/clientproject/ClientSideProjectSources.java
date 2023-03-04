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
