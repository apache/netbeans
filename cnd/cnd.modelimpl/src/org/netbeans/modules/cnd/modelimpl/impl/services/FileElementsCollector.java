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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmUsingDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUsingDirective;
import org.netbeans.modules.cnd.api.model.deep.CsmDeclarationStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmExpressionStatement;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmObjectAttributeQuery;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;

/**
 *
 */
public class FileElementsCollector {
    // use big negative instead of 0 as minimal start file offset
    // because CsmInclude associated with "-include file" has negative start offset
    private static final int MIN_FILE_OFFSET = Short.MIN_VALUE; // use Short.MIN_VALUE to prevent overflow
    private static final int MAX_FILE_OFFSET = Integer.MAX_VALUE;
    private final CsmFile destFile;
    private int startOffset;
    private int destOffset;
    private int returnPoint;
    private int localReturnPoint;
    private int localEndPoint;
    private final CsmProject onlyInProject;

//    private final ProjectBase project;

    public FileElementsCollector(CsmFile file, int offset, CsmProject onlyInProject) {
        this.destFile = file;
//        this.project = (ProjectBase) file.getProject();
        this.destOffset = offset;
        this.startOffset = MIN_FILE_OFFSET;
        this.returnPoint = MIN_FILE_OFFSET;
        this.localReturnPoint = MIN_FILE_OFFSET;
        this.onlyInProject = onlyInProject;
    }

    public int getReturnPoint() {
        return returnPoint;
    }

    public synchronized void incrementOffset(int newOffset){
        if (mapsGathered) {
            if (newOffset <= destOffset && newOffset > localReturnPoint ||
                newOffset <= localEndPoint && newOffset > localReturnPoint) {
                // All local maps is up-to-date.
                return;
            }
            startOffset = returnPoint;
        }
        destOffset = newOffset;
        if (startOffset < destOffset) {
            mapsGathered = false;
            visibleUsedDeclarations = null;
            visibleNamespaces = null;

            localDirectVisibleNamespaceDefinitions = new LinkedHashSet<>();
            localDirectVisibleNamespaces = new LinkedHashSet<>();
            localUsingNamespaces = new LinkedHashSet<>();
            localNamespaceAliases = new LinkedHashSet<>();
            localUsingDeclarations = new LinkedHashSet<>();
        } else if (startOffset > destOffset) {
            throw new IllegalArgumentException("Start offset "+startOffset+" > destination offset "+destOffset); // NOI18N
        }
    }

    private final LinkedHashSet<CsmNamespace> globalDirectVisibleNamespaces = new LinkedHashSet<>();
    private final LinkedHashSet<CsmUsingDirective> globalUsingNamespaces = new LinkedHashSet<>();
    private final LinkedHashSet<CsmNamespaceAlias> globalNamespaceAliases = new LinkedHashSet<>();
    private final LinkedHashSet<CsmUsingDeclaration> globalUsingDeclarations = new LinkedHashSet<>();

    private LinkedHashSet<CsmNamespace> localDirectVisibleNamespaces = new LinkedHashSet<>();
    private LinkedHashSet<CsmUsingDirective> localUsingNamespaces = new LinkedHashSet<>();
    private LinkedHashSet<CsmNamespaceAlias> localNamespaceAliases = new LinkedHashSet<>();
    private LinkedHashSet<CsmUsingDeclaration> localUsingDeclarations = new LinkedHashSet<>();

    private final LinkedHashSet<CsmNamespaceDefinition> globalDirectVisibleNamespaceDefinitions = new LinkedHashSet<>();
    private LinkedHashSet<CsmNamespaceDefinition> localDirectVisibleNamespaceDefinitions = new LinkedHashSet<>();

    public Collection<CsmUsingDeclaration> getUsingDeclarations() {
        initMaps();
        Collection<CsmUsingDeclaration> res = new LinkedHashSet<>();
        res.addAll(globalUsingDeclarations);
        res.addAll(localUsingDeclarations);
        return Collections.unmodifiableCollection(res);
    }

    public Collection<CsmUsingDirective> getUsingDirectives() {
        initMaps();
        Collection<CsmUsingDirective> res = new LinkedHashSet<>();
        res.addAll(globalUsingNamespaces);
        res.addAll(localUsingNamespaces);
        return Collections.unmodifiableCollection(res);
    }

    public Collection<CsmNamespaceAlias> getNamespaceAliases() {
        initMaps();
        Collection<CsmNamespaceAlias> res = new LinkedHashSet<>();
        res.addAll(globalNamespaceAliases);
        res.addAll(localNamespaceAliases);
        return Collections.unmodifiableCollection(res);
    }

    private Collection<CsmDeclaration> visibleUsedDeclarations = null;
    public Collection<CsmDeclaration> getUsedDeclarations() {
        initMaps();
        return _getUsedDeclarations();
    }

    private synchronized Collection<CsmDeclaration> _getUsedDeclarations() {
        Collection<CsmDeclaration> res = visibleUsedDeclarations;
        if (res == null) {
            res = UsingResolverImpl.extractDeclarations(globalUsingDeclarations);
            res.addAll(UsingResolverImpl.extractDeclarations(localUsingDeclarations));
            visibleUsedDeclarations = res;
        }
        return Collections.unmodifiableCollection(res);
    }

