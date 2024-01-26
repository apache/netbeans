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
package org.netbeans.modules.php.editor.codegen;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.ConstantElement;
import org.netbeans.modules.php.editor.api.elements.FieldElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.NavUtils;
import org.netbeans.modules.php.editor.codegen.SemiAttribute.AttributedElement.Kind;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ClassName;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Dispatch;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.GlobalStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.InstanceOfExpression;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar.Type;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.netbeans.modules.php.editor.parser.astnodes.Variadic;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

/**
 *
 * @author Jan Lahoda, Radek Matous
 */
public class SemiAttribute extends DefaultVisitor {
    private static final List<String> SUPERGLOBALS = Arrays.asList(
            "GLOBALS", "_SERVER", "_GET", "_POST", "_FILES", //NOI18N
            "_COOKIE", "_SESSION", "_REQUEST", "_ENV"); //NOI18N

    public DefinitionScope global;
    private ArrayDeque<DefinitionScope> scopes = new ArrayDeque<>();
    private Map<ASTNode, AttributedElement> node2Element = new HashMap<>();
    private int offset;
    private ParserResult info;
    private ArrayDeque<ASTNode> nodes = new ArrayDeque<>();

    public SemiAttribute(ParserResult info) {
        this(info, -1);
    }

    public SemiAttribute(ParserResult info, int o) {
        this.offset = o;
        this.info = info;
        global = new DefinitionScope();
        scopes.push(global);
    }

    @Override
    public void scan(ASTNode node) {
        if (node == null) {
            return;
        }
        if ((offset != (-1) && offset <= node.getStartOffset())) {
            throw new Stop();
        }
        nodes.push(node);
        super.scan(node);
        nodes.pop();
        if ((offset != (-1) && offset <= node.getEndOffset())) {
            throw new Stop();
        }
    }

    @Override
    public void visit(Program program) {
        //functions defined on top-level of the current file are visible before declared:
        performEnterPass(global, program.getStatements());
        //enterAllIndexedClasses();
        super.visit(program);
    }

    @Override
    public void visit(Assignment node) {
        final VariableBase vb = node.getLeftHandSide();

        if (vb instanceof Variable) {
            AttributedType at = null;
            Expression rightSideExpression = node.getRightHandSide();
            if (rightSideExpression instanceof Reference) {
                rightSideExpression = ((Reference) rightSideExpression).getExpression();
            }

            if (rightSideExpression instanceof ClassInstanceCreation) {
                ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) rightSideExpression;
                Expression className = classInstanceCreation.getClassName().getName();

                if (className instanceof Identifier) {
                    Identifier identifier = (Identifier) className;
                    ClassElementAttribute ce = (ClassElementAttribute) lookup(identifier.getName(), Kind.CLASS);

                    if (ce != null) {
                        at = new ClassType(ce);
                    }
                }
            } else if (rightSideExpression instanceof FieldAccess) {
                FieldAccess access = (FieldAccess) rightSideExpression;
                Variable field = access.getField();
                String name = extractVariableName(field);

                if (name != null) {
                    node2Element.put(vb, scopes.peek().enterWrite(name, Kind.VARIABLE, access, at));
                }
            }

            String name = extractVariableName((Variable) vb);

            if (name != null) {
                node2Element.put(vb, scopes.peek().enterWrite(name, Kind.VARIABLE, vb, at));
            }
        }

