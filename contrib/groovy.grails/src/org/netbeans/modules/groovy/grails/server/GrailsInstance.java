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

package org.netbeans.modules.groovy.grails.server;

import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grails.api.GrailsConstants;
import org.netbeans.modules.groovy.grails.api.GrailsPlatform;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.openide.nodes.*;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public final class GrailsInstance implements ServerInstanceImplementation {

    private final GrailsChildFactory childFactory;
    private final GrailsPlatform runtime;
    private final Node node;
    

    private GrailsInstance(GrailsInstanceProvider provider, GrailsPlatform runtime) {
        this.childFactory = new GrailsChildFactory(provider);
        this.runtime = runtime;
        this.node = new GrailsNode(Children.create(childFactory, false), getDisplayName());
    }

    public static final GrailsInstance forProvider(GrailsInstanceProvider provider) {
        // when we will support multiple runtimes it has to be list of instances
        return new GrailsInstance(provider, GrailsPlatform.getDefault());
    }

    @Override
    public Node getBasicNode() {
        synchronized (this) {
            return new FilterNode(node, Children.LEAF);
        }
    }

    @Override
    public Node getFullNode() {
        synchronized (this) {
            return node;
        }
    }

    @Override
    public JComponent getCustomizer() {
        return null;
    }

    @Override
    public final String getDisplayName() {
        String version;
        if (!runtime.isConfigured()) {
            version = NbBundle.getMessage(GrailsInstance.class, "GrailsInstance.unknownVersion");
        }

        version = runtime.getVersion().toString();
        return NbBundle.getMessage(GrailsInstance.class, "GrailsInstance.displayName", version);
    }

    @Override
    public final String getServerDisplayName() {
        return NbBundle.getMessage(GrailsInstance.class, "GrailsInstance.serverDisplayName");
    }

    public final void refreshChildren() {
        childFactory.refresh();
    }

    public final void refreshNode() {
        synchronized (this) {
            node.setDisplayName(getDisplayName());
        }
    }

    @Override
    public final boolean isRemovable() {
        return false;
    }

    @Override
    public final void remove() {
        // noop
    }

    private static class GrailsNode extends AbstractNode {

        public GrailsNode(Children children, String displayName) {
            super(children);
            setDisplayName(displayName);
            setIconBaseWithExtension(GrailsConstants.GRAILS_ICON_16x16);
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[] {};
        }
    }

    private static class GrailsChildFactory extends ChildFactory<Map.Entry<Process, Project>> {

        private final GrailsInstanceProvider provider;

        public GrailsChildFactory(GrailsInstanceProvider provider) {
            this.provider = provider;
        }

        public void refresh() {
            super.refresh(false);
        }

        @Override
        protected Node createNodeForKey(Map.Entry<Process, Project> key) {
            return new ApplicationNode(key.getValue(), key.getKey());
        }

        @Override
        protected boolean createKeys(List<Map.Entry<Process, Project>> toPopulate) {
            for (Map.Entry<Process, Project> entry : provider.getRunningProjects().entrySet()) {
                toPopulate.add(entry);
            }
            return true;
        }
    }
}
