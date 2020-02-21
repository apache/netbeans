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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
