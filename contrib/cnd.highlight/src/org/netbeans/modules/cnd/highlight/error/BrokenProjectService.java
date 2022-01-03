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

package org.netbeans.modules.cnd.highlight.error;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.project.BrokenIncludes;
import org.netbeans.modules.cnd.api.project.NativeProject;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.project.BrokenIncludes.class)
public class BrokenProjectService implements BrokenIncludes {
    private static final WeakHashMap<ChangeListener,Boolean> listeners = new WeakHashMap<>();
    private final static Object lock = new Object();
    
    public BrokenProjectService() {
    }

    @Override
    public boolean isBroken(NativeProject project) {
        return BadgeProvider.getInstance().isBroken(project);
    }

    @Override
    public void addChangeListener(ChangeListener provider){
        synchronized(lock) {
            listeners.put(provider,Boolean.TRUE);
        }
    }

    @Override
    public void removeChangeListener(ChangeListener provider){
        synchronized(lock) {
            listeners.remove(provider);
        }
    }

    /*package*/static void fireChanges(ChangeEvent e) {
        List<ChangeListener> list = null;
        synchronized (lock) {
            list = new ArrayList<>(listeners.keySet());
        }
        for (ChangeListener provider : list) {
            provider.stateChanged(e);
        }
    }
}
