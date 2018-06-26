/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javaee.specs.support.bridge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.moduleinfo.JPAModuleInfo;
import org.netbeans.modules.javaee.specs.support.api.JpaProvider;
import org.netbeans.modules.javaee.specs.support.spi.JpaProviderFactory;
import org.netbeans.modules.javaee.specs.support.spi.JpaSupportImplementation;

/**
 *
 * @author Petr Hejl
 */
public class BridgingJpaSupportImpl implements JpaSupportImplementation {

    private final J2eePlatform platform;

    public BridgingJpaSupportImpl(J2eePlatform platform) {
        this.platform = platform;
    }

    @Override
    public JpaProvider getDefaultProvider() {
        for (JpaProvider provider : getProviders()) {
            if (provider.isDefault()) {
                return provider;
            }
        }
        return null;
    }

    @Override
    public Set<JpaProvider> getProviders() {
        Set<JpaProvider> result = new HashSet<JpaProvider>();
        boolean check = platform.isToolSupported(JPAModuleInfo.JPACHECKSUPPORTED);
        boolean jpa1 = !check
                || platform.isToolSupported(JPAModuleInfo.JPAVERSIONPREFIX + Persistence.VERSION_1_0);
        boolean jpa2 = !check
                || platform.isToolSupported(JPAModuleInfo.JPAVERSIONPREFIX + Persistence.VERSION_2_0);
        boolean jpa21 = !check
                || platform.isToolSupported(JPAModuleInfo.JPAVERSIONPREFIX + Persistence.VERSION_2_1);
        
        for (Map.Entry<Provider, String> entry : getPossibleContainerProviders().entrySet()) {
            Provider provider = entry.getKey();
            if (platform.isToolSupported(provider.getProviderClass())) {
                JpaProvider jpaProvider = JpaProviderFactory.createJpaProvider(
                        provider.getProviderClass(), platform.isToolSupported(entry.getValue()), jpa1, jpa2, jpa21);
                result.add(jpaProvider);
            }
        }
        return result;
    }
    
    private static Map<Provider, String> getPossibleContainerProviders() {
        Map<Provider, String> candidates = new HashMap<Provider, String>();
        candidates.put(ProviderUtil.HIBERNATE_PROVIDER, "hibernatePersistenceProviderIsDefault1.0"); // NOI18N
        candidates.put(ProviderUtil.HIBERNATE_PROVIDER2_0, "hibernatePersistenceProviderIsDefault2.0"); // NOI18N
        candidates.put(ProviderUtil.HIBERNATE_PROVIDER2_1, "hibernatePersistenceProviderIsDefault2.1"); // NOI18N
        candidates.put(ProviderUtil.TOPLINK_PROVIDER1_0, "toplinkPersistenceProviderIsDefault"); // NOI18N
        candidates.put(ProviderUtil.KODO_PROVIDER, "kodoPersistenceProviderIsDefault"); // NOI18N
        candidates.put(ProviderUtil.DATANUCLEUS_PROVIDER, "dataNucleusPersistenceProviderIsDefault"); // NOI18N
        candidates.put(ProviderUtil.OPENJPA_PROVIDER, "openJpaPersistenceProviderIsDefault2.0"); // NOI18N
        candidates.put(ProviderUtil.OPENJPA_PROVIDER1_0, "openJpaPersistenceProviderIsDefault1.0"); // NOI18N
        candidates.put(ProviderUtil.ECLIPSELINK_PROVIDER1_0, "eclipseLinkPersistenceProviderIsDefault1.0"); // NOI18N
        candidates.put(ProviderUtil.ECLIPSELINK_PROVIDER2_0, "eclipseLinkPersistenceProviderIsDefault2.0"); // NOI18N
        candidates.put(ProviderUtil.ECLIPSELINK_PROVIDER, "eclipseLinkPersistenceProviderIsDefault2.1"); // NOI18N
        return candidates;
    }
}
