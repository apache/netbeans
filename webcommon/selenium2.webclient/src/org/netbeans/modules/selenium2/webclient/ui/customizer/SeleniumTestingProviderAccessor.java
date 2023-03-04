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
package org.netbeans.modules.selenium2.webclient.ui.customizer;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.selenium2.webclient.api.SeleniumTestingProvider;
import org.netbeans.modules.selenium2.webclient.spi.SeleniumTestingProviderImplementation;
import org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation;

/**
 *
 * @author Theofanis Oikonomou
 */
public abstract class SeleniumTestingProviderAccessor {

    private static volatile SeleniumTestingProviderAccessor accessor;


    public static synchronized SeleniumTestingProviderAccessor getDefault() {
        if (accessor != null) {
            return accessor;
        }
        Class<?> c = SeleniumTestingProvider.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            assert false : ex;
        }
        assert accessor != null;
        return accessor;
    }

    public static void setDefault(SeleniumTestingProviderAccessor accessor) {
        if (SeleniumTestingProviderAccessor.accessor != null) {
            throw new IllegalStateException("Already initialized accessor");
        }
        SeleniumTestingProviderAccessor.accessor = accessor;
    }

    public abstract SeleniumTestingProvider create(SeleniumTestingProviderImplementation jsTestingProviderImplementation);

    public abstract boolean isEnabled(SeleniumTestingProvider jsTestingProvider, Project project);

    public abstract void notifyEnabled(SeleniumTestingProvider jsTestingProvider, Project project, boolean enabled);

    @CheckForNull
    public abstract CustomizerPanelImplementation createCustomizerPanel(SeleniumTestingProvider jsTestingProvider, @NonNull Project project);
    
}
