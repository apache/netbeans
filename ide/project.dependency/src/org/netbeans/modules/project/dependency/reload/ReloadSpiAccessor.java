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

import java.util.Set;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation.LoadContext;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation.ProjectStateData;
import org.netbeans.modules.project.dependency.reload.Reloader.LoadContextImpl;

/**
 *
 * @author sdedic
 */
public abstract class ReloadSpiAccessor {
    private static volatile ReloadSpiAccessor INSTANCE;
    
    static {
        try {
            Class.forName("org.netbeans.modules.project.dependency.spi.ReloadSpiAccessorImpl", true, ReloadSpiAccessor.class.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        assert INSTANCE != null;
    }

    public static void set(ReloadSpiAccessor a) {
        assert INSTANCE == null;
        INSTANCE = a;
    }
    
    public static ReloadSpiAccessor get() {
        return INSTANCE;
    }
    
    public abstract void addProjectStateListener(ProjectStateData data, ProjectStateListener l);
    public abstract void removeProjectStateListener(ProjectStateData data, ProjectStateListener l);
    public abstract void release(ProjectStateData data);
    public abstract LoadContext createLoadContext(LoadContextImpl impl);
    public abstract void clear(LoadContext ctx);
    public abstract Set<Class> getInconsistencies(ProjectStateData data);
}
