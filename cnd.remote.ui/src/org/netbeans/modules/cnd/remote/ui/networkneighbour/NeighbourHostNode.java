/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
