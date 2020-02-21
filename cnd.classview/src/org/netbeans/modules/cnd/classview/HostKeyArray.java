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

package org.netbeans.modules.cnd.classview;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.classview.model.CVUtil;
import org.netbeans.modules.cnd.modelutil.AbstractCsmNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.CharSequences;
import org.openide.util.RequestProcessor;

/**
 *
 */
abstract public class HostKeyArray extends Children.Keys<PersistentKey> implements UpdatebleHost{
    private static final boolean traceEvents = Boolean.getBoolean("cnd.classview.key-events"); // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(HostKeyArray.class.getName(), 1);
    // for testing only
    private static final boolean noLoadinNode = Boolean.getBoolean("cnd.classview.no-loading-node"); // NOI18N
    private static Comparator<java.util.Map.Entry<PersistentKey, SortedName>> COMARATOR = new MyComparator();
    
    private ChildrenUpdater childrenUpdater;
    private CsmProject myProject;
    private PersistentKey myID;
    private boolean update;
    private java.util.Map<PersistentKey,SortedName> myKeys ;
    private java.util.Map<PersistentKey,ChangeListener> myChanges;
    private boolean isInited = false;
    private boolean isDisposed = false;

    public HostKeyArray(ChildrenUpdater childrenUpdater, CsmProject project, PersistentKey id) {
        this.childrenUpdater = childrenUpdater;
        this.myProject = project;
        this.myID = id;
        childrenUpdater.register(project,id,this);
    }
    
    protected ChildrenUpdater getUpdater(){
        return childrenUpdater;
    }
    
    protected void dispose(){
        this.isDisposed = true;
        if (isInited) {
            isInited = false;
            myKeys.clear();
            myChanges.clear();
            childrenUpdater.unregister(myProject, myID);
            //setKeys(new PersistentKey[0]);
        }
    }
    
    private synchronized void resetKeys(){
        List<java.util.Map.Entry<PersistentKey,SortedName>> list =
                new ArrayList<java.util.Map.Entry<PersistentKey,SortedName>>();
        if (myKeys != null){
            list.addAll(myKeys.entrySet());
        }
        Collections.sort(list, COMARATOR);
        final List<PersistentKey> res = new ArrayList<PersistentKey>();
        for(java.util.Map.Entry<PersistentKey,SortedName> entry :list){
            PersistentKey key = entry.getKey();
            if (key != null) {
                res.add(key);
            }
        }
        setKeys(res);
    }

    abstract protected java.util.Map<PersistentKey,SortedName> getMembers();
    abstract protected CsmOffsetableDeclaration findDeclaration(PersistentKey key);
    abstract protected boolean canCreateNode(CsmOffsetableDeclaration d);
    abstract protected Node createNode(PersistentKey key);
    
    protected boolean isGlobalNamespace() {
        return false;
    }
    
    protected boolean isNamespace() {
        return false;
    }
    
    protected SortedName getSortedName(CsmNamespace ns){
        return new SortedName(0,CVUtil.getNamespaceDisplayName(ns),0);
    }
    
    protected SortedName getSortedName(CsmOffsetableDeclaration d){
        if( CsmKindUtilities.isClass(d) ) {
            return new SortedName(1,d.getName(),0);
        } else if( d.getKind() == CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION ) {
            return new SortedName(1,d.getName(),0);
        } else if( d.getKind() == CsmDeclaration.Kind.ENUM ) {
            return new SortedName(1,d.getName(),1);
        } else if (d.getKind() == CsmDeclaration.Kind.ENUM_FORWARD_DECLARATION) {
            return new SortedName(1, d.getName(), 1);
        } else if( d.getKind() == CsmDeclaration.Kind.TYPEDEF ) {
            return new SortedName(1,d.getName(),2);
        } else if( d.getKind() == CsmDeclaration.Kind.VARIABLE ) {
            return new SortedName(2,d.getName(),0);
        } else if( d.getKind() == CsmDeclaration.Kind.FUNCTION ) {
            return new SortedName(3,CVUtil.getSignature((CsmFunction)d),0);
        } else if( d.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ) {
            return new SortedName(3,CVUtil.getSignature((CsmFunction)d),1);
        } else if( d.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND ) {
            return new SortedName(3,CVUtil.getSignature((CsmFunction)d),0);
        } else if( d.getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION ) {
            return new SortedName(3,CVUtil.getSignature((CsmFunction)d),1);
        }
        return new SortedName(9,d.getName(),0);
    }
    
    protected CsmProject getProject(){
        return myProject;
    }
    
