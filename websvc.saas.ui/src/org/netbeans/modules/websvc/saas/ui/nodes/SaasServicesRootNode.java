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

package org.netbeans.modules.websvc.saas.ui.nodes;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.ui.actions.AddGroupAction;
import org.netbeans.modules.websvc.saas.ui.actions.AddServiceAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author nam
 */
@ServicesTabNodeRegistration(
    position=210,
    name="rootSaasGroup",
    displayName="org.netbeans.modules.websvc.saas.ui.nodes.Bundle#Web_Services",
    shortDescription="org.netbeans.modules.websvc.saas.ui.nodes.Bundle#Web_Services_Desc",
    iconResource="org/netbeans/modules/websvc/saas/ui/resources/webservicegroup.png"
)
public class SaasServicesRootNode extends AbstractNode {
    
    public SaasServicesRootNode() {
        this(new RootNodeChildren(SaasServicesModel.getInstance().getInitialRootGroup()), new InstanceContent());
    }

    SaasServicesRootNode(RootNodeChildren children, InstanceContent content) {
        super(children, new AbstractLookup(content));
        content.add(SaasServicesModel.getInstance().getInitialRootGroup());
    }
    
    @Override
    public String getName() {
        return "rootSaasGroup"; // NOI18N
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SaasServicesRootNode.class, "Web_Services"); // NOI18N
    }
    
    @Override
    public String getShortDescription() {
        return NbBundle.getMessage(SaasServicesRootNode.class, "Web_Services_Desc"); // NOI18N
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = SaasNode.getActions(getLookup());
        actions.add(SystemAction.get(AddServiceAction.class));
        actions.add(SystemAction.get(AddGroupAction.class));
        return actions.toArray(new Action[actions.size()]);
    }
    
    static final java.awt.Image ICON =
            ImageUtilities.loadImage( "org/netbeans/modules/websvc/saas/ui/resources/webservicegroup.png" ); //NOI18N
    
    @Override
    public Image getIcon(int type){
        return ICON;
    }
    
    @Override
    public Image getOpenedIcon(int type){
        return ICON;
    }
    
    static class RootNodeChildren extends SaasGroupNodeChildren {

        public RootNodeChildren(SaasGroup group) {
            super(group);
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() == SaasServicesModel.getInstance().getRootGroup() &&
                SaasServicesModel.getInstance().getState() == SaasServicesModel.State.READY) {
                updateKeys();
            }
            super.propertyChange(evt);
        }
    
        @Override
        protected void updateKeys() {
            if (needsWait()) {
                setKeys(SaasNodeChildren.WAIT_HOLDER);
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        SaasServicesModel.getInstance().initRootGroup();
                    }
                });
            } else {
                super.updateKeys();
            }
        }
        
        private boolean needsWait() {
            return SaasServicesModel.getInstance().getState() != SaasServicesModel.State.READY;
        }
        
        @Override
        protected Node[] createNodes(Object key) {
            if (needsWait()) {
                return SaasNodeChildren.getWaitNode();
            }
            return super.createNodes(key);
        }
    }
}
