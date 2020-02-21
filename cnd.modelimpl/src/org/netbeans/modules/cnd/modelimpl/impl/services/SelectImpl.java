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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind;
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
import org.netbeans.modules.cnd.api.model.util.CsmSortUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.uid.LazyCsmCollection;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.spi.model.services.CsmSelectProvider;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.spi.model.services.CsmSelectProvider.class)
public class SelectImpl implements CsmSelectProvider {
    private static final FilterBuilder builder = new FilterBuilder();

    @Override
    public CsmFilterBuilder getFilterBuilder() {
        return builder;
    }

    @Override
    public Iterator<CsmMacro> getMacros(CsmFile file, CsmFilter filter) {
        if (file instanceof FileImpl){
            Iterator<CsmMacro> res = analyzeFilter((FileImpl)file, filter);
            if (res != null) {
                return res;
            }
            return ((FileImpl)file).getMacros(filter);
        }
        return file.getMacros().iterator();
    }

    private Iterator<CsmMacro> analyzeFilter(FileImpl file, CsmFilter filter){
        if (filter instanceof FilterBuilder.NameFilterImpl) {
            FilterBuilder.NameFilterImpl implName = (FilterBuilder.NameFilterImpl) filter;
            if (implName.caseSensitive && implName.match && !implName.allowEmptyName) {
                // can be optimized
                Collection<CsmUID<CsmMacro>>res = file.findMacroUids(implName.strPrefix);
                return new LazyCsmCollection<>(res, true).iterator(filter);
            }
        }
        return null;
    }
    
    @Override
    public Iterator<CsmInclude> getIncludes(CsmFile file, CsmFilter filter) {
        if (file instanceof FileImpl){
            return ((FileImpl)file).getIncludes(filter);
        }
        return file.getIncludes().iterator();
    }


