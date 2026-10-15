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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.spi.SaasNodeActionsProvider;
import org.netbeans.modules.websvc.saas.ui.actions.DeleteServiceAction;
import org.netbeans.modules.websvc.saas.ui.actions.RefreshServiceAction;
import org.netbeans.modules.websvc.saas.ui.actions.ViewApiDocAction;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.openide.nodes.AbstractNode;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;

/**
 *
 * @author nam
 */
public abstract class SaasNode extends AbstractNode {
    protected Saas saas;

    public SaasNode(SaasNodeChildren nodeChildren, AbstractLookup lookup, Saas saas) {
        super(nodeChildren, lookup);
        this.saas = saas;
    }

    public Saas getSaas() {
        return saas;
    }

    @Override
    public String getDisplayName() {
        return saas.getDisplayName();
    }

    @Override
    public String getShortDescription() {
        return saas.getDescription();
    }

    @Override
    public Image getIcon(int type) {
        return getGenericIcon(type);
    }

    protected abstract Image getGenericIcon(int type);

    public static List<Action> getActions(Lookup lookup) {
        List<Action> actions = new ArrayList<Action>();
        for (SaasNodeActionsProvider ext : SaasUtil.getSaasNodeActionsProviders()) {
            actions.addAll(Arrays.asList(ext.getSaasActions(lookup)));
        }
        return actions;
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = getActions(getLookup());
        actions.add(SystemAction.get(ViewApiDocAction.class));
        actions.add(SystemAction.get(DeleteServiceAction.class));
        actions.add(SystemAction.get(RefreshServiceAction.class));

        return actions.toArray(new Action[0]);
    }

    @Override
    public void destroy() throws IOException {
        SaasServicesModel.getInstance().removeService(getSaas());
        super.destroy();
    }

    @Override
    public boolean canDestroy() {
        return getSaas().isUserDefined();
    }
}
