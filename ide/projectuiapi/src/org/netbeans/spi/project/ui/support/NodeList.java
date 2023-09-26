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

package org.netbeans.spi.project.ui.support;

import java.util.List;
import javax.swing.event.ChangeListener;
import org.openide.nodes.Node;

/**
 * Represents a series of nodes which can be spliced into a children list.
 * @param <K> the type of key you would like to use to represent nodes
 * @author mkleint
 * @since org.netbeans.modules.projectuiapi/1 1.18
 * @see NodeFactory
 * @see NodeFactorySupport
 * @see org.openide.nodes.Children.Keys
 */
public interface NodeList<K> {
    /**
     * Obtains child keys which will be passed to {@link #node}.
     * If there is a change in the set of keys based on external events,
     * fire a <code>ChangeEvent</code>.
     * @return list of zero or more keys to display
     */
    List<K> keys();
    /**
     * Adds a listener to a change in keys.
     * @param l a listener to add
     */
    void addChangeListener(ChangeListener l);
    /**
     * Removes a change listener.
     * @param l a listener to remove
     */
    void removeChangeListener(ChangeListener l);
    /**
     * Creates a node for a given key.
     * @param key a key which was included in {@link #keys}
     * @return a node which should represent that key visually or null if no such node can be created currently.
     */
    Node node(K key);
    /**
     * Called when the node list is to be active. Equivalent to {@link org.openide.nodes.Children#addNotify}.
     * If there is any need to register listeners or begin caching of state, do it here.
     * @see org.openide.nodes.Children#addNotify
     */
    void addNotify();
    /**
     * Called when the node list is no longer needed. Equivalent to {@link org.openide.nodes.Children#removeNotify}.
     * @see org.openide.nodes.Children#removeNotify
     */
    void removeNotify();
}
