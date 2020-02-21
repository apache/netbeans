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

package org.netbeans.modules.cnd.modelimpl.csm.deep;


import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;

import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;

import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase.ScopedDeclarationBuilder;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * Common ancestor for all statements
 */
public abstract class StatementBase extends OffsetableBase implements CsmStatement {

    private CsmScope scopeRef;
    private CsmUID<CsmScope> scopeUID;
    protected final int macroStartMarker;

    protected StatementBase(AST ast, CsmFile file, CsmScope scope) {
        this(ast, file, getStartOffset(ast), getEndOffset(ast), getMacroStartMarker(ast), scope);
    }

    protected StatementBase(CsmFile file, int start, int end, CsmScope scope) {
        this(null, file, start, end, 0, scope);
    }
    
    protected StatementBase(CsmFile file, int start, int end, int macroStartMarker, CsmScope scope) {
        this(null, file, start, end, macroStartMarker, scope);
    }

    private StatementBase(AST ast, CsmFile file, int start, int end, int macroStartMarker, CsmScope scope) {
        super(file, start, end);
        if (scope != null) {
            setScope(scope);
        }
        this.macroStartMarker = macroStartMarker;
    }

    @Override
    public synchronized CsmScope getScope() {
        CsmScope scope = this.scopeRef;
        if (scope == null) {
            scope = UIDCsmConverter.UIDtoScope(this.scopeUID);
            // this is possible situation when scope is already invalidated (see IZ#154264)
            // assert (scope != null || this.scopeUID == null) : "null object for UID " + this.scopeUID;
        }
        return scope;
    }

    protected final void setScope(CsmScope scope) {
	// within bodies scope is a statement - it is not Identifiable
        if (scope instanceof CsmIdentifiable) {
            this.scopeUID = UIDCsmConverter.scopeToUID(scope);
            assert scopeUID != null;
        } else {
            this.scopeRef = scope;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        onDispose();
    }

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        UIDObjectFactory.getDefaultFactory().writeUID(this.scopeUID, output);
        output.writeInt(macroStartMarker);
    }

    protected StatementBase(RepositoryDataInput input) throws IOException {
        super(input);
        this.scopeUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        this.macroStartMarker = input.readInt();
    }

    public interface StatementBuilderContainer {
        public void addStatementBuilder(StatementBuilder builder);
    }

    public static abstract class StatementBuilder extends ScopedDeclarationBuilder {
        abstract StatementBase create();
    }

    @Override
    public String toString() {
        return "" + getKind() + ' ' + getOffsetString(); // NOI18N
    }

    private synchronized void onDispose() {
        // restore scope from it's UID
        if (this.scopeRef == null) {
            this.scopeRef = UIDCsmConverter.UIDtoScope(scopeUID);
            assert this.scopeRef != null : "no object for UID " + scopeUID;
        }
    }
}
