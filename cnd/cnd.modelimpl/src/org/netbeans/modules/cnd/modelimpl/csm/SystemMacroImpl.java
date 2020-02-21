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

package org.netbeans.modules.cnd.modelimpl.csm;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMacroParameter;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.api.model.CsmParameterList;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmIdentifiable;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.Unresolved;
import org.netbeans.modules.cnd.modelimpl.parser.clank.MacroReference;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDProviderIml;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.openide.util.CharSequences;
import org.netbeans.modules.cnd.utils.cache.TextCache;

/**
 * Implementation of system macros and user-defined (in project properties) ones.
 *
 */
public final class SystemMacroImpl implements CsmMacro, CsmIdentifiable {
    
    private final CharSequence macroName;
    private final CharSequence macroBody;
    private final Kind macroKind;
    private final List<CharSequence> params;
    private final CsmFile containingFile;
    private final CsmUID<CsmMacro> uid;

    private SystemMacroImpl(CharSequence macroName, CharSequence macroBody, List<CharSequence> macroParams, CsmFile containingFile, Kind macroKind) {
        this.macroName = NameCache.getManager().getString(macroName);
        this.macroBody = TextCache.getManager().getString(macroBody);
        this.macroKind = macroKind;
        if (macroParams != null) {
            this.params = Collections.unmodifiableList(macroParams);
        } else {
            this.params = null;
        }
        this.containingFile = containingFile;
        if (APTTraceFlags.USE_CLANK) {
            this.uid = new BuiltInMacroUID(UIDs.get(containingFile), macroName);
        } else {
            assert containingFile instanceof Unresolved.UnresolvedFile;
            this.uid = UIDProviderIml.createSelfUID((CsmMacro)this);
        }
    }

    public static SystemMacroImpl create(CharSequence macroName, CharSequence macroBody, List<CharSequence> macroParams, CsmFile containingFile, Kind macroKind) {
        return new SystemMacroImpl(macroName, macroBody, macroParams, containingFile, macroKind);
    }
    
    @Override
    public List<CharSequence> getParameters() {
        return params;
    }

    @Override
    public CharSequence getBody() {
        return macroBody;
    }

    @Override
    public Kind getKind() {
        return macroKind;
    }

    @Override
    public CharSequence getName() {
        return macroName;
    }

    @Override
    public CsmFile getContainingFile() {
        return containingFile;
    }

    @Override
    public int getStartOffset() {
        return 0;
    }

    @Override
    public int getEndOffset() {
        return 0;
    }

    @Override
    public Position getStartPosition() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public Position getEndPosition() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public CharSequence getText() {
        return "#define " + macroName + " " + macroBody; // NOI18N
    }

    @Override
    public boolean equals(Object obj) {
        boolean retValue;
        if (obj == null || !(obj instanceof SystemMacroImpl)) {
            retValue = false;
        } else {
            SystemMacroImpl other = (SystemMacroImpl)obj;
            retValue = CharSequences.comparator().compare(getName(), other.getName()) == 0;
        }
        return retValue;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.macroName != null ? this.macroName.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder retValue = new StringBuilder();
        retValue.append("#define '"); // NOI18N
        retValue.append(getName());
        if (getParameters() != null) {
            retValue.append("["); // NOI18N
            for (Iterator<CharSequence> it = getParameters().iterator(); it.hasNext();) {
                CharSequence param = it.next();
                retValue.append(param);
                if (it.hasNext()) {
                    retValue.append(", "); // NOI18N
                }                
            }
            retValue.append("]"); // NOI18N
        }
        if (getBody().length() > 0) {
            retValue.append("'='"); // NOI18N
            retValue.append(getBody());
        }
        retValue.append("' [").append(macroKind == Kind.USER_SPECIFIED ? "user defined" : "system").append("]"); // NOI18N
        return retValue.toString();
    }

    @Override
    public CsmParameterList<CsmMacroParameter> getParameterList() {
        return null;
    }

    @Override
    public CsmUID<?> getUID() {
        return uid;
    }

    private static class FactoryBasedUID<I,T> implements CsmUID<T>, SelfPersistent {
        private final CsmUID<I> instance;
        private final CharSequence argument;

        protected FactoryBasedUID(CsmUID<I> instance, CharSequence argument) {
            this.instance = instance;
            this.argument = argument;
        }

        @Override
        public T getObject() {
            I object = instance.getObject();
            if (object == null) {
                // could be request from delayed clients like Sema HL over deleted project
                return null;
            }
            FileImpl file = (FileImpl)object;
            CharSequence body = MacroReference.findBody(file, argument);
            if (body == null) {
                // macros is not defined in item properties.
                body = "";
            }
            SystemMacroImpl res = SystemMacroImpl.create(argument, body, null, file, MacroReference.findType(file, argument));
            return (T)res;
        }

        @Override
        public void write(RepositoryDataOutput aStream) throws IOException {
            UIDObjectFactory.getDefaultFactory().writeUID(instance, aStream);
            aStream.writeCharSequenceUTF(argument);
        }

        public FactoryBasedUID(RepositoryDataInput aStream) throws IOException {
            instance = UIDObjectFactory.getDefaultFactory().readUID(aStream);
            argument = aStream.readCharSequenceUTF();
        }
    }

    public static final class BuiltInMacroUID extends FactoryBasedUID<CsmFile, CsmMacro> {

        public BuiltInMacroUID(CsmUID<CsmFile> instance, CharSequence argument) {
            super(instance, argument);
        }

        public BuiltInMacroUID(RepositoryDataInput aStream) throws IOException {
            super(aStream);
        }
    }
}
