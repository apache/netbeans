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

import java.util.HashMap;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmCompoundClassifier;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmFriendClass;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.support.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.classview.model.ClassNode;
import org.netbeans.modules.cnd.classview.model.EnumNode;
import org.netbeans.modules.cnd.classview.model.EnumeratorNode;
import org.netbeans.modules.cnd.classview.model.ForwardClassNode;
import org.netbeans.modules.cnd.classview.model.FriendClassNode;
import org.netbeans.modules.cnd.classview.model.FriendFunctionNode;
import org.netbeans.modules.cnd.classview.model.GlobalFuncNode;
import org.netbeans.modules.cnd.classview.model.MemberNode;
import org.netbeans.modules.cnd.classview.model.TypedefNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 */
public class ClassifierKeyArray extends HostKeyArray implements UpdatebleHost {
    private static final boolean traceEvents = Boolean.getBoolean("cnd.classview.key-events"); // NOI18N
    
    public ClassifierKeyArray(ChildrenUpdater childrenUpdater, CsmCompoundClassifier classifier){
        super(childrenUpdater, classifier.getContainingFile().getProject(),PersistentKey.createKey(classifier));
    }
    
    public ClassifierKeyArray(ChildrenUpdater childrenUpdater, CsmTypedef typedef, CsmCompoundClassifier classifier){
        super(childrenUpdater, classifier.getContainingFile().getProject(), PersistentKey.createKey(typedef));
    }

    public ClassifierKeyArray(ChildrenUpdater childrenUpdater, CsmClassForwardDeclaration fd, CsmCompoundClassifier classifier){
        super(childrenUpdater, classifier.getContainingFile().getProject(), PersistentKey.createKey(fd));
    }
    
    @Override
    public boolean newNamespsce(CsmNamespace ns) {
        return false;
    }
    
    @Override
    public boolean removeNamespsce(CsmNamespace ns) {
        return false;
    }
    
    protected boolean canCreateNode(CsmOffsetableDeclaration d) {
        return true;
    }
    
    protected java.util.Map<PersistentKey, SortedName> getMembers() {
        java.util.Map<PersistentKey, SortedName> res = new HashMap<PersistentKey, SortedName>();
        try {
            CsmCompoundClassifier classifier = getClassifier();
            if (classifier != null) {
                if (CsmKindUtilities.isClass(classifier)) {
                    initClass((CsmClass) classifier, res);
                } else if (CsmKindUtilities.isEnum(classifier)) {
                    initEnum((CsmEnum) classifier, res);
                }
            }
        } catch (AssertionError ex){
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return res;
    }
    
    private void initClass(CsmClass cls, java.util.Map<PersistentKey, SortedName> res){
        for(CsmMember member : cls.getMembers()) {
            PersistentKey key = PersistentKey.createKey(member);
            if (key != null) {
                res.put(key, getSortedName(member));
            }
        }
        for (CsmFriend friend : cls.getFriends()){
            PersistentKey key = PersistentKey.createKey(friend);
            if (key != null) {
                res.put(key, getSortedName(friend));
            }
        }
    }
    
    private void initEnum(CsmEnum en, java.util.Map<PersistentKey, SortedName> res){
        for (CsmEnumerator val : en.getEnumerators()) {
            PersistentKey key = PersistentKey.createKey(val);
            if (key != null) {
                res.put(key, new SortedName(0,val.getName(),0));
            }
        }
    }
    
    private CsmCompoundClassifier getClassifier(){
        Object object = getHostId().getObject();
        if (object instanceof CsmCompoundClassifier) {
            return (CsmCompoundClassifier)object;
        } else if (CsmKindUtilities.isCsmObject(object) && CsmKindUtilities.isTypedef((CsmObject)object)){
            CsmTypedef def = (CsmTypedef) object;
	    CsmType type = def.getType();
	    if( type != null ) {
		return (CsmCompoundClassifier)type.getClassifier();
	    }
        } else if (CsmKindUtilities.isCsmObject(object) && CsmKindUtilities.isClassForwardDeclaration((CsmObject)object)){
            CsmClassForwardDeclaration fd = (CsmClassForwardDeclaration) object;
	    CsmClass cls = fd.getCsmClass();
	    if( cls != null ) {
		return cls;
	    }
        } 
	return null;
    }
    
    protected CsmOffsetableDeclaration findDeclaration(PersistentKey declId){
        CsmOffsetableDeclaration res = (CsmOffsetableDeclaration) declId.getObject();
        return res;
    }
    
//    private CsmNamespace findNamespace(String nsId){
//        return getProject().findNamespace(nsId);
//    }
    
    protected Node createNode(PersistentKey key) {
        ChildrenUpdater updater = getUpdater();
        Node node = null;
        if (updater != null) {
            try {
                CsmOffsetableDeclaration member = findDeclaration(key);
                if (member != null) {
                    if (CsmKindUtilities.isClass(member)) {
                        node = new ClassNode((CsmClass) member, new ClassifierKeyArray(updater, (CsmClass) member));
                    } else if (CsmKindUtilities.isEnum(member)) {
                        node = new EnumNode((CsmEnum) member, new ClassifierKeyArray(updater, (CsmEnum) member));
                    } else if (CsmKindUtilities.isEnumerator(member)) {
                        node = new EnumeratorNode((CsmEnumerator) member);
                    } else if (CsmKindUtilities.isFriendClass(member)) {
                        node = new FriendClassNode((CsmFriendClass) member);
                    } else if (CsmKindUtilities.isFriendMethod(member)) {
                        node = new FriendFunctionNode((CsmFriendFunction) member);
                    } else if (CsmKindUtilities.isTypedef(member)) {
                        CsmTypedef def = (CsmTypedef) member;
                        if (def.isTypeUnnamed()) {
                            CsmClassifier cls = def.getType().getClassifier();
                            if (cls != null && cls.getName().length()==0 &&
                                    (cls instanceof CsmCompoundClassifier)) {
                                return new TypedefNode(def, new ClassifierKeyArray(updater, def, (CsmCompoundClassifier) cls));
                            }
                        }
                        node = new MemberNode((CsmMember) member);
                    } else if (CsmKindUtilities.isClassForwardDeclaration(member)) {
                        CsmClassForwardDeclaration fd = (CsmClassForwardDeclaration) member;
                        CsmClass csmClass = fd.getCsmClass();
                        if (csmClass != null) {
                            if (CsmClassifierResolver.getDefault().isForwardClass(csmClass)) {
                                node = new ForwardClassNode(fd, Children.LEAF);
                            } else {
                                node = new ForwardClassNode(fd, new ClassifierKeyArray(updater, fd, csmClass));
                            }
                        } else {
                            node = new MemberNode((CsmMember) member);
                        }
                    } else if (CsmKindUtilities.isClassMember(member)) {
                        node = new MemberNode((CsmMember) member);
                    } else if (CsmKindUtilities.isFunction(member)) {
                        if (traceEvents) {
                            System.out.println("It should be member:" + member.getUniqueName()); // NOI18N
                        }
                        node = new GlobalFuncNode((CsmFunction) member);
                    } else {
                        if (traceEvents) {
                            System.out.println("It should be member:" + member.getUniqueName()); // NOI18N
                        }
                    }
                }
            } catch (AssertionError ex){
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return node;
    }
}