    private Collection<CsmNamespace> visibleNamespaces = null;
    public Collection<CsmNamespace> getVisibleNamespaces() {
        initMaps();
        return _getVisibleNamespaces();
    }

    public synchronized Collection<CsmNamespace> _getVisibleNamespaces() {
        Collection<CsmNamespace> res = visibleNamespaces;
        if (res == null) {
            res = UsingResolverImpl.extractNamespaces(globalUsingNamespaces, destFile.getProject());
            res.addAll(UsingResolverImpl.extractNamespaces(localUsingNamespaces, destFile.getProject()));
            // add scope's and unnamed visible namespaces
            res.addAll(globalDirectVisibleNamespaces);
            res.addAll(localDirectVisibleNamespaces);
            visibleNamespaces = res;
        }
        return Collections.unmodifiableCollection(res);
    }

//    public Collection<CsmNamespaceDefinition> getDirectVisibleNamespaceDefinitions() {
//        initMaps();
//        return Collections.unmodifiableCollection(directVisibleNamespaceDefinitions);
//    }

    private boolean mapsGathered = false;
    private synchronized void initMaps() {
        if( mapsGathered ) {
            return;
        }
        mapsGathered = true;
        gatherFileMaps();
    }

    private void gatherFileMaps() {
        final HashSet<CsmFile> visitedFiles = new HashSet<>();
        // gather all visible by this file's include stack
        List<CsmInclude> includeStack = CsmFileInfoQuery.getDefault().getIncludeStack(destFile);
        for (CsmInclude inc : includeStack) {
            CsmFile includedFrom = inc.getContainingFile();
            int incOffset = inc.getStartOffset();
            gatherFileMaps(visitedFiles, includedFrom, MIN_FILE_OFFSET, incOffset);
        }
        // gather all visible by this file upto destination offset
        gatherFileMaps(visitedFiles, this.destFile, this.startOffset, this.destOffset);
    }

    private void gatherFileMaps(Set<CsmFile> visitedFiles, CsmFile file, int startOffset, int endOffset) {
        if( !file.isValid() || visitedFiles.contains(file) ) {
            return;
        }
        visitedFiles.add(file);
        CsmFilter filter = CsmSelect.getFilterBuilder().createOffsetFilter(startOffset, endOffset);
        Iterator<CsmInclude> iter = CsmSelect.getIncludes(file, filter);
        while (iter.hasNext()){
            CsmInclude inc = iter.next();
            if (inc.getStartOffset() < startOffset) {
                continue;
            }
            // check that include is above the end offset
            if (inc.getEndOffset() < endOffset) {
                if (endOffset != MAX_FILE_OFFSET) {
                    returnPoint = inc.getEndOffset();
                    localReturnPoint = returnPoint;
                    localEndPoint = localReturnPoint;
                }
                CsmFile incFile = inc.getIncludeFile();
                if( incFile != null && (onlyInProject == null || incFile.getProject() == onlyInProject)) {
                    // in includes look for everything
                    gatherFileMaps(visitedFiles, incFile, MIN_FILE_OFFSET, MAX_FILE_OFFSET);
                }
            }
        }
        // gather this file maps
        if (endOffset == MAX_FILE_OFFSET) {
            filter = CsmSelect.getFilterBuilder().createKindFilter(
                    CsmDeclaration.Kind.NAMESPACE_DEFINITION,
                    CsmDeclaration.Kind.NAMESPACE_ALIAS,
                    CsmDeclaration.Kind.USING_DECLARATION,
                    CsmDeclaration.Kind.USING_DIRECTIVE);
            gatherDeclarationsMaps(CsmSelect.getDeclarations(file, filter), startOffset, endOffset, true);
        } else {
            gatherDeclarationsMaps(CsmSelect.getDeclarations(file, filter), startOffset, endOffset, true);
        }
    }

    private void gatherDeclarationsMaps(Iterable declarations, int startOffset, int endOffset, boolean global) {
        gatherDeclarationsMaps(declarations.iterator(), startOffset, endOffset, global);
    }

    private void gatherDeclarationsMaps(Iterator it, int startOffset, int endOffset, boolean global) {
        while(it.hasNext()) {
            CsmOffsetable o = (CsmOffsetable) it.next();
            try {
                int start = o.getStartOffset();
                int end = o.getEndOffset();
                if (end < startOffset) {
                    continue;
                }
                if( start >= endOffset ) {
                    break;
                }
                //assert o instanceof CsmScopeElement;
                if(CsmKindUtilities.isScopeElement((CsmObject)o)) {
                    if (endOffset != MAX_FILE_OFFSET) {
                        if (global) {
                            returnPoint = Math.max(returnPoint, start);
                            localReturnPoint = returnPoint;
                            localEndPoint = localReturnPoint;
                        } else {
                            localReturnPoint = Math.max(returnPoint, start);
                            localEndPoint = localReturnPoint;
                        }
                    }
                    gatherScopeElementMaps((CsmScopeElement) o, end, endOffset, global);
                    if (endOffset != MAX_FILE_OFFSET) {
                        if (o instanceof CsmExpressionStatement) {
                            if (!global) {
                                localEndPoint = Math.max(localEndPoint, end);
                            }
                        }
                    }
                } else {
                    if( FileImpl.reportErrors ) {
                        System.err.println("Expected CsmScopeElement, got " + o);
                    }
                }
            } catch (NullPointerException ex) {
                if( FileImpl.reportErrors ) {
                    // FIXUP: do not crush on NPE
                    System.err.println("Unexpected NULL element in declarations collection");
                    DiagnosticExceptoins.register(ex);
                }
            }
        }
    }

