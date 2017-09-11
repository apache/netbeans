/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.errors;

import org.netbeans.modules.java.hints.friendapi.OverrideErrorMessage;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor8;
import javax.tools.Diagnostic;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 * This is not a real hint/fix, but rather an attempt to produce a better message for 'foo() is erroneous'.
 * The code will inspect parameter types of the method invocation marked as an error, and will report a missing
 * type instead, if it finds some.
 * 
 * @author sdedic
 */
@NbBundle.Messages({
    "DN_TypeErroneous=Type is erroneous"
})
final class TypeErroneous implements OverrideErrorMessage<Void> {
    private static final Set<String> CODES = Collections.singleton("compiler.err.type.error"); // NOI18N
    
    @Override
    public Set<String> getCodes() {
        return CODES;
    }

    @Override
    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        return Collections.<Fix>emptyList(); // no real fixes.
    }

    @NbBundle.Messages({
            "# {0} - the unrechable type",
            "ERR_Unreachable_Type_1=Referenced method uses a type which is not available: {0}",
            "# {0} - the unrechable type",
            "# {1} - the other unrechable type",
            "ERR_Unreachable_Type_2=Referenced method uses types which are not available: {0} and {1}",
            "# {0} - the unrechable type",
            "# {1} - number of unreachable types",
            "ERR_Unreachable_Type_3=Referenced method uses types which are not available: {0} and {1} other"
    })
    @Override
    public String createMessage(CompilationInfo info, Diagnostic d, int offset, TreePath treePath, Data<Void> data) {
        Tree t = treePath.getLeaf();
        if (t.getKind() != Tree.Kind.MEMBER_SELECT) {
            return null;
        }
        TypeMirror mt = info.getTrees().getTypeMirror(treePath);
        if (!Utilities.isValidType(mt)) {
            return null;
        }
        Collection<TypeMirror> unreachables = Collections.emptyList();
        if (mt.getKind() == TypeKind.EXECUTABLE) {
            ExecutableType etype = (ExecutableType)mt;
            Collection<TypeMirror> toCheck = new ArrayList<>();
            toCheck.addAll(etype.getParameterTypes());
            toCheck.add(etype.getReturnType());
            toCheck.addAll(etype.getThrownTypes());
            
            V v = new V(info);
            for (TypeMirror m : toCheck) {
                m.accept(v, null);
            }
            unreachables = v.unknownDeclaredTypes;
        }
        if (unreachables.isEmpty()) {
            // I don't know - something else which makes the type erroneous ?
            return null;
        }
        switch (unreachables.size()) {
            case 1:
                return Bundle.ERR_Unreachable_Type_1(unreachables.iterator().next());
            case 2:
                Iterator<TypeMirror> it = unreachables.iterator();
                return Bundle.ERR_Unreachable_Type_2(it.next(), it.next());
            default:
                return Bundle.ERR_Unreachable_Type_2(unreachables.iterator().next(), unreachables.size());        
        }        
    }
    
    private static class V extends SimpleTypeVisitor8 {
        private final CompilationInfo info;
        
        Collection<TypeMirror>  unknownDeclaredTypes = new ArrayList<>();

        public V(CompilationInfo info) {
            this.info = info;
        }
        
        @Override
        public Object visitDeclared(DeclaredType t, Object p) {
            Element e = t.asElement();
            if (e == null) {
                unknownDeclaredTypes.add(t);
            } else {
                TypeMirror back = e.asType();
                if (back == null || back.getKind() == TypeKind.ERROR) {
                    unknownDeclaredTypes.add(t);
                }
            }
            return super.visitDeclared(t, p);
        }
    }

    @Override
    public String getId() {
        return TypeErroneous.class.getName();
    }

    @Override
    public String getDisplayName() {
        return Bundle.DN_TypeErroneous();
    }

    @Override
    public void cancel() {
    }
    
}
