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

package org.openide.loaders;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/** Provisional mechanism for displaying the Repository object.
 * It will show all filesystems, possibly with a filter.
 * @deprecated Probably unwise to call this for any reason; obsolete UI.
 * @author Jesse Glick
 * @since 3.14
 */
@Deprecated
public abstract class RepositoryNodeFactory {

    /** Get the default factory.
     * @return the default instance from lookup
     */
    public static RepositoryNodeFactory getDefault() {
        RepositoryNodeFactory rnf = Lookup.getDefault().lookup(RepositoryNodeFactory.class);
        if (rnf == null) {
            rnf = new Trivial();
        }
        return rnf;
    }

    /** Subclass constructor. */
    protected RepositoryNodeFactory() {}
    
    /** Create a node representing a subset of the repository of filesystems.
     * You may filter out certain data objects.
     * If you do not wish to filter out anything, just use {@link DataFilter#ALL}.
     * Nodes might be reused between calls, so if you plan to add this node to a
     * parent, clone it first.
     * @param f a filter
     * @return a node showing part of the repository
     */
    public abstract Node repository(DataFilter f);

    private static final class Trivial extends RepositoryNodeFactory {

        public Node repository(DataFilter f) {
            return new AbstractNode(Children.LEAF);
        }

    }

}
