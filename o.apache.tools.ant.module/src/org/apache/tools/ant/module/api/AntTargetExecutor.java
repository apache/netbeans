/**
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

package org.apache.tools.ant.module.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.run.TargetExecutor;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.execution.ExecutorTask;
import org.openide.util.NbCollections;
import org.openide.util.Parameters;

/**
 * Executes an Ant target or list of targets asynchronously inside NetBeans.
 * @since 2.15
 */
final public class AntTargetExecutor {

    private final Env env;

    /** Create instance of Ant target executor for the given Ant project.
     */
    private AntTargetExecutor(Env env) {
        this.env = env;
    }
    
    /** Factory method for creation of AntTargetExecutor with the given environment.
     * The factory does not clone Env what means that any change to Env will
     * influence the factory.
     * @param env a configuration for the executor
     * @return an executor which can run projects with the given configuration
     */
    public static AntTargetExecutor createTargetExecutor(Env env) {
        return new AntTargetExecutor(env);
    }
    
    /** Execute given target(s).
     * <p>The {@link AntProjectCookie#getFile} must not be null, since Ant can only
     * run files present on disk.</p>
     * <p>The returned task may be used to wait for completion of the script
     * and check result status.</p>
     * <p class="nonnormative">
     * The easiest way to get the project cookie is to get a <code>DataObject</code>
     * representing an Ant build script and to ask it for this cookie. Alternatively,
     * you may implement the cookie interface directly, where
     * <code>getFile</code> is critical and other methods may do nothing
     * (returning <code>null</code> as needed).
     * While the specification for <code>AntProjectCookie</code> says that
     * <code>getDocument</code> and <code>getParseException</code> cannot
     * both return <code>null</code> simultaneously, the <em>current</em>
     * executor implementation does not care; to be safe, return an
     * {@link UnsupportedOperationException} from <code>getParseException</code>.
     * </p>
     * @param antProject a representation of the project to run
     * @param targets non-empty list of target names to run; may be null to indicate default target
     * @return task for tracking of progress of execution
     * @throws IOException if there is a problem running the script
     */
    public ExecutorTask execute(AntProjectCookie antProject, String[] targets) throws IOException {
        TargetExecutor te = new TargetExecutor(antProject, targets);
        te.setVerbosity(env.getVerbosity());
        te.setProperties(NbCollections.checkedMapByCopy(env.getProperties(), String.class, String.class, true));
        te.setConcealedProperties(env.getConcealedProperties());
        if (env.getLogger() == null) {
            return te.execute();
        } else {
            return te.execute(env.getLogger());
        }
    }

    /** Class describing the environment in which the Ant target will be executed.
     * The class can be used for customization of properties avaialble during the 
     * execution, verbosity of Ant target execution and output stream definition.
     */
    final public static class Env {

        private int verbosity;
        private Properties properties;
        private OutputStream outputStream;
        private volatile Set<String> concealedProperties;

        /** Create instance of Env class describing environment for Ant target execution.
         */
        public Env() {
            verbosity = AntSettings.getVerbosity();
            properties = new Properties();
            properties.putAll(AntSettings.getProperties());
            concealedProperties = Collections.emptySet();
        }

        /**
         * Set verbosity of Ant script execution.
         * @param v the new verbosity (e.g. {@link org.apache.tools.ant.module.spi.AntEvent#LOG_VERBOSE})
         */
        public void setVerbosity(int v) {
            verbosity = v;
        }

        /** Get verbosity of Ant script execution.
         * @return the current verbosity (e.g. {@link org.apache.tools.ant.module.spi.AntEvent#LOG_VERBOSE})
         */
        public int getVerbosity() {
            return verbosity;
        }

        /** Set properties of Ant script execution.
         * @param p a set of name/value pairs passed to Ant (will be cloned)
         */
        public synchronized void setProperties(Properties p) {
            properties = (Properties) p.clone();
        }
        
        /** Get current Ant script execution properties. The clone of
         * real properties is returned.
         * @return the current name/value pairs passed to Ant
         */
        public synchronized Properties getProperties() {
            return (Properties)properties.clone();
        }

        /** Set output stream into which the output of the
         * Ant script execution will be sent. If not set
         * the standard NetBeans output window will be used.
         * @param outputStream a stream to send output to, or <code>null</code> to reset
         * @see org.apache.tools.ant.module.spi.AntOutputStream
         * @deprecated Usage of a custom output stream is not recommended, and prevents some
         *             Ant module features from working correctly.
         */
        @Deprecated
        public void setLogger(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        /** Get output stream. If no output stream was
         * set then null will be returned what means that standard
         * NetBeans output window will be used.
         * @return the output stream to which Ant output will be sent, or <code>null</code>
         */
        public OutputStream getLogger() {
            return outputStream;
        }

        /**
         * Sets names of the properties whose values should not be visible to the user.
         * This method can be used to safely pass password to ant script.
         * @param properties the names of properties to be concealed
         * @since 3.71
         */
        public void setConcealedProperties(@NonNull final Set<? extends String> properties) {
            Parameters.notNull("properties", properties);   //NOI18N
            concealedProperties = Collections.unmodifiableSet(new HashSet<String>(properties));
        }

        /**
         * Returns names of the properties whose values should not be visible to the user.
         * @return the {@link Set} of property names
         * @since 3.71
         */
        @NonNull
        public Set<String> getConcealedProperties() {
            return concealedProperties;
        }
    }
    
}
