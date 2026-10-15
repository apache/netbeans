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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.websvc.saas.model.WadlSaas;
import org.netbeans.modules.websvc.saas.ui.actions.ViewWadlAction;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author nam
 */
public class WadlSaasNode extends SaasNode {
    
    public WadlSaasNode(WadlSaas wadlSaas) {
        this(wadlSaas, new InstanceContent());
    }

    public WadlSaasNode(WadlSaas wadlSaas, InstanceContent content) {
        super(new WadlSaasNodeChildren(wadlSaas), new AbstractLookup(content), wadlSaas);
        content.add(wadlSaas);
    }

    @Override
    public WadlSaas getSaas() {
        return (WadlSaas) super.getSaas();
    }
    
    private static final java.awt.Image SERVICE_BADGE =
            ImageUtilities.loadImage( "org/netbeans/modules/websvc/saas/ui/resources/restservice.png" ); //NOI18N
    
    @Override
    public Image getGenericIcon(int type) {
        return SERVICE_BADGE;
    }
    
    @Override
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }

    @Override
    public boolean canRename() {
        return super.canRename();
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>(Arrays.asList(super.getActions(context)));
        actions.add(SystemAction.get(ViewWadlAction.class));

        return actions.toArray(new Action[0]);
    }

}
