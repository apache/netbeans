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

package org.netbeans.modules.cnd.api.model.services;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmErrorDirective;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItem.Language;
import org.netbeans.modules.cnd.api.project.NativeFileItem.LanguageFlavor;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.util.CharSequences;
import org.openide.util.Lookup;
import org.openide.util.Pair;

/**
 * query to obtain information associated with CsmFile object
 * 
 */
public abstract class CsmFileInfoQuery {
    /** A dummy resolver that never returns any results.
     */
    private static final CsmFileInfoQuery EMPTY = new Empty();
    
    /** default instance */
    private static CsmFileInfoQuery defaultResolver;
    
    protected CsmFileInfoQuery() {
    }
    
    /** Static method to obtain the resolver.
     * @return the resolver
     */
    public static CsmFileInfoQuery getDefault() {
        /*no need for sync synchronized access*/
        if (defaultResolver != null) {
            return defaultResolver;
        }
        defaultResolver = Lookup.getDefault().lookup(CsmFileInfoQuery.class);
        return defaultResolver == null ? EMPTY : defaultResolver;
    }
    
    /**
     * 
     * @param csmFile
     * @return true if file was compiled as cpp98 or later
     */
    public abstract boolean isCpp98OrLater(CsmFile csmFile);
    
    /**
     * 
     * @param csmFile
     * @return true if file was compiled as cpp11 or later
     */
    public abstract boolean isCpp11OrLater(CsmFile csmFile);
    
    /**
     * 
     * @param csmFile
     * @return true if file was compiled as cpp14 or later
     */
    public abstract boolean isCpp14OrLater(CsmFile csmFile);
    
    /**
     * @return list of system include paths used to parse file
     */
    public abstract List<IncludePath> getSystemIncludePaths(CsmFile file);
    
    /**
     * @return list of user include paths used to parse file
     */
    public abstract List<IncludePath> getUserIncludePaths(CsmFile file);
    
    /**
     *
     * @return list of code blocks which are excluded from compilation
     * due to current set of preprocessor directives
     * NOTE: last offsetable object could have Integer.MAX_VALUE value:
     *  - it means dead block from start offset till the end of file
     */
    public abstract List<CsmOffsetable> getUnusedCodeBlocks(CsmFile file, Interrupter interrupter);

    /**
     * @return SORTED list of macro's usages in the file
     */
    public abstract List<CsmReference> getMacroUsages(CsmFile file, Document doc, Interrupter interrupter);

    /**
     * @return dwarf block offset or null if there are no dwarf blocks in file
     */
    public abstract CsmOffsetable getGuardOffset(CsmFile file);
    
    /**
     * 
     * @param file header file
     * @return true if header file has a guard block
     */
    public abstract boolean hasGuardBlock(CsmFile file);
    
    /**
     * @return native file item associated with model file
     */
    public abstract NativeFileItem getNativeFileItem(CsmFile file);
    
    /**
     * 
     * @param file header file (for source file result is empty list)
     * @return list of include directives from source file to header file
     */
    public abstract List<CsmInclude> getIncludeStack(CsmFile file);

    /**
     *
     * @param incl #include directive
     * @return list of include directives from source file to header file
     */
    public abstract List<CsmInclude> getIncludeStack(CsmInclude incl);

    /**
     *
     * @param err #error directive
     * @return list of include directives from source file to header file
     */
    public abstract List<CsmInclude> getIncludeStack(CsmErrorDirective err);
    
    /**
     * 
     * @param file
     * @return pair with preferable language and flavor pair
     */
    public abstract Pair<Language, LanguageFlavor> getFileLanguageFlavor(CsmFile file);
    
    /**
     * 
     * @param langFlavor - pair of language and flavor
     * @return pair of language and flavor in terms of APT
     */
    public abstract Pair<String, String> getAPTLanguageFlavor(Pair<Language, LanguageFlavor> langFlavor);

    /**
     *
     * @param file any file
     * @return compilation units for file which includes context offset (at least with one element)
     */
    public abstract Collection<CsmCompilationUnit> getCompilationUnits(CsmFile file, int contextOffset);

    /**
     *
     * @param file file
     * @return list of broken include directives in file
     */
    public abstract Collection<CsmInclude> getBrokenIncludes(CsmFile file);

    /**
     *
     * @param file file
     * @return check if file has broken include directives
     */
    public abstract boolean hasBrokenIncludes(CsmFile file);

    /**
     * Attempts to get the version of a file.
     * @param file - the file to get a version for.
     * @return The file's version or 0 if the document does not
     *   support versioning
     */
    public abstract long getFileVersion(CsmFile file);

