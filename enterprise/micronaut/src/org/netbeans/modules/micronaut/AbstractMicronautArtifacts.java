/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.micronaut;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.ProjectArtifactsQuery;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author sdedic
 */
public abstract class AbstractMicronautArtifacts implements PropertyChangeListener {
    private static final List<ArtifactSpec> UNKNOWN = new ArrayList<>();
    private static final RequestProcessor ARTIFACTS_RP = new RequestProcessor(AbstractMicronautArtifacts.class);
    
    private final Project project;
    protected final ProjectArtifactsQuery.Filter query;

    // @GuardedBy(this)
    private final List<ChangeListener> listeners = new ArrayList<>();
    // @GuardedBy(this)
    private List<ArtifactSpec> artifacts;

    private PropertyChangeListener projectL;
    
    private RequestProcessor.Task refreshTask;
    
    private int eventNo;

    public AbstractMicronautArtifacts(Project project, ProjectArtifactsQuery.Filter query) {
        this.project = project;
        this.query = query;
    }

    public final Project getProject() {
        return project;
    }

    protected abstract List<ArtifactSpec> compute();

    protected abstract void attach(PropertyChangeListener l);

    protected abstract void detach(PropertyChangeListener l);

    protected abstract boolean accept(PropertyChangeEvent e);

    public void addChangeListener(ChangeListener l) {
        synchronized (this) {
            if (projectL == null) {
                projectL = WeakListeners.propertyChange(this, project);
                attach(projectL);
            }
            listeners.add(l);
        }
    }

    public void removeChangeListener(ChangeListener l) {
        synchronized (this) {
            listeners.remove(l);
        }
    }

    public List<ArtifactSpec> getArtifacts() {
        synchronized (this) {
            if (artifacts != null) {
                return artifacts;
            }
        }
        return refresh(null);
    }
    
    private List<ArtifactSpec> refresh(List<ArtifactSpec> old) {
        int mySerial;
        synchronized (this) {
            mySerial = eventNo;
        }
        List<ArtifactSpec> as = Collections.unmodifiableList(compute());
        synchronized (this) {
            if (mySerial != eventNo) {
                return as;
            }
            if (artifacts != null) {
                return artifacts;
            }
            if (as.equals(old)) {
                this.artifacts = old;
                return old;
            }
            this.artifacts = as;
            if (listeners == null || listeners.isEmpty()) {
                return as;
            }
        }
        fireChanged();
        return as;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!accept(evt)) {
            return;
        }
        List<ArtifactSpec> old;

        boolean fire = false;
        synchronized (this) {
            ++eventNo;
            old = artifacts;
            artifacts = null;
            if (old != null) {
                if (refreshTask != null) {
                    refreshTask.cancel();
                }
                refreshTask = ARTIFACTS_RP.post(() -> refresh(old), 50);
                return;
            }
            if (listeners != null && !listeners.isEmpty()) {
                fire = true;
            }
        }
        if (fire) {
            fireChanged();
        }
    }
    
    protected void fireChanged() {
        ChangeListener[] ll;

        synchronized (this) {
            if (listeners == null || listeners.isEmpty()) {
                return;
            }
            ll = listeners.toArray(new ChangeListener[0]);
        }
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : ll) {
            l.stateChanged(e);
        }
    }

    public Collection<ArtifactSpec> getExcludedArtifacts() {
        return null;
    }
}
