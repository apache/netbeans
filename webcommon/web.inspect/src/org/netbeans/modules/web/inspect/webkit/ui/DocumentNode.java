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

import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.inspect.webkit.WebKitPageModel;
import org.netbeans.modules.web.webkit.debugging.api.css.CSS;
import org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetHeader;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Root node of the document section of CSS Styles view.
 *
 * @author Jan Stola
 */
public class DocumentNode extends AbstractNode {
    /** Icon base of the node. */
    static final String ICON_BASE = "org/netbeans/modules/web/inspect/resources/matchedRules.png"; // NOI18N

    /**
     * Creates a new {@code DocumentNode}.
     *
     * @param pageModel owning model of the node.
     * @param filter filter for the subtree of the node.
     */
    DocumentNode(WebKitPageModel pageModel, Filter filter) {
        super(Children.create(
                new DocumentChildFactory(
                    pageModel.getProject(),
                    pageModel.getWebKit().getCSS(),
                    filter),
                true), Lookups.fixed(pageModel));
        setDisplayName(NbBundle.getMessage(DocumentNode.class, "DocumentNode.displayName")); // NOI18N
        setIconBaseWithExtension(ICON_BASE);
    }

    /**
     * Factory for children of {@code DocumentNode}.
     */
    static class DocumentChildFactory extends ChildFactory<StyleSheetHeader> {
        /** CSS domain of the corresponding WebKit debugging. */
        private final CSS css;
        /** Filter for the subtree of the node. */
        private final Filter filter;
        /** Owning project of the inspected page. */
        private final Project project;

        /**
         * Creates a new {@code DocumentChildFactory}.
         *
         * @param project owning project of the inspected page.
         * @param css CSS domain of the corresponding WebKit debugging.
         * @param filter filter for the subtree of the node.
         */
        DocumentChildFactory(Project project, CSS css, Filter filter) {
            this.project = project;
            this.css = css;
            this.filter = filter;
        }

        @Override
        protected boolean createKeys(List<StyleSheetHeader> toPopulate) {
            toPopulate.addAll(css.getAllStyleSheets());
            return true;
        }

        @Override
        protected Node createNodeForKey(StyleSheetHeader key) {
            return new StyleSheetNode(project, css, key, filter);
        }

    }

}
