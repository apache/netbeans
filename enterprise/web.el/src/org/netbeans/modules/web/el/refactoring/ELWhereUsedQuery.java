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
package org.netbeans.modules.web.el.refactoring;

import com.sun.el.parser.*;
import com.sun.source.tree.Tree.Kind;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.ELException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.web.el.*;
import org.netbeans.modules.web.el.ELIndexer.Fields;
import org.netbeans.modules.web.el.spi.ELVariableResolver.VariableInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Finds usages of managed beans in Expression Language.
 *
 * @author Erno Mononen
 */
public class ELWhereUsedQuery extends ELRefactoringPlugin {

    private static final Logger LOGGER = Logger.getLogger(ELWhereUsedQuery.class.getName());

    private static final String FACES_EVENT_CLASS = "javax.faces.event.FacesEvent";         //NOI18N

    ELWhereUsedQuery(AbstractRefactoring whereUsedQuery) {
        super(whereUsedQuery);
    }

    @Override
    public Problem prepare(final RefactoringElementsBag refactoringElementsBag) {
        final TreePathHandle handle = getHandle();
        if (handle == null) {
            return null;
        }

        final FileObject file = handle.getFileObject();
        if (file == null) {
            return null;
        }

        JavaSource jsource = JavaSource.create(ELTypeUtilities.getElimplExtendedCPI(file));
        if (jsource == null) {
            return null;
        }

        final AtomicReference<Problem> problemRef = new AtomicReference<>();
        try {
            jsource.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController info) throws Exception {
                    info.toPhase(JavaSource.Phase.RESOLVED);

                    CompilationContext ccontext = CompilationContext.create(file, info);
                    Element element = handle.resolveElement(info);
                    if (element == null) {
                        LOGGER.log(Level.INFO, "Could not resolve Element for TPH: {0}", handle); //NOI18N
                        return;
                    }
                    if ((Kind.METHOD == handle.getKind() || Kind.MEMBER_SELECT == handle.getKind())
                            && element instanceof ExecutableElement) {
                        problemRef.set(handleProperty(ccontext, refactoringElementsBag, handle, (ExecutableElement) element));
                    }
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(handle.getKind())) {
                        problemRef.set(handleClass(ccontext, refactoringElementsBag, handle, element));
                    }
                }
                
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return problemRef.get();
    }

    protected Problem handleClass(CompilationContext info, RefactoringElementsBag refactoringElementsBag, TreePathHandle handle, Element targetType) {
        TypeElement type = (TypeElement) targetType;
        String clazz = type.getQualifiedName().toString();
        String beanName = ELVariableResolvers.findBeanName(info, clazz, getFileObject());
        if (beanName != null) {
            ELIndex index = ELIndex.get(handle.getFileObject());
            Collection<? extends IndexResult> result = index.findIdentifierReferences(beanName);
            for (ELElement elem : getMatchingElements(result)) {
                addElements(info, elem, findMatchingIdentifierNodes(elem.getNode(), beanName), refactoringElementsBag);
            }
        }
        return null;
    }

    protected Problem handleProperty(CompilationContext info, RefactoringElementsBag refactoringElementsBag, TreePathHandle handle, ExecutableElement targetType) {
        String propertyName = RefactoringUtil.getPropertyName(targetType.getSimpleName().toString(), targetType.getReturnType());
        ELIndex index = ELIndex.get(handle.getFileObject());
        final Set<IndexResult> result = new HashSet<>();

        // search for property nodes only under specific circumstances
        if (searchPropertyReferences(info, targetType)) {
            result.addAll(index.findPropertyReferences(propertyName));
        }
        result.addAll(index.findMethodReferences(propertyName));

        // logic: first try to find all properties for which can resolve the type directly,
        // then search for occurrences in variables
        for (ELElement each : getMatchingElements(result)) {
            //use the Node's original offset since at least the JsfELVariableResolver uses the html snapshot embedded
            //offsets. Since the html is the top level for facelets it will match.
            List<VariableInfo> variables = ELVariableResolvers.getVariables(info, each.getSnapshot(), each.getOriginalOffset().getStart()); 

            //finds all EL AST Node-s representing the refactored property
            //the code tries to resolves the base object either as a bean or as a property
            List<Node> matchingNodes = findMatchingPropertyNodes(info, each.getNode(),
                    targetType,
                    each.getSnapshot().getSource().getFileObject(),
                    variables);            
            
            addElements(info, each, matchingNodes, refactoringElementsBag);
            
        }
        
        return null;
    }

    private static boolean searchPropertyReferences(CompilationContext info, ExecutableElement targetType) {
        // no params method
        if (targetType.getParameters().isEmpty()) {
            return true;
        }

        int argumentsNumber = targetType.getParameters().size();
        if (argumentsNumber != 1) {
            return false;
        }

        // method accepts one vararg parameter
        if (targetType.isVarArgs()) {
            return true;
        }

        // method accepts one argument which can be injected by JSF framework
        VariableElement parameter = targetType.getParameters().get(0);
        if (ELTypeUtilities.isSubtypeOf(info, parameter.asType(), FACES_EVENT_CLASS)) {
            return true;
        }

        return false;
    }

    protected void addElements(CompilationContext info, ELElement elem, List<Node> matchingNodes, RefactoringElementsBag refactoringElementsBag) {
        for (Node property : matchingNodes) {
            WhereUsedQueryElement wuqe =
                    new WhereUsedQueryElement(elem.getSnapshot().getSource().getFileObject(), property.getImage(), elem, property, getParserResult(elem.getSnapshot().getSource().getFileObject()));
            refactoringElementsBag.add(refactoring, wuqe);
        }
    }

    private List<Node> findMatchingPropertyNodes(final CompilationContext info, Node root,
            final ExecutableElement targetMethod,
            final FileObject context,
            final List<VariableInfo> variables) {

        final List<Node> result = new ArrayList<>();
        final TypeMirror targetType = targetMethod.getEnclosingElement().asType();
        root.accept(new NodeVisitor() {

            @Override
            public void visit(Node node) throws ELException {
                if (node instanceof AstIdentifier) {
                    Node parent = node.jjtGetParent();
                    String astIdent = node.getImage();
                    
                    //try to resolve the identifier as a bean class
                    String beanClass = ELVariableResolvers.findBeanClass(info, astIdent, context);
                    
                    TypeElement fmbType = null;
                    if (beanClass != null) {
                        //found corresponding bean class
                        fmbType = info.info().getElements().getTypeElement(beanClass);                        
                    } else {
                        //no bean found, try to resolve as a variable
                        VariableInfo var = findVariable(astIdent, variables);
                        
                        if(var != null) {
                            //looks like the identifier represents a variable
                            if(var.clazz != null) {
                                //resolved variable
                                beanClass = var.clazz;
                            } else {
                                // unresolved, we need to resolve the corresponding expression to get the type
                                Element referredType = ELTypeUtilities.getReferredType(info, var, context);
                                if (referredType instanceof TypeElement) {
                                    fmbType = (TypeElement) referredType;
                                }
                            }
                        }
                    }
                    
                    if(fmbType == null) {
                        //no such element found on the classpath
                        return ;
                    }
                    
                    TypeMirror enclosing = fmbType.asType();
                    for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
                        Node child = parent.jjtGetChild(i);
                        if (!(child instanceof AstDotSuffix || NodeUtil.isMethodCall(child))) {
                            continue;
                        }
                        if (enclosing == null) {
                            break;
                        }
                        if (isSameTypeOrSupertype(info, targetType, enclosing) && ELTypeUtilities.isSameMethod(info, child, targetMethod)) {
                            TypeMirror matching = getTypeForProperty(info, child, enclosing);
                            if (matching != null) {
                                result.add(child);
                            }

                        } else {
                            enclosing = getTypeForProperty(info, child, enclosing);
                        }
                    }
                }
            }
        });
        return result;
    }
    
    private boolean isSameTypeOrSupertype(CompilationContext info, TypeMirror tm1, TypeMirror tm2) {
        if (tm2 instanceof DeclaredType == false) {
            return info.info().getTypes().isSameType(tm1, tm2);
        }

        DeclaredType declaredTm2 = (DeclaredType) tm2;
        List<Element> all = ELTypeUtilities.getSuperTypesFor(info, declaredTm2.asElement());
        for (Element e : all) {
            TypeMirror tm = e.asType();
            if(info.info().getTypes().isSameType(tm1, tm)) {
                return true;
            }
        }
        return false;
    }
    
    
    private VariableInfo findVariable(String varName, List<VariableInfo> variables) {
        for(VariableInfo var : variables) {
            if(var.name.equals(varName)) {
                return var;
            }
        }
        return null;
    }

    private List<Node> findMatchingIdentifierNodes(Node root, final String identifierName) {
        final List<Node> result = new ArrayList<>();
        root.accept(new NodeVisitor() {

            @Override
            public void visit(Node node) throws ELException {
                if (node instanceof AstIdentifier) {
                    if (identifierName.equals(node.getImage())) {
                        result.add(node);
                    }
                }
            }
        });
        return result;
    }

    /**
     * Gets the element matching the given name from the given enclosing class.
     * @param name the name of the element to find.
     * @param enclosing
     * @return
     */
    private TypeMirror getTypeForProperty(CompilationContext info, Node property, TypeMirror enclosing) {
        String name = property.getImage();
        Element el = info.info().getTypes().asElement(enclosing);
        for(Element element : ELTypeUtilities.getSuperTypesFor(info, el)) {
            List<? extends Element> enclosedElements = element.getEnclosedElements();
            for (Element each : ElementFilter.methodsIn(enclosedElements)) {
                // we're only interested in public methods
                // XXX: should probably include public fields too
                if (!each.getModifiers().contains(Modifier.PUBLIC)) {
                    continue;
                }
                ExecutableElement methodElem = (ExecutableElement) each;
                String methodName = methodElem.getSimpleName().toString();

                if (ELTypeUtilities.isSameMethod(info, property, methodElem)) {
                    return ELTypeUtilities.getReturnType(info, methodElem);

                } else if (RefactoringUtil.getPropertyName(methodName, methodElem.getReturnType()).equals(name) || methodName.equals(name)) {
                    return ELTypeUtilities.getReturnType(info, methodElem);
                }
            }
        }
        return null;
    }

    private List<ELElement> getMatchingElements(Collection<? extends IndexResult> indexResult) {
        // probably should store offsets rather than doing full expression comparison
        List<ELElement> result = new ArrayList<>();
        for (IndexResult ir : indexResult) {
            FileObject file = ir.getFile();
            if(file == null) {
                continue; //looks like a deleted file hasn't been removed from the index properly
            }
            ParserResultHolder parserResultHolder = getParserResult(file);
            if (parserResultHolder.parserResult == null) {
                continue;
            }
            String expression = ir.getValue(Fields.EXPRESSION);
            for (ELElement element : parserResultHolder.parserResult.getElements()) {
                if (expression.equals(element.getExpression().getPreprocessedExpression())) {
                    if (!result.contains(element)) {
                        result.add(element);
                    }
                }
            }
        }
        return result;

    }
    
    protected static class RefactoringSessionContext {
        
        private CompilationContext info;
        private boolean active;

        public RefactoringSessionContext(CompilationContext info) {
            this.info = info;
            this.active = true;
        }

        public CompilationContext getInfo() {
            checkActive();
            return info;
        }

        
        protected void dispose() {
            info = null;
            active = false;
        }
        
        private void checkActive() {
            if(!active) {
                throw new IllegalStateException("already disposed");//NOI18N
            }
        }
        
    }
    
}
