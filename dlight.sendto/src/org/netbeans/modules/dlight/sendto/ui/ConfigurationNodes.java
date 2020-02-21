/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
