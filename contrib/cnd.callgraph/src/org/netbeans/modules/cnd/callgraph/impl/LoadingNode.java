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

package org.netbeans.modules.cnd.callgraph.impl;

import java.awt.Image;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.cnd.callgraph.api.Call;
import org.netbeans.modules.cnd.callgraph.api.Function;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 */
public class LoadingNode extends AbstractNode implements Call {
    public LoadingNode() {
        super(Children.LEAF);
        setName("dummy"); // NOI18N
        setDisplayName(NbBundle.getMessage(getClass(), "Loading")); // NOI18N
    }
    
    @Override
    public Image getIcon(int param) {
        return ImageUtilities.loadImage("org/netbeans/modules/cnd/callgraph/resources/waitNode.gif"); // NOI18N
    }

    @Override
    public void open() {
    }

    @Override
    public String getHtmlDisplayName() {
        return getDescription();
    }

    @Override
    public Function getCallee() {
        return null;
    }

    @Override
    public Function getCaller() {
        return null;
    }

    @Override
    public Collection<Occurrence> getOccurrences() {
        return Collections.EMPTY_LIST;
    }
    
    @Override
    public int compareTo(Call o) {
        return -1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LoadingNode) {
            return this == obj;
        }
        return false;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "Loading");
    }

    @Override
    public String getDescription() {
        return  getDisplayName();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }
}
