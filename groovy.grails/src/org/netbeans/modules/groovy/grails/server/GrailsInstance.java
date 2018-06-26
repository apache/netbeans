/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
