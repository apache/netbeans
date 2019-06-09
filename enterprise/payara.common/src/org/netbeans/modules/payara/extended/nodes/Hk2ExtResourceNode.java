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
package org.netbeans.modules.payara.extended.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.payara.common.nodes.Hk2ResourceNode;
import org.netbeans.modules.payara.common.ui.ConnectionPoolCustomizer;
import org.netbeans.modules.payara.extended.nodes.actions.ConnectionPoolAdvancedAttributesAction;
import org.netbeans.modules.payara.spi.ResourceDecorator;
import org.netbeans.modules.payara.spi.ResourceDesc;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Gaurav Gupta
 */
public class Hk2ExtResourceNode extends Hk2ResourceNode {

    private final Class customizer;

    public Hk2ExtResourceNode(Lookup lookup, ResourceDesc resource, ResourceDecorator decorator, Class customizer) {
        super(lookup, resource, decorator, customizer);
        this.customizer = customizer;
        if (customizer == ConnectionPoolCustomizer.class) {
            // add the ConnectionPoolAdvancedAttributes cookie
            getCookieSet().add(new Hk2ExtCookie.ConnectionPoolAdvancedAttributes(
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
        return actions.toArray(new Action[actions.size()]);
    }

}
