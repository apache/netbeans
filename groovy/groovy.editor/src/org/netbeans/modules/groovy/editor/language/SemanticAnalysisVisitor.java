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

package org.netbeans.modules.groovy.editor.language;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.groovy.editor.api.ASTUtils;

/**
 *
 * @author Martin Adamek
 */
public class SemanticAnalysisVisitor extends ClassCodeVisitorSupport {

    private final ModuleNode root;
    private final BaseDocument doc;
    private final Map<OffsetRange, Set<ColoringAttributes>> highlights;

    public SemanticAnalysisVisitor(ModuleNode root, BaseDocument document) {
        this.root = root;
        this.doc = document;
        this.highlights = new HashMap<OffsetRange, Set<ColoringAttributes>>();
    }

    public Map<OffsetRange, Set<ColoringAttributes>> annotate() {
        highlights.clear();

        for (Object object : root.getClasses()) {
            visitClass((ClassNode)object);
        }

        for (Object object : root.getMethods()) {
            visitMethod((MethodNode)object);
        }

        visitBlockStatement(root.getStatementBlock());

        return highlights;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return root.getContext();
    }

    @Override
    public void visitField(FieldNode node) {
        if (isInSource(node)) {
            OffsetRange range = ASTUtils.getRange(node, doc);
            EnumSet<ColoringAttributes> attributes = EnumSet.of(ColoringAttributes.FIELD);

            if (node.isStatic()) {
                attributes.add(ColoringAttributes.STATIC);
            }
            highlights.put(range, attributes);
        }
        super.visitField(node);
    }

    @Override
    public void visitConstructor(ConstructorNode node) {
        if (isInSource(node)) {
            // Beware, a ConstructorNode is a MethodNode as well, (see below)
            // but we have to catch the Constructors first.
            OffsetRange range = ASTUtils.getRange(node, doc);
            highlights.put(range, ColoringAttributes.CONSTRUCTOR_SET);
        }
        super.visitConstructor(node);
    }

    @Override
    public void visitMethod(MethodNode node) {
        if (isInSource(node)) {
            OffsetRange range = ASTUtils.getRange(node, doc);
            EnumSet<ColoringAttributes> attributes = EnumSet.of(ColoringAttributes.METHOD);

            if (node.isStatic()) {
                attributes.add(ColoringAttributes.STATIC);
            }
            highlights.put(range, attributes);
        }
        super.visitMethod(node);
    }

    @Override
    public void visitPropertyExpression(PropertyExpression node) {
        // FIXME: Houston, we have a problem:
        // the PropertyExpression comes with no line/column information
        // this is supposed to be fixed in Groovy. See:
        // http://jira.codehaus.org/browse/GROOVY-2575

        super.visitPropertyExpression(node);
    }

    @Override
    public void visitClass(ClassNode node) {
        if (isInSource(node)) {
            OffsetRange range = ASTUtils.getRange(node, doc);
            highlights.put(range, ColoringAttributes.CLASS_SET);
        }
        super.visitClass(node);
    }

    @Override
    public void visitVariableExpression(VariableExpression node) {
        Variable var = node.getAccessedVariable();

        if (var instanceof FieldNode) {
            if (isInSource(node)) {
                OffsetRange range = ASTUtils.getRange(node, doc);
                highlights.put(range, ColoringAttributes.FIELD_SET);
            }
        }
        super.visitVariableExpression(node);
    }

    private boolean isInSource(AnnotatedNode node) {
        return node.getLineNumber() > 0 && !node.hasNoRealSourcePosition();
    }
}
