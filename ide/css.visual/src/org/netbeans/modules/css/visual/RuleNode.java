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
package org.netbeans.modules.css.visual;

import org.netbeans.modules.css.visual.spi.RuleHandle;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.visual.actions.OpenLocationAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * A node representing a CSS rule.
 *
 * @author Jan Stola
 */
public class RuleNode extends AbstractNode {
    /** Icon base of the node. */
    static final String ICON_BASE = "org/netbeans/modules/css/visual/resources/style_sheet_16.png"; // NOI18N

    /**
     * Creates a new {@code RuleNode}.
     *
     * @param rule rule represented by the node.
     * @param ruleOrigin origin of the rule.
     */
    RuleNode(RuleHandle ruleHandle, Project project) {
        super(Children.LEAF, (project == null) ? Lookups.fixed(ruleHandle) : Lookups.fixed(ruleHandle, project));
        setDisplayName(ruleHandle.getDisplayName());
        setIconBaseWithExtension(ICON_BASE);
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(OpenLocationAction.class);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(OpenLocationAction.class)
        };
    }

}
