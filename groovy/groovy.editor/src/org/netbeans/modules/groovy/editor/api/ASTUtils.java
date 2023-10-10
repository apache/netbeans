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

package org.netbeans.modules.groovy.editor.api;

import groovyjarjarasm.asm.Opcodes;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.FinderFactory;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.elements.ast.ASTElement;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedElement;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.netbeans.modules.groovy.editor.occurrences.VariableScopeVisitor;
import org.netbeans.modules.groovy.editor.utils.ASTChildrenVisitor;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Adamek
 */
public class ASTUtils {

    private static final Logger LOGGER = Logger.getLogger(ASTUtils.class.getName());

    public static int getAstOffset(Parser.Result info, int lexOffset) {
        GroovyParserResult result = getParseResult(info);
        if (result != null) {
            return result.getSnapshot().getEmbeddedOffset(lexOffset);
        }
        return lexOffset;
    }

    public static GroovyParserResult getParseResult(Parser.Result info) {
        assert info instanceof GroovyParserResult : "Expecting GroovyParseResult, but have " + info; //NOI18N
        return (GroovyParserResult) info;
    }

    public static ModuleNode getRoot(ParserResult r) {
        assert r instanceof GroovyParserResult;

        GroovyParserResult result = (GroovyParserResult)r;

        if (result.getRootElement() == null) {
            return null;
        }
        
        return result.getRootElement().getModuleNode();
    }

    public static OffsetRange getRangeFull(ASTNode node, BaseDocument doc) {
            if (node.getLineNumber() < 0 || node.getColumnNumber() < 0 || node.getLastLineNumber() < 0 || node.getLastColumnNumber() < 0) {
                return OffsetRange.NONE;
            }
            int start = getOffset(doc, node.getLineNumber(), node.getColumnNumber());
            if (start < 0) {
                start = 0;
            }
            int end = getOffset(doc, node.getLastLineNumber(), node.getLastColumnNumber());
            if (end < 0) {
                end = 0;
            }
            if (start > end) {
                return OffsetRange.NONE;
            }
            return new OffsetRange(start, end);
    }

