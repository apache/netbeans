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
package org.netbeans.modules.php.zend2.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.editor.PhpType;
import org.netbeans.modules.php.api.editor.PhpVariable;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.Occurence;
import org.netbeans.modules.php.editor.model.OccurencesSupport;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayElement;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.StaticDispatch;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.netbeans.modules.php.spi.editor.EditorExtender;
import org.netbeans.modules.php.zend2.util.Zend2Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class Zend2EditorExtender extends EditorExtender {

    private static final Logger LOGGER = Logger.getLogger(Zend2EditorExtender.class.getName());


    @Override
    public List<PhpBaseElement> getElementsForCodeCompletion(FileObject fo) {
        File file = FileUtil.toFile(fo);
        if (Zend2Utils.isView(file)) {
            return new ArrayList<PhpBaseElement>(parseAction(file));
        }
        return Collections.emptyList();
    }

    private Set<PhpVariable> parseAction(final File view) {
        assert Zend2Utils.isView(view) : "Not a view: " + view;

        final File controller = Zend2Utils.getController(view);
        if (controller == null) {
            return Collections.emptySet();
        }
        FileObject fo = FileUtil.toFileObject(controller);
        final Set<PhpVariable> phpVariables = Collections.synchronizedSet(new HashSet<PhpVariable>());
        try {
            ParserManager.parse(Collections.singleton(Source.create(fo)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    PHPParseResult parseResult = (PHPParseResult) resultIterator.getParserResult();
                    // view ("$this" in views)
                    PhpVariable viewVariable = new ViewResolver(parseResult).getView();
                    phpVariables.add(viewVariable);
                    // find actions
                    ControllerVisitor controllerVisitor = new ControllerVisitor(view);
                    controllerVisitor.scan(Utils.getRoot(parseResult));
                    // parse actions
                    FileObject controller = parseResult.getSnapshot().getSource().getFileObject();
                    Model model = parseResult.getModel();
                    ArrayVisitor arrayVisitor = new ArrayVisitor(controller, model);
                    ObjectVisitor objectVisitor = new ObjectVisitor(controller, model);
                    for (ASTNode actionDeclaration : controllerVisitor.getActionDeclarations()) {
                        // array()
                        arrayVisitor.setActionMethod(actionDeclaration);
                        actionDeclaration.accept(arrayVisitor);
                        addViewVariables(viewVariable, phpVariables, arrayVisitor.getVariables());
                        // ModelAndView
                        objectVisitor.setActionMethod(actionDeclaration);
                        actionDeclaration.accept(objectVisitor);
                        addViewVariables(viewVariable, phpVariables, objectVisitor.getVariables());
                    }
                }

                private void addViewVariables(PhpVariable viewVariable, Set<PhpVariable> allVariables, Set<PhpVariable> newVariables) {
                    // add them to global variables...
                    allVariables.addAll(newVariables);
                    // ...and also as fields to $this variable
                    PhpType type = viewVariable.getType();
                    assert type != null;
                    if (type instanceof PhpClass) {
                        PhpClass phpClass = (PhpClass) type;
                        for (PhpVariable variable : newVariables) {
                            phpClass.addField(variable.getName().substring(1), variable.getType(), variable.getFile(), variable.getOffset());
                        }
                    }
                }

            });
        } catch (ParseException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return phpVariables;
    }

    //~ Inner classes

    private static final class ViewResolver {

        private final PhpVariable view;


        public ViewResolver(PHPParseResult actionParseResult) {
            assert actionParseResult != null;

            ElementQuery.Index index = actionParseResult.getModel().getIndexScope().getIndex();
            Set<InterfaceElement> interfaces = index.getInterfaces(NameKind.exact("\\Zend\\View\\Renderer\\RendererInterface")); // NOI18N
            PhpClass phpClass;
            if (interfaces.size() > 0) {
                InterfaceElement zendViewIface = interfaces.iterator().next();
                phpClass = new PhpClass("RendererInterface", "\\Zend\\View\\Renderer\\RendererInterface", zendViewIface.getOffset()); // NOI18N
                phpClass.setFile(zendViewIface.getFileObject());
            } else {
                phpClass = new PhpClass("RendererInterface", "\\Zend\\View\\Renderer\\RendererInterface"); // NOI18N
            }
            view = new PhpVariable("$this", phpClass); // NOI18N
            FileObject file = phpClass.getFile();
            if (file != null) {
                view.setFile(file);
            }
        }

        public PhpVariable getView() {
            return view;
        }

    }

    private static final class ControllerVisitor extends DefaultVisitor {

        private final Set<ASTNode> actionDeclarations = new HashSet<>();
        private final String action;


        public ControllerVisitor(File viewFile) {
            assert viewFile != null;
            action = Zend2Utils.getActionName(viewFile);
        }

        @Override
        public void visit(ClassDeclaration node) {
            if (CodeUtils.extractClassName(node).toLowerCase().endsWith(Zend2Utils.CONTROLLER_CLASS_SUFFIX.toLowerCase())) {
                super.visit(node);
            }
        }

        @Override
        public void visit(MethodDeclaration node) {
            if (CodeUtils.extractMethodName(node).equalsIgnoreCase(action)) {
                actionDeclarations.add(node);
            }
        }

        public Set<ASTNode> getActionDeclarations() {
            return actionDeclarations;
        }

    }

    private static final class ArrayVisitor extends ZendVisitor {

        private final Map<String, Set<Assignment>> arrayAssigments = new HashMap<>();


        public ArrayVisitor(FileObject controller, Model model) {
            super(controller, model);
        }

        @Override
        public void visit(Assignment node) {
            VariableBase leftHandSide = node.getLeftHandSide();
            if (leftHandSide instanceof ArrayAccess) {
                ArrayAccess arrayAccess = (ArrayAccess) leftHandSide;
                String name = null;
                VariableBase variableBase = arrayAccess.getName();
                if (variableBase instanceof Variable) {
                    // it is array
                    name = CodeUtils.extractVariableName((Variable) variableBase);
                }
                Set<Assignment> assignments = arrayAssigments.get(name);
                if (assignments == null) {
                    assignments = new HashSet<>();
                    arrayAssigments.put(name, assignments);
                }
                assignments.add(node);
            }
        }

        @Override
        public void visit(ReturnStatement node) {
            Expression expression = node.getExpression();
            if (expression instanceof Variable) {
                OccurencesSupport occurencesSupport = model.getOccurencesSupport(expression.getStartOffset() + 1);
                Occurence occurence = occurencesSupport.getOccurence();
                if (occurence != null) {
                    // search for array
                    Collection<? extends PhpElement> gotoDeclarations = occurence.gotoDeclarations();
                    PhpElement variableDeclaration = ModelUtils.getFirst(gotoDeclarations);
                    if (variableDeclaration != null) {
                        ASTNode[] nodeHierarchyAtOffset = Utils.getNodeHierarchyAtOffset(actionDeclaration, variableDeclaration.getOffset());
                        for (ASTNode parentNode : nodeHierarchyAtOffset) {
                            if (parentNode instanceof Assignment) {
                                Assignment assignment = (Assignment) parentNode;
                                Expression rightHandSide = assignment.getRightHandSide();
                                if (rightHandSide instanceof ArrayCreation) {
                                    // $model = array('form' => $form);
                                    process((ArrayCreation) rightHandSide);
                                }
                                break;
                            }
                        }
                    }
                }
                // find all array accesses ($model['form'] = new MyForm())
                String varName = CodeUtils.extractVariableName((Variable) expression);
                process(arrayAssigments.get(varName));
            }
        }

        private void process(Set<Assignment> assignments) {
            if (assignments == null) {
                return;
            }
            for (Assignment assignment : assignments) {
                ArrayAccess arrayAccess = (ArrayAccess) assignment.getLeftHandSide();
                Expression index = arrayAccess.getDimension().getIndex();
                if (!(index instanceof Scalar)) {
                    // not string key
                    continue;
                }
                Scalar scalar = (Scalar) index;
                String stringValue = scalar.getStringValue();
                String varName = stringValue.substring(1, stringValue.length() - 1);
                Expression value = assignment.getRightHandSide();
                addPhpVariable(varName, value, scalar.getStartOffset());
            }
        }

    }

    private static final class ObjectVisitor extends ZendVisitor {

        private static final List<String> VIEW_MODEL_SEGMENTS = Arrays.asList(
                "Zend", "View", "Model", "ViewModel"); // NOI18N

        private final Map<String, Set<Assignment>> fieldAssigments = new HashMap<>();


        public ObjectVisitor(FileObject controller, Model model) {
            super(controller, model);
        }

        @Override
        public void visit(Assignment node) {
            VariableBase leftHandSide = node.getLeftHandSide();
            if (leftHandSide instanceof FieldAccess) {
                VariableBase dispatcher = ((FieldAccess) leftHandSide).getDispatcher();
                if (dispatcher instanceof Variable) {
                    String name = CodeUtils.extractVariableName((Variable) dispatcher);
                    Set<Assignment> assignments = fieldAssigments.get(name);
                    if (assignments == null) {
                        assignments = new HashSet<>();
                        fieldAssigments.put(name, assignments);
                    }
                    assignments.add(node);
                }
            }
        }

        @Override
        public void visit(ReturnStatement node) {
            Expression expression = node.getExpression();
            if (expression instanceof Variable) {
                OccurencesSupport occurencesSupport = model.getOccurencesSupport(expression.getStartOffset() + 1);
                Occurence occurence = occurencesSupport.getOccurence();
                if (occurence != null) {
                    // search for ModelAndView
                    Collection<? extends PhpElement> gotoDeclarations = occurence.gotoDeclarations();
                    PhpElement variableDeclaration = ModelUtils.getFirst(gotoDeclarations);
                    if (variableDeclaration != null) {
                        ASTNode[] nodeHierarchyAtOffset = Utils.getNodeHierarchyAtOffset(actionDeclaration, variableDeclaration.getOffset());
                        for (ASTNode parentNode : nodeHierarchyAtOffset) {
                            if (parentNode instanceof Assignment) {
                                Assignment assignment = (Assignment) parentNode;
                                Expression rightHandSide = assignment.getRightHandSide();
                                if (rightHandSide instanceof ClassInstanceCreation) {
                                    ClassInstanceCreation instanceCreation = (ClassInstanceCreation) rightHandSide;
                                    Expression className = instanceCreation.getClassName().getName();
                                    if (className instanceof NamespaceName) {
                                        if (isViewModel(((NamespaceName) className).getSegments())) {
                                            process(instanceCreation);
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                // find all field accesses ($model->form = new MyForm())
                String varName = CodeUtils.extractVariableName((Variable) expression);
                process(fieldAssigments.get(varName));
            }
        }

        // compare segments "backwards"
        private boolean isViewModel(List<Identifier> segments) {
            int i = segments.size() - 1;
            int j = VIEW_MODEL_SEGMENTS.size() - 1;
            while (i >= 0 && j >= 0) {
                if (!segments.get(i).getName().equals(VIEW_MODEL_SEGMENTS.get(j))) {
                    return false;
                }
                i--;
                j--;
            }
            return true;
        }

        private void process(ClassInstanceCreation instanceCreation) {
            List<Expression> ctorParams = instanceCreation.ctorParams();
            if (ctorParams.isEmpty()) {
                return;
            }
            Expression firstParam = ctorParams.get(0);
            if (firstParam instanceof ArrayCreation) {
                process((ArrayCreation) firstParam);
            }
        }

        private void process(Set<Assignment> assignments) {
            if (assignments == null) {
                return;
            }
            for (Assignment assignment : assignments) {
                Variable field = ((FieldAccess) assignment.getLeftHandSide()).getField();
                String varName = CodeUtils.extractVariableName(field);
                Expression value = assignment.getRightHandSide();
                addPhpVariable(varName, value, field.getStartOffset());
            }
        }

    }

    private abstract static class ZendVisitor extends DefaultVisitor {

        private final Set<PhpVariable> variables = new HashSet<>();
        protected final FileObject controller;
        protected final Model model;

        protected ASTNode actionDeclaration;


        public ZendVisitor(FileObject controller, Model model) {
            assert controller != null;
            assert model != null;
            this.controller = controller;
            this.model = model;
        }

        protected void addPhpVariable(String varName, Expression value, int offset) {
            PhpClass phpClass = null;
            Collection<? extends TypeScope> types = null;
            if (value instanceof VariableBase) {
                types = ModelUtils.resolveType(model, (VariableBase) value, false);
            } else if (value instanceof Assignment) {
                types = ModelUtils.resolveType(model, (Assignment) value);
            } else if (value instanceof StaticDispatch) {
                types = ModelUtils.resolveType(model, (StaticDispatch) value);
            }
            if (types != null) {
                TypeScope firstType = ModelUtils.getFirst(types);
                if (firstType != null) {
                    String typeName = firstType.getName();
                    String fqName = firstType.getFullyQualifiedName().toString();
                    phpClass = new PhpClass(typeName, fqName);
                }
            }
            variables.add(new PhpVariable("$" + varName, phpClass, controller, offset)); // NOI18N
        }

        protected void process(ArrayCreation arrayCreation) {
            List<ArrayElement> elements = arrayCreation.getElements();
            for (ArrayElement arrayElement : elements) {
                if (!(arrayElement.getKey() instanceof Scalar)) {
                    // not string key
                    continue;
                }
                Scalar scalar = (Scalar) arrayElement.getKey();
                String stringValue = scalar.getStringValue();
                String varName = stringValue.substring(1, stringValue.length() - 1);
                Expression value = arrayElement.getValue();
                addPhpVariable(varName, value, scalar.getStartOffset());
            }
        }

        public Set<PhpVariable> getVariables() {
            return variables;
        }

        public void setActionMethod(ASTNode actionDeclaration) {
            this.actionDeclaration = actionDeclaration;
        }

    }

}
