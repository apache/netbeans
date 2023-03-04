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

package org.netbeans.modules.java.hints.jackpot.hintsimpl;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;

/**
 *
 * @author Jan Lahoda
 */
public class FileToURL {

//    @Hint(value="org.netbeans.modules.java.hints.jackpot.hintsimpl.FileToURL.computeTreeKind", category="General")
    @TriggerTreeKind(Kind.METHOD_INVOCATION)
    public static ErrorDescription computeTreeKind(HintContext ctx) {
        MethodInvocationTree mit = (MethodInvocationTree) ctx.getPath().getLeaf();

        if (!mit.getArguments().isEmpty() || !mit.getTypeArguments().isEmpty()) {
            return null;
        }

        CompilationInfo info = ctx.getInfo();
        Element e = info.getTrees().getElement(new TreePath(ctx.getPath(), mit.getMethodSelect()));

        if (e == null || e.getKind() != ElementKind.METHOD) {
            return null;
        }

        if (e.getSimpleName().contentEquals("toURL") && info.getElementUtilities().enclosingTypeElement(e).getQualifiedName().contentEquals("java.io.File")) {
            ErrorDescription w = ErrorDescriptionFactory.forName(ctx, mit, "Use of java.io.File.toURL()");

            return w;
        }

        return null;
    }
    
//    @Hint(value="org.netbeans.modules.java.hints.jackpot.hintsimpl.FileToURL.computeTreeKind", category="General")
    @TriggerPattern(value="$1.toURL()", constraints=@ConstraintVariableType(variable="$1", type="java.io.File"))
    public static ErrorDescription computePattern(HintContext ctx) {
        ErrorDescription w = ErrorDescriptionFactory.forName(ctx, ctx.getPath(), "Use of java.io.File.toURL()");

        return w;
    }

}
