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
package org.netbeans.spi.search.provider;

import javax.swing.JComponent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.nodes.Node;

/**
 * Define how search results (and search controls) should be displayed.
 *
 * @param <T> Type of result of file content matching.
 *
 * @author jhavlin
 */
public abstract class SearchResultsDisplayer<T> {

    /** Constructor for subclasses. */
    protected SearchResultsDisplayer() {}

    /**
     * Get component that will be shown in the Search Results window. Is should
     * be created lazily.
     */
    public abstract @NonNull JComponent getVisualComponent();

    /**
     * This method is called when a new matching object is
     * found. It should add representation of this matching object to model of
     * created visual component.
     */
    public abstract void addMatchingObject(@NonNull T object);

    /**
     * Called right after the search was started. Default implementation does
     * nothing.
     */
    public void searchStarted() {
    }

    /**
     * Called right after the search was finished. Default implementation does
     * nothing.
     */
    public void searchFinished() {
    }

    /**
     * Get default displayer that shows results as a tree of nodes.
     *
     * @param helper Helper that returns nodes for matching objects.
     * @param searchComposition Search composition of the displayer is created
     * for.
     * @param presenter Presenter that can be shown to modify search criteria.
     * @param title Title that will be shown in the tab of search results
     * window.
     */
    public static <U> DefaultSearchResultsDisplayer<U> createDefault(
            @NonNull NodeDisplayer<U> helper,
            @NonNull SearchComposition<U> searchComposition,
            @NullAllowed SearchProvider.Presenter presenter,
            @NonNull String title) {

        return new DefaultSearchResultsDisplayer<U>(helper, searchComposition,
                presenter, title);
    }

    /**
     * Helper class for transforming matching objects to nodes.
     */
    public static abstract class NodeDisplayer<T> {
        
        /** Constructor for subclasses. */
        protected NodeDisplayer() {}

        public abstract Node matchToNode(T match);

    }

    /**
     * Return title of this displayer. It will be displayed in the tab within
     * search results window.
     */
    public abstract @NonNull String getTitle();

    /**
     * Set node that display information from the search listener.
     *
     * This method is called right after a new displayer is created, before
     * method {@link #getVisualComponent()}.
     *
     * The default implementation does nothing. Override it if you want to add
     * the info node to your UI.
     */
    public void setInfoNode(Node infoNode) {
    }

    /**
     * Called right after the displayer is closed. It should be overriden to
     * release all held resources. The default implementation does nothing.
     */
    public void closed() {
    }
}