/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
