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
package org.netbeans.modules.websvc.core;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.netbeans.api.project.Project;

/**
 * This is base implementation of ProjectWebServiceViewImpl.
 * Implements the change support.
 * @author Ajit Bhate
 */
public abstract class AbstractProjectWebServiceViewImpl implements ProjectWebServiceViewImpl {

    private ChangeSupport serviceListeners,  clientListeners;
    private Reference<Project> project;
    protected AbstractProjectWebServiceViewImpl(Project project) {
        this.project = new WeakReference<Project>(project);
        serviceListeners = new ChangeSupport(this);
        clientListeners = new ChangeSupport(this);
    }

    public void addChangeListener(ChangeListener l, ProjectWebServiceView.ViewType viewType) {
        switch (viewType) {
            case SERVICE:
                serviceListeners.addChangeListener(l);
                break;
            case CLIENT:
                clientListeners.addChangeListener(l);
                break;
        }
    }

    public void removeChangeListener(ChangeListener l, ProjectWebServiceView.ViewType viewType) {
        switch (viewType) {
            case SERVICE:
                if (serviceListeners != null) {
                    serviceListeners.removeChangeListener(l);
                }
                break;
            case CLIENT:
                if (clientListeners != null) {
                    clientListeners.removeChangeListener(l);
                }
                break;
        }
    }

    protected void fireChange(ProjectWebServiceView.ViewType viewType) {
        switch (viewType) {
            case SERVICE:
                serviceListeners.fireChange();
                return;
            case CLIENT:
                clientListeners.fireChange();
                return;
        }
    }
    
    @Override
    public boolean equals(Object object) {
        if ( object == null ){
            return false;
        }
        if(getClass() == object.getClass()) {
            AbstractProjectWebServiceViewImpl other = (AbstractProjectWebServiceViewImpl) object;
            return other.getProject() == this.getProject();
        }
        return false;
    }

    @Override
    public int hashCode() {
        super.hashCode();
        int hash = 3;
        hash = 23 * hash + (this.getProject() != null ? this.getProject().hashCode() : 0);
        return hash;
    }

    protected Project getProject() {
        return project.get();
    }

}
