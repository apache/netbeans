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

package org.netbeans.modules.javaee.project.api.ant.ui.logicalview;

import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

/**
 * AbstractLogicalViewProvider enhanced with initialization state.
 *
 * @since 1.54
 * @author Petr Hejl
 */
public abstract class AbstractLogicalViewProvider2 extends AbstractLogicalViewProvider {

    private volatile boolean initialized;

    protected AbstractLogicalViewProvider2(Project project, UpdateHelper helper,
            PropertyEvaluator evaluator, ReferenceHelper resolver, J2eeModuleProvider j2eeModuleProvider) {
        super(project, helper, evaluator, resolver, j2eeModuleProvider);
    }

    public void initialize() {
        initialized = true;
    }

    @Override
    protected boolean isInitialized() {
        return super.isInitialized() && initialized;
    }
}
