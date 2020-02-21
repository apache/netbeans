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
package org.netbeans.modules.cnd.qnavigator.navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.model.CsmErrorDirective;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.services.CsmStandaloneFileProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 *
 */
public class CsmFileModel {
    static final Logger logger = Logger.getLogger("org.netbeans.modules.cnd.qnavigator"); // NOI18N
    private final List<IndexOffsetNode> lineNumberIndex = Collections.synchronizedList(new ArrayList<IndexOffsetNode>());
    private final List<CppDeclarationNode> list = Collections.synchronizedList(new ArrayList<CppDeclarationNode>());
    private final CsmFileFilter filter;
    private final Action[] actions;
    private FileObject fileObject;
    private CsmFile csmModelFile;
    private DataObject cdo;
    private boolean isStandalone;
    private Project unopenedProject;

    public CsmFileModel(CsmFileFilter filter, Action[] actions){
        this.filter = filter;
        this.actions = actions;
    }

    public Node[] getNodes() {
        return list.toArray(new Node[0]);
    }
    
    void clear(){
        lineNumberIndex.clear();
        list.clear();
    }
    
    public CsmFileFilter getFilter(){
        return filter;
    }

    public FileObject getFileObject(){
        return fileObject;
    }

    public DataObject getDataObject(){
        return cdo;
    }

    public boolean isStandalone(){
        return isStandalone;
    }

    public Project getUnopenedProject() {
        return unopenedProject;
    }

    public void addOffset(Node node, CsmOffsetable element, List<IndexOffsetNode> lineNumberIndex) {
        if (csmModelFile.equals(element.getContainingFile())) {
            lineNumberIndex.add(new IndexOffsetNode(node,element.getStartOffset(), element.getEndOffset()));
        } else {
            // element from another file (include in namespace/class/enum)
            // set negative offsets
            lineNumberIndex.add(new IndexOffsetNode(node, -1, -1));
        }
    }

    public void addFileOffset(Node node, CsmFile element, List<IndexOffsetNode> lineNumberIndex) {
        lineNumberIndex.add(new IndexOffsetNode(node, 0, 0));
    }

    public PreBuildModel buildPreModel(DataObject cdo, FileObject fo, CsmFile csmFile, AtomicBoolean canceled) {
        boolean oldValue = isStandalone;
        this.fileObject = fo;
        this.cdo = cdo;
        this.csmModelFile = csmFile;
        isStandalone = CsmStandaloneFileProvider.getDefault().isStandalone(csmFile);
        PreBuildModel preBuildModel = new PreBuildModel(oldValue != isStandalone);
        unopenedProject = null;
        if (csmFile != null && csmFile.isValid()) {
            if (isStandalone) {
                CppDeclarationNode node = CppDeclarationNode.nodeFactory(csmFile, this, false, preBuildModel.newLineNumberIndex, canceled);
                if (node != null) {
                    preBuildModel.newList.add(node);
                }
                unopenedProject = FileOwnerQuery.getOwner(fileObject);
                if (OpenProjects.getDefault().isProjectOpen(unopenedProject)) {
                    unopenedProject = null;
                }
            }
            if (filter.isApplicableInclude()) {
                if (!canceled.get()) {
                    for (CsmInclude element : csmFile.getIncludes()) {
                        if (canceled.get()) {
                            break;
                        }
                        CppDeclarationNode node = CppDeclarationNode.nodeFactory((CsmObject) element, this, false, preBuildModel.newLineNumberIndex, canceled);
                        if (node != null) {
                            preBuildModel.newList.add(node);
                        }
                    }
                }
            }
            if (filter.isApplicableMacro()) {
                if (!canceled.get()) {
                    for (CsmMacro element : csmFile.getMacros()) {
                        if (canceled.get()) {
                            break;
                        }
                        CppDeclarationNode node = CppDeclarationNode.nodeFactory((CsmObject) element, this, false, preBuildModel.newLineNumberIndex, canceled);
                        if (node != null) {
                            preBuildModel.newList.add(node);
                        }
                    }
                }
                if (!canceled.get()) {
                    for (CsmErrorDirective element : csmFile.getErrors()) {
                        if (canceled.get()) {
                            break;
                        }
                        CppDeclarationNode node = CppDeclarationNode.nodeFactory((CsmObject) element, this, false, preBuildModel.newLineNumberIndex, canceled);
                        if (node != null) {
                            preBuildModel.newList.add(node);
                        }
                    }
                }
            }
            if (!canceled.get()) {
                for (CsmOffsetableDeclaration element : csmFile.getDeclarations()) {
                    if (canceled.get()) {
                        break;
                    }
                    if (filter.isApplicable(element)) {
                        CppDeclarationNode node = CppDeclarationNode.nodeFactory((CsmObject) element, this, false, preBuildModel.newLineNumberIndex, canceled);
                        if (node != null) {
                            preBuildModel.newList.add(node);
                        }
                    }
                }
            }
        }
        if (csmFile != null &&  csmFile.isValid() && !canceled.get()) {
            Collections.<CppDeclarationNode>sort(preBuildModel.newList);
            Collections.<IndexOffsetNode>sort(preBuildModel.newLineNumberIndex);
        }
        return preBuildModel;
    }

