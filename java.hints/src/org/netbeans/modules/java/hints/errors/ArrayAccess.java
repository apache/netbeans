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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.NbBundle.Messages;

/**TODO: set/put return the previous value, may 'alter' semantics (but is erroneous at the beginning anyway...)
 *
 * @author lahvac
 */
public class ArrayAccess implements ErrorRule<Void> {

    private static final Set<String> CODES = new HashSet<>(Arrays.asList("compiler.err.array.req.but.found"));
    
    @Override
    public Set<String> getCodes() {
        return CODES;
    }

    private static final Set<Kind> CANNOT_HANDLE_PARENTS = EnumSet.of(
            Kind.MULTIPLY_ASSIGNMENT, Kind.DIVIDE_ASSIGNMENT,
            Kind.REMAINDER_ASSIGNMENT, Kind.PLUS_ASSIGNMENT,
            Kind.MINUS_ASSIGNMENT, Kind.LEFT_SHIFT_ASSIGNMENT,
            Kind.RIGHT_SHIFT_ASSIGNMENT, Kind.UNSIGNED_RIGHT_SHIFT_ASSIGNMENT,
            Kind.AND_ASSIGNMENT, Kind.XOR_ASSIGNMENT,
            Kind.OR_ASSIGNMENT,
            Kind.POSTFIX_DECREMENT, Kind.POSTFIX_INCREMENT,
            Kind.PREFIX_DECREMENT, Kind.PREFIX_INCREMENT
    );
    
    @Override
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        if (treePath.getLeaf().getKind() != Kind.ARRAY_ACCESS) {
            return Collections.emptyList();
        }
        ArrayAccessTree aa = (ArrayAccessTree) treePath.getLeaf();
        TypeMirror onType = info.getTrees().getTypeMirror(new TreePath(treePath, aa.getExpression()));
        boolean list = isSubType(info, onType, "java.util.List");
        boolean map = isSubType(info, onType, "java.util.Map");
        
        if (list || map) {
            Kind parentKind = treePath.getParentPath().getLeaf().getKind();
            if (CANNOT_HANDLE_PARENTS.contains(parentKind)) return null;
            return Collections.singletonList(new ConvertFromArrayAccess(info, treePath, map, parentKind == Kind.ASSIGNMENT).toEditorFix());
        }
        
        return Collections.emptyList();
    }
    
    private static boolean isSubType(CompilationInfo info, TypeMirror onType, String expectedType) {
        if (!Utilities.isValidType(onType)) return false;
        
        TypeElement expectedTypeElement = info.getElements().getTypeElement(expectedType);
        
        if (expectedTypeElement == null) return false;
        
        TypeMirror expectedTypeMirror = expectedTypeElement.asType();
        
        return info.getTypes().isSubtype(info.getTypes().erasure(onType), info.getTypes().erasure(expectedTypeMirror));
    }

    @Override
    public String getId() {
        return ArrayAccess.class.getName();
    }

    @Override
    @Messages("DN_ArrayReqNotFound=Array Access to Collection Access")
    public String getDisplayName() {
        return Bundle.DN_AccessError();
    }

    @Override
    public void cancel() {}
    
    private static final class ConvertFromArrayAccess extends JavaFix {

        private final boolean map;
        private final boolean assignment;

        public ConvertFromArrayAccess(CompilationInfo info, TreePath tp, boolean map, boolean assignment) {
            super(info, tp);
            this.map = map;
            this.assignment = assignment;
        }

        @Override
        @Messages({"FIX_UseListGet=Use List.get()",
                   "FIX_UseListSet=Use List.set()",
                   "FIX_UseMapGet=Use Map.get()",
                   "FIX_UseMapPut=Use Map.put()"})
        protected String getText() {
            return map ? assignment ? Bundle.FIX_UseMapPut(): Bundle.FIX_UseMapGet()
                       : assignment ? Bundle.FIX_UseListSet() : Bundle.FIX_UseListGet();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            ArrayAccessTree aa = (ArrayAccessTree) ctx.getPath().getLeaf();
            TreeMaker make = ctx.getWorkingCopy().getTreeMaker();
            
            if (assignment) {
                AssignmentTree at = (AssignmentTree) ctx.getPath().getParentPath().getLeaf();
                ctx.getWorkingCopy().rewrite(at, make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(aa.getExpression(), map ? " put" : "set"), Arrays.asList(aa.getIndex(), at.getExpression())));
            } else {
                ctx.getWorkingCopy().rewrite(aa, make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(aa.getExpression(), "get"), Collections.singletonList(aa.getIndex())));
            }
        }
        
    }
    
}
