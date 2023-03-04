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

package org.netbeans.modules.profiler.spi;

import org.netbeans.modules.profiler.spi.LoadGenPlugin.Result;
import org.openide.filesystems.FileObject;
import java.util.Collection;
import java.util.Set;
import org.openide.util.Lookup;


/**
 * Defines an interface for accessing Load Generator features from the profiler UI
 * @author Jaroslav Bachorik
 */
public interface LoadGenPlugin {
    //~ Inner Interfaces ---------------------------------------------------------------------------------------------------------

    public static interface Callback {
        //~ Static fields/initializers -------------------------------------------------------------------------------------------

        /**
         * Null-object implementation
         */
        public static final Callback NULL = new Callback() {
            public void afterStart(Result result) {
                // do nothing
            }

            public void afterStop(Result result) {
                // do nothing
            }
        };


        //~ Methods --------------------------------------------------------------------------------------------------------------

        /**
         * Called after the start() method has been finished
         * @param result Holds the result of the start() method
         */
        void afterStart(Result result);

        /**
         * Called after the stop() method has been finished
         * @param result Holds the result of the stop() method
         */
        void afterStop(Result result);
    }

    //~ Enumerations -------------------------------------------------------------------------------------------------------------

    public static enum Result {//~ Enumeration constant initializers ------------------------------------------------------------------------------------

        FAIL, SUCCESS, TIMEOUT;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * Returns the load generator status
     */
    boolean isRunning();

    /**
     * Retrieves the set of all supported file extensions; depends on the loadgenerator implementations installed in the system
     */
    Set<String> getSupportedExtensions();

    /**
     * Lists all supported loadgen scripts contained in the given project
     * @param project The project to search for scripts
     * @return Returns a list of FileObject instances representing loadgen scripts
     */
    Collection<FileObject> listScripts(Lookup.Provider project);

    /**
     * Runs a given loadgen script
     * @param scriptPath The path to the script to be run
     * @param callback Callback to be called upon finishing the start method; must not be null - us <code>Callback.NULL</code> instead
     */
    void start(String scriptPath, Callback callback);

    /**
     * Stops the last successfuly started load generator process
     */
    void stop();

    /**
     * Stops a load generator process givent the script path that started it
     * @param scriptPath The path to the loadgen script
     */
    void stop(String scriptPath);
}
