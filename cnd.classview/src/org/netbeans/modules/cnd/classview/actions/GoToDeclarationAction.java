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
