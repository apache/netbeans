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
package org.netbeans.modules.java.lsp.server.ui;

import java.util.concurrent.CompletableFuture;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.lsp.server.project.BrokenReferencesImpl;
import org.netbeans.spi.project.ui.ProjectProblemsImplementation;

/**
 * Implementation of {@link ProjectProblemsImplementation} implemented using
 * dialogs over LSP.
 * @since 1.19
 * @author sdedic
 */
public abstract class AbstractLspBrokenReferences implements ProjectProblemsImplementation {
    private final BrokenReferencesImpl delegate;

    protected AbstractLspBrokenReferences() {
        delegate = new BrokenReferencesImpl();
    }

    @Override
    public CompletableFuture<Void> showAlert(Project project) {
        return delegate.showAlert(project);
    }

    @Override
    public CompletableFuture<Void> showCustomizer(Project project) {
        return delegate.showCustomizer(project);
    }
}
