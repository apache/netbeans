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
package org.netbeans.modules.groovy.support.api;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.support.spi.GroovyExtenderImplementation;
import org.openide.util.Parameters;

/**
 * An API providing ability to activate Groovy support in specified {@link Project}.
 * <p>
 * Client can use this interface to activate/deactivate Groovy support or to
 * find out if the support is already active for a certain {@link Project}.
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 *
 * @see GroovyExtenderImplementation
 * @since 1.31
 */
public final class GroovyExtender {

    private GroovyExtender() {
    }

    /**
     * Check if the Groovy support had been already activated for the given {@link Project}.
     *
     * @param project project
     * @return {@code true} if the Groovy support is already active,
     *         {@code false} if the Groovy support is not active yet
     *
     * @since 1.31
     */
    public static boolean isActive(@NonNull Project project) {
        Parameters.notNull("project", project); //NOI18N

        GroovyExtenderImplementation extender = project.getLookup().lookup(GroovyExtenderImplementation.class);
        if (extender != null) {
            return extender.isActive();
        }
        return false;
    }

    /**
     * Activates Groovy support for the given {@link Project}.
     *
     * @param project project
     * @return {@code true} if activation were successful, {@code false} otherwise
     *
     * @since 1.31
     */
    public static boolean activate(@NonNull Project project) {
        Parameters.notNull("project", project); //NOI18N

        GroovyExtenderImplementation extender = project.getLookup().lookup(GroovyExtenderImplementation.class);
        if (extender != null) {
            return extender.activate();
        }
        return false;
    }

    /**
     * Deactivates Groovy support for the given {@link Project}.
     *
     * @param project project
     * @return {@code true} if deactivation were successful, {@code false} otherwise
     *
     * @since 1.31
     */
    public static boolean deactivate(@NonNull Project project) {
        Parameters.notNull("project", project); //NOI18N

        GroovyExtenderImplementation extender = project.getLookup().lookup(GroovyExtenderImplementation.class);
        if (extender != null) {
            return extender.deactivate();
        }
        return false;
    }
}
