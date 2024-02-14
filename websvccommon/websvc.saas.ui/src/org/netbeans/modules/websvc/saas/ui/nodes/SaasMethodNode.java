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

package org.netbeans.modules.websvc.saas.ui.nodes;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.websvc.saas.model.CustomSaas;
import org.netbeans.modules.websvc.saas.model.SaasMethod;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author nam
 */
public class SaasMethodNode extends AbstractNode {
    private final SaasMethod method;
    
    public SaasMethodNode(CustomSaas saas, SaasMethod method) {
        super(Children.LEAF);
        this.method = method;
    }
    
    @Override
    public String getDisplayName() {
        return method.getName();
    }
    
    @Override
    public String getShortDescription() {
        return method.getDocumentation();
    }

    private static final Image ICON = ImageUtilities.loadImage("org/netbeans/modules/websvc/saas/ui/resources/method.png"); // NOI18N
    
    @Override
    public Image getIcon(int type){
        return ICON;
    }
    
    @Override
    public Image getOpenedIcon(int type){
        return ICON;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = SaasNode.getActions(getLookup());
        return actions.toArray(new Action[0]);
    }
    
    @Override
    protected void createPasteTypes(final Transferable t, List<PasteType> s) {
        //TODO review original
    }
    
}
