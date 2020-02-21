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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.csm.CsmObjectBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.NameHolder;
import org.netbeans.modules.cnd.modelimpl.csm.SpecializationDescriptor;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDProviderIml;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.CharSequences;

/**
 * class to present object that has unique ID and is offsetable
 * unique ID is used to long-time stored references on Csm Objects
 * 
 * @see CsmUID
 */
public abstract class OffsetableIdentifiableBase<T> extends OffsetableBase implements CsmIdentifiable, Persistent, SelfPersistent {
    
    private CsmUID<?> uid = null;

    protected OffsetableIdentifiableBase(CsmFile containingFile, int startOffset, int endOffset) {
        super(containingFile, startOffset, endOffset);
    }

    protected abstract CsmUID<?> createUID();

    @Override
    public void dispose() {
        RepositoryUtils.disposeUID(uid, this);
        super.dispose();
    }

    @SuppressWarnings("unchecked")
    @Override
    public CsmUID<T> getUID() {
        if (uid == null) {
            uid = createUID();
        }
        return (CsmUID<T>) uid;
    }

    protected final void setSelfUID() {
        if (uid != null) {
            if (UIDProviderIml.isSelfUID(uid)) {
                return;
            }
            new Exception("replacing " + uid + " to self UID").printStackTrace(); // NOI18N
        }
        uid = UIDProviderIml.createSelfUID(this);
    }

    protected void cleanUID() {
        // this.uid = null;
    }

    public static class NameBuilder extends OffsetableBuilder {
        
        boolean global = false;
        final List<NamePart> nameParts = new ArrayList<>();
        
        public void addNamePart(CharSequence part) {
            // detect and merge ~ in destructor name
            if (!nameParts.isEmpty() && "~".contentEquals(getLastNamePart())) { //NOI18N
                nameParts.remove(nameParts.size() - 1);
                part = CharSequences.create(CharSequenceUtils.concatenate("~", part)); //NOI18N
            }
            nameParts.add(new NamePart(part));
        }

        public void setGlobal() {
            global = true;
        }

        public List<CharSequence> getNameParts() {
            List<CharSequence> names = new ArrayList<>();
            for (NamePart namePart : nameParts) {
                names.add(namePart.part);
            }
            return names;
        }
        
        public boolean isEmpty() {
            return nameParts.isEmpty();
        }

        public List<NamePart> getNames() {
            return nameParts;
        }
        
        public CharSequence getName() {
            StringBuilder sb = new StringBuilder();
            boolean firstScope = !global;
            for (NamePart part : nameParts) {                
                if(!firstScope) {
                    sb.append("::"); // NOI18N
                }
                sb.append(part.part);
                firstScope = false;
            }
            return NameCache.getManager().getString(sb);
        }
        
        public CharSequence getLastNamePart() {
            return nameParts.get(nameParts.size() - 1).part;
        }

        public void addParameterBuilder(SpecializationDescriptor.SpecializationParameterBuilder param) {
            if(!nameParts.isEmpty()) {
                nameParts.get(nameParts.size() - 1).params.add(param);
            }
        }
        
        public static final class NamePart {
            final CharSequence part;
            
            final List<SpecializationDescriptor.SpecializationParameterBuilder> params = new ArrayList<>();

            public NamePart(CharSequence part) {
                this.part = part;
            }

            public CharSequence getPart() {
                return part;
            }
            
            public List<SpecializationDescriptor.SpecializationParameterBuilder> getParams() {
                return params;
            }

            @Override
            public String toString() {
                return part.toString();
            }
        }

        @Override
        public String toString() {
            return "NameBuilder{" + getName() + super.toString() + '}'; //NOI18N
        }
    }
    
    public static abstract class OffsetableIdentifiableBuilder extends OffsetableBuilder implements CsmObjectBuilder {
        
        private CharSequence name;
        private int nameStartOffset = -1;
        private int nameEndOffset = -1;
        private boolean isMacroExpanded = false;

        public OffsetableIdentifiableBuilder() {
        }

        protected OffsetableIdentifiableBuilder(OffsetableIdentifiableBuilder builder) {
            super(builder);
            name = builder.name;
            nameStartOffset = builder.nameStartOffset;
            nameEndOffset = builder.nameEndOffset;
            isMacroExpanded = builder.isMacroExpanded;
        }
        
        public void setName(CharSequence name) {
            this.name = name;
        }

        public void setNameStartOffset(int nameStartOffset) {
            this.nameStartOffset = nameStartOffset;
        }

        public void setNameEndOffset(int nameEndOffset) {
            this.nameEndOffset = nameEndOffset;
        }

        public void setMacroExpanded() {
            this.isMacroExpanded = true;
        }

        public CharSequence getName() {
            if(name== null) {
                return NameCache.getManager().getString(CharSequences.empty()); //NOI18N
            } 
            return NameCache.getManager().getString(name);
        }
        
        public CharSequence getRawName() {
            if(name== null) {
                return NameCache.getManager().getString(CharSequences.empty()); //NOI18N
            } 
            return NameCache.getManager().getString(CharSequences.create(name.toString().replace("::", "."))); //NOI18N
        }
        
        public NameHolder getNameHolder() {
            if(nameStartOffset != -1 && nameEndOffset != -1) {
                return NameHolder.createName(name, nameStartOffset, nameEndOffset, isMacroExpanded);
            } else {
                return NameHolder.createName(name);
            }
        }

        protected void addReference(CsmObject obj) {
            getNameHolder().addReference(getFileContent(), obj);
        }

        @Override
        public String toString() {
            return "{" + "name=" + name + ", nameStartOffset=" + nameStartOffset + //NOI18N
                    ", nameEndOffset=" + nameEndOffset + ", isMacroExpanded=" + //NOI18N
                    isMacroExpanded + super.toString() + '}'; //NOI18N
        }
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
    }

    protected OffsetableIdentifiableBase(RepositoryDataInput input) throws IOException {
        super(input);
    }

    protected final void writeUID(RepositoryDataOutput output) throws IOException {
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.writeUID(uid, output);
    }

    protected final void readUID(RepositoryDataInput input) throws IOException {
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        this.uid = factory.readUID(input);
    }
}
