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
package org.netbeans.modules.java.hints.spiimpl;

import java.util.Collection;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;

/**
 * Creates a {@link RefactoringUI} initialized for the given collection of hints. The returned UI must be suitable for execution 
 * using {@link UI#openRefactoringUI(org.netbeans.modules.refactoring.spi.ui.RefactoringUI)} and its variants.
 * 
 * @author sdedic
 */
public interface HintsRefactoringFactory {
    /**
     * Constructs a refactoring UI for the given collection of hints. The implementation may refuse creation if (any of) the requested
     * hints are not suitable or unsupported by returning {@code null}.
     * @param hintsCollection the hints 
     * @return initialized UI delegate or {@code null}.
     */
    public @CheckForNull RefactoringUI   createRefactoringUI(@NonNull Map<HintMetadata, Collection<? extends HintDescription>> hintsCollection);
}
