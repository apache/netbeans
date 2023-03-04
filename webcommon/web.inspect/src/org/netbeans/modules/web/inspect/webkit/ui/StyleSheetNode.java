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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.inspect.CSSUtils;
import org.netbeans.modules.web.inspect.actions.OpenResourceAction;
import org.netbeans.modules.web.inspect.actions.Resource;
import org.netbeans.modules.web.inspect.webkit.Utilities;
import org.netbeans.modules.web.webkit.debugging.api.css.CSS;
import org.netbeans.modules.web.webkit.debugging.api.css.Rule;
import org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetBody;
import org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetHeader;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * A node representing a style sheet.
 *
 * @author Jan Stola
 */
public class StyleSheetNode extends AbstractNode {
    /** Icon base of the node. */
    static final String ICON_BASE = "org/netbeans/modules/web/inspect/resources/matchedRules.png"; // NOI18N
    /** Header of the style sheet. */
    private final StyleSheetHeader header;
    /** Owning project of the inspected page. */
    private final Project project;

    /**
     * Creates a new {@code StyleSheetNode}.
     *
     * @param project owning project of the inspected page.
     * @param css CSS domain of the corresponding WebKit debugging.
     * @param header header of the represented stylesheet.
     * @param filter filter for the subtree of the node.
     */
    StyleSheetNode(Project project, CSS css, StyleSheetHeader header, Filter filter) {
        super(Children.create(new StyleSheetChildFactory(project, css, header, filter), true),
                Lookups.fixed(new Resource(project, header.getSourceURL())));
        this.project = project;
        this.header = header;
        updateDisplayName();
        setIconBaseWithExtension(ICON_BASE);
    }

    /**
     * Updates the display name of the node.
     */
    private void updateDisplayName() {
        String sourceURL = header.getSourceURL();
        String displayName = Utilities.relativeResourceName(sourceURL, project);
        String title = header.getTitle();
        if (title != null && !title.trim().isEmpty()) {
            displayName = title + " (" + displayName + ")"; // NOI18N
        }
        setDisplayName(displayName);
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(OpenResourceAction.class);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(OpenResourceAction.class)
        };
    }

    /**
     * Factory for children of {@code StyleSheetNode}.
     */
    static class StyleSheetChildFactory extends ChildFactory<Rule> {
        /** Owning project of the inspected page. */
        private final Project project;
        /** CSS domain of the corresponding WebKit debugging. */
        private final CSS css;
        /** Header of the style sheet. */
        private final StyleSheetHeader header;
        /** Body of the style sheet. */
        private StyleSheetBody body;
        /** Filter of the subtree of the node. */
        private final Filter filter;

        /**
         * Creates a new {@code StyleSheetChildFactory}.
         *
         * @param project owning project of the inspected page.
         * @param css CSS domain of the corresponding WebKit debugging.
         * @param header header of the style sheet.
         * @param filter filter for the subtree of the node.
         */
        StyleSheetChildFactory(Project project, CSS css, StyleSheetHeader header, Filter filter) {
            this.project = project;
            this.css = css;
            this.header = header;
            this.filter = filter;
            filter.addPropertyChangeListener(createListener());
        }

        /**
         * Creates a property change listener on the changes of the filter.
         *
         * @return property change listener on the changes of the filter.
         */
        private PropertyChangeListener createListener() {
            return new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String propertyName = evt.getPropertyName();
                    if (Filter.PROPERTY_PATTERN.equals(propertyName)) {
                        refresh(false);
                    }
                }
            };
        }

        @Override
        protected boolean createKeys(List<Rule> toPopulate) {
            String styleSheetId = header.getStyleSheetId();
            if (body == null) {
                body = css.getStyleSheet(styleSheetId);
            }
            if (body == null) {
                Logger.getLogger(StyleSheetNode.class.getName())
                        .log(Level.INFO, "Null body obtained for style sheet {0}!", styleSheetId); // NOI18N
            } else {
                for (Rule rule : body.getRules()) {
                    if (includeKey(rule)) {
                        toPopulate.add(rule);
                    }
                }
            }
            return true;
        }

        /**
         * Determines whether the specified rule should be included among keys.
         *
         * @param rule rule to check.
         * @return {@code true} when the rule should be included among keys,
         * returns {@code false} otherwise.
         */
        private boolean includeKey(Rule rule) {
            boolean include = true;
            String pattern = filter.getPattern();
            if (pattern != null) {
                pattern = CSSUtils.normalizeSelector(pattern);
                String selector = CSSUtils.normalizeSelector(rule.getSelector());
                include = selector.contains(pattern);
            }
            return include;
        }

        @Override
        protected Node createNodeForKey(Rule key) {
            return new RuleNode(key, new Resource(project, header.getSourceURL()));
        }

    }

}
