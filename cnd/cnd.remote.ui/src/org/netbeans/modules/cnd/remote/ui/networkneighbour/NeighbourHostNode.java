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
package org.netbeans.modules.cnd.remote.ui.networkneighbour;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 */
final class NeighbourHostNode extends AbstractNode {

    public static final String PROP_ACCEPTS_SSH = "ACCEPTS_SSH"; // NOI18N

    public NeighbourHostNode(final NeighbourHost host) {
        super(Children.LEAF, Lookups.fixed(host));
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Set set = Sheet.createPropertiesSet();
        set.put(new SSHIconProperty(getHost().acceptsSSH()));
        sheet.put(set);
        return sheet;
    }

    @Override
    public PropertySet[] getPropertySets() {
        return super.getPropertySets();
    }

    @Override
    public Image getIcon(int type) {
        if (getHost().acceptsSSH()) {
            return ImageUtilities.loadImage("org/netbeans/modules/cnd/remote/resources/host_with_ssh.png"); // NOI18N
        } else {
            return ImageUtilities.loadImage("org/netbeans/modules/cnd/remote/resources/host_no_ssh.png"); // NOI18N
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        return super.getOpenedIcon(type);
    }

    @Override
    public String getDisplayName() {
        return getHost().getName();
    }

    private NeighbourHost getHost() {
        return getLookup().lookup(NeighbourHost.class);
    }

    private final static class SSHIconProperty extends PropertySupport.ReadOnly<Image> {

        private final Image image;
        private final boolean acceptsSSH;

        public SSHIconProperty(boolean acceptsSSH) {
            super(PROP_ACCEPTS_SSH, Image.class, "", ""); // NOI18N
            this.acceptsSSH = acceptsSSH;
            image = acceptsSSH
                    ? ImageUtilities.loadImage("org/netbeans/modules/cnd/remote/resources/sshOpen.gif") // NOI18N
                    : ImageUtilities.loadImage("org/netbeans/modules/cnd/remote/resources/sshClosed.gif"); // NOI18N
        }

        @Override
        public Image getValue() throws IllegalAccessException, InvocationTargetException {
            return image;
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return new PropertyEditorSupport() {

                @Override
                public String getAsText() {
                    return acceptsSSH
                            ? NbBundle.getMessage(NeighbourHostNode.class, "NeighbourHostNode.HostAcceptsConnection")//NOI18N
                            : NbBundle.getMessage(NeighbourHostNode.class, "NeighbourHostNode.HostDoesNotAcceptConnection");//NOI18N
                }

                @Override
                public boolean isPaintable() {
                    return true;
                }

                @Override
                public void paintValue(Graphics g, Rectangle r) {
                    g.drawImage(image, 1, 1, null);
                }
            };
        }
    }
}
