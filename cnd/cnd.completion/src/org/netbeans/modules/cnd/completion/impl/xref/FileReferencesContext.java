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

package org.netbeans.modules.cnd.completion.impl.xref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.completion.csm.CsmContextUtilities;
import org.netbeans.modules.cnd.completion.csm.CsmProjectContentResolver;
import org.openide.util.CharSequences;

/**
 *
 *
 */
public final class FileReferencesContext extends CsmProgressAdapter {
    private static final class Lock{}
    private final Lock lock = new Lock();
    private CsmFile csmFile;
    //private int lastOffset;
    private boolean isClened = false;
    private Map<String,List<CsmUID<CsmVariable>>> fileLocalVars;
    private Map<String,Collection<CsmEnumerator>> libEnumerators;
    private Map<String,CsmEnumerator> hotSpotEnumerators;
    private OffsetsObjects offsets;
    private Map<CharSequence,CsmUID<CsmMacro>> projectMacros;
    private SymTabCache symTabCache = new SymTabCache();
    
    FileReferencesContext(CsmScope csmScope){
        if (CsmKindUtilities.isFile(csmScope)) {
            csmFile = (CsmFile) csmScope;
        } else if (CsmKindUtilities.isFunction(csmScope)) {
            csmFile = ((CsmFunction)csmScope).getContainingFile();
        } else if (CsmKindUtilities.isOffsetable(csmScope)) {
            csmFile = ((CsmOffsetable)csmScope).getContainingFile();
        }
        //lastOffset = 0;
        CsmListeners.getDefault().addProgressListener(this);
    }

    public void clean(){
        isClened = true;
        CsmListeners.getDefault().removeProgressListener(this);
        _clean();
    }

    private void _clean(){
        synchronized(lock) {
            fileLocalVars = null;
            offsets = null;
            projectMacros = null;
            libEnumerators = null;
            symTabCache.clear();
        }
    }
    
    public boolean isCleaned(){
        return isClened;
    }

    public void advance(int offset){
        if (csmFile == null) {
            return;
        }
        //lastOffset = offset;
    }

    public SymTabCache getSymTabCache(){
        return symTabCache;
    }

    public void putHotSpotEnum(Collection<CsmEnumerator> enumerators) {
        if (isCleaned()){
            return;
        }
        if (hotSpotEnumerators == null) {
            hotSpotEnumerators = new HashMap<String, CsmEnumerator>();
        }
        Set<CsmEnum> enums = new HashSet<CsmEnum>();
        for(CsmEnumerator e : enumerators) {
            CsmEnum parent = e.getEnumeration();
            if (parent != null) {
                enums.add(parent);
            }
        }
        for(CsmEnum e : enums) {
            if (!e.isStronglyTyped()) {
                for(CsmEnumerator i : e.getEnumerators()) {
                    hotSpotEnumerators.put(i.getName().toString(), i);
                }
            }
        }
    }
    
    public CsmEnumerator getHotSpotEnum(String name) {
        if (isCleaned()){
            return null;
        }
        if (hotSpotEnumerators == null) {
            hotSpotEnumerators = new HashMap<String, CsmEnumerator>();
        }
        return hotSpotEnumerators.get(name);
    }

    public Collection<CsmEnumerator> getLibEnumerators(String name){
        if (isCleaned()){
            return null;
        }
        if (libEnumerators == null) {
            libEnumerators = new HashMap<String,Collection<CsmEnumerator>>();
        }
        return libEnumerators.get(name);
    }

    public void putLibEnumerators(String name, Collection<CsmEnumerator> value){
        if (isCleaned()){
            return;
        }
        if (libEnumerators == null) {
            libEnumerators = new HashMap<String,Collection<CsmEnumerator>>();
        }
        libEnumerators.put(name, value);
    }

    public List<CsmVariable> getFileLocalIncludeVariables(String name){
        if (isCleaned()){
            return null;
        }
        List<CsmUID<CsmVariable>> vars = getFileLocalIncludeVariables().get(name);
        if (vars == null || vars.isEmpty()){
            return Collections.<CsmVariable>emptyList();
        }
        List<CsmVariable> res = new ArrayList<CsmVariable>(vars.size());
        for(CsmUID<CsmVariable> uid : vars){
            CsmVariable v = uid.getObject();
            if (v != null){
                res.add(v);
            }
        }
        return res;
    }
    
    public CsmObject findInnerFileDeclaration(int offset){
        if (isCleaned()){
            return null;
        }
        OffsetsObjects anOffsets = getFileOffsets();
        Offsets key = new Offsets(offset);
        int res = Collections.binarySearch(anOffsets.fileDeclarationsOffsets, key);
        if (res >= 0) {
            if (res < anOffsets.fileDeclarationsOffsets.size()-1) {
                Offsets next = anOffsets.fileDeclarationsOffsets.get(res+1);
                if (next.compareTo(key) == 0) {
                    return next.object;
                }
            }
            return anOffsets.fileDeclarationsOffsets.get(res).object;
        }
        return null;
    }

    public CsmObject findInnerFileObject(int offset){
        if (isCleaned()){
            return null;
        }
        OffsetsObjects anOffsets = getFileOffsets();
        Offsets key = new Offsets(offset);
        int res = Collections.binarySearch(anOffsets.fileObjectOffsets, key);
        if (res >=0) {
            if (res < anOffsets.fileObjectOffsets.size()-1) {
                Offsets next = anOffsets.fileObjectOffsets.get(res+1);
                if (next.compareTo(key) == 0) {
                    return next.object;
                }
            }
            return anOffsets.fileObjectOffsets.get(res).object;
        }
        return null;
    }

