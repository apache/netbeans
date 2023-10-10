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
package org.netbeans.modules.project.dependency.spi;


import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.project.dependency.DependencyResult;
import org.netbeans.modules.project.dependency.ProjectDependencies;
import org.netbeans.modules.project.dependency.ProjectOperationException;

/**
 *
 * @author sdedic
 */
public interface ProjectDependenciesImplementation {
    // TODO: change to CompletionStage<>, as the implementation is likely to use some dedicated
    // thread to evaluate the project.
    @NonNull
    public DependencyResult findDependencies(@NonNull ProjectDependencies.DependencyQuery query)
            throws ProjectOperationException;
}
