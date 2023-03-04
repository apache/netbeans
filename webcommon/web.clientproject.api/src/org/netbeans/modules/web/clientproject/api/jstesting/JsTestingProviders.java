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

package org.netbeans.modules.web.clientproject.api.jstesting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.jstesting.CompositeCategoryProviderImpl;
import org.netbeans.modules.web.clientproject.jstesting.JsTestingProviderAccessor;
import org.netbeans.modules.web.clientproject.jstesting.SelectProviderPanel;
import org.netbeans.modules.web.clientproject.spi.jstesting.JsTestingProviderImplementation;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;

/**
 * This class provides access to the list of registered JS testing providers. The path
 * for registration is "{@value #JS_TESTING_PATH}" on SFS.
 * <p>
 * This class is thread safe.
 * @since 1.49
 */
public final class JsTestingProviders {

    /**
     * Path on SFS for JS testing providers registrations.
     */
    public static final String JS_TESTING_PATH = "JS/Testing"; // NOI18N
    /**
     * Category name in Project Customizer.
     * @see #createCustomizer()
     * @since 1.52
     */
    public static final String CUSTOMIZER_IDENT = "JS_TESTING"; // NOI18N

    private static final Lookup.Result<JsTestingProviderImplementation> JS_TESTING_PROVIDERS = Lookups.forPath(JS_TESTING_PATH)
            .lookupResult(JsTestingProviderImplementation.class);
    private static final JsTestingProviders INSTANCE = new JsTestingProviders();

    private final List<JsTestingProvider> jsTestingProviders = new CopyOnWriteArrayList<>();

