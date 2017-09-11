/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
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
