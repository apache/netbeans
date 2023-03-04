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

package org.netbeans.spi.java.queries;

import java.net.URL;
import org.netbeans.api.java.queries.SourceForBinaryQuery;

/**
 * Information about where Java sources corresponding to binaries
 * (classfiles) can be found. 
 * <p>
 * In addition to the original SourceForBinaryQueryImplementation this interface
 * also provides information used by the java infrastructure if sources should be
 * preferred over the binaries. When sources are preferred the java infrastructure
 * will use sources as a primary source of the metadata otherwise the binaries
 * (classfiles) are used as a primary source of information and sources are used
 * as a source of formal parameter names and javadoc only.
 * In general sources should be preferred for projects which are user editable
 * but not for libraries or platforms where the sources may not be complete or 
 * up to date.
 * </p>
 * @see org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation
 * @since org.netbeans.api.java/1 1.15
 */
public interface SourceForBinaryQueryImplementation2 extends SourceForBinaryQueryImplementation {

    /**
     * Returns the source root(s) for a given binary root.
     * @see SourceForBinaryQueryImplementation#findSourceRoots(java.net.URL) 
     * @param binaryRoot the class path root of Java class files
     * @return a result object encapsulating the answer or null if the binaryRoot is not recognized
     */
    public Result findSourceRoots2 (final URL binaryRoot);
    
    public static interface Result extends SourceForBinaryQuery.Result {
        
        /**
         * When true the java model prefers sources otherwise binaries are used.
         * Project's {@link SourceForBinaryQueryImplementation} should return
         * true. The platform and libraries {@link SourceForBinaryQueryImplementation}
         * should return false - the attached sources may not be complete.
         * @see SourceForBinaryQueryImplementation2
         * @return true if sources should be used by the java infrastructure
         */
        public boolean preferSources();
    }
}
