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

package org.netbeans.modules.maven.api.execute;

/**
 * check wheather the given runConfig has a chance to be successful or needs corrective measure.
 * Alternatively can be used for post processing of the RunConfig before it gets
 * executed.
 * Similar to PrerequisitesChecker but this one is called late in the game and
 * is called for every rerun of the action (as triggered from output window for example)
 * @see PrerequisitesChecker
 * @author mkleint
 */
public interface LateBoundPrerequisitesChecker {
    
    /**
     * @param config
     * @return true if the execution shall continue., otherwise it will be aborted.
     */
    boolean checkRunConfig(RunConfig config, ExecutionContext con);

}
