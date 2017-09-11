/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search;

import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.junit.MockServices;
import org.netbeans.spi.search.provider.SearchComposition;
import org.netbeans.spi.search.provider.SearchProvider;
import org.netbeans.spi.search.provider.SearchResultsDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author jhavlin
 */
public class SearchPanelTest {

    public SearchPanelTest() {
    }

    @Test
    public void testMakePresenters() {
        MockServices.setServices(MockSearchProvider.class);
        try {
            SearchProvider prov = Lookup.getDefault().lookup(
                    MockSearchProvider.class);
            assertNotNull(prov);
            SearchPanel sp = new SearchPanel(false);
            List<?> list = sp.makePresenters(new MockPresenter(prov, false));
            boolean found = false;
            for (Object o : list) {
                if ("Proxy presenter for Mock search provider".equals(
                        o.toString())) {
                    found = true;
                    break;
                }
            }
            assertTrue("Explicitly passed presenter for disabled provider"
                    + " should be used", found);
        } finally {
            MockServices.setServices();
        }
    }

    public static class MockSearchProvider extends SearchProvider {

        @Override
        public Presenter createPresenter(boolean replaceMode) {
            return new MockPresenter(this, false);
        }

        @Override
        public String getTitle() {
            return "Mock search provider";
        }

        @Override
        public boolean isReplaceSupported() {
            return false;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }

    public static class MockPresenter extends SearchProvider.Presenter {

        private final JPanel panel = new JPanel();

        public MockPresenter(SearchProvider searchProvider, boolean replacing) {
            super(searchProvider, replacing);
        }

        @Override
        public JComponent getForm() {
            return panel;
        }

        @Override
        public SearchComposition<?> composeSearch() {
            return new SearchComposition<Object>() {

                @Override
                public void start(SearchListener listener) {
                }

                @Override
                public void terminate() {
                }

                @Override
                public boolean isTerminated() {
                    return false;
                }

                @Override
                public SearchResultsDisplayer<Object> getSearchResultsDisplayer() {
                    return SearchResultsDisplayer.createDefault(
                            new SearchResultsDisplayer.NodeDisplayer<Object>() {

                        @Override
                        public Node matchToNode(Object match) {
                            return new AbstractNode(Children.LEAF);
                        }
                    }, this, null, "Mock results");
                }
            };
        }

        @Override
        public boolean isUsable(NotificationLineSupport notificationLineSupport) {
            return true;
        }
    }
}
