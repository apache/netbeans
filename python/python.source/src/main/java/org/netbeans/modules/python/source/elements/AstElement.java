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
package org.netbeans.modules.python.source.elements;

import java.util.Set;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.python.source.PythonAstUtils;
import org.netbeans.modules.python.source.PythonParserResult;
import org.netbeans.modules.python.source.PythonStructureItem;
import org.netbeans.modules.python.source.PythonStructureScanner;
import org.netbeans.modules.python.source.scopes.SymbolTable;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Name;

/**
 * Elements representing a node in a parse tree
 *
 * @author Tor Norbye
 */
public class AstElement extends Element {
    protected PythonTree node;
    protected String name;
    protected ElementKind kind;
    //protected CompilationInfo info;
    protected Set<Modifier> modifiers;
    protected SymbolTable scopes;

    public AstElement(SymbolTable scopes, PythonTree node, String name, ElementKind kind) {
        //this.info = info;
        this.scopes = scopes;
        this.node = node;
        this.name = name;
        this.kind = kind;
    }

    public static AstElement create(PythonParserResult result, PythonTree node) {
        SymbolTable scopes = result.getSymbolTable();

        if (node instanceof FunctionDef) {
            return new PythonStructureItem(scopes, (FunctionDef)node);
        } else if (node instanceof ClassDef) {
            return new PythonStructureItem(scopes, (ClassDef)node);
        } else if (node instanceof Call) {
            String name = PythonAstUtils.getCallName((Call)node);
            return new AstElement(scopes, node, name, ElementKind.METHOD);
        } else if (node instanceof Name) {
            return new AstElement(scopes, node, ((Name)node).getInternalId(), ElementKind.VARIABLE);
        } else {
            return new AstElement(scopes, node, null, ElementKind.OTHER);
        }
    }

    public PythonTree getNode() {
        return node;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ElementKind getKind() {
        return kind;
    }

    @Override
    public Set<Modifier> getModifiers() {
        if (modifiers == null) {
            if (name != null && scopes.isPrivate(node, name)) {
                modifiers = IndexedElement.PRIVATE_MODIFIERS;
            } else {
                modifiers = IndexedElement.PUBLIC_MODIFIERS;
            }
        }

        return modifiers;
    }

    public Object getSignature() {
        return name;
    }
}
