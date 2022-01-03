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

package org.netbeans.modules.cnd.spi.model.services;

import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilterBuilder;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.NameAcceptor;

/**
 * SPI used by CsmSelect
 */
public interface CsmSelectProvider {

    public CsmFilterBuilder getFilterBuilder();
    public Iterator<CsmMacro> getMacros(CsmFile file, CsmFilter filter);
    public Iterator<CsmInclude> getIncludes(CsmFile file, CsmFilter filter);
    public boolean hasDeclarations(CsmFile file);
    public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmFile file, CsmFilter filter);
    public Iterator<CsmOffsetableDeclaration> getExternalDeclarations(CsmFile file);
    public Iterator<CsmVariable> getStaticVariables(CsmFile file, CsmFilter filter);
    public Iterator<CsmFunction> getStaticFunctions(CsmFile file, CsmFilter filter);
    public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmNamespace namespace, CsmFilter filter);
    public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmNamespaceDefinition namespace, CsmFilter filter);
    public Iterator<CsmScopeElement> getScopeDeclarations(CsmNamespaceDefinition namespace, CsmFilter filter);
    public Iterator<CsmMember> getClassMembers(CsmClass cls, CsmFilter filter);
    public Iterator<CsmFriend> getClassFriends(CsmClass cls, CsmFilter filter);
    public Iterator<CsmEnumerator> getEnumerators(CsmEnum en, CsmFilter filter);

    public Iterator<CsmUID<CsmFile>> getFileUIDs(CsmProject csmProject, NameAcceptor nameFilter);

}
