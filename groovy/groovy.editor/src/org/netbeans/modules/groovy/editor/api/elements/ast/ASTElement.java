/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.groovy.editor.api.elements.ast;

import groovyjarjarasm.asm.Opcodes;
import java.util.*;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.elements.GroovyElement;

/**
 *
 * @author Martin Adamek
 */
public abstract class ASTElement extends GroovyElement {

    protected final ASTNode node;
    protected final List<ASTElement> children;
    protected Set<Modifier> modifiers;


    public ASTElement(ASTNode node) {
        this(node, null);
    }

    public ASTElement(ASTNode node, String in) {
        this(node, in, null);
    }

    public ASTElement(ASTNode node, String in, String name) {
        super(in, name);
        this.node = node;
        this.children = new ArrayList<>();
        if (node != null) {
            node.putNodeMetaData(ASTElement.class, this);
        }
    }

    public ASTNode getNode() {
        return node;
    }

    public List<ASTElement> getChildren() {
        return children;
    }

    public void addChild(ASTElement child) {
        children.add(child);
    }

    @Override
    public Set<Modifier> getModifiers() {
        if (modifiers == null) {
            int flags = -1;
            if (node instanceof FieldNode) {
                flags = ((FieldNode) node).getModifiers();
            } else if (node instanceof MethodNode) {
                flags = ((MethodNode) node).getModifiers();
            }
            if (flags != -1) {
                Set<Modifier> result = EnumSet.noneOf(Modifier.class);
                if ((flags & Opcodes.ACC_PUBLIC) != 0) {
                    result.add(Modifier.PUBLIC);
                }
                if ((flags & Opcodes.ACC_PROTECTED) != 0) {
                    result.add(Modifier.PROTECTED);
                }
                if ((flags & Opcodes.ACC_PRIVATE) != 0) {
                    result.add(Modifier.PRIVATE);
                }
                if ((flags & Opcodes.ACC_STATIC) != 0) {
                    result.add(Modifier.STATIC);
                }
                modifiers = result;
            } else {
                modifiers = Collections.<Modifier>emptySet();
            }
        }

        return modifiers;
    }

    @Override
    public boolean signatureEquals(final ElementHandle handle) {
        if (handle instanceof ASTElement) {
            return this.equals(handle);
        }
        return false;
    }

    @Override
    public OffsetRange getOffsetRange(ParserResult result) {
        BaseDocument doc = (BaseDocument) result.getSnapshot().getSource().getDocument(false);
        int lineNumber = node.getLineNumber();
        int columnNumber = node.getColumnNumber();
        int start = ASTUtils.getOffset(doc, lineNumber, columnNumber);

        return new OffsetRange(start, start);
    }

    public static ASTElement create(ASTNode node) {
        if (node instanceof MethodNode) {
            return new ASTMethod(node);
        }
        return null;
    }

    @Override
    public String toString() {
        return getKind() + "<" + getName() + ">";
    }
}
