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
package org.netbeans.modules.php.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.api.testing.PhpTesting;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;

/**
 * Handler for testing providers of the project.
 */
public final class TestingProviders implements PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(TestingProviders.class.getName());

    private final PhpProject project;
    private final List<PhpTestingProvider> testingProviders = new CopyOnWriteArrayList<>();

    private volatile boolean providersDirty = true;


    private TestingProviders(PhpProject project) {
        this.project = project;
    }

    public static TestingProviders create(PhpProject project) {
        TestingProviders providers = new TestingProviders(project);
        ProjectPropertiesSupport.addWeakPropertyEvaluatorListener(project, providers);
        return providers;
    }

    public List<PhpTestingProvider> getTestingProviders() {
        synchronized (testingProviders) {
            if (providersDirty) {
                providersDirty = false;
                Set<String> storedTestingProviders = new HashSet<>(new PhpProjectProperties(project).getTestingProviders());
                List<PhpTestingProvider> projectProviders = new ArrayList<>(storedTestingProviders.size());
                for (PhpTestingProvider provider : PhpTesting.getTestingProviders()) {
                    if (storedTestingProviders.contains(provider.getIdentifier())) {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine(String.format("Adding testing provider %s for project %s", provider.getIdentifier(), project.getName()));
                        }
                        projectProviders.add(provider);
                    }
                }
                testingProviders.clear();
                testingProviders.addAll(projectProviders);
            }
        }
        return new ArrayList<>(testingProviders);
    }

    void resetTestingProviders() {
        providersDirty = true;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (propertyName == null
                || PhpProjectProperties.TESTING_PROVIDERS.equals(propertyName)) {
            resetTestingProviders();
        }
    }

}
