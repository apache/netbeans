/*
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

package org.netbeans.modules.maven.api.execute;

/**
 * check wheather the given runConfig has a chance to be successful or needs corrective measure.
 * Usecase would be the netbeans-run-plugin which requires a netbeans-public profile with a
 * jar and assembly configurations..
 * Similar to LateBoundPrerequisitesChecker but this one is called only once per action invokation.
 * Reruns (from output window for example) don't retrigger this.
 * <p>
 * The {@code PrerequisitesChecker} must be registered in <b>Project type's Lookup</b>,
 * and can be registered for a <b>specific packaging type</b>. There's a special packaging pseudo-type <b>_any</b>,
 * that is included for every packaging type, and its services are run after all packaging-specific ones. So the
 * execution happens the following order:
 * <ol>
 * <li>generic services, registered in project type's Lookup. They should establish the defaults suitable
 * for all projects.
 * <li>packaging-specific services. Can override values or provide special values specifically for the packaging type.
 * <li><i>_any</i> services. Provide a fallback if no packaging (or other specific) service created the necessary data. 
 * These are also run for all packaging types.
 * </ol>
 * <p>
 * <div class="nonnormative">
 * Let's have some examples:
 * {@snippet file="org/netbeans/modules/maven/api/execute/RunUtilsTest.java" region="GeneralPrerequisiteChecker"}
 * {@snippet file="org/netbeans/modules/maven/api/execute/RunUtilsTest.java" region="SpecificPrerequisiteChecker"}
 * {@snippet file="org/netbeans/modules/maven/api/execute/RunUtilsTest.java" region="FallbackPrerequisiteChecker"}
 * </div>

* @see LateBoundPrerequisitesChecker
 * @author mkleint
 */
public interface PrerequisitesChecker {
    
    /**
     * @param config
     * @return true if the execution shall continue., otherwise it will be aborted.
     */
    boolean checkRunConfig(RunConfig config);

}
