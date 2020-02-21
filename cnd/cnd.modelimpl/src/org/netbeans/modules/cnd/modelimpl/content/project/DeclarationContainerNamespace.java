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

package org.netbeans.modules.cnd.modelimpl.content.project;

import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.modelimpl.repository.NamespaceDeclarationContainerKey;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;

/**
 *
 */
public class DeclarationContainerNamespace extends DeclarationContainer {
    private static final DeclarationContainerNamespace EMPTY = new DeclarationContainerNamespace() {

        @Override
        public void put() {
        }

        @Override
        public void putDeclaration(CsmOffsetableDeclaration decl) {
        }
    };

    public DeclarationContainerNamespace(CsmNamespace ns) {
        super(new NamespaceDeclarationContainerKey(ns));
        put();
    }

    public DeclarationContainerNamespace(RepositoryDataInput input) throws IOException {
        super(input);
    }

    // only for EMPTY static field
    private DeclarationContainerNamespace() {
        super((Key) null);
    }

    public static DeclarationContainerNamespace empty() {
        return EMPTY;
    }
}