    @NonNull
    public static OffsetRange getRange(ASTNode node, BaseDocument doc) {

        // Warning! The implicit class and some other nodes has line/column numbers below 1
        // if line is wrong, let's invalidate also column and vice versa
        int lineNumber = node.getLineNumber();
        int columnNumber = node.getColumnNumber();
        if (lineNumber < 1 || columnNumber < 1) {
            return OffsetRange.NONE;
        }
        if (doc == null) {
            LOGGER.log(Level.INFO, "Null document in getRange()");
            return OffsetRange.NONE;
        }

        if (node instanceof FieldNode) {
            int start = getOffset(doc, lineNumber, columnNumber);
            FieldNode fieldNode = (FieldNode) node;
            return getNextIdentifierByName(doc, fieldNode.getName(), start);
        } else if (node instanceof ClassNode) {
            final ClassNode classNode = (ClassNode) node;
            int start = getOffset(doc, lineNumber, columnNumber);

            // classnode for script does not have real declaration and thus location
            if (classNode.isScript()) {
                return getNextIdentifierByName(doc, classNode.getNameWithoutPackage(), start);
            }

            // ok, here we have to move the Range to the first character
            // after the "class" keyword, plus an indefinite nuber of spaces
            // FIXME: have to check what happens with other whitespaces between
            // the keyword and the identifier (like newline)

            // happens in some cases when groovy source uses some non-imported java class
            if (doc != null) {

                // if we are dealing with an empty groovy-file, we have take into consideration,
                // that even though we're running on an ClassNode, there is no "class " String
                // in the sourcefile. So take doc.getLength() as maximum.

                int docLength = doc.getLength();
                int limit = getLimit(node, doc, docLength);

                try {
                    // we have to really search for class keyword other keyword
                    // (such as abstract) can precede class
                    start = doc.find(new FinderFactory.StringFwdFinder("class", true), start, limit) + "class".length(); // NOI18N
                } catch (BadLocationException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }

                if (start > docLength) {
                    start = docLength;
                }

                try {
                    start = Utilities.getFirstNonWhiteFwd(doc, start);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }

                // This seems to happen every now and then ...
                if (start < 0) {
                    start = 0;
                }

                int end = start + classNode.getNameWithoutPackage().length();

                if (end > docLength) {
                    end = docLength;
                }

                if (start == end) {
                    return OffsetRange.NONE;
                }
                return new OffsetRange(start, end);
            }
        } else if (node instanceof ConstructorNode) {
            int start = getOffset(doc, lineNumber, columnNumber);
            ConstructorNode constructorNode = (ConstructorNode) node;
            return getNextIdentifierByName(doc, constructorNode.getDeclaringClass().getNameWithoutPackage(), start);
        } else if (node instanceof MethodNode) {
            int start = getOffset(doc, lineNumber, columnNumber);
            MethodNode methodNode = (MethodNode) node;
            return getNextIdentifierByName(doc, methodNode.getName(), start);
        } else if (node instanceof VariableExpression) {
            int start = getOffset(doc, lineNumber, columnNumber);
            VariableExpression variableExpression = (VariableExpression) node;
            return getNextIdentifierByName(doc, variableExpression.getName(), start);
        } else if (node instanceof Parameter) {

            int docLength = doc.getLength();
            int start = getOffset(doc, node.getLineNumber(), node.getColumnNumber());
            int limit = getLimit(node, doc, docLength);

            Parameter parameter = (Parameter) node;
            String name = parameter.getName();

            try {
                // we have to really search for the name
                start = doc.find(new FinderFactory.StringFwdFinder(name, true), start, limit);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }

            int end = start + name.length();
            if (end > docLength) {
                return OffsetRange.NONE;
            }
            return getNextIdentifierByName(doc, name, start);
        } else if (node instanceof MethodCallExpression) {
            MethodCallExpression methodCall = (MethodCallExpression) node;
            Expression method = methodCall.getMethod();
            lineNumber = method.getLineNumber();
            columnNumber = method.getColumnNumber();
            if (lineNumber < 1 || columnNumber < 1) {
                lineNumber = 1;
                columnNumber = 1;
            }
            int start = getOffset(doc, lineNumber, columnNumber);
            return new OffsetRange(start, start + methodCall.getMethodAsString().length());
        } else if (node instanceof ConstructorCallExpression) {
            ConstructorCallExpression methodCall = (ConstructorCallExpression) node;
            String name = methodCall.getType().getNameWithoutPackage();
            // +4 because we don't want to have "new " in the offset
            // would be good to do this in more sofisticated way than this shit
            int start = getOffset(doc, lineNumber, columnNumber + 4);
            return getNextIdentifierByName(doc, name, start);
        } else if (node instanceof ClassExpression) {
            ClassExpression clazz = (ClassExpression) node;
            String name = clazz.getType().getNameWithoutPackage();
            int start = getOffset(doc, lineNumber, columnNumber);
            return getNextIdentifierByName(doc, name, start);
        } else if (node instanceof ConstantExpression) {
            ConstantExpression constantExpression = (ConstantExpression) node;
            int start = getOffset(doc, lineNumber, columnNumber);
            return new OffsetRange(start, start + constantExpression.getText().length());
        } else if (node instanceof FakeASTNode) {
            final String typeName = ElementUtils.getTypeNameWithoutPackage(((FakeASTNode) node).getOriginalNode());
            final int start = getOffset(doc, lineNumber, columnNumber);
            
            return getNextIdentifierByName(doc, typeName, start);
        }
        return OffsetRange.NONE;
    }

