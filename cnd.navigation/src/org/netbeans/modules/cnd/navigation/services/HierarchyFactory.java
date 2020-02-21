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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
