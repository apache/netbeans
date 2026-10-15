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
import java.io.IOException;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import org.netbeans.modules.websvc.saas.util.SaasTransferable;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author nam
 */
public class WadlMethodNode extends AbstractNode {
    private WadlSaasMethod method;
    private Transferable transferable;
    
    public WadlMethodNode(WadlSaasMethod method) {
        this(method, new InstanceContent());
    }

    public WadlMethodNode(WadlSaasMethod method, InstanceContent content) {
        super(Children.LEAF, new AbstractLookup(content));
        this.method = method;
        content.add(method);
        transferable = ExTransferable.create(
            new SaasTransferable<WadlSaasMethod>(method, SaasTransferable.WADL_METHOD_FLAVORS));
    }

    @Override
    public String getDisplayName() {
        return method.getDisplayName();
    }
    
    @Override
    public String getShortDescription() {
        if (method.getMethod() != null) {
            return method.getMethod().getDocumentation();
        }
        
        return SaasUtil.getSignature(method);
    }
    
    private static final java.awt.Image ICON =
            ImageUtilities.loadImage( "org/netbeans/modules/websvc/saas/ui/resources/method.png" ); //NOI18N
    
    @Override
    public java.awt.Image getIcon(int type) {
        return ICON;
    }
    
    @Override
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }
    
    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = SaasNode.getActions(getLookup());
        //TODO maybe ???
        //actions.add(SystemAction.get(TestMethodAction.class));
        return actions.toArray(new Action[0]);
    }

    @Override
    public Transferable clipboardCopy() throws IOException {
        if (method.getSaas().getState() != Saas.State.READY) {
            method.getSaas().toStateReady(false);
            return super.clipboardCopy();
        }
        return SaasTransferable.addFlavors(transferable);
    }
    
}
