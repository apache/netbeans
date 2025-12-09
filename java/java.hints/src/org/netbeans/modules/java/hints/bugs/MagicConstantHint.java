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
package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbCollections;

@Hint(displayName="Magic Constant", description="Verifies magic constants", category="bugs")
public class MagicConstantHint {

    @TriggerTreeKind({Kind.METHOD_INVOCATION, Kind.NEW_CLASS})
    @Messages({
        "# {0} - the list of valid values",
        "ERR_NotAValidValue=Not a valid value, expected one of: {0}"
    })
    public static List<ErrorDescription> hint(HintContext ctx) {
        Element el = ctx.getInfo().getTrees().getElement(ctx.getPath());

        if (el == null || (el.getKind() != ElementKind.METHOD && el.getKind() != ElementKind.CONSTRUCTOR)) return null;

        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        List<? extends ExpressionTree> arguments = ctx.getPath().getLeaf().getKind() == Kind.METHOD_INVOCATION ?
                                                   ((MethodInvocationTree) ctx.getPath().getLeaf()).getArguments() :
                                                   ((NewClassTree) ctx.getPath().getLeaf()).getArguments();
        int parameterIndex = 0;

        for (VariableElement param : ((ExecutableElement) el).getParameters()) {
            int currentParam = parameterIndex++;

            List<? extends AnnotationMirror> annotations = ctx.getInfo().getElementUtilities().getAugmentedAnnotationMirrors(param);
            List<VariableElement> validValues = new ArrayList<VariableElement>();

            for (AnnotationMirror am : annotations) {
                if (!((TypeElement) am.getAnnotationType().asElement()).getQualifiedName().contentEquals("org.intellij.lang.annotations.MagicConstant")) continue;

                //TODO: caching(!)
                for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : am.getElementValues().entrySet()) {
                    boolean isFlagsFromClass = e.getKey().getSimpleName().contentEquals("flagsFromClass");
                    boolean isValuesFromClass = e.getKey().getSimpleName().contentEquals("valuesFromClass");
                    if ((isFlagsFromClass || isValuesFromClass) && e.getValue().getValue() instanceof DeclaredType) {
                        for (VariableElement flag : ElementFilter.fieldsIn(((TypeElement) ((DeclaredType) e.getValue().getValue()).asElement()).getEnclosedElements())) {
                            if (!flag.getModifiers().contains(Modifier.STATIC)) continue;

                            validValues.add(flag);
                        }

                        break;
                    }
                    if (e.getKey().getSimpleName().contentEquals("intValues") && e.getValue().getValue() instanceof Collection) {
                        for (AnnotationValue field : NbCollections.iterable(NbCollections.checkedIteratorByFilter(((Collection) e.getValue().getValue()).iterator(), AnnotationValue.class, false))) {
                            if (!(field.getValue() instanceof String)) continue;
                            Element foundField = lookupField(ctx.getInfo(), (String) field.getValue());
                            if (foundField == null || foundField.getKind() != ElementKind.FIELD) continue;

                            validValues.add((VariableElement) foundField);
                        }

                        break;
                    }
                }
            }

            if (validValues.isEmpty()) continue;

            Element resolved = ctx.getInfo().getTrees().getElement(new TreePath(ctx.getPath(), arguments.get(currentParam)));

            //TODO: "values" vs. "flags":
            if (!validValues.contains(resolved)) {
                result.add(ErrorDescriptionFactory.forTree(ctx, arguments.get(currentParam), Bundle.ERR_NotAValidValue(validValues.stream().map(ve -> ctx.getInfo().getElementUtilities().getElementName(ve, false)).collect(Collectors.joining(", ")))));
            }
        }

        return result;
    }

    //should be replaced with ElementUtilities.findElement when the platform is NB7.4+:
    private static Element lookupField(CompilationInfo info, String field) {
        int lastDot = field.lastIndexOf('.');
        if (lastDot == (-1)) return null;
        TypeElement clazz = info.getElements().getTypeElement(field.substring(0, lastDot));
        if (clazz == null) return null;
        String simpleName = field.substring(lastDot + 1);
        for (VariableElement var : ElementFilter.fieldsIn(clazz.getEnclosedElements())) {
            if (var.getSimpleName().contentEquals(simpleName))
                return var;
        }
        return null;
    }

}
