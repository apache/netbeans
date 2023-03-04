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
import org.netbeans.modules.web.inspect.CSSUtils;
import org.netbeans.modules.web.inspect.actions.Resource;
import org.netbeans.modules.web.inspect.webkit.Utilities;
import org.netbeans.modules.web.inspect.webkit.actions.GoToPropertySourceAction;
import org.netbeans.modules.web.webkit.debugging.api.css.Rule;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Node that represents a property of a rule that matches a node.
 *
 * @author Jan Stola
 */
public class MatchedPropertyNode extends AbstractNode {
    /** Name of the "value" property. */
    public static final String PROPERTY_VALUE = "value"; // NOI18N
    /** Name of an attribute that determines whether the matched property determines some color. */
    static final String COLOR_PROPERTY = "color"; // NOI18N
    /** Property sets of this node. */
    private PropertySet[] propertySets;
    /** Property represented by this node. */
    org.netbeans.modules.web.webkit.debugging.api.css.Property property;
    /** Preferred action. */
    private Action preferredAction;

    /**
     * Creates a new {@code MatchedPropertyNode}.
     *
     * @param rule owning rule of the property to represent.
     * @param ruleOrigin origin of the rule.
     * @param property property to represent.
     */
    MatchedPropertyNode(Rule rule, Resource ruleOrigin, org.netbeans.modules.web.webkit.debugging.api.css.Property property) {
        super(Children.LEAF, Lookups.fixed(rule, ruleOrigin, property));
        this.property = property;
        setDisplayName(property.getName());
        RuleInfo ruleInfo = new RuleInfo();
        ruleInfo.fillMetaSourceInfo(rule, ruleOrigin.getProject());
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
            String description = NbBundle.getMessage(MatchedPropertyNode.class,
                "MatchedPropertyNode.description", rule.getSelector(), stylesheet); // NOI18N
            setShortDescription(description);
        }
    }

    /**
     * Adds a sub-node to this node. This method is called on nodes that
     * represent a short-hand property only.
     *
     * @param subNode sub-node that represents a property that belongs under
     * a short-hand property represented by this node.
     */
    void addSubNode(MatchedPropertyNode subNode) {
        if (isLeaf()) {
            setChildren(new Children.Array());
        }
        Children.Array children = (Children.Array)getChildren();
        children.add(new Node[] { subNode });
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
        String displayName = NbBundle.getMessage(MatchedPropertyNode.class, "MatchedPropertyNode.displayName"); // NOI18N
        PropertySet set = new PropertySet(Sheet.PROPERTIES, displayName, null) {
            private final Property<?> valueProperty = new PropertySupport.ReadOnly<String>(
                    PROPERTY_VALUE, String.class, null, null) {
                {
                    setValue(COLOR_PROPERTY, CSSUtils.isColorProperty(property.getName()));
                }
                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    return property.getValue();
                }
                @Override
                public String getShortDescription() {
                    return MatchedPropertyNode.this.getShortDescription();
                }
            };
            @Override
            public Property<?>[] getProperties() {
                return new Property[] { valueProperty };
            }
        };
        return new PropertySet[] { set };
    }

    @Override
    public synchronized Action getPreferredAction() {
        if (preferredAction == null) {
            preferredAction = new GoToPropertySourceAction(this);
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
