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
