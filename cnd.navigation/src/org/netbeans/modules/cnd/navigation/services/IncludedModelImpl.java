/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
