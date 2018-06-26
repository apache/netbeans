/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect.webkit.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.inspect.CSSUtils;
import org.netbeans.modules.web.inspect.actions.Resource;
import org.netbeans.modules.web.inspect.webkit.Utilities;
import org.netbeans.modules.web.webkit.debugging.api.css.InheritedStyleEntry;
import org.netbeans.modules.web.webkit.debugging.api.css.MatchedStyles;
import org.netbeans.modules.web.webkit.debugging.api.css.PropertyInfo;
import org.netbeans.modules.web.webkit.debugging.api.css.Rule;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 * Root node of Property Summary section of CSS Styles view.
 *
 * @author Jan Stola
 */
public class MatchedPropertiesNode extends AbstractNode {
    /** Rules matching a node. */
    private final MatchedStyles matchedStyles;
    /** Information about the supported properties. */
    private final Map<String,PropertyInfo> propertyInfos;
    /** Owning project of the inspected page. */
    private final Project project;

    /**
     * Creates a new {@code MatchedPropertiesNode}.
     *
     * @param project owning project of the inspected page.
     * @param matchedStyles rules matching a node.
     * @param propertyInfos information about the supported properties.
     */
    MatchedPropertiesNode(Project project, MatchedStyles matchedStyles, Map<String,PropertyInfo> propertyInfos) {
        super(new Children.Array());
        this.project = project;
        this.matchedStyles = matchedStyles;
        this.propertyInfos = propertyInfos;
        initChildren();
        setDisplayName(NbBundle.getMessage(MatchedPropertiesNode.class, "MatchedPropertiesNode.displayName")); // NOI18N
    }

    /**
     * Initializes the children of this node.
     */
    private void initChildren() {
        Set<String> properties = new HashSet<String>();
        Children.Array children = (Children.Array)getChildren();
        List<MatchedPropertyNode> nodes = new ArrayList<MatchedPropertyNode>();
        for (Rule rule : matchedStyles.getMatchedRules()) {
            if (Utilities.showInCSSStyles(rule)) {
                addChildrenFor(rule, nodes, properties, true);
            }
        }
        for (InheritedStyleEntry entry : matchedStyles.getInheritedRules()) {
            for (Rule rule : entry.getMatchedRules()) {
                if (Utilities.showInCSSStyles(rule)) {
                    addChildrenFor(rule, nodes, properties, false);
                }
            }
        }
        children.add(nodes.toArray(new MatchedPropertyNode[nodes.size()]));
    }

    /**
     * Creates subnodes of this node.
     *
     * @param rule rule for which the children should be created.
     * @param toPopulate list where the newly created children should be appended.
     * @param properties names of properties for which there are children already created.
     * @param matchingSelection determines whether the given rule matches the selected
     * node or whether it matches some parent of the selected node.
     */
    private void addChildrenFor(Rule rule, List<MatchedPropertyNode> toPopulate, Set<String> properties, boolean matchingSelection) {
        List<org.netbeans.modules.web.webkit.debugging.api.css.Property> ruleProperties = rule.getStyle().getProperties();
        int index = toPopulate.size();
        Map<String, MatchedPropertyNode> parentMap = new HashMap<String, MatchedPropertyNode>();
        Map<String, MatchedPropertyNode> childrenMap = new HashMap<String, MatchedPropertyNode>();
        for (int i=ruleProperties.size()-1; i>=0; i--) {
            org.netbeans.modules.web.webkit.debugging.api.css.Property property = ruleProperties.get(i);
            String name = property.getName();
            if (property.isParsedOk() && !properties.contains(name) && (matchingSelection || CSSUtils.isInheritedProperty(name))) {
                properties.add(name);
                // Declared properties
                if (property.getText() != null) {
                    MatchedPropertyNode node = new MatchedPropertyNode(rule, new Resource(project, rule.getSourceURL()), property);
                    parentMap.put(name, node);
                    toPopulate.add(node);
                }
            }
        }
        // Shorthands
        for (org.netbeans.modules.web.webkit.debugging.api.css.Property property : ruleProperties) {
            if (property.getText() == null) {
                String shorthandName = property.getShorthandName();
                MatchedPropertyNode child = new MatchedPropertyNode(rule, new Resource(project, rule.getSourceURL()), property);
                if (shorthandName == null) {
                    childrenMap.put(property.getName(), child);
                } else {
                    MatchedPropertyNode parent = parentMap.get(shorthandName);
                    if (parent != null) {
                        parent.addSubNode(child);
                    }
                }
            }
        }
        // Longhands
        for (int i=index; i<toPopulate.size(); i++) {
            MatchedPropertyNode parent = toPopulate.get(i);
            String propertyName = parent.property.getName();
            List<String> longhands = Collections.EMPTY_LIST;
            if (propertyInfos != null) {
                PropertyInfo info = propertyInfos.get(propertyName);
                if (info != null) {
                    longhands = info.getLonghands();
                }
            }
            for (String longhand : longhands) {
                MatchedPropertyNode child = childrenMap.remove(longhand);
                if (child != null) {
                    parent.addSubNode(child);
                }
            }
        }
    }

}
