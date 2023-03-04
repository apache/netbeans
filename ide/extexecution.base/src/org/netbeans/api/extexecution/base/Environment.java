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
package org.netbeans.api.extexecution.base;

import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.extexecution.base.EnvironmentAccessor;
import org.netbeans.spi.extexecution.base.EnvironmentImplementation;
import org.openide.util.Parameters;

/**
 * The class that provides an access to environment variables.
 *
 * @see ProcessBuilder#getEnvironment()
 * @see EnvironmentImplementation
 * @author Petr Hejl
 */
public final class Environment {

    private final EnvironmentImplementation implementation;

    static {
        EnvironmentAccessor.setDefault(new EnvironmentAccessor() {

            @Override
            public Environment createEnvironment(EnvironmentImplementation impl) {
                return new Environment(impl);
            }
        });
    }

    private Environment(EnvironmentImplementation implementation) {
        this.implementation = implementation;
    }

    /**
     * Returns the value of the variable or <code>null</code>.
     *
     * @param name the name of the variable
     * @return the value of the variable or <code>null</code>
     */
    @CheckForNull
    public String getVariable(@NonNull String name) {
        Parameters.notNull("name", name);

        return implementation.getVariable(name);
    }

    /**
     * Appends a path to a path-like variable. The proper path separator is used
     * to separate the new value.
     *
     * @param name the name of the variable such as for example
     *             <code>PATH</code> or <code>LD_LIBRARY_PATH</code>
     * @param value the value (path to append)
     */
    public void appendPath(@NonNull String name, @NonNull String value) {
        Parameters.notNull("name", name);
        Parameters.notNull("value", value);

        implementation.appendPath(name, value);
    }

    /**
     * Prepends a path to a path-like variable. The proper path separator is used
     * to separate the new value.
     *
     * @param name the name of the variable such as for example
     *             <code>PATH</code> or <code>LD_LIBRARY_PATH</code>
     * @param value the value (path to prepend)
     */
    public void prependPath(@NonNull String name, @NonNull String value) {
        Parameters.notNull("name", name);
        Parameters.notNull("value", value);

        implementation.prependPath(name, value);
    }

    /**
     * Sets a value for a variable with the given name.
     *
     * @param name the name of the variable
     * @param value the value
     */
    public void setVariable(@NonNull String name, @NonNull String value) {
        Parameters.notNull("name", name);
        Parameters.notNull("value", value);

        implementation.setVariable(name, value);
    }

    /**
     * Removes a variable with the given name. The subsequent call to
     * {@link #getVariable(java.lang.String)} with the same argument will return
     * <code>null</code>.
     *
     * @param name the name of the variable
     */
    public void removeVariable(@NonNull String name) {
        Parameters.notNull("name", name);

        implementation.removeVariable(name);
    }

    /**
     * Returns all variable names and associated values as a {@link Map}.
     * Changes to the map are not propagated back to the {@link Environment}.
     *
     * @return all variable names and associated values
     */
    @NonNull
    public Map<String, String> values() {
        return implementation.values();
    }
}
