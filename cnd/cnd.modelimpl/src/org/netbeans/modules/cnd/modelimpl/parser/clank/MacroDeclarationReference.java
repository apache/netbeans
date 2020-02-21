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
package org.netbeans.modules.cnd.modelimpl.parser.clank;

import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;

/**
 *
 */
final class MacroDeclarationReference extends OffsetableBase implements CsmReference {
    private final CsmMacro referencedMacro;

    MacroDeclarationReference(FileImpl curFile, CsmMacro referencedMacro, int macroNameOffset) {
        super(curFile, macroNameOffset, macroNameOffset + referencedMacro.getName().length());
        this.referencedMacro = referencedMacro;
    }

    @Override
    public CsmObject getReferencedObject() {
        return referencedMacro;
    }

    @Override
    public CsmObject getOwner() {
        return referencedMacro;
    }

    @Override
    public CsmReferenceKind getKind() {
        return CsmReferenceKind.DECLARATION;
    }

    @Override
    public CharSequence getText() {
        return referencedMacro.getName();
    }

    @Override
    public CsmObject getClosestTopLevelObject() {
        return getContainingFile();
    }

}