    protected PersistentKey getHostId(){
        return myID;
    }
    
    @Override
    public boolean newNamespsce(CsmNamespace ns) {
        if (!isInited){
            return false;
        }
        PersistentKey key = PersistentKey.createKey(ns);
        myKeys.put(key,getSortedName(ns));
        update = true;
        return true;
    }
    
    @Override
    public boolean removeNamespsce(CsmNamespace ns) {
        if (!isInited){
            return false;
        }
        PersistentKey key = PersistentKey.createKey(ns);
        myKeys.remove(key);
        childrenUpdater.unregister(myProject,key);
        update = true;
        return true;
    }
    
    @Override
    public boolean newDeclaration(CsmOffsetableDeclaration decl) {
        if (!isInited){
            return false;
        }
        if (CsmKindUtilities.isFunctionDefinition(decl)) {
            CsmFunctionDefinition def = (CsmFunctionDefinition) decl;
            CsmFunction fun = def.getDeclaration();
            if (fun != null && fun != decl){
                PersistentKey funKey = PersistentKey.createKey(fun);
                if (myKeys.containsKey(funKey)) {
                    return false;
                }
                decl= fun;
            }
        } else if (CsmKindUtilities.isFunctionDeclaration(decl)) {
            CsmFunction fun = (CsmFunction)decl;
            CsmFunctionDefinition def = fun.getDefinition();
            if (def != null && def != decl){
                PersistentKey defKey = PersistentKey.createKey(def);
                if (myKeys.containsKey(defKey)) {
                    myKeys.remove(defKey);
                    myChanges.remove(defKey);
                    childrenUpdater.unregister(myProject,defKey);
                }
            }
        }
        PersistentKey key = PersistentKey.createKey(decl);
        myKeys.put(key,getSortedName(decl));
        myChanges.remove(key);
        update = true;
        return true;
    }
    
    @Override
    public boolean removeDeclaration(CsmOffsetableDeclaration decl) {
        if (!isInited){
            return false;
        }
        PersistentKey key = PersistentKey.createKey(decl);
        if (!myKeys.containsKey(key)){
            return false;
        }
        myKeys.remove(key);
        myChanges.remove(key);
        childrenUpdater.unregister(myProject,key);
        if (CsmKindUtilities.isFunctionDeclaration(decl)) {
            removeFunctionDeclaration(decl);
        } else if (CsmKindUtilities.isFunctionDefinition(decl)) {
            removeFunctionDefinition(decl);
        }
        update = true;
        return true;
    }
    
    private void removeFunctionDeclaration(final CsmOffsetableDeclaration decl) {
        CsmFunction fun = (CsmFunction)decl;
        CsmFile file = fun.getContainingFile();
        if (file != null && file.isValid()){
            CsmOffsetableDeclaration other = (CsmOffsetableDeclaration) file.getProject().findDeclaration(fun.getUniqueName());
            if (other != null) {
                PersistentKey otherKey = PersistentKey.createKey(other);
                if (!myKeys.containsKey(otherKey)) {
                    myKeys.put(otherKey,getSortedName(other));
                    myChanges.remove(otherKey);
                    return;
                }
            }
        }
        CsmFunctionDefinition def = fun.getDefinition();
        if (def != null && def != decl){
            file = fun.getContainingFile();
            if (file != null && file.isValid() &&
                    file.getProject().findDeclaration(def.getUniqueName()) != null){
                PersistentKey defKey = PersistentKey.createKey(def);
                if (!myKeys.containsKey(defKey)) {
                    myKeys.put(defKey,getSortedName(def));
                    myChanges.remove(defKey);
                }
            }
        }
    }
    
    private void removeFunctionDefinition(final CsmOffsetableDeclaration decl) {
        CsmFunctionDefinition def = (CsmFunctionDefinition) decl;
        CsmFunction fun = def.getDeclaration();
        if (fun != null && fun != decl){
            CsmFile file = fun.getContainingFile();
            if (file != null && file.isValid()){
                CsmOffsetableDeclaration other = (CsmOffsetableDeclaration) file.getProject().findDeclaration(fun.getUniqueName());
                if (other != null) {
                    PersistentKey otherKey = PersistentKey.createKey(other);
                    if (!myKeys.containsKey(otherKey)) {
                        myKeys.put(otherKey,getSortedName(other));
                        myChanges.remove(otherKey);
                        return;
                    }
                }
            }
        }
        CsmFile file = def.getContainingFile();
        if (file != null && file.isValid()){
            CsmOffsetableDeclaration other = (CsmOffsetableDeclaration) file.getProject().findDeclaration(def.getUniqueName());
            if (other != null) {
                PersistentKey otherKey = PersistentKey.createKey(other);
                if (!myKeys.containsKey(otherKey)) {
                    myKeys.put(otherKey,getSortedName(other));
                    myChanges.remove(otherKey);
                }
            }
        }
    }
    
