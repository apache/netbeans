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

import java.util.List;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.xtext.xbase.lib.Pure;

/**
 *
 * @author sdedic
 */
public class LspDependencyChangeResult {
    private WorkspaceEdit   edit;
    private List<String> modifiedUris;

    @Pure
    public WorkspaceEdit getEdit() {
        return edit;
    }

    public void setEdit(WorkspaceEdit edit) {
        this.edit = edit;
    }

    @Pure
    public List<String> getModifiedUris() {
        return modifiedUris;
    }

    public void setModifiedUris(List<String> modifiedUris) {
        this.modifiedUris = modifiedUris;
    }
}
