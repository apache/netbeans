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
package org.netbeans.modules.gradle.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.openide.util.ChangeSupport;

/**
 *
 * @author lkishalmi
 */
public abstract class AbstractProjectChangeAdaptor {
    final Project project;
    private final ChangeSupport cs = new ChangeSupport(this);
    private final PropertyChangeListener propChange;

    public AbstractProjectChangeAdaptor(Project project) {
        this.project = project;
        propChange = (PropertyChangeEvent evt) -> {
            if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                projectChanged();
            }
        };
    }

    protected void projectChanged() {
        cs.fireChange();
    }

    public void addChangeListener(ChangeListener cl) {
        if (!cs.hasListeners()) {
            NbGradleProject.addPropertyChangeListener(project, propChange);
        }
        cs.addChangeListener(cl);
    }

    public void removeChangeListener(ChangeListener cl) {
        cs.removeChangeListener(cl);
        if (!cs.hasListeners()) {
            NbGradleProject.removePropertyChangeListener(project, propChange);
        }
    }
}
