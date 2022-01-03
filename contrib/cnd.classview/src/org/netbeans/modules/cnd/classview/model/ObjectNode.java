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

package org.netbeans.modules.cnd.classview.model;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.services.CsmFriendResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.classview.PersistentKey;
import org.openide.nodes.*;

import  org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.classview.actions.GoToDeclarationAction;
import org.netbeans.modules.cnd.classview.actions.MoreDeclarations;
import org.netbeans.modules.cnd.refactoring.api.ui.CsmRefactoringActionsFactory;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;

/**
 */
public abstract class ObjectNode extends BaseNode implements ChangeListener {
    private PersistentKey key;
    
    public ObjectNode(CsmOffsetableDeclaration declaration) {
        this(declaration, Children.LEAF);
    }

    public ObjectNode(CsmOffsetableDeclaration declaration, Children children) {
        super(children);
        setObject(declaration);
    }
    
    /** Implements AbstractCsmNode.getData() */
    @Override
    public final CsmObject getCsmObject() {
        return getObject();
    }
    
    public final CsmOffsetableDeclaration getObject() {
        return (CsmOffsetableDeclaration) key.getObject();
    }
    
    protected final void setObject(CsmOffsetableDeclaration declaration) {
        key = PersistentKey.createKey(declaration);
    }
    
    @Override
    public Action getPreferredAction() {
        return createOpenAction();
    }
    
    private Action createOpenAction() {
        CsmOffsetableDeclaration decl = getObject();
        if (decl != null) {
            return new GoToDeclarationAction(decl);
        }
        return null;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        List<Action> res = new ArrayList<Action>();
        Action action = createOpenAction();
        if (action != null){
            res.add(action);
            CsmOffsetableDeclaration decl = getObject();
            CharSequence name = decl.getUniqueName();
            final CsmFile file = decl.getContainingFile();
            CsmProject project = file.getProject();
            if (project != null){
                Collection<CsmOffsetableDeclaration> arr = project.findDeclarations(name);
                for(CsmFriend friend : CsmFriendResolver.getDefault().findFriends(decl)){
                    if (CsmKindUtilities.isFriendMethod(friend)) {
                        arr.add(friend);
                    }
                }
                if (CsmKindUtilities.isFunctionDeclaration(decl)) {
                    // add all definitions
                    CsmFunctionDefinition def = ((CsmFunction)decl).getDefinition();
                    if (def != null && def != decl) {
                        arr.addAll(project.findDeclarations(def.getUniqueName()));
                        
                    }
                }
                if (arr.size() > 1){
                    Action more = new MoreDeclarations(arr);
                    res.add(more);
                }
            }
            if (CsmRefactoringActionsFactory.supportRefactoring(file)) {
                res.add(RefactoringActionsFactory.renameAction());
                res.add(RefactoringActionsFactory.whereUsedAction());
                if (CsmKindUtilities.isField(decl) || CsmKindUtilities.isClass(decl)) {
                    res.add(CsmRefactoringActionsFactory.encapsulateFieldsAction());
                } else if (CsmKindUtilities.isFunction(decl) && !CsmKindUtilities.isDestructor(decl)) {
                    res.add(CsmRefactoringActionsFactory.changeParametersAction());
                }
            }
        }
        return res.toArray(new Action[res.size()]);
    }
}
