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

package org.netbeans.modules.gradle.javaee;

import org.netbeans.modules.gradle.spi.actions.GradleActionsProvider;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Laszlo Kishalmi
 */
@ServiceProvider(service = GradleActionsProvider.class)
public class JavaEEActionProvider implements GradleActionsProvider {

    @Override
    public boolean isActionEnabled(String action, Project project, Lookup context) {
        return false;
    }

    @Override
    public Set<String> getSupportedActions() {
        return Collections.<String>emptySet();
    }

    @Override
    public InputStream defaultActionMapConfig() {
        return getClass().getResourceAsStream("action-mapping.xml"); //NOI18N
    }
    
}