    public CsmMacro findIncludedMacro(String name){
        if (isCleaned()){
            return null;
        }
        if (projectMacros == null) {
            projectMacros = new HashMap<CharSequence,CsmUID<CsmMacro>>();
            fillProjectMacros();
        }
        CsmUID<CsmMacro> uid = projectMacros.get(CharSequences.create(name));
        if (uid != null) {
            return uid.getObject();
        }
        return null;
    }

    private Map<String,List<CsmUID<CsmVariable>>> getFileLocalIncludeVariables() {
        synchronized(lock) {
            if (fileLocalVars == null) {
                fileLocalVars = new HashMap<String, List<CsmUID<CsmVariable>>>();
                CsmDeclaration.Kind[] kinds = new CsmDeclaration.Kind[] {
                                CsmDeclaration.Kind.VARIABLE,
                                CsmDeclaration.Kind.VARIABLE_DEFINITION};
                CsmFilter filter = CsmContextUtilities.createFilter(kinds,
                                   null, true, true, false);
                List<CsmVariable> allVars = new ArrayList<CsmVariable>(10);
                CsmProjectContentResolver.fillFileLocalVariablesByFilter(filter, csmFile, allVars);
                for (CsmVariable var : allVars) {
                    String name = var.getName().toString();
                    List<CsmUID<CsmVariable>> list = fileLocalVars.get(name);
                    if (list == null) {
                        list = new ArrayList<CsmUID<CsmVariable>>();
                        fileLocalVars.put(name, list);
                    }
                    list.add(UIDs.get(var));
                }
            }
            return fileLocalVars;
        }
    }
    
    private OffsetsObjects getFileOffsets(){
        synchronized(lock) {
            if (offsets == null) {
                offsets = new OffsetsObjects(new ArrayList<Offsets>(), new ArrayList<Offsets>());

                for(CsmOffsetableDeclaration declaration : csmFile.getDeclarations()){
                    offsets.fileDeclarationsOffsets.add(new Offsets(declaration));
                }
                for(CsmInclude declaration : csmFile.getIncludes()){
                    offsets.fileObjectOffsets.add(new Offsets(declaration));
                }
                for(CsmMacro declaration : csmFile.getMacros()){
                    offsets.fileObjectOffsets.add(new Offsets(declaration));
                }
                Collections.sort(offsets.fileObjectOffsets);
                Collections.sort(offsets.fileDeclarationsOffsets);
            }
            return offsets;
        }
    }

    @Override
    public void fileParsingFinished(CsmFile file) {
        if (file.equals(csmFile)) {
            synchronized(lock) {
                fileLocalVars = null;
                offsets = null;
                symTabCache.clear();
            }
        }
    }
    
    private void fillProjectMacros() {
        gatherIncludeMacros(csmFile, new HashSet<CsmFile>());
    }
    
    private void gatherIncludeMacros(CsmFile file, Set<CsmFile> visitedFiles) {
        if( visitedFiles.contains(file) ) {
            return;
        }
        visitedFiles.add(file);
        for (Iterator<CsmInclude> iter = file.getIncludes().iterator(); iter.hasNext();) {
            CsmInclude inc = iter.next();
            CsmFile incFile = inc.getIncludeFile();
            if( incFile != null ) {
                getFileLocalMacros(incFile);
                gatherIncludeMacros(incFile, visitedFiles);
            }
        }
    }

    private void getFileLocalMacros(CsmFile file){
        for (CsmMacro macro : file.getMacros()) {
            CharSequence name = macro.getName();
            CsmUID<CsmMacro> uid = projectMacros.get(name);
            if (uid == null) {
                projectMacros.put(name, UIDs.get(macro));
            }
        }
    }

    private static final class OffsetsObjects {
        private final List<Offsets> fileObjectOffsets;
        private final List<Offsets> fileDeclarationsOffsets;
        private OffsetsObjects(List<Offsets> fileObjectOffsets, List<Offsets> fileDeclarationsOffsets) {
            this.fileObjectOffsets = fileObjectOffsets;
            this.fileDeclarationsOffsets = fileDeclarationsOffsets;
        }
    }

    private static class Offsets implements Comparable<Offsets> {
        private final int startOffset;
        private final int endOffset;
        private final CsmObject object;
        Offsets(CsmOffsetableDeclaration declaration){
            startOffset = declaration.getStartOffset();
            endOffset = declaration.getEndOffset();
            object = declaration;
        }
        Offsets(CsmMacro macros){
            startOffset = macros.getStartOffset();
            endOffset = macros.getEndOffset();
            object = macros;
        }
        Offsets(CsmInclude include){
            startOffset = include.getStartOffset();
            endOffset = include.getEndOffset();
            object = include;
        }
        Offsets(int offset){
            startOffset = offset;
            endOffset = offset;
            object = null;
        }

        @Override
        public int compareTo(Offsets o) {
            if (object != null && o.object == null) {
                if (startOffset <= o.startOffset && o.startOffset < endOffset) {
                    return 0;
                }
            } else if (object == null && o.object != null){
                if (o.startOffset <= startOffset && startOffset < o.endOffset) {
                    return 0;
                }
            }
            int res = startOffset - o.startOffset;
            if (res == 0) {
                res = endOffset - o.endOffset;
            }
            return res;
        }
    }
}
