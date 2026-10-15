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
package org.netbeans.modules.cloud.oracle.assets;

import java.util.concurrent.CompletableFuture;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Horvath
 */
public abstract class OpenProjectsFinder {
    private static OpenProjectsFinder finder;

    public abstract CompletableFuture<Project[]> findOpenProjects();

    public abstract CompletableFuture<Project[]> findTopLevelProjects();
    
    public static OpenProjectsFinder getDefault() {
        if (finder == null) {
            finder = Lookup.getDefault().lookup(OpenProjectsFinder.class);
        }
        if (finder == null) {
            finder = new DefaultOpenProjectsFinder();
        }
        return finder;
    }


    static class DefaultOpenProjectsFinder extends OpenProjectsFinder {

        @Override
        public CompletableFuture<Project[]> findOpenProjects() {
            return CompletableFuture.completedFuture(new Project[0]);
        }

        @Override
        public CompletableFuture<Project[]> findTopLevelProjects() {
            return CompletableFuture.completedFuture(new Project[0]);
        }

    }
}
