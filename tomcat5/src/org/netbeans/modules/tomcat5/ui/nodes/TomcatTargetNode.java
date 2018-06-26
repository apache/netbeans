/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.tomcat5.ui.nodes;
import java.awt.Image;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport.ServerIcon;
import org.openide.util.Lookup;
import org.netbeans.modules.tomcat5.ui.nodes.actions.RefreshWebModulesAction;
import org.netbeans.modules.tomcat5.ui.nodes.actions.RefreshWebModulesCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author  Petr Pisl
 */

public class TomcatTargetNode extends AbstractNode {

    /** Creates a new instance of TomcatTargetNode */
    public TomcatTargetNode(final Lookup lookup) {
        super(Children.create(new ChildFactory<Class<WebModuleHolderNode>>() {

            @Override
            protected boolean createKeys(List<Class<WebModuleHolderNode>> toPopulate) {
                toPopulate.add(WebModuleHolderNode.class);
                return true;
            }

            @Override
            protected Node createNodeForKey(Class<WebModuleHolderNode> key) {
                return new WebModuleHolderNode(new TomcatWebModuleChildrenFactory(lookup));
            }   
        }, false));
    }

    @Override
    public Action[] getActions(boolean b) {
        return new Action[] {};
    }

    private static class WebModuleHolderNode extends AbstractNode {

        WebModuleHolderNode(TomcatWebModuleChildrenFactory factory) {
            super(Children.create(factory, true));
            setDisplayName(NbBundle.getMessage(TomcatTargetNode.class, "LBL_WebApps"));  // NOI18N
            getCookieSet().add(new RefreshWebModuleChildren(factory));
        }

        public Image getIcon(int type) {
            return UISupport.getIcon(ServerIcon.WAR_FOLDER);
        }

        public Image getOpenedIcon(int type) {
            return UISupport.getIcon(ServerIcon.WAR_OPENED_FOLDER);
        }

        public javax.swing.Action[] getActions(boolean context) {
            return new SystemAction[] {
                   SystemAction.get(RefreshWebModulesAction.class)
               };
        }
    }


    private static class RefreshWebModuleChildren implements RefreshWebModulesCookie {

        private final TomcatWebModuleChildrenFactory factory;

        RefreshWebModuleChildren(TomcatWebModuleChildrenFactory factory){
            this.factory = factory;
        }

        public void refresh() {
            factory.updateKeys();
        }
    }
}