        super.visit(node);
    }

    @Override
    public void visit(FunctionDeclaration node) {
        String name = node.getFunctionName().getName();
        FunctionElementAttribute fc = (FunctionElementAttribute) global.enterWrite(name, Kind.FUNC, node);

        DefinitionScope top = scopes.peek();

        if (!node2Element.containsKey(node)) {
            assert !top.classScope;
            node2Element.put(node, fc);
        }

        scopes.push(fc.enclosedElements);

        if (top.classScope) {
            assert top.thisVar != null;
            scopes.peek().enter(top.thisVar.name, top.thisVar.getKind(), top.thisVar);
        }

        super.visit(node);

        scopes.pop();
    }

    @Override
    public void visit(InstanceOfExpression node) {
        ClassName className = node.getClassName();
        if (className != null) {
            Expression expr = className.getName();
            String name = (expr instanceof Identifier) ? ((Identifier) expr).getName() : null;
            if (name != null) {
                Collection<AttributedElement> namedGlobalElements = getNamedGlobalElements(Kind.CLASS, name);
                if (!namedGlobalElements.isEmpty()) {
                    node2Element.put(expr, lookup(name, Kind.CLASS));
                } else {
                    node2Element.put(expr, lookup(name, Kind.IFACE));
                }
            }
        }
        Expression expression = node.getExpression();
        if (expression instanceof Variable) {
            Variable var = (Variable) expression;
            final String name = extractVariableName(var);
            if (name != null) {
                node2Element.put(var,
                        scopes.peek().enterWrite(name, Kind.VARIABLE, var));
            }
        }
        super.visit(node);
    }

    @Override
    public void visit(CatchClause node) {
        for (Expression clsName : node.getClassNames()) {
            Identifier className = (clsName != null) ? CodeUtils.extractUnqualifiedIdentifier(clsName) : null;
            AttributedElement ae;
            if (className != null) {
                String name = className.getName();
                Collection<AttributedElement> namedGlobalElements
                        = getNamedGlobalElements(Kind.CLASS, name);
                if (!namedGlobalElements.isEmpty()) {
                    ae = lookup(name, Kind.CLASS);
                    node2Element.put(className, ae);
                } else {
                    ae = lookup(name, Kind.IFACE);
                    node2Element.put(className, ae);
                }
            }
        }

        Variable var = node.getVariable();
        final String name = extractVariableName(var);

        if (var != null && name != null) {
            node2Element.put(var,
                    scopes.peek().enterWrite(name, Kind.VARIABLE, var));
        }

        super.visit(node);
    }


    @Override
    public void visit(FormalParameter node) {
        Variable var = null;
        if (node.getParameterName() instanceof Reference) {
            Reference ref = (Reference) node.getParameterName();
            Expression parameterName = ref.getExpression();
            if (parameterName instanceof Variadic) {
                parameterName = ((Variadic) parameterName).getExpression();
            }
            if (parameterName instanceof Variable) {
                var = (Variable) parameterName;
            }
        } else if (node.getParameterName() instanceof Variable) {
            var = (Variable) node.getParameterName();
        }
        if (var != null) {
            String name = extractVariableName(var);
            if (name != null) {
                scopes.peek().enterWrite(name, Kind.VARIABLE, var);
            }
        }
        Identifier parameterType = (node.getParameterType() != null) ? CodeUtils.extractUnqualifiedIdentifier(node.getParameterType()) : null;

        if (parameterType != null) {
            String name = parameterType.getName();
            if (name != null) {
                Collection<AttributedElement> namedGlobalElements = getNamedGlobalElements(Kind.CLASS, name);
                if (!namedGlobalElements.isEmpty()) {
                    node2Element.put(parameterType, lookup(name, Kind.CLASS));
                } else {
                    node2Element.put(parameterType, lookup(name, Kind.IFACE));
                }
            }
        }

        super.visit(node);
    }

    @Override
    public void visit(Variable node) {
        if (!node2Element.containsKey(node)) {
            String name = extractVariableName(node);
            if (name != null) {
               node2Element.put(node, lookup(name, Kind.VARIABLE));
            }
        }

        super.visit(node);
    }

    @Override
    public void visit(FunctionInvocation node) {
        Expression exp = node.getFunctionName().getName();
        String name = null;

        if (exp instanceof Identifier) {
            name = ((Identifier) exp).getName();
        }

        if (exp instanceof Variable) {
            Expression n = ((Variable) exp).getName();
            if (n instanceof Identifier) {
                name = ((Identifier) n).getName();
            }
        }

        if (name != null) {
            AttributedElement thisEl = null;
            ASTNode n = nodes.pop();
            ASTNode par = nodes.peek();
            nodes.push(n);
            if (par instanceof MethodInvocation) {
                ClassElementAttribute ce = resolveTypeSimple((Dispatch) par);

                if (ce != null) {
                    thisEl = ce.lookup(name, Kind.FUNC);
                }
            } else {
                if (par instanceof StaticMethodInvocation) {
                    StaticMethodInvocation smi = (StaticMethodInvocation) par;
                    final String clsName = CodeUtils.extractUnqualifiedClassName(smi);
                    Collection<AttributedElement> nn = getNamedGlobalElements(Kind.CLASS, clsName);
                    if (!nn.isEmpty()) {
                        String contextClassName = clsName;
                        switch (clsName) {
                            case "parent": //NOI18N
                                contextClassName = getContextSuperClassName();
                                break;
                            case "self": //NOI18N
                                contextClassName = getContextClassName();
                                break;
                            default:
                                // no-op
                        }
                        for (AttributedElement ell : nn) {
                            ClassElementAttribute ce = (ClassElementAttribute) ell;
                            if (ce != null && (contextClassName == null || contextClassName.equals(ce.getName()))) {
                                thisEl = ce.lookup(name, Kind.FUNC);
                                if (thisEl != null) {
                                    node2Element.put(smi.getDispatcher(), ce);
                                    node2Element.put(smi, thisEl);
                                    node2Element.put(smi.getMethod(), thisEl);
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    thisEl = lookup(name, Kind.FUNC);
                }
            }

            node2Element.put(node, thisEl);

            if ("define".equals(name) && node.getParameters().size() == 2) {
                Expression d = node.getParameters().get(0);

                if (d instanceof Scalar && ((Scalar) d).getScalarType() == Type.STRING) {
                    String value = ((Scalar) d).getStringValue();

                    if (NavUtils.isQuoted(value)) {
                        node2Element.put(d, global.enterWrite(NavUtils.dequote(value), Kind.CONST, d));
                    }
                }
            }
        }

        if (node2Element.containsKey(node)) {
            //super.visit(node);
            scan(node.getParameters());
        } else {
            super.visit(node);
        }
    }

    @Override
    public void visit(InterfaceDeclaration node) {
        String name = node.getName().getName();
        ClassElementAttribute ce = (ClassElementAttribute) global.enterWrite(name, Kind.IFACE, node);

        node2Element.put(node, ce);
        List<Expression> interfaes = node.getInterfaces();
        for (Expression identifier : interfaes) {
            ClassElementAttribute iface = (ClassElementAttribute) lookup(CodeUtils.extractUnqualifiedName(identifier), Kind.IFACE);
            ce.ifaces.add(iface);
            node2Element.put(identifier, iface);
        }


        scopes.push(ce.enclosedElements);

        if (node.getBody() != null) {
            performEnterPass(ce.enclosedElements, node.getBody().getStatements());
        }

        super.visit(node);

        scopes.pop();
    }



    @Override
    public void visit(ClassDeclaration node) {
        String name = node.getName().getName();
        ClassElementAttribute ce = (ClassElementAttribute) global.enterWrite(name, Kind.CLASS, node);

        node2Element.put(node, ce);
        Identifier superClsName = (node.getSuperClass() != null) ? CodeUtils.extractUnqualifiedIdentifier(node.getSuperClass()) : null;
        if (superClsName != null) {
            ce.superClass = (ClassElementAttribute) lookup(superClsName.getName(), Kind.CLASS);
        }
        List<Expression> interfaes = node.getInterfaces();
        for (Expression identifier : interfaes) {
            ClassElementAttribute iface = (ClassElementAttribute) lookup(CodeUtils.extractUnqualifiedName(identifier), Kind.IFACE);
            ce.ifaces.add(iface);
            node2Element.put(identifier, iface);
        }

        scopes.push(ce.enclosedElements);

        if (node.getBody() != null) {
            performEnterPass(ce.enclosedElements, node.getBody().getStatements());
        }

        super.visit(node);

        scopes.pop();
    }

    @Override
    public void visit(ClassInstanceCreation node) {
        Expression name = node.getClassName().getName();

        if (name instanceof Identifier) {
            node2Element.put(node, lookup(((Identifier) name).getName(), Kind.CLASS));
        }

        super.visit(node);
    }

    @Override
    public void visit(GlobalStatement node) {
        for (Variable v : node.getVariables()) {
            String name = extractVariableName(v);

            if (name != null) {
                enterGlobalVariable(name);
            }
        }
        super.visit(node);
    }

    @Override
    public void visit(Scalar scalar) {
        if (scalar.getScalarType() == Type.STRING && !NavUtils.isQuoted(scalar.getStringValue())) {
            AttributedElement def = global.lookup(scalar.getStringValue(), Kind.CONST);

            node2Element.put(scalar, def);
        }

        super.visit(scalar);
    }

    @Override
    public void visit(FieldAccess node) {
        scan(node.getDispatcher());

        ClassElementAttribute ce = resolveTypeSimple(node);
        String name = extractVariableName(node.getField());

        if (ce != null && name != null) {
            AttributedElement thisEl = ce.lookup(name, Kind.VARIABLE);
            node2Element.put(node, thisEl);
            Variable field = node.getField();
            node2Element.put(field, thisEl);
            if (field instanceof ArrayAccess) {
                Expression exprName = field.getName();
                if (exprName instanceof Variable) {
                    node2Element.put(exprName, thisEl);
                }
                super.visit(node);
            }
        } else {
            scan(node.getField());
        }
    }

    private ClassElementAttribute getCurrentClassElement() {
        ClassElementAttribute c = null;
        Iterator<DefinitionScope> elements = scopes.descendingIterator();
        while (elements.hasNext()) {
            DefinitionScope scope = elements.next();
            if (scope != null
                    && scope.enclosingClass != null) {
                c = scope.enclosingClass;
                break;
            }
        }
        return c;
    }

    @Override
    public void visit(StaticConstantAccess node) {
        String clsName = CodeUtils.extractUnqualifiedClassName(node);
        ClassElementAttribute c = getCurrentClassElement();
        switch (clsName) {
            case "self": //NOI18N
                if (c != null) {
                    clsName = c.getName();
                }
                break;
            case "parent": //NOI18N
                if (c != null) {
                    c = c.getSuperClass();
                    if (c != null) {
                        clsName = c.getName();
                    }
                }
                break;
            default:
                //no-op
        }
        Collection<AttributedElement> nn = getNamedGlobalElements(Kind.CLASS, clsName); //NOI18N
        if (!nn.isEmpty()) {
            for (AttributedElement ell : nn) {
                ClassElementAttribute ce = (ClassElementAttribute) ell;
                if (ce != null && ce.getName().equals(clsName)) {
                    String name = CodeUtils.extractUnqualifiedClassName(node);
                    AttributedElement thisEl = ce.lookup(name, Kind.CONST);
                    node2Element.put(node.getDispatcher(), ce);
                    node2Element.put(node, thisEl);
                    node2Element.put(node.getConstant(), thisEl);
                    break;
                }
            }

        }
        super.visit(node);
    }

    @Override
    public void visit(StaticFieldAccess node) {
        Collection<AttributedElement> nn = getNamedGlobalElements(Kind.CLASS,
                CodeUtils.extractUnqualifiedClassName(node));
        if (!nn.isEmpty()) {
            String contextClassName = CodeUtils.extractUnqualifiedClassName(node);
            switch (contextClassName) {
                case "parent": //NOI18N
                    contextClassName = getContextSuperClassName();
                    break;
                case "self": //NOI18N
                    contextClassName = getContextClassName();
                    break;
                default:
                    // no-op
            }
            for (AttributedElement ell : nn) {
                ClassElementAttribute ce = (ClassElementAttribute) ell;
                if (ce != null && (contextClassName == null || contextClassName.equals(ce.getName()))) {
                    String name = extractVariableName(node.getField());

                    if (name != null) {
                        AttributedElement thisEl = ce.lookup(name, Kind.VARIABLE);
                        if (thisEl != null) {
                            Variable field = node.getField();
                            node2Element.put(node.getDispatcher(), ce);
                            node2Element.put(node, thisEl);
                            node2Element.put(field, thisEl);
                            if (field instanceof ArrayAccess) {
                                Expression expr = field.getName();
                                if (expr instanceof Variable) {
                                    node2Element.put(expr, thisEl);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        super.visit(node);
    }

    private AttributedElement enterGlobalVariable(String name) {
        AttributedElement g = global.lookup(name, Kind.VARIABLE);

        if (g == null) {
            //XXX: untested:
            g = global.enterWrite(name, Kind.VARIABLE, (ASTNode) null);
        }

        scopes.peek().enter(name, Kind.VARIABLE, g);

        return g;
    }

    @Override
    public void visit(ArrayAccess node) {
        if (node.getName() instanceof Variable && node.getDimension().getIndex() instanceof Scalar) {
            String variableName = extractVariableName((Variable) node.getName());

            if (variableName != null && "GLOBALS".equals(variableName)) {
                Scalar v = (Scalar) node.getDimension().getIndex();

                if (v.getScalarType() == Type.STRING) {
                    String value = v.getStringValue();
                    if (NavUtils.isQuoted(value)) {
                        node2Element.put(v, enterGlobalVariable(NavUtils.dequote(value)));
                    }
                }
            }
        }

        super.visit(node);
    }

    private String getContextClassName() {
        String contextClassName = null;
        Iterator<DefinitionScope> elements = scopes.descendingIterator();
        while (elements.hasNext()) {
            DefinitionScope nextElement = elements.next();
            if (nextElement.enclosingClass != null) {
                contextClassName = nextElement.enclosingClass.getName();
            }
        }
        return contextClassName;
    }

    private String getContextSuperClassName() {
        String contextClassName = null;
        Iterator<DefinitionScope> elements = scopes.descendingIterator();
        while (elements.hasNext()) {
            DefinitionScope nextElement = elements.next();
            if (nextElement.enclosingClass != null && nextElement.enclosingClass.superClass != null) {
                contextClassName = nextElement.enclosingClass.superClass.getName();
            }
        }
        return contextClassName;
    }

    private ParserResult getInfo() {
        return info;
    }

    private AttributedElement lookup(String name, Kind k) {
        DefinitionScope ds = scopes.peek();

        AttributedElement e;

        switch (k) {
            case FUNC:
            case IFACE:
            case CLASS:
                e = global.lookup(name, k);
                break;
            default:
                e = ds.lookup(name, k);
                break;
        }

        if (e != null) {
            return e;
        }

        switch (k) {
            case FUNC:
            case IFACE:
            case CLASS:
                return global.enterWrite(name, k, (ASTNode) null);
            default:
                return ds.enterWrite(name, k, (ASTNode) null);
        }
    }

    public Collection<AttributedElement> getGlobalElements(Kind k) {
        return global.getElements(k);
    }

    public Collection<AttributedElement> getNamedGlobalElements(Kind k, String fName) {
        final List<AttributedElement> retval = new ArrayList<>();
        final Map<String, AttributedElement> name2El = global.name2Writes.get(k);
        if (StringUtils.hasText(fName)) {
            if (fName.equals("self")) { //NOI18N
                String ctxName = getContextClassName();
                if (ctxName != null) {
                    fName = ctxName;
                }
            }
            if (Kind.CLASS.equals(k) && fName.equals("parent")) { //NOI18N
                if (name2El != null) {
                    Collection<AttributedElement> values = name2El.values();
                    for (AttributedElement ael : values) {
                        if (ael instanceof ClassElementAttribute) {
                            ClassElementAttribute ce = (ClassElementAttribute) ael;
                            ClassElementAttribute superClass = ce.getSuperClass();
                            if (superClass != null) {
                                retval.add(superClass);
                            }
                        }
                    }
                }
            } else {
                AttributedElement el = (name2El != null) ? name2El.get(fName) : null;
                if (el != null) {
                    retval.add(el);
                } else {
                    Index index = ElementQueryFactory.getIndexQuery(info);
                    for (ClassElement m : index.getClasses(NameKind.prefix(fName))) {
                        String idxName = m.getName();
                        el = global.enterWrite(idxName, Kind.CLASS, m);
                        if (el != null) {
                            retval.add(el);
                        }
                    }
                }
            }
        }
        return retval;
    }

    public AttributedElement getElement(ASTNode n) {
        return node2Element.get(n);
    }
    private Collection<PhpElement> name2ElementCache;

    public void enterAllIndexedClasses() {
        if (name2ElementCache == null) {
            Index index = ElementQueryFactory.getIndexQuery(info);
            name2ElementCache = new LinkedList<>();
            name2ElementCache.addAll(index.getClasses(NameKind.empty()));
        }

        for (PhpElement f : name2ElementCache) {
            if (f instanceof ClassElement) {
                global.enterWrite(f.getName(), Kind.CLASS, f);
            }
        }
    }

    private void performEnterPass(DefinitionScope scope, Collection<? extends ASTNode> nodes) {
        for (ASTNode n : nodes) {
            if (n instanceof MethodDeclaration) {
                FunctionDeclaration nn = ((MethodDeclaration) n).getFunction();
                String name = nn.getFunctionName().getName();
                node2Element.put(n, scope.enterWrite(name, Kind.FUNC, n));
                node2Element.put(nn, scope.enterWrite(name, Kind.FUNC, n));
                continue;
            }
            if (n instanceof FunctionDeclaration) {
                String name = ((FunctionDeclaration) n).getFunctionName().getName();

                node2Element.put(n, scope.enterWrite(name, Kind.FUNC, n));
            }
            if (n instanceof FieldsDeclaration) {
                for (SingleFieldDeclaration f : ((FieldsDeclaration) n).getFields()) {
                    String name = extractVariableName(f.getName());
                    if (name != null) {
                        node2Element.put(n, scope.enterWrite(name, Kind.VARIABLE, n));
                    }
                }
            }
            if (n instanceof ClassDeclaration) {
                ClassDeclaration node = (ClassDeclaration) n;
                String name = node.getName().getName();
                ClassElementAttribute ce = (ClassElementAttribute) global.enterWrite(name, Kind.CLASS, node);
                node2Element.put(node, ce);
                Identifier superClsName = (node.getSuperClass() != null) ? CodeUtils.extractUnqualifiedIdentifier(node.getSuperClass()) : null;
                if (superClsName != null) {
                    ce.superClass = (ClassElementAttribute) lookup(superClsName.getName(), Kind.CLASS);
                    node2Element.put(node.getSuperClass(), ce.superClass);
                }
                List<Expression> interfaces = node.getInterfaces();
                for (Expression identifier : interfaces) {
                    //TODO: ifaces must be fixed;
                }
                if (node.getBody() != null) {
                    performEnterPass(ce.enclosedElements, node.getBody().getStatements());
                }
            }
            if (n instanceof ConstantDeclaration) {
                List<Identifier> constNames = ((ConstantDeclaration) n).getNames();
                for (Identifier id : constNames) {
                    node2Element.put(n, scope.enterWrite(id.getName(), Kind.CONST, n));
                }
            }

        }
    }
    private static Map<ParserResult, SemiAttribute> info2Attr = new WeakHashMap<>();

    public static SemiAttribute semiAttribute(ParserResult info) {
        SemiAttribute a = info2Attr.get(info);

        if (a == null) {
            long startTime = System.currentTimeMillis();

            a = new SemiAttribute(info);
            a.scan(Utils.getRoot(info));

            a.info = null;

            info2Attr.put(info, a);

            long endTime = System.currentTimeMillis();

            FileObject fo = info.getSnapshot().getSource().getFileObject();

            Logger.getLogger("TIMER").log(Level.FINE, "SemiAttribute global instance", new Object[]{fo, a});
            Logger.getLogger("TIMER").log(Level.FINE, "SemiAttribute global time", new Object[]{fo, (endTime - startTime)});
        }

        return a;
    }

    public static SemiAttribute semiAttribute(ParserResult info, int stopOffset) {
        SemiAttribute a = new SemiAttribute(info, stopOffset);

        try {
            a.scan(Utils.getRoot(info));
        } catch (Stop s) {
        }

        return a;
    }

    private static String name(ASTNode n) {
        if (n instanceof Identifier) {
            return ((Identifier) n).getName();
        }

        return null;
    }

    @CheckForNull
    //TODO converge this method with CodeUtils.extractVariableName()
    public static String extractVariableName(Variable var) {
        String varName = CodeUtils.extractVariableName(var);
        if (varName != null && varName.startsWith("$")) { //NOI18N
            return varName.substring(1);
        }
        return varName;
    }

    private ClassElementAttribute resolveTypeSimple(Dispatch node) {
        ClassElementAttribute ce = null;
        AttributedElement el = node2Element.get(node.getDispatcher());

        if (el != null) {
            AttributedType type = el.writesTypes.get(el.getWrites().size() - 1);

            if (type instanceof ClassType) {
                ce = ((ClassType) type).getElement();
            }
        }

        return ce;
    }

    public Collection<AttributedElement> getFunctions() {
        Collection<AttributedElement> retval;
        if (global != null) {
            retval = global.getFunctions();
        } else {
            retval = Collections.emptyList();
        }
        return retval;
    }

    public Collection<AttributedElement> getConstants() {
        Collection<AttributedElement> retval;
        if (global != null) {
            retval = global.getConstants();
        } else {
            retval = Collections.emptyList();
        }
        return retval;
    }

    public Collection<AttributedElement> getGlobalVariables() {
        Collection<AttributedElement> retval;
        if (global != null) {
            retval = global.getVariables();
        } else {
            retval = Collections.emptyList();
        }
        return retval;
    }

    public Collection<ClassElementAttribute> getClasses() {
        Collection<ClassElementAttribute> retval;
        if (global != null) {
            retval = global.getClasses();
        } else {
            retval = Collections.emptyList();
        }
        return retval;
    }

    public boolean hasGlobalVisibility(AttributedElement elem) {
        if (elem.isClassMember()) {
            assert (elem instanceof ClassMemberElement);
            ClassMemberElement cme = (ClassMemberElement) elem;
            boolean isGlobal = (cme.getModifier() == -1 || !cme.isPrivate()) && hasGlobalVisibility(cme.getClassElement());
            return isGlobal;
        }
        return (global != null) ? global.getElements(elem.getKind()).contains(elem) : false;
    }

    public static class AttributedElement {

        private List<Union2<ASTNode, PhpElement>> writes; //aka declarations

        private List<AttributedType> writesTypes;
        private String name;
        private Kind k;

        public AttributedElement(Union2<ASTNode, PhpElement> n, String name, Kind k) {
            this(n, name, k, null);
        }

        public AttributedElement(Union2<ASTNode, PhpElement> n, String name, Kind k, AttributedType type) {
            this.writes = new LinkedList<>();
            this.writesTypes = new LinkedList<>();
            this.writes.add(n);

            this.writesTypes.add(type);
            this.name = name;
            this.k = k;
        }

        public boolean isClassMember() {
            return false;
        }

        public List<Union2<ASTNode, PhpElement>> getWrites() {
            return writes;
        }

        public Kind getKind() {
            return k;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof AttributedElement)) {
                return false;
            }
            AttributedElement element = (AttributedElement) obj;
            return this.name.equals(element.name) && this.k.equals(element.k);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 53 * hash + (this.k != null ? this.k.hashCode() : 0);
            return hash;
        }

        void addWrite(Union2<ASTNode, PhpElement> node, AttributedType type) {
            writes.add(node);
            writesTypes.add(type);
        }

        Types getTypes() {
            return new Types(this);
        }

        public String getScopeName() {
            String retval = ""; //NOI18N
            Types types = getTypes();
            for (int i = 0; i < types.size(); i++) {
                AttributedType type = types.getType(i);
                if (type != null) {
                    retval = type.getTypeName();
                    break;
                }
            }
            return retval;
        }

        public enum Kind {

            VARIABLE, FUNC, CLASS, CONST, IFACE;
        }
    }

    private static class Types {

        private AttributedElement el;

        Types(AttributedElement el) {
            this.el = el;
        }

        int size() {
            return el.writesTypes.size();
        }

        AttributedType getType(int idx) {
            return el.writesTypes.get(idx);
        }
    }

    public static class ClassMemberElement extends AttributedElement {

        private ClassElementAttribute classElement;
        int modifier = -1;

        public ClassMemberElement(Union2<ASTNode, PhpElement> n, ClassElementAttribute classElement, String name, Kind k) {
            super(n, name, k);
            this.classElement = classElement;
            setModifiers(n, name);
            assert classElement != null;
        }

        public String getClassName() {
            return getClassElement().getName();
        }

        @Override
        public String getScopeName() {
            return getClassName();
        }

        public int getModifier() {
            return modifier;
        }

        public boolean isPublic() {
            return BodyDeclaration.Modifier.isPublic(getModifier());
        }

        public boolean isPrivate() {
            return BodyDeclaration.Modifier.isPrivate(getModifier());
        }

        public boolean isProtected() {
            return BodyDeclaration.Modifier.isProtected(getModifier());
        }

        public boolean isStatic() {
            return BodyDeclaration.Modifier.isStatic(getModifier());
        }

        public ClassElementAttribute getClassElement() {
            return classElement;
        }

        @Override
        public boolean isClassMember() {
            return true;
        }

        public ClassMemberKind getClassMemberKind() {
            ClassMemberKind retval = null;
            switch (getKind()) {
                case CONST:
                    retval = ClassMemberKind.CONST;
                    break;
                case FUNC:
                    retval = ClassMemberKind.METHOD;
                    break;
                case VARIABLE:
                    retval = ClassMemberKind.FIELD;
                    break;
                default:
                    assert false;

            }
            assert retval != null;
            return retval;
        }

        private void setModifiers(Union2<ASTNode, PhpElement> n, String name) {
            if (n.hasFirst()) {
                ASTNode node = n.first();
                if (node instanceof BodyDeclaration) {
                    modifier = ((BodyDeclaration) node).getModifier();
                } else if (name.equals("this")) {
                    //NOI18N
                    assert false;
                } else if (node instanceof ConstantDeclaration) {
                    modifier |= BodyDeclaration.Modifier.PUBLIC;
                } else {
                    assert false : name;
                }
            } else if (n.hasSecond()) {
                PhpElement index = n.second();
                if (index != null) {
                    Set<Modifier> modifiers = index.getModifiers();
                    for (Modifier mod : modifiers) {
                        switch (mod) {
                            case PRIVATE:
                                modifier |= BodyDeclaration.Modifier.PRIVATE;
                                break;
                            case PROTECTED:
                                modifier |= BodyDeclaration.Modifier.PROTECTED;
                                break;
                            case PUBLIC:
                                modifier |= BodyDeclaration.Modifier.PUBLIC;
                                break;
                            case STATIC:
                                modifier |= BodyDeclaration.Modifier.STATIC;
                                break;
                            default:
                                // no-op
                        }
                    }
                }
            }
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + (this.classElement != null ? this.classElement.hashCode() : 0);
            hash = 97 * hash + this.modifier;
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
            final ClassMemberElement other = (ClassMemberElement) obj;
            if (this.classElement != other.classElement && (this.classElement == null || !this.classElement.equals(other.classElement))) {
                return false;
            }
            return this.modifier == other.modifier;
        }

        public enum ClassMemberKind {

            FIELD, METHOD, CONST;
        }
    }
    public  class ClassElementAttribute extends AttributedElement {

        private final DefinitionScope enclosedElements;
        private ClassElementAttribute superClass;
        private Set<ClassElementAttribute> ifaces = new HashSet<>();
        private boolean initialized;

        public ClassElementAttribute(Union2<ASTNode, PhpElement> n, String name, Kind k) {
            super(n, name, k);
            enclosedElements = new DefinitionScope(this);
        }

        public AttributedElement lookup(String name, Kind k) {
            AttributedElement el = enclosedElements.lookup(name, k);
            if (el != null) {
                return el;
            }
            Index index = ElementQueryFactory.getIndexQuery(info);
            switch (k) {
                case CONST:
                    for (TypeConstantElement classMember : index.getAllTypeConstants(NameKind.exact(getName()), NameKind.prefix(name))) {
                        String idxName = classMember.getName();
                        idxName = (idxName.startsWith("$")) ? idxName.substring(1) : idxName;
                        enclosedElements.enterWrite(idxName, Kind.CONST, classMember);
                    }
                    break;
                case FUNC:
                    for (MethodElement classMember : index.getAllMethods(NameKind.exact(getName()), NameKind.prefix(name))) {
                        enclosedElements.enterWrite(classMember.getName(), Kind.FUNC, classMember);
                    }
                    break;
                case VARIABLE:
                    for (FieldElement classMember : index.getAlllFields(NameKind.exact(getName()), NameKind.prefix(name))) {
                        String idxName = classMember.getName();
                        idxName = (idxName.startsWith("$")) ? idxName.substring(1) : idxName;
                        enclosedElements.enterWrite(idxName, Kind.VARIABLE, classMember);
                    }
                    break;
                default:
                    //no-op
            }
            return enclosedElements.lookup(name, k);
        }

        public Collection<AttributedElement> getElements(Kind k) {
            List<AttributedElement> elements = new ArrayList<>();

            getElements0(elements, k);

            return Collections.unmodifiableList(elements);
        }

        public Collection<AttributedElement> getNamedElements(Kind k, String... filterNames) {
            Collection<AttributedElement> elements = getElements(k);
            List<AttributedElement> retval = new ArrayList<>();
            for (String fName : filterNames) {
                for (AttributedElement el : elements) {
                    if (el.getName().equals(fName)) {
                        retval.add(el);
                    }
                }
            }
            return retval;
        }

        public Collection<AttributedElement> getMethods() {
            return getElements(Kind.FUNC);
        }

        public Collection<AttributedElement> getFields() {
            Collection<AttributedElement> elems = getElements(Kind.VARIABLE);
            List<AttributedElement> retval = new ArrayList<>();
            for (AttributedElement elm : elems) {
                if (!elm.getName().equals("this")) {
                    retval.add(elm);
                }
            }
            return retval;
        }

        public ClassElementAttribute getSuperClass() {
            return superClass;
        }

        private void getElements0(List<AttributedElement> elements, Kind k) {
            elements.addAll(enclosedElements.getElements(k));

            if (superClass != null) {
                superClass.getElements0(elements, k);
            }
        }

        boolean isInitialized() {
            return initialized;
        }

        void initialized() {
            initialized = true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + (this.enclosedElements != null ? this.enclosedElements.hashCode() : 0);
            hash = 79 * hash + (this.superClass != null ? this.superClass.hashCode() : 0);
            hash = 79 * hash + (this.ifaces != null ? this.ifaces.hashCode() : 0);
            hash = 79 * hash + (this.initialized ? 1 : 0);
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
            final ClassElementAttribute other = (ClassElementAttribute) obj;
            if (this.enclosedElements != other.enclosedElements && (this.enclosedElements == null || !this.enclosedElements.equals(other.enclosedElements))) {
                return false;
            }
            if (this.superClass != other.superClass && (this.superClass == null || !this.superClass.equals(other.superClass))) {
                return false;
            }
            if (this.ifaces != other.ifaces && (this.ifaces == null || !this.ifaces.equals(other.ifaces))) {
                return false;
            }
            return this.initialized == other.initialized;
        }


    }

    public  class FunctionElementAttribute extends AttributedElement {

        private final DefinitionScope enclosedElements;
        private boolean initialized;

        public FunctionElementAttribute(Union2<ASTNode, PhpElement> n, String name, Kind k) {
            super(n, name, k);
            enclosedElements = new DefinitionScope(this);
        }

        public AttributedElement lookup(String name, Kind k) {
            return enclosedElements.lookup(name, k);
        }

        public Collection<AttributedElement> getElements(Kind k) {
            List<AttributedElement> elements = new ArrayList<>();

            getElements0(elements, k);

            return Collections.unmodifiableList(elements);
        }

        public Collection<AttributedElement> getNamedElements(Kind k, String... filterNames) {
            Collection<AttributedElement> elements = getElements(k);
            List<AttributedElement> retval = new ArrayList<>();
            for (String fName : filterNames) {
                for (AttributedElement el : elements) {
                    if (el.getName().equals(fName)) {
                        retval.add(el);
                    }
                }
            }
            return retval;
        }

        public Collection<AttributedElement> getVariables() {
            return getElements(Kind.VARIABLE);
        }

        private void getElements0(List<AttributedElement> elements, Kind k) {
            elements.addAll(enclosedElements.getElements(k));
        }

        boolean isInitialized() {
            return initialized;
        }

        void initialized() {
            initialized = true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 41 * hash + (this.enclosedElements != null ? this.enclosedElements.hashCode() : 0);
            hash = 41 * hash + (this.initialized ? 1 : 0);
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
            final FunctionElementAttribute other = (FunctionElementAttribute) obj;
            if (this.enclosedElements != other.enclosedElements && (this.enclosedElements == null || !this.enclosedElements.equals(other.enclosedElements))) {
                return false;
            }
            return this.initialized == other.initialized;
        }
    }

    public final class DefinitionScope {

        private final Map<Kind, Map<String, AttributedElement>> name2Writes = new EnumMap<>(Kind.class);
        private boolean classScope;
        private boolean functionScope;
        private AttributedElement thisVar;
        private ClassElementAttribute enclosingClass;
        private FunctionElementAttribute enclosingFunction;

        public DefinitionScope() {
        }

        public DefinitionScope(ClassElementAttribute enclosingClass) {
            this.enclosingClass = enclosingClass;
            this.classScope = enclosingClass != null;
            if (classScope) {
                thisVar = enterWrite("this", Kind.VARIABLE, (ASTNode) null, new ClassType(enclosingClass));
            }
        }

        public DefinitionScope(FunctionElementAttribute enclosingFunction) {
            this.enclosingFunction = enclosingFunction;
            this.functionScope = enclosingFunction != null;
        }

        public AttributedElement enterWrite(String name, Kind k, ASTNode node) {
            return enterWrite(name, k, node, null);
        }

        public AttributedElement enterWrite(String name, Kind k, ASTNode node, AttributedType type) {
            return enterWrite(name, k, Union2.<ASTNode, PhpElement>createFirst(node), type);
        }

        public AttributedElement enterWrite(String name, Kind k, PhpElement el) {
            return enterWrite(name, k, Union2.<ASTNode, PhpElement>createSecond(el), null);
        }

        private AttributedElement enterWrite(String name, Kind k, Union2<ASTNode, PhpElement> node, AttributedType type) {
            if (k == Kind.VARIABLE && this != global) {
                //TODO: review
                if (SUPERGLOBALS.contains(name)) {
                    return SemiAttribute.this.enterGlobalVariable(name);
                }
            }

            Map<String, AttributedElement> name2El = name2Writes.get(k);

            if (name2El == null) {
                name2El = new HashMap<>();
                name2Writes.put(k, name2El);
            }

            AttributedElement el = name2El.get(name);

            if (el == null) {
                if (k == Kind.CLASS || k == Kind.IFACE) {
                    el = new ClassElementAttribute(node, name, k);
                } else {
                    if (classScope && !Arrays.asList(new String[]{"this"}).contains(name)) {
                        switch (k) {
                            case CONST:
                            case FUNC:
                            case VARIABLE:
                                el = new ClassMemberElement(node, enclosingClass, name, k);
                                break;
                            default:
                                assert false;
                        }
                    } else {
                        if (k == Kind.FUNC) {
                            el = new FunctionElementAttribute(node, name, k);
                        } else if (k == Kind.VARIABLE) {
                            if (type == null && functionScope && enclosingFunction != null) {
                                type = new FunctionType(enclosingFunction);
                            }
                            el = new AttributedElement(node, name, k, type);
                        } else {
                            el = new AttributedElement(node, name, k, type);
                        }
                    }
                }

                name2El.put(name, el);
            } else {
                el.addWrite(node, type);
            }

            return el;
        }

        public AttributedElement enter(String name, Kind k, AttributedElement el) {
            Map<String, AttributedElement> name2El = name2Writes.get(k);
            if (name2El == null) {
                name2El = new HashMap<>();
                name2Writes.put(k, name2El);
            }
            name2El.put(name, el);
            return el;
        }

        public AttributedElement lookup(String name, Kind k) {
            AttributedElement el = null;
            Map<String, AttributedElement> name2El = name2Writes.get(k);
            if (name2El != null) {
                el = name2El.get(name);
            }
            if (el == null) {
                Index index = ElementQueryFactory.getIndexQuery(info);
                switch (k) {
                    case CONST:
                        for (ConstantElement m : index.getConstants(NameKind.prefix(name))) {
                            String idxName = m.getName();
                            el = enterWrite(idxName, Kind.CONST, m);
                        }
                        break;
                    default:
                        //no-op
                }
            }
            return el;
        }

        public Collection<AttributedElement> getElements(Kind k) {
            Map<String, AttributedElement> name2El = name2Writes.get(k);
            if (name2El != null) {
                return Collections.unmodifiableCollection(name2El.values());
            }
            return Collections.emptyList();
        }

        public Collection<AttributedElement> getFunctions() {
            return getElements(Kind.FUNC);
        }

        public Collection<AttributedElement> getVariables() {
            return getElements(Kind.VARIABLE);
        }

        private Collection<AttributedElement> getConstants() {
            return getElements(Kind.CONST);
        }

        public Collection<ClassElementAttribute> getClasses() {
            Collection<ClassElementAttribute> retval = new LinkedHashSet<>();
            Collection<AttributedElement> elements = getElements(Kind.CLASS);
            for (AttributedElement el : elements) {
                assert el instanceof ClassElementAttribute;
                retval.add((ClassElementAttribute) el);
            }
            return retval;
        }
    }

    private static final class Stop extends Error {
    }

    public abstract static class AttributedType {

        public abstract String getTypeName();

    }

    public static class ClassType extends AttributedType {

        private final ClassElementAttribute element;

        public ClassType(ClassElementAttribute element) {
            this.element = element;
        }

        public ClassElementAttribute getElement() {
            return element;
        }

        @Override
        public String getTypeName() {
            return getElement().getName();
        }
    }

    public static class FunctionType extends AttributedType {

        private final FunctionElementAttribute element;

        public FunctionType(FunctionElementAttribute element) {
            this.element = element;
        }

        public FunctionElementAttribute getElement() {
            return element;
        }

        @Override
        public String getTypeName() {
            return getElement().getName();
        }
    }
}
