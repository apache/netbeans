/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
