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
