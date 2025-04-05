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
            case RECORD:
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
