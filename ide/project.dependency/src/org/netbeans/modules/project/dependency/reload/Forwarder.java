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
package org.netbeans.modules.project.dependency.reload;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.project.dependency.ProjectReload;
import org.netbeans.modules.project.dependency.ProjectReload.ProjectState;
import org.netbeans.modules.project.dependency.reload.ProjectReloadInternal.StateParts;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation.ProjectStateData;

/**
 * Forwards events between ProjectStateData from-to project states. If a state is merged with another
 * or thrown away (because it is duplicate)
 * @author sdedic
 */
class Forwarder implements ProjectStateListener {
    
    private final Map<ProjectReloadImplementation.ProjectStateData, ProjectReloadImplementation.ProjectStateData> forwardMap;
    private final ProjectReload.ProjectState invalidateTo;

    Forwarder(Map<ProjectReloadImplementation.ProjectStateData, ProjectReloadImplementation.ProjectStateData> forwardMap, ProjectReload.ProjectState invalidateTo) {
        this.forwardMap = forwardMap;
        for (ProjectReloadImplementation.ProjectStateData d : forwardMap.keySet()) {
            ReloadSpiAccessor.get().addProjectStateListener(d, this);
        }
        this.invalidateTo = invalidateTo;
    }

    @Override
    public void fireDataInconsistent(ProjectReloadImplementation.ProjectStateData d, Class<?> dataClass) {
        ProjectReloadImplementation.ProjectStateData f = forwardMap.get(d);
        if (f != null) {
            f.fireDataInconsistent(dataClass);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() instanceof ProjectReloadImplementation.ProjectStateData) {
            ProjectReloadImplementation.ProjectStateData d = (ProjectReloadImplementation.ProjectStateData) e.getSource();
            ProjectReloadImplementation.ProjectStateData f = forwardMap.get(d);
            if (f != null) {
                f.fireChanged(!d.isValid(), !d.isConsistent());
                f.fireFileSetChanged(d.getChangedFiles());
            }
        } else if (e.getSource() instanceof ProjectReload.ProjectState) {
            ProjectReload.ProjectState s = (ProjectReload.ProjectState) e.getSource();
            ReloadApiAccessor.get().updateProjectState(invalidateTo, !s.isValid(), !s.isConsistent(), null, null, null);
        }
    }
    
    static Forwarder create(ProjectState cached, StateParts parts, ProjectState now, boolean check) {
        StateParts oldParts = ReloadApiAccessor.get().getParts(cached);
        Map<ProjectStateData, ProjectStateData> forwardMap = new IdentityHashMap<>();
        for (Entry<ProjectReloadImplementation<?>, ProjectStateData<?>> en : parts.entrySet()) {
            ProjectStateData nd = en.getValue();
            ProjectStateData od = oldParts.get(en.getKey());
            if (check) {
                if (!Objects.equals(od, nd)) {
                    return null;
                }
            }
            if (nd != od) {
                forwardMap.put(nd, od);
            }
        }
        Forwarder f = new Forwarder(forwardMap, cached);
        ReloadApiAccessor.get().updateProjectState(cached, false, false, null, null, now);
        if (now != null) {
            now.addChangeListener(f);
        }
        return f;
    }
}
