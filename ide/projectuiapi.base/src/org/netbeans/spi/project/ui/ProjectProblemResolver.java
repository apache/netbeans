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
package org.netbeans.spi.project.ui;

import java.util.concurrent.Future;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;

/**
 * A fix of the {@link ProjectProblemsProvider.ProjectProblem}.
 * As the {@link ProjectProblemResolver} is the only project specific
 * part of the {@link ProjectProblemsProvider.ProjectProblem} it's used
 * by {@link ProjectProblemsProvider.ProjectProblem#hashCode} and
 * {@link ProjectProblemsProvider.ProjectProblem#equals} and therefore
 * it should provide reasonable {@link Object#hashCode} and {@link Object#equals}
 * implementation, for example based on project property which should be fixed.
 *
 * @since 1.60
 * @author Tomas Zezula
 */
public interface ProjectProblemResolver {

    /**
     * Resolves the project problem.
     * Resolves the {@link ProjectProblemsProvider.ProjectProblem}
     * returning the {@link Future} holding the resolution result.
     * The method is called by the Event Dispatch Thread. When the
     * resolution needs to be done by a background thread, eg. downloading
     * an archive from repository, the implementation directly returns
     * a {@link Future} which is completed by the background thread.
     * @return the {@link Future} holding the resolution result.
     */
    Future<ProjectProblemsProvider.Result> resolve();

    /**
     * {@inheritDoc}
     */
    boolean equals(Object other);

    /**
     * {@inheritDoc}
     */
    int hashCode();

}