    static {
        JS_TESTING_PROVIDERS.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                INSTANCE.reinitProviders();
            }
        });
    }


    private JsTestingProviders() {
        initProviders();
    }

    /**
     * Get JsTestingProviders instance.
     * @return JsTestingProviders instance
     */
    public static JsTestingProviders getDefault() {
        return INSTANCE;
    }

    /**
     * Get list of all registered JS testing providers.
     * @return list of all registered JS testing providers, can be empty but never {@code null}
     */
    public List<JsTestingProvider> getJsTestingProviders() {
        return new ArrayList<>(jsTestingProviders);
    }

    /**
     * Find JS testing provider for the given {@link JsTestingProvider#getIdentifier() identifier}.
     * @param identifier identifier of JS testing provider
     * @return JS testing provider or {@code null} if not found
     * @since 1.54
     */
    @CheckForNull
    public JsTestingProvider findJsTestingProvider(@NonNull String identifier) {
        Parameters.notNull("identifier", identifier); // NOI18N
        for (JsTestingProvider jsTestingProvider : jsTestingProviders) {
            if (jsTestingProvider.getIdentifier().equals(identifier)) {
                return jsTestingProvider;
            }
        }
        return null;
    }

    /**
     * Get selected JS testing provider for the given project. Returns {@code null} if none is selected (yet);
     * can display dialog for JS testing provider selection if {@code showSelectionPanel} is set to {@code true}.
     * @param project project to be checked
     * @param showSelectionPanel {@code true} for displaying dialog for JS testing provider selection, {@code false} otherwise
     * @return selected JS testing provider for the given project, can be {@code null} if none selected (yet)
     * @since 1.51
     */
    @CheckForNull
    public JsTestingProvider getJsTestingProvider(@NonNull Project project, boolean showSelectionPanel) {
        Parameters.notNull("project", project); // NOI18N
        for (JsTestingProvider jsTestingProvider : jsTestingProviders) {
            if (jsTestingProvider.isEnabled(project)) {
                // simply returns the first one
                return jsTestingProvider;
            }
        }
        // provider not set or found
        if (showSelectionPanel) {
            final JsTestingProvider jsTestingProvider = SelectProviderPanel.open();
            if (jsTestingProvider != null) {
                jsTestingProvider.notifyEnabled(project, true);
                return jsTestingProvider;
            }
        }
        return null;
    }

    /**
     * Set given JS testing provider for the given project. If there already is any JS testing provider
     * and if it is the same as the given one, this method does nothing.
     * @param project project to be configured
     * @param jsTestingProvider JS testing provider to be set
     * @since 1.54
     */
    public void setJsTestingProvider(@NonNull Project project, @NonNull JsTestingProvider jsTestingProvider) {
        Parameters.notNull("project", project); // NOI18N
        Parameters.notNull("jsTestingProvider", jsTestingProvider); // NOI18N
        JsTestingProvider currentTestingProvider = getJsTestingProvider(project, false);
        if (currentTestingProvider != null) {
            if (currentTestingProvider.getIdentifier().equals(jsTestingProvider.getIdentifier())) {
                // already set the same one
                return;
            }
            currentTestingProvider.notifyEnabled(project, false);
        }
        jsTestingProvider.notifyEnabled(project, true);
    }

    /**
     * Create project customizer for JS testing providers.
     * @return project customizer for JS testing providers
     * @since 1.51
     */
    public ProjectCustomizer.CompositeCategoryProvider createCustomizer() {
        return new CompositeCategoryProviderImpl();
    }

    /**
     * Create factory for project nodes provided by JS testing providers.
     * @return factory for project nodes provided by JS testing providers
     */
    public NodeFactory createJsTestingProvidersNodeFactory() {
        return new NodeFactory() {
            @Override
            public NodeList<?> createNodes(Project project) {
                return new ProxyNodeList(project);
            }
        };
    }

    private void initProviders() {
        assert jsTestingProviders.isEmpty() : "Empty providers expected but: " + jsTestingProviders;
        jsTestingProviders.addAll(map(JS_TESTING_PROVIDERS.allInstances()));
    }

    void reinitProviders() {
        synchronized (jsTestingProviders) {
            clearProviders();
            initProviders();
        }
    }

    private void clearProviders() {
        jsTestingProviders.clear();
    }

    //~ Mappers

    private Collection<JsTestingProvider> map(Collection<? extends JsTestingProviderImplementation> providers) {
        List<JsTestingProvider> result = new ArrayList<>();
        for (JsTestingProviderImplementation provider : providers) {
            result.add(JsTestingProviderAccessor.getDefault().create(provider));
        }
        return result;
    }

    //~ Inner classes

    private static final class ProxyNodeList implements NodeList<Node>, ChangeListener {

        private final Project project;
        private final List<NodeList<Node>> nodeList = new CopyOnWriteArrayList<>();
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private final AtomicLong listenerCounter = new AtomicLong(0);


        private ProxyNodeList(Project project) {
            assert project != null;
            this.project = project;
            // XXX listen on provider changes
            nodeList.addAll(initNodeList(project));
        }

        private List<NodeList<Node>> initNodeList(Project project) {
            List<NodeList<Node>> result = new ArrayList<>();
            for (JsTestingProvider provider : JsTestingProviders.getDefault().getJsTestingProviders()) {
                NodeList<Node> providerNodeList = provider.createNodeList(project);
                if (providerNodeList != null) {
                    result.add(providerNodeList);
                }
            }
            return result;
        }

        @Override
        public List<Node> keys() {
            List<Node> nodes = new ArrayList<>();
            for (NodeList<Node> list : nodeList) {
                nodes.addAll(list.keys());
            }
            return nodes;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            if (listenerCounter.incrementAndGet() == 1) {
                for (NodeList<Node> list : nodeList) {
                    list.addChangeListener(this);
                }
            }
            changeSupport.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            if (listenerCounter.decrementAndGet() == 0) {
                for (NodeList<Node> list : nodeList) {
                    list.removeChangeListener(this);
                }
            }
            changeSupport.removeChangeListener(listener);
        }

        @Override
        public Node node(Node node) {
            return node;
        }

        @Override
        public void addNotify() {
            for (NodeList<Node> list : nodeList) {
                list.addNotify();
            }
        }

        @Override
        public void removeNotify() {
            for (NodeList<Node> list : nodeList) {
                list.removeNotify();
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            changeSupport.fireChange();
        }

    }

}
