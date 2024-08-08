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
package org.netbeans.modules.project.dependency.spi;

import java.util.Set;
import org.netbeans.modules.project.dependency.reload.ProjectStateListener;
import org.netbeans.modules.project.dependency.reload.ReloadSpiAccessor;
import org.netbeans.modules.project.dependency.reload.Reloader.LoadContextImpl;

/**
 *
 * @author sdedic
 */
class ReloadSpiAccessorImpl {
    static {
        ReloadSpiAccessor.set(new ReloadSpiAccessor() {
            @Override
            public Set<Class> getInconsistencies(ProjectReloadImplementation.ProjectStateData data) {
                return data.getInconsistencies();
            }
            
            @Override
            public void addProjectStateListener(ProjectReloadImplementation.ProjectStateData data, ProjectStateListener l) {
                if (data != null) {
                    data.addListener(l);
                }
            }

            @Override
            public ProjectReloadImplementation.LoadContext createLoadContext(LoadContextImpl impl) {
                return new ProjectReloadImplementation.LoadContext<>(impl, impl.getProject(), impl.getRequest(), impl.getOriginalData());
            }

            @Override
            public void removeProjectStateListener(ProjectReloadImplementation.ProjectStateData data, ProjectStateListener l) {
                if (data != null) {
                    data.removeListener(l);
                }
            }

            @Override
            public void clear(ProjectReloadImplementation.LoadContext ctx) {
                if (ctx != null) {
                    ctx.impl = null;
                }
            }

            @Override
            public void release(ProjectReloadImplementation.ProjectStateData data) {
                data.clear();
            }
        });
    }
}
