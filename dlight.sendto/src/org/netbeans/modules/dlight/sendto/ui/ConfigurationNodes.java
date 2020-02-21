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

package org.netbeans.modules.dlight.sendto.ui;

import org.netbeans.modules.dlight.sendto.api.Configuration;
import org.netbeans.modules.dlight.sendto.api.ConfigurationsModel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public final class ConfigurationNodes extends Children.Keys<Configuration> {

    private final ChangeListener cl;

    public ConfigurationNodes(final ConfigurationsModel model) {
        cl = new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                setKeys(model.getConfigurations());
            }
        };

        model.addChangeListener(cl);
        cl.stateChanged(null);
    }

    @Override
    protected Node[] createNodes(Configuration key) {
        return new ConfigurationNode[]{new ConfigurationNode(key)};
    }

    public static class ConfigurationNode extends AbstractNode {

        private final BufferedImage icon;

        public ConfigurationNode(Configuration cfg) {
            super(Children.LEAF, Lookups.fixed(cfg));
            icon = new BufferedImage(15, 15, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = (Graphics2D) icon.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.black);
            g.fillOval(7, 4, 5, 5);
        }

        public Configuration getConfiguration() {
            return getLookup().lookup(Configuration.class);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return icon;
        }

        // TODO: How to make this correctly?
        public void updateName() {
            fireDisplayNameChange(null, getConfiguration().getName());
            fireNameChange(null, getConfiguration().getName());
        }

        @Override
        public Image getIcon(int type) {
            return icon;
        }

        @Override
        public String getDisplayName() {
            return getConfiguration().getName();
        }
    }
}
