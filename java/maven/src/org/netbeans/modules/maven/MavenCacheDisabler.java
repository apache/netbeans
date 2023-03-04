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
package org.netbeans.modules.maven;

import org.openide.modules.OnStart;

import static org.apache.maven.project.DefaultProjectBuilder.DISABLE_GLOBAL_MODEL_CACHE_SYSTEM_PROPERTY;

/**
 * Disable the maven global cache. With the cache enabled updates to the pom or
 * the dependencies are not picked up correctly.
 *
 * @see
 * <a href="https://issues.apache.org/jira/browse/NETBEANS-4341">NETBEANS-4341</a>
 * @see <a href="https://issues.apache.org/jira/browse/MNG-6530">MNG-6530</a>
 */
@OnStart
public class MavenCacheDisabler implements Runnable {

    @Override
    public void run() {
        if(System.getProperty(DISABLE_GLOBAL_MODEL_CACHE_SYSTEM_PROPERTY) == null) {
            System.setProperty(DISABLE_GLOBAL_MODEL_CACHE_SYSTEM_PROPERTY, "true");
        }
    }
}
