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

package org.netbeans.modules.cnd.navigation.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;

/**
 *
 */
/*package-local*/ class IncludedModelImpl implements IncludedModel {
    private Map<CsmFile,Set<CsmFile>> map;
    private final Action[] actions;
    private Action close;
    private final boolean direction;
       
    /** Creates a new instance of IncludedModel */
    public IncludedModelImpl(CsmFile file, Action[] actions, boolean whoIncludes, boolean plain, boolean recursive) {
        this.actions = actions;
        direction = whoIncludes;
        if (whoIncludes) {
            map = buildWhoIncludes(file);
        } else {
            map = buildWhoIsIncluded(file);
        }
        if (!recursive) {
            Set<CsmFile> result = map.get(file);
            if (result == null){
                result = new HashSet<CsmFile>();
            }
            map = new HashMap<CsmFile,Set<CsmFile>>();
            map.put(file,result);
        }
        if (plain) {
            Set<CsmFile> result = new HashSet<CsmFile>();
            gatherList(file, result, map);
            map = new HashMap<CsmFile,Set<CsmFile>>();
            map.put(file,result);
        }
    }
    
    private void gatherList(CsmFile file, Set<CsmFile> result, Map<CsmFile,Set<CsmFile>> map){
        Set<CsmFile> set = map.get(file);
        if (set == null) {
            return;
        }
        for(CsmFile f : set){
            if (!result.contains(f)) {
                result.add(f);
                gatherList(f, result, map);
            }
        }
    }
    
    @Override
    public Map<CsmFile,Set<CsmFile>> getModel(){
        return map;
    }

    @Override
    public boolean isDownDirection() {
        return !direction;
    }
    
    private Map<CsmFile,Set<CsmFile>> buildWhoIncludes(CsmFile file){
        HashMap<CsmFile,Set<CsmFile>> aMap = new HashMap<CsmFile,Set<CsmFile>>();
        for(CsmProject prj :CsmModelAccessor.getModel().projects()){
            for(CsmFile f : prj.getSourceFiles()){
                buildWhoIncludes(f, aMap);
            }
            for(CsmFile f : prj.getHeaderFiles()){
                buildWhoIncludes(f, aMap);
            }
            for (CsmProject lib : prj.getLibraries()){
                for(CsmFile f : lib.getSourceFiles()){
                    buildWhoIncludes(f, aMap);
                }
                for(CsmFile f : lib.getHeaderFiles()){
                    buildWhoIncludes(f, aMap);
                }
            }
        }
        return aMap;
    }
    
    private void buildWhoIncludes(CsmFile file, Map<CsmFile,Set<CsmFile>> map){
        for(CsmInclude include : file.getIncludes()){
            CsmFile included = include.getIncludeFile();
            if (included != null){
                Set<CsmFile> back = map.get(included);
                if (back == null){
                    back = new HashSet<CsmFile>();
                    map.put(included,back);
                }
                if (!back.contains(file)) {
                    back.add(file);
                    buildWhoIncludes(included, map);
                }
            }
        }
    }

    private Map<CsmFile, Set<CsmFile>> buildWhoIsIncluded(CsmFile file) {
        HashMap<CsmFile,Set<CsmFile>> aMap = new HashMap<CsmFile,Set<CsmFile>>();
        buildWhoIsIncluded(file, aMap);
        return aMap;
    }

    private void buildWhoIsIncluded(CsmFile file, Map<CsmFile,Set<CsmFile>> map){
        Set<CsmFile> includes = map.get(file);
        if (includes != null){
            return;
        }
        includes = new HashSet<CsmFile>();
        map.put(file, includes);
        for(CsmInclude include : file.getIncludes()){
            CsmFile included = include.getIncludeFile();
            if (included != null){
                includes.add(included);
                buildWhoIsIncluded(included, map);
            }
        }
    }

    @Override
    public Action[] getDefaultActions() {
        return actions;
    }

    @Override
    public Action getCloseWindowAction() {
        return close;
    }

    @Override
    public void setCloseWindowAction(Action close) {
        this.close = close;
    }

}
