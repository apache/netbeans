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

package org.netbeans.modules.cnd.repository.api;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.openide.util.lookup.Lookups;

/**
 *
 */
/*package*/class RepositoryListenersManager {
    private static final RepositoryListenersManager instance = new RepositoryListenersManager();
    private CopyOnWriteArrayList<RepositoryListener>  listeners = new CopyOnWriteArrayList<RepositoryListener>();

    /** Creates a new instance of RepositoryListenersManager */
    private RepositoryListenersManager() {
        final Collection<? extends RepositoryListener> lst =
                Lookups.forPath(RepositoryListener.PATH).lookupAll(RepositoryListener.class);
        this.listeners = new CopyOnWriteArrayList<RepositoryListener>(lst);
    }
    
    public static RepositoryListenersManager getInstance() {
        return instance;
    }
    
    public void registerListener (final RepositoryListener listener){
        listeners.add(listener);
    }
    
    public void unregisterListener(final RepositoryListener listener){
        listeners.remove(listener);
    }
    
    public boolean fireUnitOpenedEvent(final int unitId){
        boolean toOpen = true;
        for (RepositoryListener theListener : listeners) {
            toOpen &= theListener.unitOpened(unitId);
        }        
        return toOpen;
    }
    

    public void fireUnitClosedEvent(final int unitId) {
        for (RepositoryListener theListener : listeners) {
            theListener.unitClosed(unitId);
        }
    }


  
}
