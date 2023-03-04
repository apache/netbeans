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

package org.netbeans.modules.web.inspect.webkit.knockout.unused;

import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Node representing an unused binding.
 *
 * @author Jan Stola
 */
public class UnusedBindingNode extends AbstractNode {
    /** Unused binding represented by this node. */
    private UnusedBinding unusedBinding;

    /**
     * Creates a new {@code UnusedBindingNode}.
     * 
     * @param unusedBinding unused binding to represent by the node.
     */
    public UnusedBindingNode(UnusedBinding unusedBinding) {
        super(Children.LEAF, new UnusedBindingLookup(unusedBinding));
        this.unusedBinding = unusedBinding;
        setIconBaseWithExtension("org/netbeans/modules/web/inspect/resources/domElement.png"); // NOI18N
    }

    @Override
    public synchronized String getHtmlDisplayName() {
        return unusedBinding.getNodeDisplayName();
    }

    /**
     * Returns the unused binding represented by this node.
     * 
     * @return unused binding represented by this node.
     */
    synchronized UnusedBinding getUnusedBinding() {
        return unusedBinding;
    }

    /**
     * Sets the unused binding represented by this node.
     * 
     * @param unusedBinding unused binding represented by this node.
     */
    synchronized void setUnusedBinding(UnusedBinding unusedBinding) {
        this.unusedBinding = unusedBinding;
        ((UnusedBindingLookup)getLookup()).update(unusedBinding);
        fireDisplayNameChange(null, null);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(GoToBindingSourceAction.class),
            SystemAction.get(ShowInDOMAction.class)
        };
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(GoToBindingSourceAction.class);
    }

    /**
     * Lookup that provides the latest unused binding represented by
     * the owning node.
     */
    private static class UnusedBindingLookup extends ProxyLookup {

        /**
         * Creates a new {@code UnusedBindingLookup}.
         * 
         * @param unusedBinding unused binding to put into the lookup.
         */
        UnusedBindingLookup(UnusedBinding unusedBinding) {
            super(Lookups.fixed(unusedBinding));
        }

        /**
         * Updates the content of the lookup.
         * 
         * @param unusedBinding unused binding to put into the lookup.
         */
        void update(UnusedBinding unusedBinding) {
            setLookups(Lookups.fixed(unusedBinding));
        }

    }

}
