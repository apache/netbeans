/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
