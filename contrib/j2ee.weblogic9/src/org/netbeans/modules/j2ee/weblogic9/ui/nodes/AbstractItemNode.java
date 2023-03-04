/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import javax.swing.Action;

import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.RefreshModulesAction;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.RefreshModulesCookie;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.UnregisterCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.util.actions.SystemAction;


/**
 * @author ads
 *
 */
abstract class AbstractItemNode extends AbstractNode {
    
    AbstractItemNode(final ChildFactory<?> childFactory, String name)
    {
        super(Children.create(childFactory, true));
        this.childFactory = childFactory;
        setDisplayName(name);
        if(childFactory instanceof RefreshModulesCookie) {
            getCookieSet().add((RefreshModulesCookie)childFactory);
        }
    }

    AbstractItemNode(Children children)
    {
        super(children);
        childFactory = null;
    }

    public Action[] getActions(boolean context)
    {
        if(getChildFactory() instanceof RefreshModulesCookie)
            return (new SystemAction[] {
                SystemAction.get(RefreshModulesAction.class)
            });
        else
            return new SystemAction[0];
    }

    protected ChildFactory<?> getChildFactory()
    {
        return childFactory;
    }

    private final ChildFactory<?> childFactory;
}