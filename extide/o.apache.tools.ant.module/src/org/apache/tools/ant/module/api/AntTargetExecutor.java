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

package org.apache.tools.ant.module.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.tools.ant.module.AntSettings;
import org.apache.tools.ant.module.run.TargetExecutor;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.execution.ExecutorTask;
import org.openide.util.NbCollections;
import org.openide.util.Parameters;

/**
 * Executes an Ant target or list of targets asynchronously inside NetBeans.
 * @since 2.15
 */
public final class AntTargetExecutor {

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
        if (env.shouldSaveAllDocs != null) {
            te.setSaveAllDocuments(env.shouldSaveAllDocs);
        }
        te.setDisplayName(env.preferredName);
        if (env.canReplace != null) {
            assert env.canBeReplaced != null;
            te.setTabReplaceStrategy(env.canReplace,env.canBeReplaced);
        }
        te.setUserAction(env.userAction);
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
    public static final class Env {

        private int verbosity;
        private Properties properties;
        private OutputStream outputStream;
        private volatile Set<String> concealedProperties;
        private Boolean shouldSaveAllDocs;
        private String preferredName;
        private Predicate<String> canReplace;
        private Predicate<String> canBeReplaced;
        private boolean userAction;

        /** Create instance of Env class describing environment for Ant target execution.
         */
        public Env() {
            verbosity = AntSettings.getVerbosity();
            properties = new Properties();
            properties.putAll(AntSettings.getProperties());
            concealedProperties = Collections.emptySet();
            userAction = true;
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

        /**
         * Overrides the default save all behavior.
         * @param shouldSave if true all modified documents are saved before running Ant.
         * @since 3.84
         */
        public void setSaveAllDocuments(final boolean shouldSave) {
            this.shouldSaveAllDocs = shouldSave;
        }

        /**
         * Sets the preferred name for output windows.
         * @param name the preferred name in case of null the name is assigned automatically
         * @since 3.84
         */
        public void setPreferredName(@NullAllowed final String name) {
            this.preferredName = name;
        }

        /**
         * Sets the output tab replacement strategy.
         * When the IDE is set to the automatic close tabs mode the tabs created by the previous
         * run of the {@link AntTargetExecutor} are closed by successive run. This behavior can be overridden
         * by this method.
         * @param canReplace the {@link Predicate} used to decide if this execution
         * can replace existing tab. The predicate parameter is a name of tab being replaced.
         * @param canBeReplaced the {@link Predicate} used to decide if tab can be
         * replaced by a new execution. The predicate parameter is a name of a tab being created.
         * @since 3.84
         */
        public void setTabReplaceStrategy(
                @NonNull final Predicate<String> canReplace,
            @NonNull final Predicate<String> canBeReplaced) {
            Parameters.notNull("canReplace", canReplace);   //NOI18N
            Parameters.notNull("canBeReplaced", canBeReplaced); //NOI18N
            this.canReplace = canReplace;
            this.canBeReplaced = canBeReplaced;
        }

        /**
         * Marks the execution as an user action.
         * The executions marked as user actions are registered in the
         * UI support {@link org.netbeans.spi.project.ui.support.BuildExecutionSupport}.
         * By default the execution is an user action.
         * @param userAction if true the execution is registered into
         * the {@link org.netbeans.spi.project.ui.support.BuildExecutionSupport}
         * @since 3.85
         */
        public void setUserAction(final boolean userAction) {
            this.userAction = userAction;
        }
    }
}
