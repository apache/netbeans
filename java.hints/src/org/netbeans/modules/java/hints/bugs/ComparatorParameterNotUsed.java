/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.StopProcessing;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.openide.util.NbBundle;

import static org.netbeans.modules.java.hints.bugs.Bundle.*;

/**
 * The hint checks that the both the parameters in compare() implementation are actually used.
 * The implementation NOW does not detect actual usage of initially passed value, just parameter usage, so if
 * screwed code first overwrites the parameter var with some nonsense, then references it, the hint will produce OK.
 * @author sdedic
 */
@NbBundle.Messages({
    "# {0} - parameter name",
    "TEXT_ComparatorParameterNotUsed=Comparator.comparable does not use parameter ''{0}''"
})
public class ComparatorParameterNotUsed extends TreePathScanner {
    private final CompilationInfo   ci;
    private final Set<VariableElement>    unusedVars;

    public ComparatorParameterNotUsed(CompilationInfo ci, Set<VariableElement> unusedVars) {
        this.ci = ci;
        this.unusedVars = unusedVars;
    }

    @Hint(
        displayName = "#DN_ComparatorParameterNotUsed",
        description = "#DESC_ComparatorParameterNotUsed",
        category = "bugs",
        suppressWarnings = { "ComparatorMethodParameterNotUsed" },
        enabled = true
    )
    @TriggerPattern("public int compare($v1, $v2) { $stmts$; } ")
    public static List<ErrorDescription> run(HintContext ctx) {
        CompilationInfo ci = ctx.getInfo();
        Element me = ci.getTrees().getElement(ctx.getPath());
        if (me == null) {
            return null;
        }
        Element clazz = me.getEnclosingElement();
        if (clazz == null || !(
                clazz.getKind().isClass() || 
            /* JDK8 */ (clazz.getKind().isInterface() && me.getModifiers().contains(Modifier.DEFAULT)))
        ) {
            // method bod not valid at this point
            return null;
        }
        TypeMirror tm = clazz.asType();
        TypeElement comparableIface = ci.getElements().getTypeElement("java.lang.Comparable"); // NOI18N
        // the eclosing type must implement or extend comparable
        if (comparableIface == null || 
            !ci.getTypes().isSubtype(tm, comparableIface.asType())) {
            return null;
        }
        Set<VariableElement> vars = new HashSet<VariableElement>(2);
        ExecutableElement ee  = (ExecutableElement)me;
        vars.addAll(ee.getParameters());

        ComparatorParameterNotUsed v = new ComparatorParameterNotUsed(ci, vars);
        MethodTree mt = (MethodTree)ctx.getPath().getLeaf();
        if (mt.getBody() == null) {
            return null;
        }
        try {
            v.scan(new TreePath(ctx.getPath(), mt.getBody()), v);
        } catch (StopProcessing ex) {
            // nothing, just fast interrupt
        }
        if (v.unusedVars.isEmpty()) {
            return null;
        }
        // the method has exactly 2 parameters:
        VariableTree par1 = mt.getParameters().get(0);
        VariableTree par2 = mt.getParameters().get(1);
        
        List<? extends VariableElement> ll = new ArrayList<VariableElement>(v.unusedVars);
        List<ErrorDescription> res = new ArrayList<>(ll.size());
        Collections.sort(ll, Collator.getInstance());
        for (VariableElement ve : ll) {
            Tree vt = ve.getSimpleName() == par1.getName() ? par1 : par2;
            res.add(
                    ErrorDescriptionFactory.forName(ctx, vt, 
                        TEXT_ComparatorParameterNotUsed(ve.getSimpleName().toString())
            ));
        }
        return res;
    }

    @Override
    public Object visitIdentifier(IdentifierTree node, Object p) {
        Element el = ci.getTrees().getElement(getCurrentPath());
        if (el != null && el.getKind() == ElementKind.PARAMETER) {
            unusedVars.remove(el);
            if (unusedVars.isEmpty()) {
                throw new StopProcessing();
            }
        }
        return super.visitIdentifier(node, p);
    }
}
