/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.spi.search.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchInfoUtils;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.api.search.provider.impl.AbstractCompoundIterator;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Marian Petras
 */
public final class SubnodesSearchInfoDefinition extends SearchInfoDefinition {

    /**
     * Children - subnodes.
     */
    private final Children children;

    public SubnodesSearchInfoDefinition(final Children children) {
        this.children = children;
    }

    /**
     */
    @Override
    public boolean canSearch() {
        return true;
    }

    /**
     */
    @Override
    public Iterator<FileObject> filesToSearch(SearchScopeOptions options,
            SearchListener listener, AtomicBoolean terminated) {
        final Node[] nodes = children.getNodes(true);
        if (nodes.length == 0) {
            return SimpleSearchInfoDefinition.EMPTY_SEARCH_INFO.filesToSearch(
                    options, listener, terminated);
        }

        List<SearchInfo> searchInfoElements =
                new ArrayList<SearchInfo>(nodes.length);
        for (int i = 0; i < nodes.length; i++) {
            Node child = nodes[i];
            SearchInfo subInfo = 
                    SearchInfoUtils.getSearchInfoForNode(child);
            if (subInfo != null && subInfo.canSearch()) {
                searchInfoElements.add(subInfo);
            }
        }

        final int size = searchInfoElements.size();
        switch (size) {
            case 0:
                return Collections.<FileObject>emptyList().iterator();
            case 1:
                return searchInfoElements.get(0).getFilesToSearch(
                        options, listener, terminated).iterator();
            default:
                return new AbstractCompoundIterator<SearchInfo, FileObject>(
                        searchInfoElements.toArray(
                        new SearchInfo[size]),
                        options, listener, terminated) {
                    @Override
                    protected Iterator<FileObject> getIteratorFor(
                            SearchInfo element, SearchScopeOptions options,
                            SearchListener listener, AtomicBoolean terminated) {
                        return element.getFilesToSearch(
                                options, listener, terminated).iterator();
                    }
                };
        }
    }

    @Override
    public List<SearchRoot> getSearchRoots() {
        final Node[] nodes = children.getNodes(true);
        if (nodes.length == 0) {
            return Collections.emptyList();
        }
        List<SearchRoot> allRoots = new LinkedList<SearchRoot>();
        for (Node subNode : nodes) {
            SearchInfoDefinition subInfo =
                    SearchInfoDefinitionUtils.getSearchInfoDefinition(subNode);
            if (subInfo != null && subInfo.canSearch()) {
                allRoots.addAll(subInfo.getSearchRoots());
            }
        }
        return allRoots;
    }
}
