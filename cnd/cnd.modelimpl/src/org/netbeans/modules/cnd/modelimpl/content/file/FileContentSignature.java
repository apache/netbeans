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
package org.netbeans.modules.cnd.modelimpl.content.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.content.project.GraphContainer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.trace.LineDiff;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.openide.util.CharSequences;

/**
 *
 */
public final class FileContentSignature {

    private final List<KindAndSignature> signature;
    private final CsmUID<CsmFile> file;
    private final int hashCode;
    private final GraphContainer.CoherenceFiles coherence;

    private FileContentSignature(List<KindAndSignature> signature, CsmUID<CsmFile> file,
            GraphContainer.CoherenceFiles coherence) {
        this.signature = signature;
        this.file = file;
        this.hashCode = hash(signature);
        this.coherence = coherence;
    }

    public static FileContentSignature create(CsmFile file) {
        List<KindAndSignature> signature = createFileSignature(file);
        ProjectBase fileProject = ((FileImpl)file).getProjectImpl(true);
        return new FileContentSignature(signature, UIDCsmConverter.fileToUID(file),
                fileProject.getGraph().getCoherenceFiles(file));
    }

    public GraphContainer.CoherenceFiles getCoherenceFiles() {
        return coherence;
    }

    private static List<KindAndSignature> createFileSignature(CsmFile csmFile) {
        Collection<FileElement> fileElements = new TreeSet<>(PAIR_COMPARATOR);
        for (CsmInclude element : csmFile.getIncludes()) {
            // TODO: what about system vs user, shouldn't it be part of Utils.getCsmIncludeKindKey?
            CharAndCharSequence cs = new CharAndCharSequence(element.getIncludeName(), Utils.getCsmIncludeKindKey());
            FileElement fe = new FileElement(element.getStartOffset(), cs);
            fileElements.add(fe);
        }

        for (CsmMacro element : csmFile.getMacros()) {
            MacroCharSequence cs = new MacroCharSequence(element.getName(), element.getBody(), Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.MACRO));
            FileElement fe = new FileElement(element.getStartOffset(), cs);
            fileElements.add(fe);
        }
        for (CsmOffsetableDeclaration element : csmFile.getDeclarations()) {
            addDeclarationAndNested(fileElements, element);
        }
        ArrayList<KindAndSignature> out = new ArrayList<>(fileElements.size());
        for (FileElement fe : fileElements) {
            out.add(fe.signature);
        }
        out.trimToSize();
        return out;
    }

    private static void addDeclarationAndNested(Collection<FileElement> toAdd, CsmOffsetableDeclaration outDecl) {
        // TODO: what about function return value?
        // TODO: what about function params?
        // TODO: what about const/virtual attributes?
        // TODO: what about static?
        CharAndCharSequence cs = new CharAndCharSequence(outDecl.getQualifiedName(), Utils.getCsmDeclarationKindkey(outDecl.getKind()));
        FileElement fe = new FileElement(outDecl.getStartOffset(), cs);
        toAdd.add(fe);
        Iterator<? extends CsmOffsetableDeclaration> it = null;
        if (CsmKindUtilities.isNamespaceDefinition(outDecl)) {
            it = ((CsmNamespaceDefinition) outDecl).getDeclarations().iterator();
        } else if (CsmKindUtilities.isClass(outDecl)) {
            CsmClass cl = (CsmClass) outDecl;
            it = cl.getMembers().iterator();
            for (CsmInheritance inh : cl.getBaseClasses()) {
                CharAndCharSequence csi = new CharAndCharSequence(inh.getAncestorType().getClassifierText(), Utils.getCsmInheritanceKindKey(inh));
                toAdd.add(new FileElement(inh.getStartOffset(), csi));
            }
        } else if (CsmKindUtilities.isEnum(outDecl)) {
            CsmEnum en = (CsmEnum) outDecl;
            it = en.getEnumerators().iterator();
        }
        if (it != null) {
            while (it.hasNext()) {
                CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) it.next();
                addDeclarationAndNested(toAdd, decl);
            }
        }
    }
    
    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileContentSignature other = (FileContentSignature) obj;
        if (this.hashCode != other.hashCode) {
            return false;
        }
        if (!this.file.equals(other.file)) {
            return false;
        }
        if (!this.signature.equals(other.signature)) {
            return false;
        }
        return true;
    }

    public enum ComparisonResult {
        SAME,
        FILE_LOCAL_CHANGE,
        CHANGE_CAN_AFFECT_INCLUDES,
    }
    
    /**
     * compare two signatures (associated file info is ignored).
     * @param first first signature
     * @param second second signature
     * @return result of comparison
     */
    public static ComparisonResult compare(FileContentSignature first, FileContentSignature second) {
        ListIterator<KindAndSignature> e1 = first.signature.listIterator();
        ListIterator<KindAndSignature> e2 = second.signature.listIterator();
        boolean changed = false;
        boolean changeCanAffectIncludes = false;
        char inclKind = Utils.getCsmIncludeKindKey();
        while (e1.hasNext() && e2.hasNext()) {
            KindAndSignature o1 = e1.next();
            KindAndSignature o2 = e2.next();
            if (!o1.equals(o2)) {
                changed = true;
            }
            if (o1.getKind() == inclKind || o2.getKind() == inclKind) {
                if (changed) {
                    changeCanAffectIncludes = true;
                }
            }
        }
        ListIterator<KindAndSignature> remaining = e1.hasNext() ? e1 : e2;
        while (remaining.hasNext()) {
            KindAndSignature next = remaining.next();
            changed = true;
            if (next.getKind() == inclKind) {
                changeCanAffectIncludes = true;
            }
        }
        if (changed) {
            return changeCanAffectIncludes ? ComparisonResult.CHANGE_CAN_AFFECT_INCLUDES : ComparisonResult.FILE_LOCAL_CHANGE;
        }
        return ComparisonResult.SAME;
    }

    public static CharSequence testDifference(FileContentSignature first, FileContentSignature second) {
        StringBuilder out = new StringBuilder();
        if (!first.file.equals(second.file)) {
            out.append("FILE - ").append(first.file).append('\n');// NOI18N
            out.append("FILE + ").append(second.file).append('\n');// NOI18N
            return out;
        }
        if (first.hashCode != second.hashCode) {
            out.append("HASH - ").append(first.hashCode).append('\n');// NOI18N
            out.append("HASH + ").append(second.hashCode).append('\n');// NOI18N
        }
        if (!first.signature.isEmpty() && !second.signature.isEmpty()) {
            List<String> diff = LineDiff.diff(first.signature, second.signature);
            if (!diff.isEmpty()) {
                for (String line : diff) {
                    out.append(line).append('\n');// NOI18N
                }
            }
        } else {
            List<KindAndSignature> sigToDump;
            if (first.signature.isEmpty()) {
                out.append("FIRST is empty, content of the second:\n"); // NOI18N
                sigToDump = first.signature;
            } else {
                out.append("SECOND is empty, content of the first:\n"); // NOI18N
                sigToDump = second.signature;
            }
            for (KindAndSignature sigElem : sigToDump) {
                out.append(sigElem).append('\n');
            }
        }
        return out;
    }

    private static int hash(List<KindAndSignature> signature) {
        int hash = 7;
        for (CharSequence charSequence : signature) {
            hash = 89 * hash + charSequence.hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (CharSequence sig : signature) {
            out.append(sig).append('\n');
        }
        return out.toString();
    }

    private static final class FileElement {
        private final int start;
        private final KindAndSignature signature;

        public FileElement(int start, KindAndSignature sig) {
            this.start = start;
            this.signature = sig;
        }
    }
    
    private static final Comparator<FileElement> PAIR_COMPARATOR = new Comparator<FileElement>() {

        @Override
        public int compare(FileElement o1, FileElement o2) {
            int res = o1.start - o2.start;
            if (res == 0) {
                res = CharSequences.comparator().compare(o1.signature.getSignature(), o2.signature.getSignature());
            }
            if (res == 0) {
                res = o1.signature.getKind() - o2.signature.getKind();
            }
            return res;
        }
    };
    
    private interface KindAndSignature extends CharSequence {
        char getKind();
        CharSequence getSignature();
    }
    
    private static class CharAndCharSequence implements KindAndSignature {

        private final CharSequence delegate;
        private final char kind;

        public CharAndCharSequence(CharSequence delegate, char kind) {
            assert delegate != null;
            this.delegate = delegate;
            this.kind = kind;
        }

        @Override
        public int length() {
            return delegate.length() + 2;
        }

        @Override
        public char charAt(int index) {
            switch (index) {
                case 0:
                    return kind;
                case 1:
                    return ':'; // see toStringImpl
                default:
                    return delegate.charAt(index - 2);
            }
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return toStringImpl().subSequence(start, end);
        }

        private String toStringImpl() {
            return kind + ":" + delegate; // NOI18N
        }

        @Override
        public String toString() {
            return toStringImpl();
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 37 * hash + this.delegate.hashCode();
            hash = 37 * hash + this.kind;
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
            final CharAndCharSequence other = (CharAndCharSequence) obj;
            if (!this.delegate.equals(other.delegate)) {
                return false;
            }
            return this.kind == other.kind;
        }

        @Override
        public char getKind() {
            return this.kind;
        }

        @Override
        public CharSequence getSignature() {
            return this.delegate;
        }
    }

    private static final class MacroCharSequence implements KindAndSignature {
        private final CharSequence signature;
        private final CharSequence extra;

        public MacroCharSequence(CharSequence sig, CharSequence extra, char kind) {
            assert sig != null;
            this.signature = sig;
            assert extra != null;
            this.extra = extra;
            assert kind == 'M' : kind + " instead of M";
        }
        
        @Override
        public int length() {
            return 2/*M[*/ + signature.length() + 1/*]*/ + extra.length();
        }

        @Override
        public char charAt(int index) {
            if (index == 0) {
                return 'M';
            } else if (index == 1) {
                return '[';// see toStringImpl
            } else {
                index = index - 2;
                int len1 = signature.length();
                if (index < len1) {
                    return signature.charAt(index);
                } else if (index == len1) {
                    return ']';// see toStringImpl
                } else {
                    return extra.charAt(index-1-len1);
                }
            }
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return toStringImpl().subSequence(start, end);
        }

        private String toStringImpl() {
            // should be in sync with charAt()
            return "M[" + signature + "]" + extra; // NOI18N
        }

        @Override
        public String toString() {
            return toStringImpl();
        }
        
        @Override
        public int hashCode() {
            int hash = 'M';
            hash = 37 * hash + this.signature.hashCode();
            hash = 37 * hash + this.extra.hashCode();
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
            final MacroCharSequence other = (MacroCharSequence) obj;
            if (!this.signature.equals(other.signature)) {
                return false;
            }
            if (!this.extra.equals(other.extra)) {
                return false;
            }
            return true;
        }
        
        @Override
        public char getKind() {
            return 'M';
        }

        @Override
        public CharSequence getSignature() {
            return this.signature;
        }
    }
}