    public boolean buildModel(PreBuildModel preBuildModel, CsmFile csmFile, boolean force) {
        boolean res = true;
        if (csmFile != null &&  csmFile.isValid()) {
            resetScope(preBuildModel.newLineNumberIndex);
            if (force || preBuildModel.forceRebuild || isNeedChange(preBuildModel.newLineNumberIndex,  preBuildModel.newList)) {
                clear();
                list.addAll(preBuildModel.newList);
                lineNumberIndex.addAll(preBuildModel.newLineNumberIndex);
                logger.log(Level.FINE, "Set new navigator model for file {0}", csmFile); // NOI18N
            } else {
                resetScope(lineNumberIndex);
                res = false;
                logger.log(Level.FINE, "Reset navigator model for file {0}", csmFile); // NOI18N
            }
        } else {
            clear();
            logger.log(Level.FINE, "Clear navigator model for file {0}", csmFile); // NOI18N
        }
        preBuildModel.newList.clear();
        preBuildModel.newLineNumberIndex.clear();
        return res;
    }

    private boolean isNeedChange(List<IndexOffsetNode> newLineNumberIndex,  List<CppDeclarationNode> newList){
        if (newLineNumberIndex.size() != lineNumberIndex.size()) {
            return true;
        }
        int i = 0;
        for (IndexOffsetNode n1 : lineNumberIndex) {
            if (newLineNumberIndex.size() <= i) {
                return true;
            }
            IndexOffsetNode n2 = newLineNumberIndex.get(i);
            if (!compareNodeContent(n1, n2)) {
                return true;
            }
            i++;
        }
        i = 0;
        for (IndexOffsetNode n1 : lineNumberIndex) {
            if (newLineNumberIndex.size() <= i) {
                return true;
            }
            IndexOffsetNode n2 = newLineNumberIndex.get(i);
            updateNodeContent(n1, n2);
            i++;
        }
        return !isTreeEquals(list, newList);
    }
    
    private boolean isTreeEquals(List<? extends Node> list1, List<? extends Node> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        for(int i = 0; i < list1.size(); i++) {
            Node n1 = list1.get(i);
            Node n2 = list2.get(i);
            List<Node> l1 = Arrays.asList(n1.getChildren().getNodes());
            List<Node> l2 = Arrays.asList(n2.getChildren().getNodes());
            if (!isTreeEquals(l1, l2)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean compareNodeContent(IndexOffsetNode n1, IndexOffsetNode n2){
        CppDeclarationNode d1 = (CppDeclarationNode) n1.getNode();
        CppDeclarationNode d2 = (CppDeclarationNode) n2.getNode();
        return d1.compareToWithoutOffset(d2) == 0;
    }

    private void updateNodeContent(IndexOffsetNode n1, IndexOffsetNode n2){
        CppDeclarationNode d1 = (CppDeclarationNode) n1.getNode();
        CppDeclarationNode d2 = (CppDeclarationNode) n2.getNode();
        d1.resetNode(d2);
        n1.resetContent(n2);
    }

    
    private void resetScope(List<IndexOffsetNode> newLineNumberIndex){
        Stack<IndexOffsetNode> stack = new Stack<IndexOffsetNode>();
        for(IndexOffsetNode node : newLineNumberIndex){
            while (!stack.empty()) {
                IndexOffsetNode scope = stack.peek();
                if (node.getStartOffset() >= scope.getStartOffset() &&
                    node.getEndOffset() <= scope.getEndOffset()) {
                    node.setScope(scope);
                    break;
                }
                stack.pop();
            }
            stack.push(node);
        }
    }
    
    public Node setSelection(long caretLineNo) {
        // Find nearest Node
        int index = Collections.<IndexOffsetNode>binarySearch(lineNumberIndex, new IndexOffsetNode(null, caretLineNo, caretLineNo));
        if (index < 0) {
            // exact line not found, but insersion index (-1) returned instead
            index = -index-2;
        }
        if (index > -1 && index < lineNumberIndex.size()) {
            IndexOffsetNode node  = lineNumberIndex.get(index);
            if (node.getStartOffset() <= caretLineNo &&
                node.getEndOffset() >= caretLineNo) {
                // exactly found
                return node.getNode();
            }
            IndexOffsetNode scopedNode = node.getScope();
            while (scopedNode != null){
                node = scopedNode;
                if (scopedNode.getStartOffset() <= caretLineNo &&
                    scopedNode.getEndOffset() >= caretLineNo) {
                    // found in parent
                    return scopedNode.getNode();
                }
                scopedNode = scopedNode.getScope();
            }
            // not found, return last scope if node from current file
            if (node.getStartOffset() >=0 && node.getEndOffset() >=0 ){
                return node.getNode();
            }
        }
        return null;
    }

    public Action[] getActions() {
        return actions;
    }

    public static final class PreBuildModel {
        final List<CppDeclarationNode> newList = new ArrayList<CppDeclarationNode>();
        final List<IndexOffsetNode> newLineNumberIndex = new ArrayList<IndexOffsetNode>();
        final boolean forceRebuild;

        public PreBuildModel(boolean forceRebuild) {
            this.forceRebuild = forceRebuild;
        }
    }
}
