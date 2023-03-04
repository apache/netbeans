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
package org.netbeans.modules.tasklist.todo;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

/**
 *
 * @author pzajac
 */
public class TopComponentRegistryMock implements TopComponent.Registry {

    private Set<TopComponent> opened = Collections.emptySet() ;
    private TopComponent activated;
    
    public TopComponentRegistryMock() {
    }

    
    public Set<TopComponent> getOpened() {
        return opened;
    }
    
    public void setOpened(Set<TopComponent> opened)  {
        this.opened = new HashSet<TopComponent>(opened);
    }

    public TopComponent getActivated() {
        return activated;
    }

    public void setActivated(TopComponent activated) {
        this.activated = activated;
    }

    public Node[] getCurrentNodes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Node[] getActivatedNodes() {
        return new Node[0];
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {}

    public void removePropertyChangeListener(PropertyChangeListener l) {}

}
