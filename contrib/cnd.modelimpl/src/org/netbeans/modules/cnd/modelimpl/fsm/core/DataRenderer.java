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
package org.netbeans.modules.cnd.modelimpl.fsm.core;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.modelimpl.accessors.CsmCorePackageAccessor;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.MutableDeclarationsContainer;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.fsm.DummyParameterImpl;
import org.netbeans.modules.cnd.modelimpl.fsm.DummyParametersListImpl;
import org.netbeans.modules.cnd.modelimpl.fsm.ModuleImpl;
import org.netbeans.modules.cnd.modelimpl.fsm.ProgramImpl;
import org.netbeans.modules.cnd.modelimpl.fsm.SubroutineImpl;
import org.netbeans.modules.cnd.modelimpl.parser.FortranParserEx;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;

/**
 *
 */
public class DataRenderer {

    private final FileImpl file;
    private final FileContent fileContent;

    public DataRenderer(CsmParserProvider.CsmParserParameters params) {
        this.fileContent = CsmCorePackageAccessor.get().getFileContent(params);
        this.file = fileContent.getFile();
    }

    public void render(List<Object> objs) {
        render(objs, (NamespaceImpl) file.getProject().getGlobalNamespace(), fileContent);
    }

    public void render(List<Object> objs, NamespaceImpl currentNamespace, MutableDeclarationsContainer container) {
        if (objs == null) {
            return;
        }
        for (Object object : objs) {
            CsmOffsetableDeclaration decl = render(object, currentNamespace, container);
            container.addDeclaration(decl);
            currentNamespace.addDeclaration(decl);
        }
    }

    public CsmOffsetableDeclaration render(Object object, NamespaceImpl currentNamespace, MutableDeclarationsContainer container) {
        if (object instanceof FortranParserEx.ProgramData) {
            FortranParserEx.ProgramData data = (FortranParserEx.ProgramData) object;
            final ProgramImpl<Object> program = ProgramImpl.create(data.name, file, data.startOffset, data.endOffset, null, currentNamespace);
            for (Object obj : data.members) {
                CsmOffsetableDeclaration decl = render(obj, currentNamespace, container);
                program.addDeclaration(decl);
            }
            return program;
        }
        if (object instanceof FortranParserEx.SubroutineData) {
            FortranParserEx.SubroutineData data = (FortranParserEx.SubroutineData) object;
            return SubroutineImpl.create(data.name, file, data.startOffset, data.endOffset, null, currentNamespace,
                    renderDummyParameters(data));
        }
        if (object instanceof FortranParserEx.ModuleData) {
            FortranParserEx.ModuleData data = (FortranParserEx.ModuleData) object;
            final ModuleImpl module = ModuleImpl.create(file, data.startOffset, data.endOffset, data.name);
            for (Object obj : data.members) {
                CsmOffsetableDeclaration decl = render(obj, currentNamespace, container);
                module.addDeclaration(decl);
            }
            return module;
        }
        return null;
    }

    DummyParametersListImpl renderDummyParameters(FortranParserEx.SubroutineData data) {
        if (data.args != null) {
            List<CsmParameter> list = new ArrayList<>();
            for (String string : data.args) {
                list.add(DummyParameterImpl.create(file, data.startOffset, data.endOffset, string, null));

            }
            return DummyParametersListImpl.create(file, data.startOffset, data.endOffset, list);
        }
        return null;
    }
}
