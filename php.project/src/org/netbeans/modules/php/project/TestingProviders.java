/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