    /**
     * It is quaranteed that element.getStartOffset < this.destOffset
     */
    private void gatherScopeElementMaps(CsmScopeElement element, int end, int endOffset, boolean global) {
        CsmDeclaration.Kind kind = CsmKindUtilities.isDeclaration(element) ? ((CsmDeclaration) element).getKind() : null;
        if (kind == CsmDeclaration.Kind.NAMESPACE_DEFINITION) {
            CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) element;
            CsmNamespace namespace = nsd.getNamespace();
            if (nsd.getName().length() == 0) {
                // this is unnamed namespace and it should be considered as
                // it declares using itself
                if (namespace != null) {
                    // due to not synchronized access to code model parent namespace can be null
                    if (global) {
                        globalDirectVisibleNamespaces.add(namespace);
                    } else {
                        localDirectVisibleNamespaces.add(namespace);
                    }
                }
            }
            // skip if we're not inside the namespace scope
            if (endOffset > CsmObjectAttributeQuery.getDefault().getLeftBracketOffset(nsd) &&
                    endOffset < end) {
                // put in the end of list
                if (namespace != null) {
                    localDirectVisibleNamespaces.remove(namespace);
                    localDirectVisibleNamespaces.add(namespace);
                }
                gatherLocalNamespaceElementsFromMaps(nsd, MIN_FILE_OFFSET, endOffset, global);
                gatherDeclarationsMaps(nsd.getDeclarations(), MIN_FILE_OFFSET, endOffset, false);
            }
            if (global) {
                globalDirectVisibleNamespaceDefinitions.add(nsd);
            } else {
                localDirectVisibleNamespaceDefinitions.add(nsd);
            }
        } else if (kind == CsmDeclaration.Kind.NAMESPACE_ALIAS) {
            CsmNamespaceAlias alias = (CsmNamespaceAlias) element;
            if (global) {
                globalNamespaceAliases.add(alias);
            } else {
                localNamespaceAliases.add(alias);
            }
//            namespaceAliases.put(alias.getAlias(), alias.getReferencedNamespace());
        } else if (kind == CsmDeclaration.Kind.USING_DECLARATION) {
            CsmUsingDeclaration udecl = (CsmUsingDeclaration) element;
            if (global) {
                globalUsingDeclarations.add(udecl);
            } else {
                localUsingDeclarations.add(udecl);
            }
        } else if (kind == CsmDeclaration.Kind.USING_DIRECTIVE) {
            CsmUsingDirective udir = (CsmUsingDirective) element;
            if (global) {
                globalUsingNamespaces.add(udir);
            } else {
                localUsingNamespaces.add(udir);
            }
//            directVisibleNamespaces.add(udir.getName()); // getReferencedNamespace()
        } else if (CsmKindUtilities.isDeclarationStatement(element)) {
            CsmDeclarationStatement ds = (CsmDeclarationStatement) element;
            if (ds.getStartOffset() < endOffset) {
                gatherDeclarationsMaps(((CsmDeclarationStatement) element).getDeclarators(), MIN_FILE_OFFSET, endOffset, false);
            }
        } else if (CsmKindUtilities.isScope(element)) {
            if (endOffset < end) {
                gatherDeclarationsMaps(((CsmScope) element).getScopeElements(), MIN_FILE_OFFSET, endOffset, false);
            }
        }
    }
    
    private void gatherLocalNamespaceElementsFromMaps(CsmNamespaceDefinition ns, int end, int endOffset, boolean global) {
        CharSequence nsName = ns.getQualifiedName();
        if (global) {
            for (CsmNamespaceDefinition nsd : globalDirectVisibleNamespaceDefinitions) {
                if (nsd.getQualifiedName().equals(nsName)) {
                    gatherDeclarationsMaps(nsd.getDeclarations(), MIN_FILE_OFFSET, MAX_FILE_OFFSET, false);
                }
            }
        } else {
            LinkedHashSet<CsmNamespaceDefinition> currentDVNDs = new LinkedHashSet<>(localDirectVisibleNamespaceDefinitions);
            for (CsmNamespaceDefinition nsd : currentDVNDs) {
                if (nsd.getQualifiedName().equals(nsName)) {
                    gatherDeclarationsMaps(nsd.getDeclarations(), MIN_FILE_OFFSET, MAX_FILE_OFFSET, false);
                }
            }
        }
    }
}
