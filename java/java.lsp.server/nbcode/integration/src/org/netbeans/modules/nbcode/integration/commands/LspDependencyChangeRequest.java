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
package org.netbeans.modules.nbcode.integration.commands;

import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.xtext.xbase.lib.Pure;
import org.netbeans.modules.project.dependency.DependencyChangeRequest;

/**
 *
 * @author sdedic
 */
public class LspDependencyChangeRequest {
    private String uri;
    private boolean applyChanges;
    private boolean saveFromServer = true;
    private DependencyChangeRequest changes;
    
    public LspDependencyChangeRequest() {
    }

    @Pure
    public boolean isSaveFromServer() {
        return saveFromServer;
    }

    public void setSaveFromServer(boolean saveFromServer) {
        this.saveFromServer = saveFromServer;
    }
    
    @Pure
    public boolean isApplyChanges() {
        return applyChanges;
    }

    public void setApplyChanges(boolean applyChanges) {
        this.applyChanges = applyChanges;
    }

    @Pure
    @NonNull
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Pure
    @NonNull
    public DependencyChangeRequest getChanges() {
        return changes;
    }

    public void setChanges(DependencyChangeRequest changes) {
        this.changes = changes;
    }
}
