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

package org.netbeans.modules.cnd.classview.actions;

import javax.swing.*;
import java.awt.event.*;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;

import  org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.services.CsmFunctionDefinitionResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;

import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.classview.resources.I18n;

/**
 */
public class GoToDeclarationAction extends AbstractAction {
    
    private final CsmOffsetable csmObject;
    private boolean more;
    
    public GoToDeclarationAction(CsmOffsetable csmObject) {
        this(csmObject, false);
    }

    public GoToDeclarationAction(CsmOffsetable csmObject, boolean more) {
        this.csmObject = csmObject;
        this.more = more;
        putValue(Action.NAME, I18n.getMessage("LBL_GoToDeclaration")); //NOI18N
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        final String taskName = "GoTo Declaratione"; //NOI18N
        Runnable run = new Runnable() {

            @Override
            public void run() {
                CsmOffsetable target = csmObject;
                if (!more) {
                    if (CsmKindUtilities.isFunctionDeclaration((CsmObject)csmObject)){
                        CsmFunctionDefinition def = ((CsmFunction)csmObject).getDefinition();
                        if (def != null){
                            target = def;
                        } else {
                            CsmReference ref = CsmFunctionDefinitionResolver.getDefault().getFunctionDefinition((CsmFunction)csmObject);
                            if (ref != null){
                                target = ref;
                            }
                        }
                    } else if(CsmKindUtilities.isVariableDeclaration((CsmObject)csmObject)){
                        CsmVariableDefinition def = ((CsmVariable)csmObject).getDefinition();
                        if (def != null){
                            target = def;
                        }
                    }
                }
                CsmUtilities.openSource(target);
            }
        };
        CsmModelAccessor.getModel().enqueue(run, taskName);
    }
}
