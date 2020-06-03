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

import java.util.Iterator;
import javax.swing.JEditorPane;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;

public class ContextUtils {
    private ContextUtils() {
    }

    public static CsmOffsetableDeclaration getContext(Node[] activatedNodes){
        if (activatedNodes != null && activatedNodes.length > 0){
            return ContextUtils.findDeclaration(activatedNodes[0]);
        }
        return null;
    }

    private static CsmOffsetableDeclaration findDeclaration(Node activatedNode) {
        EditorCookie c = activatedNode.getCookie(EditorCookie.class);
        if (c != null) {
            JEditorPane pane = CsmUtilities.findRecentEditorPaneInEQ(c);
            if (pane != null ) {
                int offset = pane.getCaret().getDot();
                CsmFile file = CsmUtilities.getCsmFile(activatedNode,false);
                if (file != null){
                    return findInnerFileDeclaration(file, offset);
                }
            }
        }
        return null;
    }
    
    private static CsmOffsetableDeclaration findInnerFileDeclaration(CsmFile file, int offset) {
        CsmOffsetableDeclaration innerDecl = null;
        for (CsmOffsetableDeclaration decl : file.getDeclarations()) {
            if (isInObject(decl, offset)) {
                innerDecl = findInnerDeclaration(decl, offset);
                innerDecl = innerDecl != null ? innerDecl : decl;
                break;
            }
        }
        return innerDecl;
    }

    private static CsmOffsetableDeclaration findInnerDeclaration(CsmOffsetableDeclaration outDecl, int offset) {
        Iterator it = null;
        CsmOffsetableDeclaration innerDecl = null;
        if (CsmKindUtilities.isNamespaceDefinition(outDecl)) {
            it = ((CsmNamespaceDefinition) outDecl).getDeclarations().iterator();
        } else if (CsmKindUtilities.isClass(outDecl)) {
            CsmClass cl  = (CsmClass)outDecl;
            it = cl.getMembers().iterator();
        }
        if (it != null) {
            while (it.hasNext()) {
                CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) it.next();
                if (isInObject(decl, offset)) {
                    innerDecl = findInnerDeclaration(decl, offset);
                    innerDecl = innerDecl != null ? innerDecl : decl;
                    break;
                }
            }
        }
        return innerDecl;
    }    

    private static boolean isInObject(CsmObject obj, int offset) {
        if (!CsmKindUtilities.isOffsetable(obj)) {
            return false;
        }
        CsmOffsetable offs = (CsmOffsetable)obj;
        if ((offs.getStartOffset() <= offset) &&
                (offset <= offs.getEndOffset())) {
            return true;
        }
        return false;
    }
}
