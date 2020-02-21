/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
