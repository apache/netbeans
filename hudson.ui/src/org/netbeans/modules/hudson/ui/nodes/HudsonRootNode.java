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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.hudson.ui.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.netbeans.modules.hudson.api.HudsonChangeListener;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.netbeans.modules.hudson.ui.actions.AddInstanceAction;
import static org.netbeans.modules.hudson.ui.nodes.Bundle.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;

@ServicesTabNodeRegistration(name=HudsonRootNode.HUDSON_NODE_NAME, displayName="#LBL_HudsonNode", shortDescription="#TIP_HudsonNode", iconResource=HudsonRootNode.ICON_BASE, position=488)
@Messages({
    "LBL_HudsonNode=Hudson Builders",
    "TIP_HudsonNode=Hudson continuous integration servers, including Jenkins."
})
public class HudsonRootNode extends AbstractNode {

    public static final String HUDSON_NODE_NAME = "hudson"; // NOI18N
    static final String ICON_BASE = "org/netbeans/modules/hudson/ui/resources/hudson.png"; // NOI18N
    

    private HudsonRootNode() {
        super(Children.create(new RootNodeChildren(), true));
        setName(HUDSON_NODE_NAME);
        setDisplayName(LBL_HudsonNode());
        setShortDescription(TIP_HudsonNode());
        setIconBaseWithExtension(ICON_BASE);
    }
    
    public @Override Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(new AddInstanceAction());
        /* http://issues.hudson-ci.org/browse/HUDSON-8644
        if (HudsonManagerImpl.getDefault().getInstances().isEmpty()) {
            actions.add(new AddTestInstanceAction());
        }
         */
        return actions.toArray(new Action[actions.size()]);
    }
    
    private static class RootNodeChildren extends ChildFactory<HudsonInstance> implements HudsonChangeListener {
        
        public RootNodeChildren() {
            HudsonManager.addHudsonChangeListener(this);
        }

        protected @Override Node createNodeForKey(HudsonInstance key) {
            return new HudsonInstanceNode(key);
        }
        
        @Override
        protected boolean createKeys(List<HudsonInstance> toPopulate) {
            toPopulate.addAll(HudsonManager.getAllInstances());
            Collections.sort(toPopulate);
            return true;
        }

        @Override
        public void stateChanged() {}
        
        @Override
        public void contentChanged() {
            refresh(false);
        }

    }

}
