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

package org.netbeans.modules.groovy.support.spi;

import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.support.api.GroovyExtender;

/**
 * SPI for activation/deactivation of Groovy support in certain {@link Project}.
 * This enables to change build script in Ant based projects, <i>pom.xml</i> in Maven based projects etc.
 *
 * <p>
 * Projects can provide implementation of this interface in its {@link Project#getLookup lookup}
 * to allow clients to activate/deactivate Groovy support or to find out if the support is
 * already active for a certain {@link Project}.
 *
 * @author Martin Janicek <mjanicek@netbeans.org>
 *
 * @see GroovyExtender
 * @since 1.22
 */
public interface GroovyExtenderImplementation {

    /**
     * Check if groovy has been already activated for the project.
     *
     * @return {@code true} if the Groovy support is already active,
     *         {@code false} if the Groovy support is not active yet
     */
    public boolean isActive();

    /**
     * Activates Groovy support for the project. (e.g. when new Groovy file
     * is created). Implementator should change project configuration with respect
     * to groovy source files (e.g. change the ant build script and use groovyc
     * instead of javac, update pom.xml in maven etc.)
     *
     * @return {@code true} if activation were successful, {@code false} otherwise
     */
    public boolean activate();

    /**
     * Called when groovy is deactivated for a certain project. This is an inverse
     * action to the {@code activate} method. Implementator should make opposite steps
     * in the project configuration (e.g. remove maven-groovy-plugin and related groovy
     * dependencies from pom.xml, change the ant build script to use javac again etc.)
     *
     * @return {@code true} if deactivation were successful, {@code false} otherwise
     */
    public boolean deactivate();
}
