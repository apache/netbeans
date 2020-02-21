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

package org.netbeans.modules.cnd.navigation.classhierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.navigation.hierarchy.LoadingNode;
import org.netbeans.modules.cnd.navigation.services.HierarchyModel;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.CharSequences;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class HierarchyChildren extends Children.Keys<HierarchyModel.Node> {
    private static final Comparator<HierarchyModel.Node> COMARATOR = new MyComparator();
    private static final RequestProcessor RP = new RequestProcessor(HierarchyChildren.class.getName(), 1);
    
    private final HierarchyModel.Node object;
    private final HierarchyModel model;
    private final HierarchyChildren parent;
    private boolean isInited = false;
    
    public HierarchyChildren(HierarchyModel.Node object, HierarchyModel model, HierarchyChildren parent) {
        this.object = object;
        this.model = model;
        this.parent = parent;
    }

    public void dispose(){
        if (isInited) {
            isInited = false;
            resetKeys(Collections.<HierarchyModel.Node>emptyList());
        }
    }
    
    private synchronized void resetKeys(List<HierarchyModel.Node> list) {
        if (list.size() > 1) {
            Collections.sort(list, COMARATOR);
        }
        setKeys(list);
    }
    
    @Override
    protected Node[] createNodes(HierarchyModel.Node cls) {
        Node node;
        if (cls.getDeclaration() instanceof DummyClass) {
            node = new LoadingNode();
        } else {
            if (checkRecursion(cls.getDeclaration())) {
                node = new HierarchyNode(cls, Children.LEAF, model, true);
            } else {
                node = new HierarchyNode(cls, model, this);
            }
        }
        return new Node[]{node};
    }
    
    private boolean checkRecursion(CsmOffsetableDeclaration cls){
        if (cls.equals(object.getDeclaration())) {
            return true;
        }
        HierarchyChildren arr = parent;
        while (arr != null){
            if (cls.equals(arr.object.getDeclaration())){
                return true;
            }
            arr = arr.parent;
        }
        return false;
    }
    
    @Override
    protected void addNotify() {
        isInited = true;
        if (!object.getDeclaration().isValid()) {
            resetKeys(Collections.<HierarchyModel.Node>emptyList());
        } else {
            resetKeys(Collections.<HierarchyModel.Node>singletonList(new HierarchyModel.Node(new DummyClass(), false)));
            RP.post(new Runnable(){
                @Override
                public void run() {
                    CsmOffsetableDeclaration decl = object.getDeclaration();
                    Collection<HierarchyModel.Node> set = (decl instanceof CsmClass) ? model.getHierarchy((CsmClass)decl) : null;
                    if (set != null && set.size() > 0) {
                        List<HierarchyModel.Node> list = new ArrayList<HierarchyModel.Node>(set);
                        resetKeys(list);
                    } else {
                        resetKeys(Collections.<HierarchyModel.Node>emptyList());
                    }
                }
            });
        }
        super.addNotify();
    }
    
    @Override
    protected void removeNotify() {
        super.removeNotify();
        dispose();
    }
    
    private static class MyComparator implements Comparator<HierarchyModel.Node> {
        @Override
        public int compare(HierarchyModel.Node o1, HierarchyModel.Node o2) {
            String n1 = o1.getDisplayName().toString();
            String n2 = o2.getDisplayName().toString();
            return n1.compareTo(n2);
        }
    }
    
    private static final class DummyClass implements CsmClass {

        @Override
        public Collection<CsmMember> getMembers() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<CsmFriend> getFriends() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<CsmInheritance> getBaseClasses() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getLeftBracketOffset() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<CsmTypedef> getEnclosingTypedefs() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<CsmVariable> getEnclosingVariables() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Kind getKind() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence getUniqueName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence getQualifiedName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence getName() {
            return CharSequences.empty();
        }

        @Override
        public CsmScope getScope() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isValid() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CsmFile getContainingFile() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getStartOffset() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getEndOffset() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Position getStartPosition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Position getEndPosition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence getText() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<CsmScopeElement> getScopeElements() {
            throw new UnsupportedOperationException();
        }
        
    }
}
