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

package org.netbeans.modules.cnd.navigation.includeview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.navigation.services.IncludedModel;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 */
public class IncludedChildren extends Children.Keys<CsmFile> {
    private static final Comparator<CsmFile> COMARATOR = new MyComparator();
    
    private final CsmFile object;
    private final IncludedChildren parent;
    private final IncludedModel model;
    private boolean isInited = false;
    
    public IncludedChildren(CsmFile object, IncludedModel model, IncludedChildren parent) {
        this.object = object;
        this.parent = parent;
        this.model = model;
    }
    
    public void dispose(){
        if (isInited) {
            isInited = false;
            setKeys(new CsmFile[0]);
        }
    }
    
    private synchronized void resetKeys(){
        if (object.isValid()) {
            Set<CsmFile> set = model.getModel().get(object);
            if (set != null && set.size() > 0) {
                List<CsmFile> list = new ArrayList<CsmFile>(set);
                Collections.sort(list, COMARATOR);
                setKeys(list);
                return;
            }
        }
        setKeys(new CsmFile[0]);
    }
    
    @Override
    protected Node[] createNodes(CsmFile file) {
        Node node;
        Set<CsmFile> set = model.getModel().get(file);
        if (set == null || set.isEmpty()) {
            node = new IncludeNode(file, Children.LEAF, model, false);
        } else {
            if (checkRecursion(file)) {
                node = new IncludeNode(file, Children.LEAF, model, true);
            } else {
                node = new IncludeNode(file, model, this);
            }
        }
        return new Node[]{node};
    }
    
    private boolean checkRecursion(CsmFile file){
        if (file.equals(object)) {
            return true;
        }
        IncludedChildren arr = parent;
        while (arr != null){
            if (file.equals(arr.object)){
                return true;
            }
            arr = arr.parent;
        }
        return false;
    }
    
    @Override
    protected void addNotify() {
        isInited = true;
        resetKeys();
        super.addNotify();
    }
    
    @Override
    protected void removeNotify() {
        super.removeNotify();
        dispose();
    }
    
    private static class MyComparator implements Comparator<CsmFile> {
        @Override
        public int compare(CsmFile o1, CsmFile o2) {
            String n1 = o1.getName().toString();
            String n2 = o2.getName().toString();
            return n1.compareTo(n2);
        }
    }
}
