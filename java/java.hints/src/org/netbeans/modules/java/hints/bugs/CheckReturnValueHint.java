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
package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.bugs.CheckReturnValueHint", description = "#DESC_org.netbeans.modules.java.hints.bugs.CheckReturnValueHint", category="bugs", suppressWarnings="ResultOfMethodCallIgnored")
public class CheckReturnValueHint {

    @TriggerPattern("$method($params$);")
    public static ErrorDescription hint(HintContext ctx) {
        Element invoked = ctx.getInfo().getTrees().getElement(new TreePath(ctx.getPath(), ((ExpressionStatementTree) ctx.getPath().getLeaf()).getExpression()));

        if (invoked == null || invoked.getKind() != ElementKind.METHOD || ((ExecutableElement) invoked).getReturnType().getKind() == TypeKind.VOID) return null;

        boolean found = false;

        for (AnnotationMirror am : invoked.getAnnotationMirrors()) {
            String simpleName = am.getAnnotationType().asElement().getSimpleName().toString();

            if ("CheckReturnValue".equals(simpleName)) {
                found = true;
                break;
            }
        }

        if (!found && !checkReturnValueForJDKMethods((ExecutableElement) invoked)) return null;

        String displayName = NbBundle.getMessage(CheckReturnValueHint.class, "ERR_org.netbeans.modules.java.hints.bugs.CheckReturnValueHint");
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    private static final Set<String> JDK_IMMUTABLE_CLASSES = new HashSet<String>(Arrays.asList("java.lang.String"));

    private static boolean checkReturnValueForJDKMethods(ExecutableElement method) {
        Element owner = method.getEnclosingElement();

        if (!owner.getKind().isClass() && !owner.getKind().isInterface()) return false;

        if (JDK_IMMUTABLE_CLASSES.contains(((TypeElement) owner).getQualifiedName().toString())) return true;

        return false;
    }
}
