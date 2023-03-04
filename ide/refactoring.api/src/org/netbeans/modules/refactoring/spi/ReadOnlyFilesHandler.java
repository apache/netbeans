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

package org.netbeans.modules.refactoring.spi;

import java.util.Collection;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;

/**
 * Interface for factory classes which allows to create Problem, which ProblemDetails
 * can handle read only files. Typically VCS can turn read only files into read write.
 * Implementation of ReadOnlyFilesHandler is required to be registered in Lookup.
 * More then one instance is not allowed to be registered.
 * @author Jan Becicka
 * @since 1.5.0
 */
public interface ReadOnlyFilesHandler {
    
    /**
     * Create a Problem, which ProblemDetails 
     * can handle read only files. Typically VCS can turn read only files into
     * read write.
     * Typical implementation will be following:
     * <pre>
     * new Problem(false,
     *    "Some files needs to be checked out for editing",
     *     ProblemDetailsFactory.createProblemDetails(new VCSDetailsImpl(files))
     * );
     * </pre>
     * @param files Collection of FileObjects
     * @param session current refactoring session
     * @return Problem with ProblemDetails, which can handle read only files.
     * @see ProblemDetailsImplementation
     */
    public Problem createProblem(RefactoringSession session, Collection files); 
}
