/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.api.extexecution.base;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.extexecution.base.ExternalProcessBuilder;
import org.netbeans.modules.extexecution.base.ProcessBuilderAccessor;
import org.netbeans.modules.extexecution.base.ProcessParametersAccessor;
import org.netbeans.spi.extexecution.base.EnvironmentFactory;
import org.netbeans.spi.extexecution.base.EnvironmentImplementation;
import org.netbeans.spi.extexecution.base.ProcessBuilderImplementation;
import org.netbeans.spi.extexecution.base.ProcessParameters;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Abstraction of process builders. You can freely configure the parameters
 * and then create a process by calling the {@link #call()} method. You can
 * also (re)configure the builder and spawn a different process.
 * <p>
 * Note the API does not prescribe the actual meaning of {@link Process}.
 * It may be local process, remote process or some other implementation.
 * <p>
 * You can use the default implementation returned by {@link #getLocal()}
 * for creating the local machine OS processes.
 * <p>
 * <i>Thread safety</i> of this class depends on thread safety of
 * the implementation class.
 * <p>
 * If the {@link ProcessBuilderImplementation} is used and it is thread
 * safe (if possible the implementation should be even stateless) this class
 * is thread safe as well.
 * <p>
 * If the {@link ProcessBuilderImplementation} is used and it is (including
 * {@link EnvironmentImplementation}) thread safe and does not have any mutable
 * configuration accessible via {@link ProcessBuilderImplementation#getLookup()}
 * it is thread safe as well. Otherwise it is not thread safe.
 * <p>
 * The synchronization mechanism used in this object is the {@link ProcessBuilderImplementation}
 * object monitor.
 *
 * @author Petr Hejl
 */
// TODO proxy autoconfiguration optional via lookup
public final class ProcessBuilder implements Callable<Process>, Lookup.Provider {

    private final ProcessBuilderImplementation implementation;

    private final Object lock;

    private final String description;

    /**<i>GuardedBy("lock")</i>*/
    private String executable;

    /**<i>GuardedBy("lock")</i>*/
    private String workingDirectory;

    /**<i>GuardedBy("lock")</i>*/
    private final List<String> arguments = new ArrayList<String>();

    /**<i>GuardedBy("lock")</i>*/
    private boolean redirectErrorStream;

    static {
        ProcessBuilderAccessor.setDefault(new ProcessBuilderAccessor() {

            @Override
            public ProcessBuilder createProcessBuilder(ProcessBuilderImplementation impl, String description) {
                return new ProcessBuilder(impl, description);
            }
        });
    }

    private ProcessBuilder(ProcessBuilderImplementation implementation2, String description) {
        assert implementation2 != null;
        this.implementation = implementation2;
        this.description = description;

        this.lock = implementation2;
    }

    /**
     * Returns the {@link ProcessBuilder} creating the OS process on local
     * machine. Returned implementation is <code>thread safe</code>.
     * The returned builder also attempts to properly configure HTTP proxy
     * for the process.
     *
     * @return the {@link ProcessBuilder} creating the OS process on local
     *             machine
     */
    public static ProcessBuilder getLocal() {
        return new ProcessBuilder(new LocalProcessBuilder(),
                NbBundle.getMessage(ProcessBuilder.class, "LocalProcessBuilder"));
    }

    /**
     * Returns the human readable description of this builder.
     *
     * @return the human readable description of this builder
     */
    @NonNull
    public String getDescription() {
        return description;
    }

    /**
     * Sets the executable to run. There is no default value. The {@link #call()}
     * methods throws {@link IllegalStateException} when there is no executable
     * configured.
     *
     * @param executable the executable to run
     */
    public void setExecutable(@NonNull String executable) {
        Parameters.notNull("executable", executable);

        synchronized (lock) {
            this.executable = executable;
        }
    }

    /**
     * Sets the working directory for the process created by subsequent call
     * of {@link #call()}. The default value is implementation specific.
     *
     * @param workingDirectory the working directory of the process
     */
    public void setWorkingDirectory(@NullAllowed String workingDirectory) {
        synchronized (lock) {
            this.workingDirectory = workingDirectory;
        }
    }

    /**
     * Sets the arguments passed to the process created by subsequent call
     * of {@link #call()}. By default there are no arguments.
     *
     * @param arguments the arguments passed to the process
     */
    public void setArguments(@NonNull List<String> arguments) {
        Parameters.notNull("arguments", arguments);

        synchronized (lock) {
            this.arguments.clear();
            this.arguments.addAll(arguments);
        }
    }

    /**
     * Configures the error stream redirection. If <code>true</code> the error
     * stream of process created by subsequent call of {@link #call()} method
     * will be redirected to standard output stream.
     *
     * @param redirectErrorStream the error stream redirection
     */
    public void setRedirectErrorStream(boolean redirectErrorStream) {
        synchronized (lock) {
            this.redirectErrorStream = redirectErrorStream;
        }
    }

    /**
     * Returns the object for environment variables manipulation.
     *
     * @return the object for environment variables manipulation
     */
    @NonNull
    public Environment getEnvironment() {
        return implementation.getEnvironment();
    }

    /**
     * Returns the associated {@link Lookup}. Extension point provided by
     * {@link ProcessBuilderImplementation}.
     *
     * @return the associated {@link Lookup}.
     * @see ProcessBuilderImplementation#getLookup()
     */
    @Override
    public Lookup getLookup() {
        if (implementation != null) {
            return implementation.getLookup();
        }
        return Lookup.EMPTY;
    }


    /**
     * Creates the new {@link Process} based on the properties configured
     * in this builder.
     * <p>
     * Actual behavior depends on the builder implementation, but it should
     * respect all the properties configured on this builder.
     *
     * @see ProcessBuilderImplementation
     * @return the new {@link Process} based on the properties configured
     *             in this builder
     * @throws IOException if the process could not be created
     * @throws IllegalStateException if there is no executable configured
     *             by {@link #setExecutable(java.lang.String)}
     */
    @NonNull
    @Override
    public Process call() throws IOException {
        String currentExecutable;
        String currentWorkingDirectory;
        List<String> currentArguments = new ArrayList<String>();
        Map<String, String> currentVariables = new HashMap<String, String>();
        boolean currentRedirectErrorStream;

        synchronized (lock) {
            currentExecutable = executable;
            currentWorkingDirectory = workingDirectory;
            currentArguments.addAll(arguments);
            currentRedirectErrorStream = redirectErrorStream;
            currentVariables.putAll(getEnvironment().values());
        }

        if (currentExecutable == null) {
            throw new IllegalStateException("The executable has not been configured");
        }

        ProcessParameters params = ProcessParametersAccessor.getDefault().createProcessParameters(
                currentExecutable, currentWorkingDirectory, currentArguments,
                currentRedirectErrorStream, currentVariables);
        return implementation.createProcess(params);
    }

//    /**
//     * Marks an object from which it is possible to get a {@link ProcessBuilder}.
//     */
//    public static interface Provider {
//
//        /**
//         * Returns the {@link ProcessBuilder} for the object.
//         *
//         * @return the {@link ProcessBuilder} for the object
//         * @throws IOException if there was a problem with the provision
//         */
//        ProcessBuilder getProcessBuilder() throws IOException;
//
//    }

    private static class LocalProcessBuilder implements ProcessBuilderImplementation {

        private final Environment environment = EnvironmentFactory.createEnvironment(
                new LocalEnvironment(this, System.getenv()));

        @Override
        public Environment getEnvironment() {
            return environment;
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public Process createProcess(ProcessParameters parameters) throws IOException {
            ExternalProcessBuilder builder = new ExternalProcessBuilder(parameters.getExecutable());
            String workingDir = parameters.getWorkingDirectory();
            if (workingDir != null) {
                builder = builder.workingDirectory(new File(workingDir));
            }
            for (String argument : parameters.getArguments()) {
                builder = builder.addArgument(argument);
            }
            builder = builder.redirectErrorStream(parameters.isRedirectErrorStream());

            for (Map.Entry<String, String> entry : parameters.getEnvironmentVariables().entrySet()) {
                builder = builder.addEnvironmentVariable(entry.getKey(), entry.getValue());
            }

            return builder.call();
        }
    }

    private static class LocalEnvironment implements EnvironmentImplementation {

        private final LocalProcessBuilder builder;

        private final Map<String, String> systemEnvironment;

        private final String pathName;

        public LocalEnvironment(LocalProcessBuilder builder, Map<String, String> systemEnvironment) {
            this.builder = builder;
            this.systemEnvironment = new HashMap<String, String>(systemEnvironment);
            this.pathName = ExternalProcessBuilder.getPathName(systemEnvironment);
        }

        @Override
        public String getVariable(String name) {
            synchronized (builder) {
                if ("PATH".equals(name.toUpperCase(Locale.ENGLISH))) { // NOI18N
                    return systemEnvironment.get(pathName);
                } else {
                    return systemEnvironment.get(name);
                }
            }
        }

        @Override
        public void appendPath(String name, String value) {
            putPath(name, value, false);
        }

        @Override
        public void prependPath(String name, String value) {
            putPath(name, value, true);
        }

        @Override
        public void setVariable(String name, String value) {
            synchronized (builder) {
                if ("PATH".equals(name.toUpperCase(Locale.ENGLISH))) { // NOI18N
                    systemEnvironment.put(pathName, value);
                } else {
                    systemEnvironment.put(name, value);
                }
            }
        }

        @Override
        public void removeVariable(String name) {
            synchronized (builder) {
                if ("PATH".equals(name.toUpperCase(Locale.ENGLISH))) { // NOI18N
                    systemEnvironment.remove(pathName);
                } else {
                    systemEnvironment.remove(name);
                }
            }
        }

        @Override
        public Map<String, String> values() {
            synchronized (builder) {
                return new HashMap<String, String>(systemEnvironment);
            }
        }

        private void putPath(String name, String value, boolean prepend) {
            synchronized (builder) {
                if ("PATH".equals(name.toUpperCase(Locale.ENGLISH))) { // NOI18N
                    ExternalProcessBuilder.putPath(new File(value), pathName,
                            prepend, systemEnvironment);
                } else {
                    ExternalProcessBuilder.putPath(new File(value), name,
                            prepend, systemEnvironment);
                }
            }
        }
    }
}
