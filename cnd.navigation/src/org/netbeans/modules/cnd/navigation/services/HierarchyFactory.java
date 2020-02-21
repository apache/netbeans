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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;

/**
 *
 */
public class HierarchyFactory {
    
    private HierarchyFactory(){
    }
    
    public static HierarchyFactory getInstance(){
        return new HierarchyFactory();
    }

    public HierarchyModel buildTypeHierarchyModel(CsmClass cls, Action[] actions, boolean subDirection, boolean plain, boolean recursive) {
        return new HierarchyModelImpl(cls, actions, subDirection, plain, recursive);
    }

    public IncludedModel buildIncludeHierarchyModel(CsmFile file, Action[] actions, boolean whoIncludes, boolean plain, boolean recursive){
        if (whoIncludes && plain && !recursive) {
            Collection<CsmFile> list = CsmIncludeHierarchyResolver.getDefault().getFiles(file);
            final Map<CsmFile, Set<CsmFile>> map = new HashMap<CsmFile, Set<CsmFile>>();
            map.put(file, new HashSet<CsmFile>(list));
            return new IncludedModelAdapter(actions, map, whoIncludes);
        }
        return new IncludedModelImpl(file, actions, whoIncludes, plain, recursive);
    }
    
    private static class IncludedModelAdapter implements IncludedModel {
        private final Map<CsmFile, Set<CsmFile>> map;
        private final Action[] actions;
        private Action close;
        private final boolean direction;
        public IncludedModelAdapter(Action[] actions, Map<CsmFile, Set<CsmFile>> map, boolean whoIncludes){
            this.map = map;
            this.actions = actions;
            direction = whoIncludes;
        }
        @Override
        public Map<CsmFile, Set<CsmFile>> getModel() {
            return map;
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

        @Override
        public boolean isDownDirection() {
            return !direction;
        }
    }
}
