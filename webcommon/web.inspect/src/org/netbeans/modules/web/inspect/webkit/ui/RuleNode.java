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
package org.netbeans.modules.web.inspect.webkit.ui;

import javax.swing.Action;
import org.netbeans.modules.web.inspect.actions.Resource;
import org.netbeans.modules.web.inspect.webkit.actions.GoToRuleSourceAction;
import org.netbeans.modules.web.webkit.debugging.api.css.Rule;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 * A node representing a CSS rule.
 *
 * @author Jan Stola
 */
public class RuleNode extends AbstractNode {
    /** Icon base of the node. */
    static final String ICON_BASE = "org/netbeans/modules/web/inspect/resources/matchedRules.png"; // NOI18N
    /** Preferred action. */
    private Action preferredAction;

    /**
     * Creates a new {@code RuleNode}.
     *
     * @param rule rule represented by the node.
     * @param ruleOrigin origin of the rule.
     */
    RuleNode(Rule rule, Resource ruleOrigin) {
        super(Children.LEAF, Lookups.fixed(rule, ruleOrigin));
        setDisplayName(rule.getSelector());
        setIconBaseWithExtension(ICON_BASE);
    }

    @Override
    public synchronized Action getPreferredAction() {
        if (preferredAction == null) {
            preferredAction = new GoToRuleSourceAction(this);
        }
        return preferredAction;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            getPreferredAction()
        };
    }

}