    @Override
    public boolean changeDeclaration(CsmOffsetableDeclaration oldDecl,CsmOffsetableDeclaration newDecl) {
        if (!isInited){
            return false;
        }
        PersistentKey oldKey = PersistentKey.createKey(oldDecl);
        if (newDecl == null) {
            // remove non-existent element
            myKeys.remove(oldKey);
            myChanges.remove(oldKey);
            childrenUpdater.unregister(myProject,oldKey);
            update = true;
            return true;
        }
        PersistentKey newKey = PersistentKey.createKey(newDecl);
        if (oldKey.equals(newKey)) {
            if (myKeys.containsKey(newKey)){
                myKeys.put(newKey,getSortedName(newDecl));
                ChangeListener l = myChanges.get(newKey);
                if (l != null) {
                    l.stateChanged(new ChangeEvent(newDecl));
                }
                return updateFunction(newDecl);
            } else {
                return newDeclaration(newDecl);
            }
        }
        removeDeclaration(oldDecl);
        newDeclaration(newDecl);
        update = true;
        return true;
    }
    
    private boolean updateFunction(CsmOffsetableDeclaration decl){
        if (CsmKindUtilities.isFunctionDeclaration(decl)) {
            // try to find definition and remove definition from view
            CsmFunction fun = (CsmFunction) decl;
            CsmFunctionDefinition def = fun.getDefinition();
            if (def != null && def != decl){
                PersistentKey defKey = PersistentKey.createKey(def);
                if (myKeys.containsKey(defKey)) {
                    myKeys.remove(defKey);
                    myChanges.remove(defKey);
                    childrenUpdater.unregister(myProject,defKey);
                    update = true;
                    return true;
                }
            }
        } else if (CsmKindUtilities.isFunctionDefinition(decl)) {
            // try to find declaration and remove definition from view
            CsmFunctionDefinition def = (CsmFunctionDefinition) decl;
            CsmFunction fun = def.getDeclaration();
            if (fun != null && fun != decl){
                PersistentKey funKey = PersistentKey.createKey(fun);
                if (myKeys.containsKey(funKey)) {
                    return removeDeclaration(decl);
                }
            }
        }
        return false;
    }
    
    @Override
    public boolean reset(CsmOffsetableDeclaration decl, List<CsmOffsetableDeclaration> recursive){
        myID = PersistentKey.createKey(decl);
        if (!isInited){
            return false;
        }
        boolean needUpdate = false;
        java.util.Map<PersistentKey,SortedName> members = getMembers();
        List<PersistentKey> toDelete = null;
        for(PersistentKey key : myKeys.keySet()){
            if (!members.containsKey(key)){
                // delete
                if (toDelete == null) {
                    toDelete = new ArrayList<PersistentKey>();
                }
                toDelete.add(key);
            }
        }
        if (toDelete != null){
            for(PersistentKey key : toDelete){
                myKeys.remove(key);
                myChanges.remove(key);
                needUpdate = true;
            }
        }
        for (java.util.Map.Entry<PersistentKey, SortedName> entry : members.entrySet()) {
            PersistentKey key = entry.getKey();
            if (myKeys.containsKey(key)) {
                // update
                myKeys.put(key, entry.getValue());
                CsmOffsetableDeclaration what = findDeclaration(key);
                if (what == null) {
                    // remove non-existent element
                    myKeys.remove(key);
                    myChanges.remove(key);
                    needUpdate = true;
                } else {
                    ChangeListener l = myChanges.get(key);
                    if (l != null) {
                        l.stateChanged(new ChangeEvent(what));
                    }
                    if (CsmKindUtilities.isClassifier(what) ||
                            CsmKindUtilities.isEnum(what)) {
                        recursive.add(what);
                    }
                }
            } else {
                // new
                myKeys.put(key, entry.getValue());
                myChanges.remove(key);
                needUpdate = true;
            }
        }
        if (needUpdate) {
            update = true;
            return true;
        }
        return false;
    }
    
    @Override
    public void flush() {
        if (update &&  isInited){
            resetKeys();
        }
        update = false;
    }
    
    @Override
    protected Node[] createNodes(PersistentKey object) {
        Node node = createNode(object);
        if (node != null) {
            if (node instanceof ChangeListener){
                myChanges.put(object,(ChangeListener)node);
            }
            return new Node[]{node};
        }
        return new Node[0];
    }

