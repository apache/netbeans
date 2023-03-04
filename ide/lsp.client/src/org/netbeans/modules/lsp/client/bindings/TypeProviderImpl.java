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
package org.netbeans.modules.lsp.client.bindings;

import java.util.EnumSet;
import java.util.Set;
import javax.swing.Icon;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=TypeProvider.class)
public class TypeProviderImpl extends BaseSymbolProvider implements TypeProvider {

    private static final Set<SymbolKind> TYPE_KINDS = EnumSet.of(
            SymbolKind.Class, SymbolKind.Enum, SymbolKind.Interface,
            SymbolKind.Struct
    );

    @Override
    @Messages("DN_TypeProviderImpl=Language Server Type Provider")
    public String getDisplayName() {
        return Bundle.DN_TypeProviderImpl();
    }

    @Override
    public void computeTypeNames(Context context, Result result) {
        computeSymbolNames(context.getSearchType(),
                           context.getText(),
                           (info, simpleName) -> {
                                SymbolKind kind = info.isLeft() ? info.getLeft().getKind() : info.getRight().getKind();
                                if (TYPE_KINDS.contains(kind)) {
                                    result.addResult(new TypeDescriptorImpl(info, simpleName));
                                }
                           });
    }

    public static class TypeDescriptorImpl extends TypeDescriptor implements BaseSymbolDescriptor {

        private final Either<SymbolInformation, WorkspaceSymbol> info;
        private final String simpleName;

        public TypeDescriptorImpl(Either<SymbolInformation, WorkspaceSymbol> info, String simpleName) {
            this.info = info;
            this.simpleName = simpleName;
        }

        @Override
        public Either<SymbolInformation, WorkspaceSymbol> getInfo() {
            return info;
        }

        @Override
        public String getSimpleName() {
            return simpleName;
        }

        @Override
        public Icon getIcon() {
            return BaseSymbolDescriptor.super.getIcon();
        }

        @Override
        public String getTypeName() {
            return BaseSymbolDescriptor.super.getSymbolName();
        }

        @Override
        public String getProjectName() {
            return BaseSymbolDescriptor.super.getProjectName();
        }

        @Override
        public Icon getProjectIcon() {
            return BaseSymbolDescriptor.super.getProjectIcon();
        }

        @Override
        public FileObject getFileObject() {
            return BaseSymbolDescriptor.super.getFileObject();
        }

        @Override
        public int getOffset() {
            return BaseSymbolDescriptor.super.getOffset();
        }

        @Override
        public void open() {
            BaseSymbolDescriptor.super.open();
        }

        @Override
        public String getOuterName() {
            return null;
        }

        @Override
        public String getContextName() {
            return null;
        }

    }
}
