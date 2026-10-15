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
package org.netbeans.modules.javafx2.editor.actions;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.util.Arrays;
import java.util.Collections;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.source.parsing.ClasspathInfoProvider;
import org.netbeans.modules.javafx2.editor.FXMLCompletionTestBase;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sdedic
 */
public class ControllerGeneratorTest  extends FXMLCompletionTestBase {

    public ControllerGeneratorTest(String testName) {
        super(testName);
    }
    
    /**
     * Checks that field type parameters are properly translated to wildcards
     * @throws Exception 
     */
    public void testFieldTypeParameters() throws Exception {
        ClassPath cp = cpInfo.getClassPath(PathKind.SOURCE);
        cp.findResource("org.netbeans.modules.javafx2.editor.actions");
        
        class UT extends UserTask implements ClasspathInfoProvider {

            @Override
            public ClasspathInfo getClasspathInfo() {
                return cpInfo;
            }

            @Override
            public void run(final ResultIterator resultIterator) throws Exception {
                final CompilationController cinfo = CompilationController.get(resultIterator.getParserResult());
                cinfo.toPhase(JavaSource.Phase.RESOLVED);
                
                JavaSource.forFileObject(resultIterator.getSnapshot().getSource().getFileObject()).runModificationTask(new Task<WorkingCopy>() {

                    @Override
                    public void run(WorkingCopy wcp) throws Exception {
                        wcp.toPhase(JavaSource.Phase.RESOLVED);
                        ClassTree clazz =wcp.getTreeMaker().Class(
                                wcp.getTreeMaker().Modifiers(Collections.<Modifier>emptySet()),
                                "FieldsWithTypeParams2", Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(),
                                Collections.<Tree>emptyList());
                        
                        TypeElement tel = cinfo.getElements().getTypeElement("org.netbeans.modules.javafx2.editor.actions.FieldsWithTypeParams");
                        for (Element e : tel.getEnclosedElements()) {
                            if (e.getKind() != ElementKind.CLASS) {
                                continue;
                            }

                            // Actually translate out type parameters
                            TypeMirror erased = ControllerGenerator.eraseFieldTypeParameters(e.asType(), cinfo);
                            
                            TypeMirror e1 = cinfo.getTypes().erasure(erased);
                            TypeMirror e2 = cinfo.getTypes().erasure(e.asType());
                            
                            // checks that the translation is STILL compatible with the original (erasure)
                            assertTrue(cinfo.getTypes().isSameType(e1, e2));
                            
                            // add to the generated class a field, with the simplename derived from the class:
                            
                            VariableTree v = wcp.getTreeMaker().Variable(
                                    wcp.getTreeMaker().Modifiers(Collections.<Modifier>emptySet()),
                                    "f" + e.getSimpleName(), 
                                    wcp.getTreeMaker().Type(erased), null);
                            
                            clazz = wcp.getTreeMaker().addClassMember(clazz, v);
                        }
                        CompilationUnitTree newSource = wcp.getTreeMaker().CompilationUnit(
                                FileUtil.toFileObject(getWorkDir()), 
                                "org/netbeans/modules/javafx2/editor/actions/FieldsWithTypeParams2.java", 
                                Collections.<ImportTree>emptyList(),
                                Collections.singletonList(clazz));
                        wcp.rewrite(null, newSource);
                    }
                }).commit();
           }
        }
        
        FileObject f = FileUtil.toFileObject(getDataDir()).getFileObject("org/netbeans/modules/javafx2/editor/actions/FieldsWithTypeParams.java");
        
        ParserManager.parse(Collections.singleton(Source.create(f)), new UT());
        
        class UT2 extends UserTask  implements ClasspathInfoProvider {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                CompilationController ctrl = CompilationController.get(resultIterator.getParserResult());
                
                ctrl.toPhase(JavaSource.Phase.RESOLVED);
                
                // there should not be any issues in the generated file.
                for (Diagnostic d : ctrl.getDiagnostics()) {
                    assertTrue(d.getKind() != Diagnostic.Kind.ERROR);
                }
            }

            @Override
            public ClasspathInfo getClasspathInfo() {
                return cpInfo;
            }
            
            
        }
        
        FileObject f2 = FileUtil.toFileObject(getWorkDir()).getFileObject("org/netbeans/modules/javafx2/editor/actions/FieldsWithTypeParams2.java");
        ParserManager.parse(Arrays.asList(Source.create(f), Source.create(f2)), new UT2());
        
    }
}
