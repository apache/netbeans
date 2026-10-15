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
package org.netbeans.modules.micronaut.hints;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class AddArgForRouteVar implements ErrorRule<String> {

    private static Pattern PATTERN = Pattern.compile("The route declares a uri variable named \\[(\\S*)\\]");

    @Override
    public Set<String> getCodes() {
        return Collections.singleton("compiler.err.proc.messager");
    }

    @Override
    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<String> data) {
        Matcher matcher = PATTERN.matcher(data.getData());
        if (matcher.find() && matcher.groupCount() == 1) {
            return Collections.singletonList(new AddArgForRouteVarFix(compilationInfo, offset, matcher.group(1)).toEditorFix());
        }
        return Collections.emptyList();
    }

    @Override
    public String getId() {
        return AddArgForRouteVar.class.getName();
    }

    @NbBundle.Messages("LBL_AddPathVarParam=Add Path Variable Parameter Fix")
    @Override
    public String getDisplayName() {
        return Bundle.LBL_AddPathVarParam();
    }

    @Override
    public void cancel() {
    }

    private static class AddArgForRouteVarFix extends JavaFix {

        private final String name;

        private AddArgForRouteVarFix(CompilationInfo info, int offset, String name) {
            super(info, info.getTreeUtilities().pathFor(offset));
            this.name = name;
        }

        @NbBundle.Messages("LBL_FIX_AddPathVarParam=Add path variable parameter \"{0}\"")
        @Override
        protected String getText() {
            return Bundle.LBL_FIX_AddPathVarParam(name);
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            WorkingCopy working = ctx.getWorkingCopy();
            TreeMaker make = working.getTreeMaker();
            TreePath tp = ctx.getPath();
            if (tp.getLeaf().getKind() == Tree.Kind.METHOD) {
                MethodTree targetTree = (MethodTree) tp.getLeaf();
                int index = targetTree.getParameters().size();
                Element el = working.getTrees().getElement(tp);
                if (el != null && el.getKind() == ElementKind.METHOD) {
                    ExecutableElement ee = (ExecutableElement) el;
                    if (ee.isVarArgs()) {
                        index = ee.getParameters().size() - 1;
                    }
                    MethodTree result = make.insertMethodParameter(targetTree, index, make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, make.QualIdent("java.lang.String"), null));
                    working.rewrite(targetTree, result);
                }
            }
        }
    }
}