    @SuppressWarnings("unchecked")
    public static List<ASTNode> children(ASTNode root) {
        List<ASTNode> children = new ArrayList<>();

        if (root instanceof ModuleNode) {
            ModuleNode moduleNode = (ModuleNode) root;
            children.addAll(moduleNode.getClasses());
            children.add(moduleNode.getStatementBlock());
        } else if (root instanceof ClassNode) {
            ClassNode classNode = (ClassNode) root;

            Set<String> possibleMethods = new HashSet<>();
            for (Object object : classNode.getProperties()) {
                PropertyNode property = (PropertyNode) object;
                if (property.getLineNumber() >= 0) {
                    children.add(property);

                    FieldNode field = property.getField();
                    String fieldName = field.getName();
                    String fieldTypeName = field.getType().getNameWithoutPackage();

                    if (fieldName.length() > 0 && !field.isStatic() && (field.getModifiers() & Opcodes.ACC_PRIVATE) != 0) {

                        fieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                        if (!field.isFinal()) {
                            possibleMethods.add("set" + fieldName); // NOI18N
                        }
                        possibleMethods.add("get" + fieldName); // NOI18N

                        if ("Boolean".equals(fieldTypeName) || "boolean".equals(fieldTypeName)) { // NOI18N
                            possibleMethods.add("is" + fieldName); // NOI18N
                        }
                    }
                }

            }

            for (FieldNode field : classNode.getFields()) {
                if (field.getLineNumber() >= 0) {
                    children.add(field);
                }
            }

            for (MethodNode method : classNode.getMethods()) {
                // getMethods() returns all methods also from superclasses
                // how to get only methods from source?
                // for now, just check line number, if < 0 it is not from source
                // Second part of condition is for generated accessors
                if ((!method.isSynthetic() && (method.isAbstract() || method.getCode() != null))
                        || (method.isSynthetic() && possibleMethods.contains(method.getName()))) {
                    children.add(method);
                }

            }

            for (ConstructorNode constructor : classNode.getDeclaredConstructors()) {
                if (constructor.getLineNumber() >= 0) {
                    children.add(constructor);
                }
            }

        } else if (root instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) root;
            children.add(methodNode.getCode());
            children.addAll(Arrays.asList(methodNode.getParameters()));
        } else if (root instanceof Parameter) {
        } else if (root instanceof FieldNode) {
            FieldNode fieldNode = (FieldNode) root;
            Expression expression = fieldNode.getInitialExpression();
            if (expression != null) {
                children.add(expression);
            }
        } else if (root instanceof PropertyNode) {
            // FIXME (?)
        } else if (root != null) {
            ASTChildrenVisitor astChildrenSupport = new ASTChildrenVisitor();
            root.visit(astChildrenSupport);
            children = astChildrenSupport.children();
        }

