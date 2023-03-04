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
package org.netbeans.modules.selenium2.webclient.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.selenium2.webclient.spi.SeleniumTestingProviderImplementation;
import org.netbeans.modules.selenium2.webclient.ui.customizer.SelectProviderPanel;
import org.netbeans.modules.selenium2.webclient.ui.customizer.CompositeCategoryProviderImpl;
import org.netbeans.modules.selenium2.webclient.ui.customizer.SeleniumTestingProviderAccessor;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;

/**
 * This class provides access to the list of registered Selenium testing providers. The path
 * for registration is "{@value #SELENIUM_TESTING_PATH}" on SFS.
 * <p>
 * This class is thread safe.
 * @author Theofanis Oikonomou
 */
public final class SeleniumTestingProviders {

    /**
     * Path on SFS for Selenium testing providers registrations.
     */
    public static final String SELENIUM_TESTING_PATH = "Selenium/Testing"; // NOI18N
    /**
     * Constant for Project Properties > Selenium Testing panel.
     */
    public static final String CUSTOMIZER_SELENIUM_TESTING_IDENT = "SELENIUM_TESTING"; // NOI18N

    private static final Lookup.Result<SeleniumTestingProviderImplementation> SELENIUM_TESTING_PROVIDERS = Lookups.forPath(SELENIUM_TESTING_PATH)
            .lookupResult(SeleniumTestingProviderImplementation.class);
    private static final SeleniumTestingProviders INSTANCE = new SeleniumTestingProviders();

    private final List<SeleniumTestingProvider> seleniumTestingProviders = new CopyOnWriteArrayList<>();

    static {
        SELENIUM_TESTING_PROVIDERS.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                INSTANCE.reinitProviders();
            }
        });
    }


    private SeleniumTestingProviders() {
        initProviders();
    }

    /**
     * Get SeleniumTestingProviders instance.
     * @return SeleniumTestingProviders instance
     */
    public static SeleniumTestingProviders getDefault() {
        return INSTANCE;
    }

    /**
     * Get list of all registered Selenium testing providers.
     * @return list of all registered Selenium testing providers, can be empty but never {@code null}
     */
    public List<SeleniumTestingProvider> getSeleniumTestingProviders() {
        return new ArrayList<>(seleniumTestingProviders);
    }

    /**
     * Find Selenium testing provider for the given {@link SeleniumTestingProvider#getIdentifier() identifier}.
     * @param identifier identifier of Selenium testing provider
     * @return Selenium testing provider or {@code null} if not found
     */
    @CheckForNull
    public SeleniumTestingProvider findSeleniumTestingProvider(@NonNull String identifier) {
        Parameters.notNull("identifier", identifier); // NOI18N
        for (SeleniumTestingProvider seleniumTestingProvider : seleniumTestingProviders) {
            if (seleniumTestingProvider.getIdentifier().equals(identifier)) {
                return seleniumTestingProvider;
            }
        }
        return null;
    }

    /**
     * Get selected Selenium testing provider for the given project. Returns {@code null} if none is selected (yet);
     * can display dialog for Selenium testing provider selection if {@code showSelectionPanel} is set to {@code true}.
     * @param project project to be checked
     * @param showSelectionPanel {@code true} for displaying dialog for Selenium testing provider selection, {@code false} otherwise
     * @return selected Selenium testing provider for the given project, can be {@code null} if none selected (yet)
     */
    @CheckForNull
    public SeleniumTestingProvider getSeleniumTestingProvider(@NonNull Project project, boolean showSelectionPanel) {
        Parameters.notNull("project", project); // NOI18N
        for (SeleniumTestingProvider seleniumTestingProvider : seleniumTestingProviders) {
            if (seleniumTestingProvider.isEnabled(project)) {
                // simply returns the first one
                return seleniumTestingProvider;
            }
        }
        // provider not set or found
        if (showSelectionPanel) {
            final SeleniumTestingProvider seleniumTestingProvider = SelectProviderPanel.open();
            if (seleniumTestingProvider != null) {
                seleniumTestingProvider.notifyEnabled(project, true);
                return seleniumTestingProvider;
            }
        }
        return null;
    }

    /**
     * Set given Selenium testing provider for the given project. If there already is any Selenium testing provider
     * and if it is the same as the given one, this method does nothing.
     * @param project project to be configured
     * @param seleniumTestingProvider Selenium testing provider to be set
     */
    public void setSeleniumTestingProvider(@NonNull Project project, @NonNull SeleniumTestingProvider seleniumTestingProvider) {
        Parameters.notNull("project", project); // NOI18N
        Parameters.notNull("seleniumTestingProvider", seleniumTestingProvider); // NOI18N
        SeleniumTestingProvider currentTestingProvider = getSeleniumTestingProvider(project, false);
        if (currentTestingProvider != null) {
            if (currentTestingProvider.getIdentifier().equals(seleniumTestingProvider.getIdentifier())) {
                // already set the same one
                return;
            }
            currentTestingProvider.notifyEnabled(project, false);
        }
        seleniumTestingProvider.notifyEnabled(project, true);
    }

    /**
     * Create project customizer for Selenium testing providers.
     * @return project customizer for Selenium testing providers
     */
    public ProjectCustomizer.CompositeCategoryProvider createCustomizer() {
        return new CompositeCategoryProviderImpl();
    }

    private void initProviders() {
        assert seleniumTestingProviders.isEmpty() : "Empty providers expected but: " + seleniumTestingProviders;
        seleniumTestingProviders.addAll(map(SELENIUM_TESTING_PROVIDERS.allInstances()));
    }

    void reinitProviders() {
        synchronized (seleniumTestingProviders) {
            clearProviders();
            initProviders();
        }
    }

    private void clearProviders() {
        seleniumTestingProviders.clear();
    }

    //~ Mappers

    private Collection<SeleniumTestingProvider> map(Collection<? extends SeleniumTestingProviderImplementation> providers) {
        List<SeleniumTestingProvider> result = new ArrayList<>();
        for (SeleniumTestingProviderImplementation provider : providers) {
            result.add(SeleniumTestingProviderAccessor.getDefault().create(provider));
        }
        return result;
    }

}
