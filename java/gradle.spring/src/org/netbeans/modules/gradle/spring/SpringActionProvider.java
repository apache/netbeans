/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.gradle.spring;

import org.netbeans.modules.gradle.spi.actions.DefaultGradleActionsProvider;
import org.netbeans.modules.gradle.spi.actions.GradleActionsProvider;
import org.openide.util.lookup.ServiceProvider;
import static org.netbeans.spi.project.ActionProvider.*;

/**
 * Provides run and debug action for spring boot apps out-of-the-box.
 * @author lkishalmi
 */
@ServiceProvider(service = GradleActionsProvider.class)
public class SpringActionProvider extends DefaultGradleActionsProvider {
    private static final String[] SUPPORTED = new String[]{
        COMMAND_RUN,
        COMMAND_DEBUG,
    };

    public SpringActionProvider() {
        super(SUPPORTED);
    }

}
