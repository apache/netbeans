/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.spi.extexecution.base;

import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.extexecution.base.ProcessParametersAccessor;

/**
 * The parameters configured for process creation.
 *
 * @see ProcessBuilderImplementation
 * @author Petr Hejl
 */
public final class ProcessParameters {

    private final String executable;

    private final String workingDirectory;

    private final List<String> arguments;

    private final boolean redirectErrorStream;

    private final Map<String, String> environmentVariables;

    static {
        ProcessParametersAccessor.setDefault(new ProcessParametersAccessor() {

            @Override
            public ProcessParameters createProcessParameters(String executable, String workingDirectory,
                    List<String> arguments, boolean redirectErrorStream, Map<String, String> environmentVariables) {
                return new ProcessParameters(executable, workingDirectory, arguments,
                        redirectErrorStream, environmentVariables);
            }
        });
    }

    private ProcessParameters(String executable, String workingDirectory, List<String> arguments,
            boolean redirectErrorStream, Map<String, String> environmentVariables) {
        this.executable = executable;
        this.workingDirectory = workingDirectory;
        this.arguments = arguments;
        this.redirectErrorStream = redirectErrorStream;
        this.environmentVariables = environmentVariables;
    }

    /**
     * Returns the configured executable.
     *
     * @return the configured executable
     */
    @NonNull
    public String getExecutable() {
        return executable;
    }

    /**
     * Returns the configured working directory or <code>null</code> in case it
     * was not configured.
     *
     * @return the configured working directory or <code>null</code> in case it
     *             was not configured
     */
    @CheckForNull
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Returns the arguments configured for the process.
     *
     * @return the arguments configured for the process
     */
    @NonNull
    public List<String> getArguments() {
        return arguments;
    }

    /**
     * Returns <code>true</code> if standard error stream should be redirected
     * to standard output stream.
     *
     * @return <code>true</code> if standard error stream should be redirected
     *             to standard output stream
     */
    public boolean isRedirectErrorStream() {
        return redirectErrorStream;
    }

    /**
     * Returns the environment variables configured for the process.
     *
     * @return the environment variables configured for the process
     */
    @NonNull
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }
}
