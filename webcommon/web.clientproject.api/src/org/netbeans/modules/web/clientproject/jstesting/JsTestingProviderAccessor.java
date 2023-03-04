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

package org.netbeans.modules.web.clientproject.jstesting;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProvider;
import org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation;
import org.netbeans.modules.web.clientproject.spi.jstesting.JsTestingProviderImplementation;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;

public abstract class JsTestingProviderAccessor {

    private static volatile JsTestingProviderAccessor accessor;


    public static synchronized JsTestingProviderAccessor getDefault() {
        if (accessor != null) {
            return accessor;
        }
        Class<?> c = JsTestingProvider.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            assert false : ex;
        }
        assert accessor != null;
        return accessor;
    }

    public static void setDefault(JsTestingProviderAccessor accessor) {
        if (JsTestingProviderAccessor.accessor != null) {
            throw new IllegalStateException("Already initialized accessor");
        }
        JsTestingProviderAccessor.accessor = accessor;
    }

    public abstract JsTestingProvider create(JsTestingProviderImplementation jsTestingProviderImplementation);

    public abstract boolean isEnabled(JsTestingProvider jsTestingProvider, Project project);

    public abstract void notifyEnabled(JsTestingProvider jsTestingProvider, Project project, boolean enabled);

    @CheckForNull
    public abstract NodeList<Node> createNodeList(JsTestingProvider jsTestingProvider, Project project);

    @CheckForNull
    public abstract CustomizerPanelImplementation createCustomizerPanel(JsTestingProvider jsTestingProvider, @NonNull Project project);

}
