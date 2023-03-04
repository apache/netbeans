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
package org.netbeans.modules.php.editor.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.editor.PhpEnum;
import org.netbeans.modules.php.api.editor.PhpFunction;
import org.netbeans.modules.php.api.editor.PhpInterface;
import org.netbeans.modules.php.api.editor.PhpTrait;
import org.netbeans.modules.php.api.editor.PhpType;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.EnumScope;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelFactory;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Radek Matous
 */
@ServiceProvider(service = EditorSupport.class)
public class EditorSupportImpl implements EditorSupport {

    private static final Logger LOGGER = Logger.getLogger(EditorSupportImpl.class.getName());


    @Override
    public Collection<PhpType> getTypes(FileObject fo) {
        final List<PhpType> retval = new ArrayList<>();
        Source source = Source.create(fo);
        if (source != null) {
            try {
                ParserManager.parse(Collections.singleton(source), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        Parser.Result pr = resultIterator.getParserResult();
                        if (pr instanceof PHPParseResult) {
                            Model model = ModelFactory.getModel((PHPParseResult) pr);
                            FileScope fileScope = model.getFileScope();
                            Collection<? extends TypeScope> allTypes = ModelUtils.getDeclaredTypes(fileScope);
                            for (TypeScope typeScope : allTypes) {
                                retval.add((PhpType) getPhpType(typeScope));
                            }
                        }
                    }
                });
            } catch (ParseException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
        return retval;
    }

    @Override
    public Collection<PhpClass> getClasses(FileObject fo) {
        final List<PhpClass> retval = new ArrayList<>();
        Source source = Source.create(fo);
        if (source != null) {
            try {
                ParserManager.parse(Collections.singleton(source), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        Parser.Result pr = resultIterator.getParserResult();
                        if (pr instanceof PHPParseResult) {
                            Model model = ModelFactory.getModel((PHPParseResult) pr);
                            FileScope fileScope = model.getFileScope();
                            Collection<? extends ClassScope> allClasses = ModelUtils.getDeclaredClasses(fileScope);
                            for (ClassScope classScope : allClasses) {
                                retval.add((PhpClass) getPhpType(classScope));
                            }
                        }
                    }
                });
            } catch (ParseException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
        return retval;
    }

    @Override
    public Collection<Pair<FileObject, Integer>> filesForClass(FileObject sourceRoot, PhpClass phpClass) {
        if (sourceRoot.isData()) {
            throw new IllegalArgumentException("sourceRoot must be a folder");
        }
        final List<Pair<FileObject, Integer>> retval = new ArrayList<>();
        Index indexQuery = ElementQueryFactory.createIndexQuery(QuerySupportFactory.get(sourceRoot));
        String fullyQualifiedName = phpClass.getFullyQualifiedName();
        String unqualifiedName = phpClass.getName();
        NameKind kind = fullyQualifiedName == null ? NameKind.prefix(unqualifiedName) : NameKind.exact(fullyQualifiedName);
        Set<ClassElement> classes = indexQuery.getClasses(kind);
        for (ClassElement indexedClass : classes) {
            FileObject fo = indexedClass.getFileObject();
            if (unqualifiedName.equals(indexedClass.getName()) && fo != null && fo.isValid()) {
                retval.add(Pair.of(fo, indexedClass.getOffset()));
            }
        }
        return retval;
    }

    @Override
    public PhpBaseElement getElement(FileObject fo, final int offset) {
        Source source = Source.create(fo);
        final List<PhpBaseElement> retval = new ArrayList<>(1);
        if (source != null) {
            try {
                ParserManager.parse(Collections.singleton(source), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        Parser.Result pr = resultIterator.getParserResult();
                        if (pr instanceof PHPParseResult) {
                            Model model = ModelFactory.getModel((PHPParseResult) pr);
                            retval.add(getPhpBaseElement(model.getVariableScopeForNamedElement(offset)));
                        }
                    }
                });
            } catch (ParseException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
        return retval.isEmpty() ? null : retval.get(0);
    }

    private PhpBaseElement getPhpBaseElement(Scope scope) {
        PhpBaseElement phpBaseElement = null;
        if (scope instanceof MethodScope) {
            PhpClass phpClass = (PhpClass) getPhpBaseElement((TypeScope) scope.getInScope());
            for (PhpClass.Method method : phpClass.getMethods()) {
                if (method.getName().equals(scope.getName())) {
                    phpBaseElement = method;
                    break;
                }
            }
        } else if (scope instanceof TypeScope) {
            phpBaseElement = getPhpType((TypeScope) scope);
        } else if (scope instanceof FunctionScope) {
            phpBaseElement = new PhpFunction(
                    scope.getName(),
                    scope.getNamespaceName().append(scope.getName()).toFullyQualified().toString(),
                    scope.getOffset());
        }
        return phpBaseElement;
    }

    private PhpType getPhpType(TypeScope typeScope) {
        PhpType phpType = null;
        if (typeScope instanceof ClassScope) {
            ClassScope classScope = (ClassScope) typeScope;
            PhpClass phpClass = new PhpClass(
                    classScope.getName(),
                    classScope.getNamespaceName().append(classScope.getName()).toFullyQualified().toString(),
                    classScope.getOffset());
            for (FieldElement fieldElement : classScope.getDeclaredFields()) {
                phpClass.addField(fieldElement.getName(), fieldElement.getName(), fieldElement.getOffset());
            }
            for (MethodScope methodScope : classScope.getDeclaredMethods()) {
                phpClass.addMethod(methodScope.getName(), methodScope.getName(), methodScope.getOffset());
            }
            phpType = phpClass;
        } else if (typeScope instanceof InterfaceScope) {
            InterfaceScope interfaceScope = (InterfaceScope) typeScope;
            PhpInterface phpInterface = new PhpInterface(
                    interfaceScope.getName(),
                    interfaceScope.getNamespaceName().append(interfaceScope.getName()).toFullyQualified().toString(),
                    interfaceScope.getOffset());
            for (MethodScope methodScope : interfaceScope.getDeclaredMethods()) {
                phpInterface.addMethod(methodScope.getName(), methodScope.getName(), methodScope.getOffset());
            }
            phpType = phpInterface;
        } else if (typeScope instanceof TraitScope) {
            TraitScope traitScope = (TraitScope) typeScope;
            PhpTrait phpTrait = new PhpTrait(
                    traitScope.getName(),
                    traitScope.getNamespaceName().append(traitScope.getName()).toFullyQualified().toString(),
                    traitScope.getOffset());
            for (FieldElement fieldElement : traitScope.getDeclaredFields()) {
                phpTrait.addField(fieldElement.getName(), fieldElement.getName(), fieldElement.getOffset());
            }
            for (MethodScope methodScope : traitScope.getDeclaredMethods()) {
                phpTrait.addMethod(methodScope.getName(), methodScope.getName(), methodScope.getOffset());
            }
            phpType = phpTrait;
        } else if (typeScope instanceof EnumScope) {
            EnumScope enumScope = (EnumScope) typeScope;
            PhpEnum phpEnum = new PhpEnum(
                    enumScope.getName(),
                    enumScope.getNamespaceName().append(enumScope.getName()).toFullyQualified().toString(),
                    enumScope.getOffset()
            );
            for (MethodScope methodScope : enumScope.getDeclaredMethods()) {
                phpEnum.addMethod(methodScope.getName(), methodScope.getName(), methodScope.getOffset());
            }
            phpType = phpEnum;
        }
        return phpType;
    }

}
