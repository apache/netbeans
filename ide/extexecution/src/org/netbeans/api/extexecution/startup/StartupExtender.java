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
package org.netbeans.api.extexecution.startup;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.extexecution.startup.StartupExtenderRegistrationOptions;
import org.netbeans.modules.extexecution.startup.StartupExtenderRegistrationProcessor;
import org.netbeans.spi.extexecution.startup.StartupExtenderImplementation;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;

/**
 * The API class allowing clients, typically server plugins or project,
 * to query process startup extenders.
 *
 * @author Petr Hejl
 * @since 1.30
 * @see StartupExtenderImplementation
 */
public final class StartupExtender {

    private static final Logger LOG = Logger.getLogger(StartupExtender.class.getName());

    private final String description;

    private final List<String> arguments;
    
    private final List<String> rawArguments;

    private StartupExtender(String description, List<String> arguments, List<String> rawArguments) {
        this.description = description;
        this.arguments = arguments;
        this.rawArguments = rawArguments;
    }

    /**
     * Returns all registered {@link StartupExtender}s for the given start mode.
     * <p>
     * The contents of the {@code context} parameter will depend on the kind of
     * execution. For a simple Java SE program being run in the Java launcher,
     * a {@code org.netbeans.api.project.Project} can be expected; where available,
     * a {@code org.netbeans.api.java.platform.JavaPlatform} will be present as well.
     * For a Java EE program being run in an application server,
     * a {@code org.netbeans.api.server.ServerInstance} can be expected in the
     * context. Other kinds of API objects may be present according to contracts
     * not specified here.
     * <p>This method should not be called unless and until the program is really
     * about to be run, i.e. all known preconditions have been satisfied.
     * @param context the lookup providing the contract between client
     *             and provider
     * @param mode the startup mode the client is going to use
     * @return the extenders representing all registered
     *             {@link StartupExtenderImplementation}
     */
    @NonNull
    public static List<StartupExtender> getExtenders(
            @NonNull Lookup context, @NonNull StartMode mode) {
        Parameters.notNull("context", context);
        Parameters.notNull("mode", mode);
        LOG.log(Level.FINE, "getExtenders: context={0} mode={1}", new Object[] {context, mode});

        Lookup lkp = Lookups.forPath(StartupExtenderRegistrationProcessor.PATH);

        List<StartupExtender> res = new ArrayList<StartupExtender>();
        for (Lookup.Item<StartupExtenderImplementation> item : lkp.lookupResult(StartupExtenderImplementation.class).allItems()) {
            StartupExtenderImplementation impl = item.getInstance();
            List<String> args = impl.getArguments(context, mode);
            List<String> rawArgs;
            
            if (!(impl instanceof StartupExtenderRegistrationOptions) ||
                ((StartupExtenderRegistrationOptions)impl).argumentsQuoted()) {
                rawArgs = new ArrayList<>(args.size());
                for (String s : args) {
                    String[] parsed = BaseUtilities.parseParameters(s);
                    rawArgs.add(String.join(" ", parsed));
                }
            } else {
                rawArgs = args;
                List<String> quotedArgs = new ArrayList<>();
                for (String s : args) {
                    if (s.isEmpty()) {
                        quotedArgs.add(s);
                    } else {
                        quotedArgs.add(BaseUtilities.escapeParameters(new String[] { s }));
                    }
                }
                args = quotedArgs;
            }
            StartupExtender extender = new StartupExtender(item.getDisplayName(), args, rawArgs);
            LOG.log(Level.FINE, " {0} => {1}", new Object[] {extender.description, extender.getArguments()});
            res.add(extender);
        }
        return res;
    }

    /**
     * Returns the description of the extender.
     *
     * @return the description of the extender
     */
    @NonNull
    public String getDescription() {
        return description;
    }

    /**
     * The list of arguments.
     *
     * @return list of arguments
     */
    @NonNull
    public List<String> getArguments() {
        return arguments;
    }
    
    /**
     * List of arguments. Items of the list are literal values that should
     * be used by the process, without escaping. They can contain spaces, and any
     * quote, doublequote or backslashes in their literal meaning. It is up to the
     * caller to appropriately quote or escape the values.
     * 
     * @return list of arguments.
     * @since 1.62
     */
    public List<String> getRawArguments() {
        return rawArguments;
    }

    /**
     * Class representing the startup mode of the process.
     */
    public static enum StartMode {

        /**
         * The normal startup mode.
         */
        @NbBundle.Messages("StartMode_Normal=Normal")
        NORMAL(Bundle.StartMode_Normal()),

        /**
         * The debug startup mode.
         */
        @NbBundle.Messages("StartMode_Debug=Debug")
        DEBUG(Bundle.StartMode_Debug()),

        /**
         * The profile startup mode.
         */
        @NbBundle.Messages("StartMode_Profile=Profile")
        PROFILE(Bundle.StartMode_Profile()),

        /**
         * The normal startup mode.
         */
        @NbBundle.Messages("StartMode_Test_Normal=Normal Test")
        TEST_NORMAL(Bundle.StartMode_Test_Normal()),

        /**
         * The debug startup mode.
         */
        @NbBundle.Messages("StartMode_Test_Debug=Debug Test")
        TEST_DEBUG(Bundle.StartMode_Test_Debug()),

        /**
         * The profile startup mode.
         */
        @NbBundle.Messages("StartMode_Test_Profile=Profile Test")
        TEST_PROFILE(Bundle.StartMode_Test_Profile());

        private final String mode;

        private StartMode(String mode) {
           this.mode = mode;
        }

        @Override
        public String toString() {
            return mode;
        }
    }
}
