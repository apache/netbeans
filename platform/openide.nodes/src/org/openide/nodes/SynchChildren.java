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
package org.openide.nodes;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Synchronous children implementation that takes a ChildFactory.
 *
 * @author Tim Boudreau
 */
final class SynchChildren<T> extends Children.Keys<T> implements ChildFactory.Observer {
    private final ChildFactory<T> factory;
    
    /** Creates a new instance of SynchChildren
     * @param factory An instance of ChildFactory which will provide keys,
     *                values, nodes
     */
    SynchChildren(ChildFactory<T> factory) {
        this.factory = factory;
    }
    
    volatile boolean active = false;
    protected @Override void addNotify() {
        active = true;
        factory.addNotify();
        refresh(true);
    }
    
    protected @Override void removeNotify() {
        active = false;
        setKeys(Collections.<T>emptyList());
        factory.removeNotify();
    }
    
    protected Node[] createNodes(T key) {
        return factory.createNodesForKey(key);
    }

    @Override
    protected void destroyNodes(Node[] arr) {
        super.destroyNodes(arr);
        factory.destroyNodes(arr);
    }
    
    
    public void refresh(boolean immediate) {
        if (active) {
            List <T> toPopulate = new LinkedList<T>();
            while (!factory.createKeys(toPopulate)) {}
            setKeys(toPopulate);
        }
    }
}
