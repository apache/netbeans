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

package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.*;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.InvertBooleanRefactoring;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.spi.java.hints.support.TransformationSupport;
import org.openide.util.Exceptions;
import org.openide.util.MapFormat;
import org.openide.util.NbBundle;
import static org.netbeans.modules.refactoring.java.plugins.Bundle.*;

/**
 *
 * @author lahvac
 */
public class InvertBooleanRefactoringPlugin implements RefactoringPlugin { //extends JackpotBasedRefactoring {

    private final InvertBooleanRefactoring invertBooleanRefactoring;
    
    protected final AtomicBoolean cancel = new AtomicBoolean();
    
    public InvertBooleanRefactoringPlugin(InvertBooleanRefactoring invertBooleanRefactoring) {
        this.invertBooleanRefactoring = invertBooleanRefactoring;
    }
    
    @Override
    @NbBundle.Messages({"ERR_InvertMethodPolymorphic=Cannot invert polymorphic method.",
        "ERR_InvertMethodInInterface=Cannot invert method from interface.",
        "ERR_InvertMethodAbstract=Cannot invert abstract method."})
    public Problem preCheck() {
        final TreePathHandle treePathHandle = this.invertBooleanRefactoring.getRefactoringSource().lookup(TreePathHandle.class);
        JavaSource js = JavaSource.forFileObject(treePathHandle.getFileObject());
        if (js == null) {
            return null;
        }
        final Problem preCheckProblem[] = {null};
        try {
            js.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController javac) throws Exception {
                    javac.toPhase(JavaSource.Phase.RESOLVED);
                    Element element = treePathHandle.resolveElement(javac);
                    preCheckProblem[0] = isElementAvail(treePathHandle, javac);
                    if (preCheckProblem[0] != null) {
                        return;
                    }

                    preCheckProblem[0] = JavaPluginUtils.isSourceElement(element, javac);
                    if (preCheckProblem[0] != null) {
                        return;
                    }

                    switch (element.getKind()) {
                        case METHOD:
                            // Method can not be in annotation or interface

                            if (element.getEnclosingElement().getKind().isInterface()) {
                                preCheckProblem[0] = createProblem(preCheckProblem[0], true, ERR_InvertMethodInInterface());
                                return;
                            }
                            if (element.getModifiers().contains(Modifier.ABSTRACT)) {
                                preCheckProblem[0] = createProblem(preCheckProblem[0], true, ERR_InvertMethodAbstract());
                                return;
                            }
                            // Method can not be polymorphic
                            Collection<ExecutableElement> overridenMethods = JavaRefactoringUtils.getOverriddenMethods((ExecutableElement) element, javac);
                            Collection<ExecutableElement> overridingMethods = JavaRefactoringUtils.getOverridingMethods((ExecutableElement) element, javac, new AtomicBoolean());
                            if (overridenMethods.size() > 0 || overridingMethods.size() > 0) {
                                preCheckProblem[0] = createProblem(preCheckProblem[0], true, ERR_InvertMethodPolymorphic());
                                return;
                            }
                    }
                }
            }, true);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return preCheckProblem[0];
    }
    
    /**
     * Copy from JavaRefactoringPlugin.
     */
    private Problem createProblem(Problem result, boolean isFatal, String message) {
        Problem problem = new Problem(isFatal, message);
        return JavaPluginUtils.chainProblems(result, problem);
    }

    /**
     * Copy from JavaRefactoringPlugin.
     */
    private Problem isElementAvail(TreePathHandle e, CompilationInfo info) {
        if (e==null) {
            //element is null or is not valid.
            return new Problem(true, NbBundle.getMessage(FindVisitor.class, "DSC_ElNotAvail")); // NOI18N
        } else {
            Element el = e.resolveElement(info);
            String elName = el != null ? el.getSimpleName().toString() : null;
            if (el == null || el.asType().getKind() == TypeKind.ERROR || "<error>".equals(elName)) { // NOI18N
                return new Problem(true, NbBundle.getMessage(FindVisitor.class, "DSC_ElementNotResolved"));
            }
            
            if ("this".equals(elName) || "super".equals(elName)) { // NOI18N
                return new Problem(true, NbBundle.getMessage(FindVisitor.class, "ERR_CannotRefactorThis", el.getSimpleName()));
            }
            
            // element is still available
            return null;
        }
    }
    
    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        String name = invertBooleanRefactoring.getNewName();
        
        if (name == null || name.length() == 0 || !SourceVersion.isIdentifier(name)) {
            return new Problem(true, NbBundle.getMessage(InvertBooleanRefactoringPlugin.class, "ERR_InvalidIdentifier", name));
        }
        return null;
    }

    private static final String INVERT_FIXES =
            "=> ${newName-with-enclosing}$ $left != $right; :: matchesWithBind($val, \"$left == $right\")\n" +
            "=> ${newName-with-enclosing}$ $left == $right; :: matchesWithBind($val, \"$left != $right\")\n" +
            "=> ${newName-with-enclosing}$ $op; :: matchesWithBind($val, \"!($op)\")" +
            "=> ${newName-with-enclosing}$ $op; :: matchesWithBind($val, \"(!$op)\")" +
            "=> ${newName-with-enclosing}$ $op; :: !matchesAny($val, \"!($op)\") && matchesWithBind($val, \"!$op\")\n" +
            "=> ${newName-with-enclosing}$ false; :: matchesAny($val, \"true\")\n" +
            "=> ${newName-with-enclosing}$ true; :: matchesAny($val, \"false\")\n" +
            "=> ${newName-with-enclosing}$ !$val; :: otherwise\n";

    private static final String VAR_SCRIPT_TEMPLATE =
            "   $enclosing.${originalName}$ :: $enclosing instanceof ${enclosing}$ && !parentMatches(\"$enclosing.${originalName}$ = $newVal\") && !parentMatches(\"!$enclosing.${originalName}$\")\n" +
            "=> !$enclosing.${newName}$\n" +
            ";;\n" +
            "   !$enclosing.${originalName}$ :: $enclosing instanceof ${enclosing}$\n" +
            "=> $enclosing.${newName}$\n" +
            ";;\n" +
            "   $enclosing.${originalName}$ = $val :: $enclosing instanceof ${enclosing}$ && !matchesAny($val, \"!$enclosing.${originalName}$\")\n" +
            INVERT_FIXES.replace(";", "").replace("${newName-with-enclosing}$", "$enclosing.${newName}$ =") +
            ";;\n";

    private static final String VAR_SCRIPT_TEMPLATE_STATIC =
            "   ${enclosing}$.${originalName}$ :: !parentMatches(\"$enclosing.${originalName}$ = $newVal\") && !parentMatches(\"!$enclosing.${originalName}$\")\n" +
            "=> !${enclosing}$.${newName}$\n" +
            ";;\n" +
            "   !${enclosing}$.${originalName}$\n" +
            "=> ${enclosing}$.${newName}$\n" +
            ";;\n" +
            "   ${enclosing}$.${originalName}$ = $val :: !matchesAny($val, \"!$enclosing.${originalName}$\")\n" +
            INVERT_FIXES.replace(";", "").replace("${newName-with-enclosing}$", "${enclosing}$.${newName}$ =") +
            ";;\n";

    private static final String VAR_INIT =
            "   $mods$ $type ${originalName}$ = $val;" +
            INVERT_FIXES.replace("${newName-with-enclosing}$", "$mods$ $type ${newName}$  =") +
            ";;";

    private static final String MTH_INIT =
            "   return $val;" +
            INVERT_FIXES.replace("${newName-with-enclosing}$", "return ") +
            ";;";


    public final Problem prepare(RefactoringElementsBag refactoringElements) {
        cancel.set(false);
//        TreePathHandle tph = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
//        FileObject file = tph.getFileObject();
//        ClassPath source = ClassPath.getClassPath(file, ClassPath.SOURCE);
//        FileObject sourceRoot = source.findOwnerRoot(file);
        final TreePathHandle original = invertBooleanRefactoring.getRefactoringSource().lookup(TreePathHandle.class);
        final String[] ruleCode = new String[1];
        final String[] toCode = new String[1];

        try {
            ModificationResult mod = JavaSource.forFileObject(original.getFileObject()).runModificationTask(new Task<WorkingCopy>() {

                @Override
                public void run(final WorkingCopy parameter) throws Exception {
                   parameter.toPhase(Phase.RESOLVED);

                    final TreePath path = original.resolve(parameter);
                    Map<String, String> arguments = new HashMap<String, String>();
                    String scriptTemplate;
                    Tree leaf = path.getLeaf();
                    TypeElement parent = (TypeElement) parameter.getTrees().getElement(path.getParentPath());
                    // XXX: parent should be checked ?
                    arguments.put("newName", invertBooleanRefactoring.getNewName());
                    arguments.put("enclosing", parent.getQualifiedName().toString());

                    if (leaf.getKind() == Kind.VARIABLE) {
                        VariableTree var = (VariableTree) leaf;

                        scriptTemplate = var.getModifiers().getFlags().contains(Modifier.STATIC) ? VAR_SCRIPT_TEMPLATE_STATIC : VAR_SCRIPT_TEMPLATE;
                        arguments.put("originalName", var.getName().toString());

                        if (var.getInitializer() != null) {
                            MapFormat format = new MapFormat(arguments);
                            format.setLeftBrace("${");
                            format.setRightBrace("}$");
                            String initFormat = format.format(VAR_INIT);

                            TransformationSupport.create(initFormat).setCancel(cancel).transformTreePath(parameter, path);
                        }
                    } else if (leaf.getKind() == Kind.METHOD) {
                        MethodTree mt = (MethodTree) leaf;

                        arguments.put("originalName", mt.getName().toString());

                        MapFormat format = new MapFormat(arguments);
                        format.setLeftBrace("${");
                        format.setRightBrace("}$");
                        final String mthFormat = format.format(MTH_INIT);

                        new ErrorAwareTreePathScanner<Void, Void>() {
                            @Override public Void visitReturn(ReturnTree node, Void p) {
                                TransformationSupport.create(mthFormat).setCancel(cancel).transformTreePath(parameter, getCurrentPath());
                                return super.visitReturn(node, p);
                            }
                            @Override public Void visitClass(ClassTree node, Void p) {
                                return null;
                            }
                        }.scan(path, null);

                        parameter.rewrite(leaf, parameter.getTreeMaker().setLabel(leaf, invertBooleanRefactoring.getNewName()));

                        StringBuilder parameters = new StringBuilder();
                        StringBuilder constraints = new StringBuilder();
                        int count = 1;
                        for (VariableTree vt : mt.getParameters()) {
                            if (count > 1) {
                                parameters.append(", ");
                                constraints.append(" && ");
                            }
                            parameters.append("$").append(count);
                            TypeMirror type = parameter.getTrees().getTypeMirror(new TreePath(new TreePath(path, vt), vt.getType()));
                            type = parameter.getTypes().erasure(type);
                            constraints.append("$").append(count).append(" instanceof ").append(type);
                            count++;
                        }

                        String andConstraints = (constraints.length() > 0 ? " && " : "") + constraints;

                        StringBuilder script = new StringBuilder();

                        if (mt.getModifiers().getFlags().contains(Modifier.STATIC)) {
                            script.append("   ${enclosing}$.<$T$>${originalName}$(").append(parameters).append(") :: !parentMatches(\"!$enclosing.${originalName}$($args$)\")").append(andConstraints);
                            script.append("=> !${enclosing}$.<$T$>${newName}$(").append(parameters).append(") :: !parentMatches(\"$enclosing.${originalName}$($args$);\")");
                            script.append("=> ${enclosing}$.<$T$>${newName}$(").append(parameters).append(")\n");
                            script.append(";;\n");
                            script.append("   !${enclosing}$.<$T$>${originalName}$(").append(parameters).append(") :: ").append(constraints);
                            script.append("=> ${enclosing}$.<$T$>${newName}$(").append(parameters).append(")\n");
                            script.append(";;\n");
                        } else {
                            script.append("   $enclosing.<$T$>${originalName}$(").append(parameters).append(") :: $enclosing instanceof ${enclosing}$ && !parentMatches(\"!$enclosing.${originalName}$($args$)\")").append(andConstraints);
                            script.append("=> !$enclosing.<$T$>${newName}$(").append(parameters).append(") :: !parentMatches(\"$enclosing.${originalName}$($args$);\")");
                            script.append("=> $enclosing.<$T$>${newName}$(").append(parameters).append(")\n");
                            script.append(";;\n");
                            script.append("   !$enclosing.<$T$>${originalName}$(").append(parameters).append(") :: $enclosing instanceof ${enclosing}$ ").append(andConstraints);
                            script.append("=> $enclosing.<$T$>${newName}$(").append(parameters).append(")\n");
                            script.append(";;\n");
                        }

                        scriptTemplate = script.toString();
                    } else {
                        throw new UnsupportedOperationException();
                    }

                    MapFormat format = new MapFormat(arguments);

                    format.setLeftBrace("${");
                    format.setRightBrace("}$");

                    ruleCode[0] = format.format(scriptTemplate);
                }
            });

            List<ModificationResult> results = new ArrayList<ModificationResult>();

            results.add(mod);

            results.addAll(TransformationSupport.create(ruleCode[0]).setCancel(cancel).processAllProjects());
            ReplaceConstructorWithBuilderPlugin.createAndAddElements(invertBooleanRefactoring, refactoringElements, results);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null/*XXX*/;
    }
    

    @Override
    public void cancelRequest() {
        cancel.set(true);
    }

}
