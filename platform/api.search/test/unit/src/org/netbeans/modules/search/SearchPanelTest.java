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
