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
