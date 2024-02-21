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
package org.netbeans.modules.payara.common.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.payara.common.ui.ConnectionPoolCustomizer;
import org.netbeans.modules.payara.common.nodes.actions.ConnectionPoolAdvancedAttributesAction;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.netbeans.modules.payara.spi.ResourceDecorator;
import org.netbeans.modules.payara.spi.ResourceDesc;
import org.netbeans.modules.payara.spi.ServerUtilities;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Peter Williams
 */
public class Hk2ResourceNode extends Hk2ItemNode {

    private final Class customizer;

    public Hk2ResourceNode(final Lookup lookup, final ResourceDesc resource,
            final ResourceDecorator decorator, final Class customizer) {
        super(Children.LEAF, lookup, resource.getName(), decorator);
        this.customizer = customizer;
        setDisplayName(resource.getName());
        setShortDescription("<html>name: " + resource.getName() + "</html>");

        if (decorator.canUnregister()) {
            getCookieSet().add(new Hk2Cookie.Unregister(lookup,
                    resource.getName(), resource.getCommandType(),
                    decorator.getCmdPropertyName(),
                    decorator.isCascadeDelete()));
        }

        if (decorator.canEditDetails()) {
            PayaraModule m = lookup.lookup(PayaraModule.class);
            if (null != m) {
                String rootDir = m.getInstanceProperties().get(PayaraModule.PAYARA_FOLDER_ATTR);
                if (ServerUtilities.isTP2(rootDir)) {
                    // don't add the edit details cookie
                } else {
                    // add the editor cookie
                    getCookieSet().add(new Hk2Cookie.EditDetails(
                            lookup, getDisplayName(),
                            resource.getCommandType(), customizer));
                }

            }

        }
        if (customizer == ConnectionPoolCustomizer.class) {
            // add the ConnectionPoolAdvancedAttributes cookie
            getCookieSet().add(new Hk2Cookie.ConnectionPoolAdvancedAttributes(
                    lookup, getDisplayName(),
                    resource.getCommandType(), customizer));
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<>(Arrays.asList(super.getActions(context)));
        if (customizer == ConnectionPoolCustomizer.class) {
            actions.add(SystemAction.get(ConnectionPoolAdvancedAttributesAction.class));
        }
        return actions.toArray(new Action[0]);
    }
}