    /**
     * Attempts to get origin of file.
     * @param file - the file to get a buffer origin.
     * @return true if file buffer is document based
     */
    public abstract boolean isDocumentBasedFile(CsmFile file);
    
    /**
     * Is file standard headers indexer?
     * 
     * @param file
     * @return true if file is standard headers indexer
     */
    public abstract boolean isStandardHeadersIndexer(CsmFile file);

    /**
     * Calculates offset by line and column
     * @param file - file.
     * @param line - line in file.
     * @param column - column.
     * @return offset in file
     */
    public abstract long getOffset(CsmFile file, int line, int column);

    public abstract int[] getLineColumnByOffset(CsmFile file, int offset);
    
    public abstract int getLineCount(CsmFile file);

    public abstract CharSequence getName(CsmUID<CsmFile> fileUID);
    
    public abstract CharSequence getAbsolutePath(CsmUID<CsmFile> fileUID);
    
    public abstract CsmFile getCsmFile(Parser.Result parseResult);
    //
    // Implementation of the default query
    //
    private static final class Empty extends CsmFileInfoQuery {
        Empty() {
        }

        @Override
        public boolean isCpp98OrLater(CsmFile csmFile) {
            return false;
        }

        @Override
        public boolean isCpp11OrLater(CsmFile csmFile) {
            return false;
        }

        @Override
        public boolean isCpp14OrLater(CsmFile csmFile) {
            return false;
        }

        @Override
        public List<IncludePath> getSystemIncludePaths(CsmFile file) {
            return Collections.<IncludePath>emptyList();
        }

        @Override
        public List<IncludePath> getUserIncludePaths(CsmFile file) {
            return Collections.<IncludePath>emptyList();
        }

        @Override
        public List<CsmOffsetable> getUnusedCodeBlocks(CsmFile file, Interrupter interrupter) {
            return Collections.<CsmOffsetable>emptyList();
        }

        @Override
        public List<CsmReference> getMacroUsages(CsmFile file, Document doc, Interrupter interrupter) {
            return Collections.<CsmReference>emptyList();
        }

        @Override
        public CsmOffsetable getGuardOffset(CsmFile file) {
            return null;
        }
        
        @Override
        public boolean hasGuardBlock(CsmFile file) {
            return false;
        }

        @Override
        public NativeFileItem getNativeFileItem(CsmFile file) {
            return null;
        }

        @Override
        public List<CsmInclude> getIncludeStack(CsmFile file) {
            return Collections.<CsmInclude>emptyList();
        }

        @Override
        public List<CsmInclude> getIncludeStack(CsmErrorDirective err) {
            return Collections.<CsmInclude>emptyList();
        }

        @Override
        public List<CsmInclude> getIncludeStack(CsmInclude incl) {
            return Collections.<CsmInclude>emptyList();
        }

        @Override
        public Pair<Language, LanguageFlavor> getFileLanguageFlavor(CsmFile file) {
            return Pair.of(Language.OTHER, LanguageFlavor.UNKNOWN);
        }

        @Override
        public Pair<String, String> getAPTLanguageFlavor(Pair<Language, LanguageFlavor> langFlavor) {
            return null;
        }

        @Override
        public long getFileVersion(CsmFile file) {
            return 0;
        }

        @Override
        public long getOffset(CsmFile file, int line, int column) {
            return 0;
        }

        @Override
        public int getLineCount(CsmFile file) {
            return 0;
        }

        @Override
        public int[] getLineColumnByOffset(CsmFile file, int offset) {
            return new int[]{0, 0};
        }
        
        @Override
        public Collection<CsmInclude> getBrokenIncludes(CsmFile file) {
            return Collections.<CsmInclude>emptyList();
        }

        @Override
        public boolean hasBrokenIncludes(CsmFile file) {
            return false;
        }

        @Override
        public Collection<CsmCompilationUnit> getCompilationUnits(CsmFile file, int offset) {
            return Collections.singleton(CsmCompilationUnit.createCompilationUnit(file));
        }

        @Override
        public CharSequence getName(CsmUID<CsmFile> fileUID) {
            CsmFile file = fileUID.getObject();
            if (file != null) {
                return file.getName();
            }
            return CharSequences.empty();
        }

        @Override
        public CharSequence getAbsolutePath(CsmUID<CsmFile> fileUID) {
            CsmFile file = fileUID.getObject();
            if (file != null) {
                return file.getAbsolutePath();
            }
            return CharSequences.empty();
        }

        @Override
        public boolean isDocumentBasedFile(CsmFile file) {
            return false;
        }

        @Override
        public boolean isStandardHeadersIndexer(CsmFile file) {
            return false;
        }

        @Override
        public CsmFile getCsmFile(Parser.Result parseResult) {
            return null;
        }
    }
}
