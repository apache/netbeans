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
package org.netbeans.modules.web.inspect.webkit.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.inspect.CSSUtils;
import org.netbeans.modules.web.inspect.actions.Resource;
import org.netbeans.modules.web.inspect.webkit.Utilities;
import org.netbeans.modules.web.webkit.debugging.api.css.InheritedStyleEntry;
import org.netbeans.modules.web.webkit.debugging.api.css.MatchedStyles;
import org.netbeans.modules.web.webkit.debugging.api.css.Rule;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Root node of Style Cascade section of CSS Styles view.
 *
 * @author Jan Stola
 */
public class MatchedRulesNode extends AbstractNode {
    /** Owning project of the inspected page. */
    private final Project project;
    /** Node that was matched by the displayed rules. */
    private final Node node;
    /** Rules matching the selected element. */
    private final MatchedStyles matchedStyles;

    /**
     * Creates a new {@code MatchedRulesNode}.
     *
     * @param project owning project of the inspected page.
     * @param node node that was matched by the displayed rules.
     * @param matchedStyles rules matching the selected element.
     */
    MatchedRulesNode(Project project, Node node, MatchedStyles matchedStyles) {
        super(new Children.Array());
        this.project = project;
        this.node = node;
        this.matchedStyles = matchedStyles;
        if (matchedStyles != null) {
            initChildren();
        }
        setDisplayName(NbBundle.getMessage(MatchedRulesNode.class, "MatchedRulesNode.displayName")); // NOI18N
    }

    /**
     * Initializes the children of this node.
     */
    private void initChildren() {
        Children.Array children = (Children.Array)getChildren();
        List<String> properties = new ArrayList<String>();
        List<MatchedRuleNode> nodes = new ArrayList<MatchedRuleNode>();
        for (Rule rule : matchedStyles.getMatchedRules()) {
            if (Utilities.showInCSSStyles(rule)) {
                nodes.add(createMatchedRuleNode(node, rule, properties, true));
            }
        }
        Node currentNode = node;
        for (InheritedStyleEntry entry : matchedStyles.getInheritedRules()) {
            currentNode = currentNode.getParentNode();
            if (currentNode == null) {
                break; // Issue 235611
            }
            for (Rule rule : entry.getMatchedRules()) {
                if (Utilities.showInCSSStyles(rule) && containsInheritedProperties(rule)) {
                    nodes.add(createMatchedRuleNode(currentNode, rule, properties, false));
                }
            }
        }
        children.add(nodes.toArray(new MatchedRuleNode[0]));
    }

    /**
     * Determines whether the specified rule contains some properties that
     * are inherited.
     *
     * @param rule rule to check.
     * @return {@code true} if the rule contains some properties that are
     * inherited, returns {@code false} otherwise.
     */
    private boolean containsInheritedProperties(Rule rule) {
        for (org.netbeans.modules.web.webkit.debugging.api.css.Property property : rule.getStyle().getProperties()) {
            String propertyName = property.getName();
            if (CSSUtils.isInheritedProperty(propertyName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a child for the specified matched rule.
     *
     * @param node node matched by the rule.
     * @param rule rule matching the node.
     * @param properties names of properties that were specified by other rules.
     * @param matched determines whether the rule matches the element
     * or whether it is inherited from some parent element.
     * @return child for the specified matched rule.
     */
    private MatchedRuleNode createMatchedRuleNode(Node node, Rule rule, List<String> properties, boolean matched) {
        RuleInfo ruleInfo = new RuleInfo();
        ruleInfo.fillMetaSourceInfo(rule, project);
        List<org.netbeans.modules.web.webkit.debugging.api.css.Property> ruleProperties = rule.getStyle().getProperties();
        List<String> active = new ArrayList<String>(); // Names of active properties in this rule
        for (int i=ruleProperties.size()-1; i>=0; i--) {
            org.netbeans.modules.web.webkit.debugging.api.css.Property property = ruleProperties.get(i);
            String name = property.getName();
            if (property.isParsedOk() && (matched || CSSUtils.isInheritedProperty(name))) {
                if (!properties.contains(name)) {
                    properties.add(name);
                    active.add(name);
                } else if (!active.contains(name)) {
                    ruleInfo.markAsOverriden(name);
                }
            }
        }
        ruleInfo.setInherited(!matched);
        if (rule.getId() == null) {
            // Debugging message inspired by issue 220611
            Logger.getLogger(MatchedRuleNode.class.getName()).log(Level.INFO, "Matched rule with null ID: {0}", rule); // NOI18N
        }
        return new MatchedRuleNode(node, rule, new Resource(project, rule.getSourceURL()), ruleInfo);
    }

}