        return children;
    }

    /**
     * Find offset in text for given line and column
     * Never returns negative number
     */
    public static int getOffset(BaseDocument doc, int lineNumber, int columnNumber) {
        assert lineNumber > 0 : "Line number must be at least 1 and was: " + lineNumber;
        assert columnNumber > 0 : "Column number must be at least 1 ans was: " + columnNumber;

        int offset = Utilities.getRowStartFromLineOffset(doc, lineNumber - 1);
        offset += (columnNumber - 1);

        // some sanity checks
        if (offset < 0){
            offset = 0;
        }

        return offset;
    }
    
    /**
     * Returns a simple name for a class. The result is not defined for local and
     * anonymous classes and for closures.
     * @param node the class
     * @return class' simple name
     */
    public static String getSimpleName(ClassNode node) {
        if (node == null) {
            return null;
        }
        if (node.getOuterClass() == null) {
            return node.getNameWithoutPackage();
        } else {
            String s = node.getName().substring(node.getOuterClass().getName().length());
            if (s.startsWith("$")) {
                return s.substring(1);
            } else {
                return s;
            }
        }
    }

    /**
     * Returns class' parent's name. For toplevel classes, returns the package name.
     * For inner classes, it returns the outer class' name. The result is undefined for
     * local, anonymous classes or closures.
     * @param node the class node.
     * @return parent name.
     */
    public static String getClassParentName(ClassNode node) {
        if (node == null) {
            return null;
        }
        if (node.getOuterClass() == null) {
            return node.getPackageName();
        } else {
            return node.getOuterClass().getName();
        }
    }

    public static ASTNode getForeignNode(final IndexedElement o) {

        final ASTNode[] nodes = new ASTNode[1];
        FileObject fileObject = o.getFileObject();
        assert fileObject != null : "null FileObject for IndexedElement " + o;

        try {
            Source source = Source.create(fileObject);
            // FIXME can we move this out of task (?)
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    GroovyParserResult result = ASTUtils.getParseResult(resultIterator.getParserResult());
                    
                    String signature = o.getSignature();
                    if (signature == null) {
                        return;
                    }
                    // strip class name from signature: Foo#method1() -> method1()
                    int index = signature.indexOf('#');
                    if (index != -1) {
                        signature = signature.substring(index + 1);
                    }
                    for (ASTElement element : result.getStructure().getElements()) {
                        ASTNode node = findBySignature(element, signature);
                        if (node != null) {
                            nodes[0] = node;
                            return;
                        }
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return nodes[0];
    }

    private static ASTNode findBySignature(ASTElement root, String signature) {

        if (signature.equals(root.getSignature())) {
            return root.getNode();
        } else {
            for (ASTElement element : root.getChildren()) {
                ASTNode node = findBySignature(element, signature);
                if (node != null) {
                    return node;
                }
            }
        }
        return null;
    }

    public static String getDefSignature(MethodNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(node.getName());

        Parameter[] parameters = node.getParameters();
        if (parameters.length > 0) {
            sb.append('('); // NOI18N
            Iterator<Parameter> it = Arrays.asList(parameters).iterator();
            sb.append(org.netbeans.modules.groovy.editor.java.Utilities.translateClassLoaderTypeName(
                    it.next().getType().getName()));

            while (it.hasNext()) {
                sb.append(','); // NOI18N
                sb.append(org.netbeans.modules.groovy.editor.java.Utilities.translateClassLoaderTypeName(
                        it.next().getType().getName()));
            }
            sb.append(')'); // NOI18N
        }

        return sb.toString();
    }

    public static OffsetRange getNextIdentifierByName(final BaseDocument doc, final String fieldName, final int startOffset) {
        final String identifier;
        if (fieldName.endsWith("[]")) { // NOI18N
            identifier = fieldName.substring(0, fieldName.length() - 2);
        } else {
            identifier = fieldName;
        }

        // since Groovy 1.5.6 the start offset is on 'def' on field/method declaration:
        // ^def foo = ...
        // ^Map bar = ...
        // find first token that is identifier and that matches given name
        final OffsetRange[] result = new OffsetRange[] { OffsetRange.NONE };
        doc.render(new Runnable() {
            @Override
            public void run() {
                TokenSequence<GroovyTokenId> ts = LexUtilities.getPositionedSequence(doc, startOffset);
                if (ts != null) {
                    Token<GroovyTokenId> token = ts.token();
                    if (token != null && token.id() == GroovyTokenId.IDENTIFIER
                            && ( TokenUtilities.textEquals(identifier, token.text())
                            || TokenUtilities.endsWith(identifier, "." + token.text()) ) ) {
                        result[0] = computeRange(ts, token);
                        return;
                    }
                    while (ts.moveNext()) {
                        token = ts.token();
                        if (token != null && token.id() == GroovyTokenId.IDENTIFIER && TokenUtilities.endsWith(identifier, token.text())) {
                            result[0] = computeRange(ts, token);
                            return;
                        }
                    }
                }
            }

            private OffsetRange computeRange(TokenSequence<GroovyTokenId> ts, Token<GroovyTokenId> token) {
                int start = ts.offset() + token.text().length() - identifier.length();
                int end = ts.offset() + token.text().length();

                if (start < 0) {
                    start = 0;
                }

                return new OffsetRange(start, end);
            }
        });
        return result[0];
    }

    /**
     * Compute the surrounding class name for the given node path or empty string
     * if none was found
     */
    public static String getFqnName(AstPath path) {
        ClassNode classNode = getOwningClass(path);
        return classNode == null ? "" : classNode.getName(); // NOI18N
    }

    public static ClassNode getOwningClass(AstPath path) {
        Iterator<ASTNode> it = path.leafToRoot();
        while (it.hasNext()) {
            ASTNode node = it.next();
            if (node instanceof ClassNode) {
                return (ClassNode) node;

            }
        }
        return null;
    }

    public static ASTNode getScope(AstPath path, Variable variable) {
        for (Iterator<ASTNode> it = path.iterator(); it.hasNext();) {
            ASTNode scope = it.next();
            if (scope instanceof ClosureExpression) {
                VariableScope variableScope = ((ClosureExpression) scope).getVariableScope();
                if (variableScope.getDeclaredVariable(variable.getName()) != null) {
                    return scope;
                } else {
                    // variables defined inside closure are not catched in VariableScope
                    // let's get the closure's code block and try there
                    Statement statement = ((ClosureExpression) scope).getCode();
                    if (statement instanceof BlockStatement) {
                        variableScope = ((BlockStatement) statement).getVariableScope();
                        if (variableScope.getDeclaredVariable(variable.getName()) != null) {
                            return scope;
                        }
                    }
                }
            } else if (scope instanceof MethodNode || scope instanceof ConstructorNode) {
                VariableScope variableScope = ((MethodNode) scope).getVariableScope();
                if (variableScope.getDeclaredVariable(variable.getName()) != null) {
                    return scope;
                } else {
                    // variables defined inside method are not catched in VariableScope
                    // let's get the method's code block and try there
                    Statement statement = ((MethodNode) scope).getCode();
                    if (statement instanceof BlockStatement) {
                        variableScope = ((BlockStatement) statement).getVariableScope();
                        if (variableScope.getDeclaredVariable(variable.getName()) != null) {
                            return scope;
                        }
                    }
                }
            } else if (scope instanceof ForStatement) {
                VariableScope variableScope = ((ForStatement) scope).getVariableScope();
                if (variableScope.getDeclaredVariable(variable.getName()) != null) {
                    return scope;
                }
            } else if (scope instanceof BlockStatement) {
                VariableScope variableScope = ((BlockStatement) scope).getVariableScope();
                if (variableScope.getDeclaredVariable(variable.getName()) != null) {
                    return scope;
                }
            } else if (scope instanceof ClosureListExpression) {
                VariableScope variableScope = ((ClosureListExpression) scope).getVariableScope();
                if (variableScope.getDeclaredVariable(variable.getName()) != null) {
                    return scope;
                }
            } else if (scope instanceof ClassNode) {
                ClassNode classNode = (ClassNode) scope;
                if (classNode.getField(variable.getName()) != null) {
                    return scope;
                }
            } else if (scope instanceof ModuleNode) {
                ModuleNode moduleNode = (ModuleNode) scope;
                BlockStatement blockStatement = moduleNode.getStatementBlock();
                VariableScope variableScope = blockStatement.getVariableScope();
                if (variableScope.getDeclaredVariable(variable.getName()) != null) {
                    return blockStatement;
                }
                // probably in script where variable is defined withoud 'def' keyword:
                // myVar = 1
                // echo myVar
                Variable classVariable = variableScope.getReferencedClassVariable(variable.getName());
                if (classVariable != null) {
                    return moduleNode;
                }
            }
        }
        return null;
    }

    /**
     * Doesn't check VariableScope if variable is declared there,
     * but assumes it is there and makes search for given variable
     */
    public static ASTNode getVariable(ASTNode scope, String variable, AstPath path, BaseDocument doc, int cursorOffset) {
        if (scope instanceof ClosureExpression) {
            ClosureExpression closure = (ClosureExpression) scope;
            for (Parameter parameter : closure.getParameters()) {
                if (variable.equals(parameter.getName())) {
                    return parameter;
                }
            }
            Statement code = closure.getCode();
            if (code instanceof BlockStatement) {
                return getVariableInBlockStatement((BlockStatement) code, variable);
            }
        } else if (scope instanceof MethodNode) {
            MethodNode method = (MethodNode) scope;
            for (Parameter parameter : method.getParameters()) {
                if (variable.equals(parameter.getName())) {
                    return parameter;
                }
            }
            Statement code = method.getCode();
            if (code instanceof BlockStatement) {
                return getVariableInBlockStatement((BlockStatement) code, variable);
            }
        } else if (scope instanceof ConstructorNode) {
            ConstructorNode constructor = (ConstructorNode) scope;
            for (Parameter parameter : constructor.getParameters()) {
                if (variable.equals(parameter.getName())) {
                    return parameter;
                }
            }
            Statement code = constructor.getCode();
            if (code instanceof BlockStatement) {
                return getVariableInBlockStatement((BlockStatement) code, variable);
            }
        } else if (scope instanceof ForStatement) {
            ForStatement forStatement = (ForStatement) scope;
            Parameter parameter = forStatement.getVariable();
            if (variable.equals(parameter.getName())) {
                return parameter;
            }
            Expression collectionExpression = forStatement.getCollectionExpression();
            if (collectionExpression instanceof ClosureListExpression) {
                ASTNode result = getVariableInClosureList((ClosureListExpression) collectionExpression, variable);
                if (result != null) {
                    return result;
                }
            }
            Statement code = forStatement.getLoopBlock();
            if (code instanceof BlockStatement) {
                ASTNode result = getVariableInBlockStatement((BlockStatement) code, variable);
                if (result != null) {
                    return result;
                }
            }
        } else if (scope instanceof BlockStatement) {
            return getVariableInBlockStatement((BlockStatement) scope, variable);
        } else if (scope instanceof ClosureListExpression) {
            return getVariableInClosureList((ClosureListExpression) scope, variable);
        } else if (scope instanceof ClassNode) {
            return ((ClassNode) scope).getField(variable);
        } else if (scope instanceof ModuleNode) {
            ModuleNode moduleNode = (ModuleNode) scope;
            BlockStatement blockStatement = moduleNode.getStatementBlock();
            ASTNode result = getVariableInBlockStatement(blockStatement, variable);
            if (result == null) {
                // probably in script where variable is defined withoud 'def' keyword:
                // myVar = 1
                // echo myVar
                VariableScope variableScope = blockStatement.getVariableScope();
                if (variableScope.getReferencedClassVariable(variable) != null) {
                    // let's take first occurrence of the variable
                    VariableScopeVisitor scopeVisitor = new VariableScopeVisitor(moduleNode.getContext(), path, doc, cursorOffset);
                    scopeVisitor.collect();
                    Set<ASTNode> occurrences = scopeVisitor.getOccurrences();
                    if (!occurrences.isEmpty()) {
                        result = occurrences.iterator().next();
                    }
                }
            }
            return result;
        }
        return null;
    }

    private static ASTNode getVariableInBlockStatement(BlockStatement block, String variable) {
        for (Object object : block.getStatements()) {
            if (object instanceof ExpressionStatement) {
                ExpressionStatement expressionStatement = (ExpressionStatement) object;
                Expression expression = expressionStatement.getExpression();
                if (expression instanceof DeclarationExpression) {
                    DeclarationExpression declaration = (DeclarationExpression) expression;
                    if (variable.equals(declaration.getVariableExpression().getName())) {
                        return declaration.getVariableExpression();
                    }
                }
            }
        }
        return null;
    }

    private static ASTNode getVariableInClosureList(ClosureListExpression closureList, String variable) {
        for (Object object : closureList.getExpressions()) {
            if (object instanceof DeclarationExpression) {
                DeclarationExpression declaration = (DeclarationExpression) object;
                if (variable.equals(declaration.getVariableExpression().getName())) {
                    return declaration.getVariableExpression();
                }
            }
        }
        return null;
    }

    private static int getLimit(ASTNode node, BaseDocument doc, int docLength) {
        int limit = (node.getLastLineNumber() > 0 && node.getLastColumnNumber() > 0)
                ? getOffset(doc, node.getLastLineNumber(), node.getLastColumnNumber())
                : docLength;

        if (limit > docLength) {
            limit = docLength;
        }
        return limit;
    }

    /**
     * Use this if you need some part of node that is not available as node.
     * For example return type of method definition is not accessible as node,
     * so I am wrapping MethodNode in this FakeASTNode and I also provide
     * text to compute OffsetRange for...
     *
     * This class is heavily used across both editor and refactoring module. In
     * a lot of cases it makes no sense to use it and a lot of those cases should
     * be removed.
     */
    public static final class FakeASTNode extends ASTNode {

        private final String name;
        private final ASTNode node;

        public FakeASTNode(ASTNode node) {
            this(node, node.getText());
        }

        public FakeASTNode(ASTNode node, String name) {
            this.node = node;
            this.name = name;

            setLineNumber(node.getLineNumber());
            setColumnNumber(node.getColumnNumber());
            setLastLineNumber(node.getLastLineNumber());
            setLastColumnNumber(node.getLastColumnNumber());
        }

        public ASTNode getOriginalNode() {
            return node;
        }

        @Override
        public String getText() {
            return name;
        }

        @Override
        public void visit(GroovyCodeVisitor visitor) {}

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 71 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 71 * hash + this.getLineNumber();
            hash = 71 * hash + this.getColumnNumber();
            hash = 71 * hash + this.getLastLineNumber();
            hash = 71 * hash + this.getLastColumnNumber();
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FakeASTNode other = (FakeASTNode) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            if (this.getLineNumber() != other.getLineNumber()) {
                return false;
            }
            if (this.getColumnNumber() != other.getColumnNumber()) {
                return false;
            }
            if (this.getLastLineNumber() != other.getLastLineNumber()) {
                return false;
            }
            if (this.getLastColumnNumber() != other.getLastColumnNumber()) {
                return false;
            }
            return true;
        }
    }
}
