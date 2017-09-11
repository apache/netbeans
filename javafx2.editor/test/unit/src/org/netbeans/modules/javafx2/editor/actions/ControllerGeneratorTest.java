/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
