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

package org.netbeans.modules.javaee.project.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.provider.PersistenceProviderSupplier;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport;
import org.netbeans.modules.javaee.specs.support.api.JpaProvider;
import org.netbeans.modules.javaee.specs.support.api.JpaSupport;

/**
 * Common implementation of {@link PersistenceProviderSupplier}. Any project type
 * which need to use {@link PersistenceProviderSupplier} should either put the
 * instance to the project lookup or better subclass this one and use
 * {@link org.netbeans.spi.project.ProjectServiceProvider} for correct registration.
 *
 * @author Martin Janicek
 */
public final class PersistenceProviderSupplierImpl implements PersistenceProviderSupplier {

    private final Project project;


    public PersistenceProviderSupplierImpl(Project project) {
        this.project = project;
    }

    @Override
    public List<Provider> getSupportedProviders() {
        try {
            J2eeModuleProvider j2eeModuleProvider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
            String serverInstanceId = j2eeModuleProvider.getServerInstanceID();
            ServerInstance si = serverInstanceId != null ? Deployment.getDefault().getServerInstance(serverInstanceId) : null;
            J2eePlatform platform = null;
            if (si != null) {
                platform = si.getJ2eePlatform();
            }
            return findPersistenceProviders(platform);
        } catch (InstanceRemovedException ex) {
            return findPersistenceProviders(null);
        }
    }

    private List<Provider> findPersistenceProviders(J2eePlatform platform) {
        final List<Provider> providers = new ArrayList<Provider>();
        boolean lessEE7 = true;//we may not know platform
        if(platform != null) {
            final Map<String, JpaProvider> jpaProviderMap = createProviderMap(platform);

            boolean defaultFound = false; // see issue #225071

            lessEE7 = !platform.getSupportedProfiles().contains(Profile.JAVA_EE_7_WEB) && !platform.getSupportedProfiles().contains(Profile.JAVA_EE_7_FULL);//we know gf4 do not support old providers, #233726
            // Here we are mapping the JpaProvider to the correct Provider
            for (Provider provider : ProviderUtil.getAllProviders()) {

                // Find JpaProvider for corespond Provider --> we are using concrete class for that
                JpaProvider jpa = jpaProviderMap.get(provider.getProviderClass());
                if (jpa != null) {
                    String version = ProviderUtil.getVersion(provider);
                    if (version == null
                            || (version.equals(Persistence.VERSION_2_1) && jpa.isJpa21Supported())
                            || (version.equals(Persistence.VERSION_2_0) && jpa.isJpa2Supported() && lessEE7)
                            || (version.equals(Persistence.VERSION_1_0) && jpa.isJpa1Supported()) && lessEE7) {

                        if (jpa.isDefault() && !defaultFound) {
                            providers.add(0, provider);
                            defaultFound = true;
                        } else {
                            providers.add(provider);
                        }
                    }
                }
            }
        }
        for (Provider each : PersistenceLibrarySupport.getProvidersFromLibraries()){
            boolean found = false;
            for (int i = 0; i < providers.size(); i++) {
                Object elem = providers.get(i);
                if (elem instanceof Provider && each.equals(elem)){
                    found = true;
                    break;
                }
            }
            if (!found){
                String version = ProviderUtil.getVersion(each);
                if(lessEE7 || version == null || version.equals(Persistence.VERSION_2_1)) {//we know gf4 do not support old providers, #233726, todo, we need to get supported from gf plugin instead
                    providers.add(each);
                }
            }
        }
        return providers;
    }

    private Map<String, JpaProvider> createProviderMap(J2eePlatform platform) {
        final JpaSupport jpaSupport = JpaSupport.getInstance(platform);
        final Map<String, JpaProvider> providerMap = new HashMap<String, JpaProvider>();

        for (JpaProvider provider : jpaSupport.getProviders()) {
            providerMap.put(provider.getClassName(), provider);
        }

        JpaProvider defaultProvider = jpaSupport.getDefaultProvider();
        if (defaultProvider != null) {
            providerMap.put(defaultProvider.getClassName(), defaultProvider);
        }

        return providerMap;
    }

    @Override
    public boolean supportsDefaultProvider() {
        final J2eeProjectCapabilities capabilities = J2eeProjectCapabilities.forProject(project);
        if (capabilities != null) {
            return capabilities.hasDefaultPersistenceProvider();
        }
        return false;
    }
}
