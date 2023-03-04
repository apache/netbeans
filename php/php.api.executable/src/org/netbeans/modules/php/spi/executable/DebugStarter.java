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
package org.netbeans.modules.php.spi.executable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.Pair;
import org.openide.util.Parameters;

/**
 * Helper class that allows to start debug session.
 * @since 0.10
 */
public interface DebugStarter {
    /**
     * Start session.
     */
    void start(Project project, Callable<Cancellable> run, Properties properties);

    /**
     * Check whether the session is already running.
     * @return {@code true} if session is already running, {@code false} otherwise
     */
    boolean isAlreadyRunning();

    /**
     * Stop session.
     */
    void stop();

    //~ Inner classes

    /**
     * Properties for starting a debug session.
     */
    public static final class Properties {

        private final FileObject startFile;
        private final boolean closeSession;
        // path mapping (remote, local); strings, never null
        private final List<Pair<String, String>> pathMapping;
        // <host, port> - can be null if not used
        private final Pair<String, Integer> debugProxy;
        private final String encoding;


        private Properties(Builder builder) {
            Parameters.notNull("startFile", builder.startFile);
            Parameters.notNull("pathMapping", builder.pathMapping);
            Parameters.notNull("encoding", builder.encoding);

            this.startFile = builder.startFile;
            this.closeSession = builder.closeSession;
            this.pathMapping = builder.pathMapping;
            this.debugProxy = builder.debugProxy;
            this.encoding = builder.encoding;
        }

        /**
         * Get the start file.
         * @return start file
         */
        public FileObject getStartFile() {
            return startFile;
        }

        /**
         * Get {@code true} if only the start file should be debugged.
         * @return {@code true} if only the start file should be debugged
         */
        public boolean isCloseSession() {
            return closeSession;
        }

        /**
         * Get path mapping, never {@code null}.
         * @return path mapping, can be empty list but never {@code null}
         */
        public List<Pair<String, String>> getPathMapping() {
            return pathMapping;
        }

        /**
         * Get debug proxy, can be {@code null}.
         * @return debug proxy, can be {@code null}
         */
        @CheckForNull
        public Pair<String, Integer> getDebugProxy() {
            return debugProxy;
        }

        /**
         * Get file encoding.
         * @return file encoding.
         */
        public String getEncoding() {
            return encoding;
        }

        //~ Inner classes

        /**
         * Builder for {@link Properties}.
         */
        public static final class Builder {

            private FileObject startFile;
            private boolean closeSession;
            // path mapping (remote, local); strings, never null
            private List<Pair<String, String>> pathMapping = Collections.<Pair<String, String>>emptyList();
            // <host, port> - can be null if not used
            private Pair<String, Integer> debugProxy;
            private String encoding;


            /**
             * Set start file.
             * @param startFile start file to be set
             * @return this instance
             */
            public Builder setStartFile(@NonNull FileObject startFile) {
                this.startFile = startFile;
                return this;
            }

            /**
             * Set {@code true} if the debug session should end after start file.
             * @param closeSession {@code true} if the debug session should end after start file
             * @return this instance
             */
            public Builder setCloseSession(boolean closeSession) {
                this.closeSession = closeSession;
                return this;
            }

            /**
             * Set path mapping.
             * @param pathMapping path mapping to be set
             * @return this instance
             */
            public Builder setPathMapping(@NonNull List<Pair<String, String>> pathMapping) {
                this.pathMapping = pathMapping;
                return this;
            }

            /**
             * Set debug proxy.
             * @param debugProxy debug proxy to be set
             * @return this instance
             */
            public Builder setDebugProxy(@NullAllowed Pair<String, Integer> debugProxy) {
                this.debugProxy = debugProxy;
                return this;
            }

            /**
             * Set encoding.
             * @param encoding encoding to be set
             * @return this instance
             */
            public Builder setEncoding(@NonNull String encoding) {
                this.encoding = encoding;
                return this;
            }

            /**
             * Create instance of Properties.
             * @return Properties instance
             */
            public Properties build() {
                return new Properties(this);
            }

        }

    }

}
