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

package org.netbeans.modules.maven.spi.queries;

/**
 * Permits a project to indicate that its JAR might bundle classes from dependencies.
 * Service in project lookup. The lookup merger checks for any "no" response (i.e. default is "yes").
 * @since 2.55
 */
public interface ForeignClassBundler {

    /**
     * Indicates whether the primary artifact from this project is more or less authoritative than sources.
     * @return true if the project's sources are complete, false if the primary artifact includes foreign classes
     * @see org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2.Result#preferSources
     */
    boolean preferSources();
    
    /**
     * typically called by the SourceForBinaryQueryImplementation when project changes to force recalculation of 
     * the <code>preferSources()</code> value in case it depends on some complex state of the maven project.
     * @since 2.55
     */
    void resetCachedValue();

}
