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
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.Pair;

/**
 *
 * @author Max Sauer
 */
//@Hint(id="org.netbeans.modules.java.hints.jackpot.hintsimpl.ForbiddenMethod.hint", category="general")
public class ForbiddenMethod {

    private static final Map<String, Pair<String, String>> map;

    static {
        map = new HashMap<String, Pair<String, String>>();
        map.put("exit", Pair.<String, String>of("System", "Non-portable"));
    }

    @TriggerTreeKind(Tree.Kind.METHOD_INVOCATION)
    public static ErrorDescription hint(HintContext ctx) {
        MethodInvocationTree mit = (MethodInvocationTree) ctx.getPath().getLeaf();

        CompilationInfo info = ctx.getInfo();
        Element e = info.getTrees().getElement(new TreePath(ctx.getPath(), mit.getMethodSelect()));
        
        if (e == null || e.getKind() != ElementKind.METHOD) {
            return null;
        }
        String simpleName = e.getSimpleName().toString();
        String parent = e.getEnclosingElement().getSimpleName().toString();

        Pair<String, String> pair = map.get(simpleName);
        if (pair != null && pair.first().equals(parent)) {
            return ErrorDescriptionFactory.forName(ctx, mit, pair.second());
        }


        return null;
//        return ErrorDescriptionFactory.forName(ctx, mit, "Use of forbidden method");
    }




}
