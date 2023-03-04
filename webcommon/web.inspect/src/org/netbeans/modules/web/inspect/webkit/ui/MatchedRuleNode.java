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

import java.lang.reflect.InvocationTargetException;
import javax.swing.Action;
import org.netbeans.modules.web.inspect.actions.Resource;
import org.netbeans.modules.web.inspect.webkit.Utilities;
import org.netbeans.modules.web.inspect.webkit.actions.GoToRuleSourceAction;
import org.netbeans.modules.web.webkit.debugging.api.css.Rule;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Node representing a rule that matches a node.
 *
 * @author Jan Stola
 */
public class MatchedRuleNode extends AbstractNode {
    /** Name of the "node" property. */
    public static final String PROPERTY_NODE = "node"; // NOI18N
    /** Property sets of this node. */
    private PropertySet[] propertySets;
    /** Node that was matched by the represented rule. */
    Node node;
    /** Preferred action. */
    private Action preferredAction;

    /**
     * Creates a new {@code MatchedRuleNode}.
     *
     * @param node node that was matched by the rule to represent.
     * @param rule rule to represent.
     * @param ruleOrigin origin of the rule.
     * @param ruleInfo additional rule information.
     */
    MatchedRuleNode(Node node, Rule rule, Resource ruleOrigin, RuleInfo ruleInfo) {
        super(Children.LEAF, Lookups.fixed(rule, ruleOrigin, ruleInfo));
        this.node = node;
        String sourceURL = ruleInfo.getMetaSourceFile();
        if (sourceURL != null && ruleInfo.getMetaSourceLine() != -1) {
            if (sourceURL.startsWith("file://") && !sourceURL.startsWith("file:///")) { // NOI18N
                // file://C:/file is understood as file on host C, should be file:///C:/file
                sourceURL = "file:/" + sourceURL.substring(5); // NOI18N
            }
        } else {
            sourceURL = rule.getSourceURL();
        }
        String stylesheet = Utilities.relativeResourceName(sourceURL, ruleOrigin.getProject());
        if (stylesheet != null) {
            if (stylesheet.isEmpty()) {
                stylesheet = Bundle.CSSStylesSelectionPanel_generatedStylesheet();
            }
            String description = NbBundle.getMessage(MatchedRuleNode.class,
                "MatchedRuleNode.description", stylesheet); // NOI18N
            setShortDescription(description);
        }
    }

    @Override
    public String getHtmlDisplayName() {
        return node.getHtmlDisplayName();
    }

    @Override
    public String getDisplayName() {
        return node.getDisplayName();
    }

    @Override
    public synchronized PropertySet[] getPropertySets() {
        if (propertySets == null) {
            propertySets = createPropertySets();
        }
        return propertySets;
    }

    /**
     * Creates property sets of this node.
     *
     * @return property sets of this node.
     */
    private PropertySet[] createPropertySets() {
        String displayName = NbBundle.getMessage(MatchedRuleNode.class, "MatchedRuleNode.properties"); // NOI18N
        PropertySet set = new PropertySet(Sheet.PROPERTIES, displayName, null) {
            private final Property<?> nodeProperty = new PropertySupport.ReadOnly<String>(
                    PROPERTY_NODE, String.class, null, null) {
                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    return node.getHtmlDisplayName();
                }
            };
            @Override
            public Property<?>[] getProperties() {
                return new Property[] { nodeProperty };
            }
        };
        return new PropertySet[] { set };
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            getPreferredAction()
        };
    }

    @Override
    public synchronized Action getPreferredAction() {
        if (preferredAction == null) {
            preferredAction = new GoToRuleSourceAction(this);
        }
        return preferredAction;
    }

}