    public Node findChild(CsmObject object) {
        Node[] list = getNodes();
        if (list.length == 0) {
            return null;
        }
        if (object == null) {
            return list[0];
        }
        Node res = null;
        CharSequence qname = null;
        if (CsmKindUtilities.isQualified(object)){
            qname = ((CsmQualifiedNamedElement)object).getQualifiedName();
        }
        CharSequence signature = null;
        if (CsmKindUtilities.isFunction(object)){
            signature = ((CsmFunction)object).getSignature();
        }
        for (int i = 0; i < list.length; i++) {
            if (list[i] instanceof AbstractCsmNode){
                CsmObject tested = ((AbstractCsmNode)list[i]).getCsmObject();
                if (object.equals(tested)){
                    // exact search
                    return list[i];
                } else if (res == null && tested != null && qname != null) {
                    // unique name search
                    if (CsmKindUtilities.isQualified(tested)){
                        CharSequence testedName = ((CsmQualifiedNamedElement)tested).getQualifiedName();
                        if (CharSequences.comparator().compare(qname, testedName)==0){
                            if (CsmKindUtilities.isFunction(object) || CsmKindUtilities.isFunction(tested)){
                                if (CsmKindUtilities.isFunction(object) && CsmKindUtilities.isFunction(tested)){
                                    CharSequence testedSignature = ((CsmFunction)tested).getSignature();
                                    if (CharSequences.comparator().compare(signature, testedSignature)==0){
                                        return list[i];
                                    }
                                    // for pure C
                                    res = list[i];
                                }
                            } else {
                                res = list[i];
                            }
                        }
                    }
                }
            }
        }
        return res;
    }

    public void ensureInited(){
        synchronized (childrenUpdater.getLock(getProject())) {
            if (!isInited) {
                addNotify(true);
            }
        }
    }

    @Override
    protected void addNotify() {
        addNotify(noLoadinNode);
    }
    
    private void addNotify(boolean force) {
        synchronized (childrenUpdater.getLock(getProject())) {
            if (isInited || isDisposed) {
                return;
            }
            if (isNamespace() && !force){ //isGlobalNamespace()) {
                myKeys = new HashMap<PersistentKey,SortedName>();
                myKeys.put(PersistentKey.createKey(getProject()), new SortedName(0,"",0)); // NOI18N
            } else {
                myKeys = getMembers();
            }
            myChanges = new HashMap<PersistentKey,ChangeListener>();
            isInited = true;
            resetKeys();
        }
        super.addNotify();
        if (isNamespace() && !force){ //isGlobalNamespace()) {
            RP.post(new Runnable(){
                @Override
                public void run() {
                    synchronized (childrenUpdater.getLock(getProject())) {
                        myKeys = getMembers();
                        resetKeys();
                    }
                }
            });
        }
    }
    
    @Override
    protected void removeNotify() {
        super.removeNotify();
        isInited = false;
        myKeys.clear();
        myChanges.clear();
        childrenUpdater.unregister(myProject, myID);
        resetKeys();
        if (traceEvents) {
            System.out.println("Remove key "+myID.toString()); // NOI18N
        }
    }
    
    @Override
    protected void destroyNodes(Node[] node) {
        for (Node n : node){
            Children children = n.getChildren();
            if (children instanceof HostKeyArray){
                ((HostKeyArray)children).dispose();
            }
        }
        super.destroyNodes(node);
        if (traceEvents) {
            System.out.println("Destroy nodes "+node.length+" in "+myID.toString()); // NOI18N
        }
    }
    
    protected void onPprojectParsingFinished(CsmProject project) {
        if (!isInited || project != getProject()){
            return;
        }
        synchronized (childrenUpdater.getLock(getProject())) {
            PersistentKey key = PersistentKey.createKey(project);
            if (myKeys.containsKey(key)){
                myKeys.remove(key);
                resetKeys();
            }
        }
    }
    
    private static class MyComparator implements Comparator<java.util.Map.Entry<PersistentKey, SortedName>>, Serializable {
        @Override
        public int compare(java.util.Map.Entry<PersistentKey, SortedName> o1, java.util.Map.Entry<PersistentKey, SortedName> o2) {
            SortedName n1 = o1.getValue();
            SortedName n2 = o2.getValue();
            int res = n1.compareTo(n2);
            if (res != 0) {
                return res;
            }
            String s1 = o1.getKey().toString();
            String s2 = o2.getKey().toString();
            return s1.compareTo(s2);
        }
    }
}