    @Override
    public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmNamespace namespace, CsmFilter filter) {
        if (namespace instanceof NamespaceImpl){
            Iterator<CsmOffsetableDeclaration> res = analyzeFilter((NamespaceImpl)namespace, filter);
            if (res != null) {
                return res;
            }
            return ((NamespaceImpl)namespace).getDeclarations(filter);
        }
        return namespace.getDeclarations().iterator();
    }

    private Iterator<CsmOffsetableDeclaration> analyzeFilter(NamespaceImpl namespace, CsmFilter filter){
        if (!namespace.isGlobal() && namespace.getName().length() == 0) {
            return null;
        }
        FilterBuilder.NameFilterImpl implName = null;
        FilterBuilder.KindFilterImpl implKind = null;
        if (filter instanceof FilterBuilder.CompoundFilterImpl) {
            FilterBuilder.CompoundFilterImpl implCompound = (FilterBuilder.CompoundFilterImpl) filter;
            if ((implCompound.first instanceof FilterBuilder.KindFilterImpl) &&
                (implCompound.second instanceof FilterBuilder.NameFilterImpl)) {
                // optimization by unique name
                implName = (FilterBuilder.NameFilterImpl) implCompound.second;
                implKind = (FilterBuilder.KindFilterImpl) implCompound.first;
            } else if ((implCompound.first instanceof FilterBuilder.NameFilterImpl) &&
                       (implCompound.second instanceof FilterBuilder.KindFilterImpl)) {
                // optimization by unique name
                implName = (FilterBuilder.NameFilterImpl) implCompound.first;
                implKind = (FilterBuilder.KindFilterImpl) implCompound.second;
            }
        } else if (filter instanceof FilterBuilder.KindFilterImpl) {
            implKind = (FilterBuilder.KindFilterImpl) filter;
        }
        List<CsmUID<CsmOffsetableDeclaration>> res = null;
        if (implName != null && implKind != null) {
            if (implName.caseSensitive && implName.match) {
                // can be optimized
                res = new ArrayList<>();
                StringBuilder from = new StringBuilder();
                for(int i = 0; i < implKind.kinds.length; i++){
                    from.setLength(0);
                    if (namespace.isGlobal()) {
                        if (implKind.kinds[i] == CsmDeclaration.Kind.VARIABLE || 
                            implKind.kinds[i] == CsmDeclaration.Kind.VARIABLE_DEFINITION) {
                            from.append(Utils.getCsmDeclarationKindkey(implKind.kinds[i]))
                                .append(OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR)
                                .append("::") // NOI18N
                                .append(implName.strPrefix);
                        } else {
                            from.append(Utils.getCsmDeclarationKindkey(implKind.kinds[i]))
                                .append(OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR)
                                .append(implName.strPrefix);
                        }
                    } else {
                        from.append(Utils.getCsmDeclarationKindkey(implKind.kinds[i]))
                            .append(OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR)
                            .append(namespace.getQualifiedName())
                            .append("::") // NOI18N
                            .append(implName.strPrefix);
                    }
                    // possible suffix after name is ' ', '(', '<'.
                    // all chars are contained in the Portable Character Set.
                    // using next char for segment works for any encodins.
                    res.addAll(namespace.findUidsRange(from.toString(), from.append('>').toString())); // NOI18N
                    //res.addAll(namespace.findUidsByPrefix(from));
                }
                if (implName.allowEmptyName) {
                    res.addAll(namespace.getUnnamedUids());
                }
            } else {
                res = new ArrayList<>();
                for(int i = 0; i < implKind.kinds.length; i++){
                    String from = ""+Utils.getCsmDeclarationKindkey(implKind.kinds[i]);
                    res.addAll(namespace.findUidsByPrefix(from));
                }
                if (implName.allowEmptyName) {
                    res.addAll(namespace.getUnnamedUids());
                }
            }
        } else if (implKind != null) {
            res = new ArrayList<>();
            for(int i = 0; i < implKind.kinds.length; i++){
                String from = ""+Utils.getCsmDeclarationKindkey(implKind.kinds[i]);
                res.addAll(namespace.findUidsByPrefix(from));
            }
        }
        if (res != null) {
            Iterator<CsmOffsetableDeclaration> iter = UIDCsmConverter.UIDsToDeclarations(res).iterator();
            return iter;
        }
        return null;
    }

    @Override
    public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmNamespaceDefinition namespace, CsmFilter filter) {
        if (namespace instanceof NamespaceDefinitionImpl){
            return ((NamespaceDefinitionImpl)namespace).getDeclarations(filter);
        }
        return namespace.getDeclarations().iterator();
    }

    @Override
    public Iterator<CsmScopeElement> getScopeDeclarations(CsmNamespaceDefinition namespace, CsmFilter filter) {
        if (namespace instanceof NamespaceDefinitionImpl){
            return ((NamespaceDefinitionImpl)namespace).getScopeElements(filter);
        }
        return namespace.getScopeElements().iterator();
    }

    @Override
    public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmFile file, CsmFilter filter) {
        if (file instanceof FileImpl){
            return analyzeFileFilter((FileImpl)file,filter);
        }
        return file.getDeclarations().iterator();
    }
    
    @Override
    public Iterator<CsmOffsetableDeclaration> getExternalDeclarations(CsmFile file) {
        CsmProject project = file.getProject();
        return ((ProjectBase)project).findExternalDeclarations(file).iterator();
    }

    private Iterator<CsmOffsetableDeclaration> analyzeFileFilter(FileImpl file, CsmFilter filter){
        if (filter instanceof FilterBuilder.InnerOffsetFilterImpl) {
            FilterBuilder.InnerOffsetFilterImpl implOffset = (FilterBuilder.InnerOffsetFilterImpl) filter;
            return file.getDeclarations(implOffset.innerOffset);
        }
        if (file.getDeclarationsSize() < 50) {
            // no optimization
            return file.getDeclarations(filter);
        }
        if (filter instanceof FilterBuilder.OffsetFilterImpl) {
            FilterBuilder.OffsetFilterImpl implOffset = (FilterBuilder.OffsetFilterImpl) filter;
            return UIDCsmConverter.UIDsToDeclarations(file.getDeclarations(implOffset.startOffset, implOffset.endOffset)).iterator();
        } else {
            FilterBuilder.NameFilterImpl implName = null;
            FilterBuilder.KindFilterImpl implKind = null;
            if (filter instanceof FilterBuilder.CompoundFilterImpl) {
                FilterBuilder.CompoundFilterImpl implCompound = (FilterBuilder.CompoundFilterImpl) filter;
                if ((implCompound.first instanceof FilterBuilder.KindFilterImpl) &&
                    (implCompound.second instanceof FilterBuilder.NameFilterImpl)) {
                    implName = (FilterBuilder.NameFilterImpl) implCompound.second;
                    implKind = (FilterBuilder.KindFilterImpl) implCompound.first;
                } else if ((implCompound.first instanceof FilterBuilder.NameFilterImpl) &&
                           (implCompound.second instanceof FilterBuilder.KindFilterImpl)) {
                    implName = (FilterBuilder.NameFilterImpl) implCompound.first;
                    implKind = (FilterBuilder.KindFilterImpl) implCompound.second;
                }
            } else if (filter instanceof FilterBuilder.KindFilterImpl) {
                implKind = (FilterBuilder.KindFilterImpl) filter;
            }
            Collection<CsmUID<CsmOffsetableDeclaration>> res = null;
            if (implName != null && implKind != null) {
                if (implName.caseSensitive && implName.match) {
                    res = file.getDeclarations(implKind.kinds, implName.strPrefix);
                    if (implName.allowEmptyName){
                        res.addAll(file.getDeclarations(implKind.kinds, ""));
                    }
                } else {
                    res = file.getDeclarations(implKind.kinds, null);
                }
            } else if (implKind != null) {
                res = file.getDeclarations(implKind.kinds, null);
            }
            if (res != null) {
                return UIDCsmConverter.UIDsToDeclarations(res, filter);
            }
        }
        return file.getDeclarations(filter);
    }

    @Override
    public Iterator<CsmVariable> getStaticVariables(CsmFile file, CsmFilter filter) {
        if (file instanceof FileImpl){
            return ((FileImpl)file).getStaticVariableDeclarations(filter);
        }
        return Collections.<CsmVariable>emptyList().iterator();
    }

    @Override
    public Iterator<CsmFunction> getStaticFunctions(CsmFile file, CsmFilter filter) {
        if (file instanceof FileImpl){
            return ((FileImpl)file).getStaticFunctionDeclarations(filter);
        }
        return Collections.<CsmFunction>emptyList().iterator();
    }


    @Override
    public Iterator<CsmMember> getClassMembers(CsmClass cls, CsmFilter filter) {
        if (cls instanceof FilterableMembers){
            return ((FilterableMembers)cls).getMembers(filter);
        }
        return cls.getMembers().iterator();
    }
    
    @Override
    public Iterator<CsmFriend> getClassFriends(CsmClass cls, CsmFilter filter) {
        if (cls instanceof FilterableFriends){
            return ((FilterableFriends)cls).getFriends(filter);
        }
        return cls.getFriends().iterator();
    }

    @Override
    public Iterator<CsmEnumerator> getEnumerators(CsmEnum en, CsmFilter filter) {
        if (en instanceof FilterableEnumerators){
            return ((FilterableEnumerators)en).getEnumerators(filter);
        }
        return en.getEnumerators().iterator();
    }

    @Override
    public boolean hasDeclarations(CsmFile file) {
        if (file instanceof FileImpl) {
            return ((FileImpl)file).hasDeclarations();
        }
        return file.getDeclarations().isEmpty();
    }

    @Override
    public Iterator<CsmUID<CsmFile>> getFileUIDs(CsmProject csmProject, NameAcceptor nameFilter) {
        if (csmProject instanceof ProjectBase) {
            return ((ProjectBase)csmProject).getFilteredFileUIDs(nameFilter);
        }
        return UIDCsmConverter.objectsToUIDs(csmProject.getAllFiles()).iterator();
    }

    private static interface Filter extends CsmFilter, UIDFilter {
    }
    
    public static interface FilterableMembers {
        Iterator<CsmMember> getMembers(CsmFilter filter);
    }
    
    public static interface FilterableFriends {
        Iterator<CsmFriend> getFriends(CsmFilter filter);
    }

    public static interface FilterableEnumerators {
        Iterator<CsmEnumerator> getEnumerators(CsmFilter filter);
    }
    
    @SuppressWarnings("unchecked")
    static class FilterBuilder implements CsmFilterBuilder {
        @Override
        public CsmFilter createKindFilter(final CsmDeclaration.Kind ... kinds) {
            return new KindFilterImpl(kinds);
        }

        @SuppressWarnings("unchecked")
        @Override
        public CsmFilter createNameFilter(final CharSequence strPrefix, final boolean match, final boolean caseSensitive, final boolean allowEmptyName) {
            return new NameFilterImpl(allowEmptyName, strPrefix, match, caseSensitive);
        }

        @Override
        public CsmFilter createOffsetFilter(final int startOffset, final int endOffset) {
            return new OffsetFilterImpl(startOffset, endOffset);
        }

        @Override
        public CsmFilter createOffsetFilter(int innerOffset) {
            return new InnerOffsetFilterImpl(innerOffset);
        }

        @Override
        public CsmFilter createCompoundFilter(final CsmFilter first, final CsmFilter second) {
            return new CompoundFilterImpl(first, second);
        }

        @Override
        public CsmFilter createOrFilter(final CsmFilter first, final CsmFilter second) {
            return new OrFilterImpl(first, second);
        }

        @SuppressWarnings("unchecked")
        @Override
        public CsmFilter createNameFilter(final NameAcceptor nameAcceptor) {
            return new NameAcceptorFilterImpl(nameAcceptor);
        }

        private static class KindFilterImpl implements Filter {
            private final Kind[] kinds;

            public KindFilterImpl(Kind[] kinds) {
                this.kinds = kinds;
            }

            @Override
            public boolean accept(CsmUID<?> uid) {
                CsmDeclaration.Kind kind = UIDUtilities.getKind(uid);
                if (kind != null) {
                    for (CsmDeclaration.Kind k : kinds) {
                        if (k == kind) {
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public String toString() {
                return Arrays.asList(kinds).toString();
            }

            @Override
            public int hashCode() {
                int hash = 7;
                for (Kind kind : kinds) {
                    hash = 53 * hash + kind.hashCode();
                }
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final KindFilterImpl other = (KindFilterImpl) obj;
                if (kinds.length != other.kinds.length) {
                    return false;
                }
                for (int i = 0; i < kinds.length; i++) {
                    if (!kinds[i].equals(other.kinds[i])) {
                        return false;
                    }
                }
                return true;
            }
        }

        private static class NameFilterImpl implements Filter {
            private final boolean allowEmptyName;
            private final CharSequence strPrefix;
            private final boolean match;
            private final boolean caseSensitive;
            
            public NameFilterImpl(boolean allowEmptyName, CharSequence strPrefix, boolean match, boolean caseSensitive) {
                this.allowEmptyName = allowEmptyName;
                this.strPrefix = strPrefix;
                this.match = match;
                this.caseSensitive = caseSensitive;
            }

            @Override
            public boolean accept(CsmUID<?> uid) {
                CharSequence name = UIDUtilities.getName(uid);
                if (name != null) {
                    if (allowEmptyName && name.length() == 0) {
                        return true;
                    }
                    return CsmSortUtilities.startsWith(name.toString(), strPrefix.toString(), match, caseSensitive);
                }
                return false;
            }

            @Override
            public String toString() {
                return "pref=" + strPrefix + "; match=" + match + "; cs=" + caseSensitive + "; allowEmpty=" + allowEmptyName; // NOI18N
            }

            @Override
            public int hashCode() {
                int hash = 3;
                hash = 73 * hash + (this.allowEmptyName ? 1 : 0);
                hash = 73 * hash + Objects.hashCode(this.strPrefix);
                hash = 73 * hash + (this.match ? 1 : 0);
                hash = 73 * hash + (this.caseSensitive ? 1 : 0);
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final NameFilterImpl other = (NameFilterImpl) obj;
                if (this.allowEmptyName != other.allowEmptyName) {
                    return false;
                }
                if (!Objects.equals(this.strPrefix, other.strPrefix)) {
                    return false;
                }
                if (this.match != other.match) {
                    return false;
                }
                if (this.caseSensitive != other.caseSensitive) {
                    return false;
                }
                return true;
            }                        
        }

        private static class OffsetFilterImpl implements Filter {
            private final int startOffset;
            private final int endOffset;

            public OffsetFilterImpl(int startOffset, int endOffset) {
                this.startOffset = startOffset;
                this.endOffset = endOffset;
            }

            @Override
            public boolean accept(CsmUID<?> uid) {
                int start = UIDUtilities.getStartOffset(uid);
                int end = UIDUtilities.getEndOffset(uid);
                if (start < 0) {
                    return true;
                }
                if (end < startOffset || start >= endOffset) {
                    return false;
                }
                return true;
            }

            @Override
            public String toString() {
                return "start offset=" + startOffset + "; endOffset=" + endOffset; // NOI18N
            }

            @Override
            public int hashCode() {
                int hash = 3;
                hash = 59 * hash + this.startOffset;
                hash = 59 * hash + this.endOffset;
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final OffsetFilterImpl other = (OffsetFilterImpl) obj;
                if (this.startOffset != other.startOffset) {
                    return false;
                }
                if (this.endOffset != other.endOffset) {
                    return false;
                }
                return true;
            }
        }

        private static class InnerOffsetFilterImpl implements Filter {
            private final int innerOffset;

            public InnerOffsetFilterImpl(int innerOffset) {
                this.innerOffset = innerOffset;
            }

            @Override
            public boolean accept(CsmUID<?> uid) {
                int start = UIDUtilities.getStartOffset(uid);
                int end = UIDUtilities.getEndOffset(uid);
                if (start < 0) {
                    return true;
                }
                if (start <= innerOffset && innerOffset <= end) {
                    return true;
                }
                return false;
            }

            @Override
            public String toString() {
                return "inner offset=" + innerOffset; // NOI18N
            }

            @Override
            public int hashCode() {
                int hash = 7;
                hash = 89 * hash + this.innerOffset;
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final InnerOffsetFilterImpl other = (InnerOffsetFilterImpl) obj;
                if (this.innerOffset != other.innerOffset) {
                    return false;
                }
                return true;
            }
        }

        private static class CompoundFilterImpl implements Filter {
            private final CsmFilter first;
            private final CsmFilter second;

            public CompoundFilterImpl(CsmFilter first, CsmFilter second) {
                this.first = first;
                this.second = second;
            }

            @Override
            public boolean accept(CsmUID<?> uid) {
                return ((UIDFilter) first).accept(uid) && ((UIDFilter) second).accept(uid);
            }

            @Override
            public String toString() {
                return "filter [" + first + "][" + second + "]"; // NOI18N
            }

            @Override
            public int hashCode() {
                return this.first.hashCode() ^ this.second.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final CompoundFilterImpl other = (CompoundFilterImpl) obj;
                if (Objects.equals(this.first, other.first) && Objects.equals(this.second, other.second)) {
                    return true;
                }                
                // it's fine if first and second were swapped
                return Objects.equals(this.first, other.second) && Objects.equals(this.second, other.first);
            }
        }

        private static class OrFilterImpl implements Filter {
            private final CsmFilter first;
            private final CsmFilter second;

            public OrFilterImpl(CsmFilter first, CsmFilter second) {
                this.first = first;
                this.second = second;
            }

            @Override
            public boolean accept(CsmUID<?> uid) {
                return ((UIDFilter) first).accept(uid) || ((UIDFilter) second).accept(uid);
            }

            @Override
            public String toString() {
                return "filter [" + first + "][" + second + "]"; // NOI18N
            }

            @Override
            public int hashCode() {
                return this.first.hashCode() ^ this.second.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final OrFilterImpl other = (OrFilterImpl) obj;
                if (Objects.equals(this.first, other.first) && Objects.equals(this.second, other.second)) {
                    return true;
                }                
                // it's fine if first and second were swapped
                return Objects.equals(this.first, other.second) && Objects.equals(this.second, other.first);
            }
        }

        private static class NameAcceptorFilterImpl implements Filter {
            private final NameAcceptor nameAcceptor;

            public NameAcceptorFilterImpl(NameAcceptor nameAcceptor) {
                this.nameAcceptor = nameAcceptor;
            }

            @Override
            public boolean accept(CsmUID<?> uid) {
                CharSequence name = UIDUtilities.getName(uid);
                return nameAcceptor.accept(name);
            }

            @Override
            public int hashCode() {
                int hash = 5;
                hash = 11 * hash + Objects.hashCode(this.nameAcceptor);
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final NameAcceptorFilterImpl other = (NameAcceptorFilterImpl) obj;
                if (!Objects.equals(this.nameAcceptor, other.nameAcceptor)) {
                    return false;
                }
                return true;
            }                        
        }
    }

}
