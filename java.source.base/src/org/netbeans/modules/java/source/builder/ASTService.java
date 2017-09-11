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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.java.source.builder;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.*;

import com.sun.source.tree.*;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.*;

import static com.sun.tools.javac.tree.JCTree.*;

/**
 * A javac abstract syntax tree which maps all nodes to a single root.
 */
public final class ASTService {
    
    private static final Context.Key<ASTService> treeKey = new Context.Key<ASTService>();

    public static synchronized ASTService instance(Context context) {
        ASTService instance = context.get(treeKey);
        if (instance == null)
            instance = new ASTService(context);
        return instance;
    }

    /**
     * Create a new Trees, using an existing root node.
     */
    protected ASTService(Context context) {
        context.put(treeKey, this);
    }
    
    /**
     * Returns the element for a specified tree.  Null is returned if the
     * tree type doesn't have an associated element, or if the reference
     * is not resolved.
     */
    public Element getElement(Tree tree) {
        return getElementImpl(tree);
    }

    public static Element getElementImpl(Tree tree) {
        if (tree == null)
            return null;
        switch (tree.getKind()) {
            case COMPILATION_UNIT: return ((JCCompilationUnit)tree).packge;
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
                return ((JCClassDecl)tree).sym;
            case METHOD:           return ((JCMethodDecl)tree).sym;
            case VARIABLE:         return ((JCVariableDecl)tree).sym;
            case MEMBER_SELECT:    return ((JCFieldAccess)tree).sym;
            case IDENTIFIER:       return ((JCIdent)tree).sym;
            case NEW_CLASS:        return ((JCNewClass)tree).constructor;
            default:
                return null;
        }
    }

    public TypeMirror getType(Tree tree) {
        if (tree == null)
            return null;
        TypeMirror type = ((JCTree)tree).type;
        if (type == null) {
            Element e = getElement(tree);
            if (e != null)
                type = e.asType();
        }
        return type;
    }

    /**
     * Sets the element associated with a Tree.  This should only be done
     * either on trees created by TreeMaker or clone(), and never on original
     * trees.
     *
     * @see org.netbeans.api.java.source.TreeMaker
     * @see #clone
     */
    public void setElement(Tree tree, Element element) {
        switch (((JCTree)tree).getTag()) {
            case TOPLEVEL:
                ((JCCompilationUnit)tree).packge = (Symbol.PackageSymbol)element;
                break;
            case CLASSDEF:
                ((JCClassDecl)tree).sym = (Symbol.ClassSymbol)element;
                break;
            case METHODDEF:
                ((JCMethodDecl)tree).sym = (Symbol.MethodSymbol)element;
                break;
            case VARDEF:
                ((JCVariableDecl)tree).sym = (Symbol.VarSymbol)element;
                break;
            case SELECT:
                ((JCFieldAccess)tree).sym = (Symbol)element;
                break;
            case IDENT:
                ((JCIdent)tree).sym = (Symbol)element;
                break;
            case NEWCLASS:
                ((JCNewClass)tree).constructor = (Symbol)element;
                break;
            default:
                throw new IllegalArgumentException("invalid tree type: " + tree.getKind());
        }
    }

    /**
     * Sets the TypeMirror associated with a Tree.  This should only be done
     * either on trees created by TreeMaker or clone(), and never on original
     * trees.
     *
     * @see org.netbeans.api.java.source.TreeMaker
     * @see #clone
     */
    public void setType(Tree tree, TypeMirror type) {
        ((JCTree)tree).type = (Type)type;
    }

    /**
     * Get the position for a tree node.
     */
    public int getPos(Tree tree) {
        if (tree == null)
	    return Position.NOPOS;
        return ((JCTree)tree).pos;
    }
    
    public void setPos(Tree tree, int newPos) {
        ((JCTree)tree).pos = newPos;
    }
    
}
