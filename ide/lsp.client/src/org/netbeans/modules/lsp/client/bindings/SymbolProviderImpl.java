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
package org.netbeans.modules.lsp.client.bindings;

import javax.swing.Icon;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.netbeans.spi.jumpto.symbol.SymbolProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=SymbolProvider.class)
public class SymbolProviderImpl extends BaseSymbolProvider implements SymbolProvider {

    @Override
    @Messages("DN_getDisplayName=Language Server Symbol Provider")
    public String getDisplayName() {
        return Bundle.DN_Symbols();
    }

    @Override
    public void computeSymbolNames(Context context, Result result) {
        computeSymbolNames(context.getSearchType(), context.getText(), (info, simpleName) -> result.addResult(new SymbolDescriptorImpl(info, simpleName)));
    }

    public static class SymbolDescriptorImpl extends SymbolDescriptor implements BaseSymbolDescriptor {

        private final Either<SymbolInformation, WorkspaceSymbol> info;
        private final String simpleName;

        public SymbolDescriptorImpl(Either<SymbolInformation, WorkspaceSymbol> info, String simpleName) {
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
        public String getSymbolName() {
            return BaseSymbolDescriptor.super.getSymbolName();
        }

        @Override
        public String getOwnerName() {
            return BaseSymbolDescriptor.super.getOwnerName();
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

    }
}
