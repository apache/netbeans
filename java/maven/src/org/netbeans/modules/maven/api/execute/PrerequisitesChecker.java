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
