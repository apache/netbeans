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

package org.netbeans.modules.maven.api.output;

import java.util.Set;
import org.netbeans.api.project.Project;

/**
 * Factory of the OutputProcessors for given project, each build
 * asks this method again. Factory classes should be registered in
 * default <code>Lookup</code> (see {@link org.openide.util.lookup.ServiceProvider}).
 * @author  Milos Kleint 
 */

public interface OutputProcessorFactory {
    /**
     * @param project the project associated with the output, can be null.
     * returns a Set of <code>OutputProcessor</code> instances or empty set, never null.
     *
     */
    Set<? extends OutputProcessor> createProcessorsSet(Project project);
}
