/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2012 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.support.FixFactory;
import org.openide.util.NbBundle.Messages;


/**
 *
 * @author Jan Lahoda
 */
@Messages({
    "DN_OverrideWeakerAccess=Fix modifiers when an overriding method has too weak access rights",
    "DESC_OverrideWeakerAccess=Fix modifiers when an overriding method has too weak access rights" 
})
public final class OverrideWeakerAccess implements ErrorRule<Void> {

    private static final Set<String> ERROR_CODES = new HashSet<String>(Arrays.asList(
            "compiler.err.override.weaker.access")); // NOI18N
    
    public Set<String> getCodes() {
        return ERROR_CODES;
    }
    
    private static final Set<Modifier> ACCESS_RIGHT_MASK = EnumSet.of(Modifier.PUBLIC, Modifier.PROTECTED, Modifier.PRIVATE);
    
    @Messages({
        "FIX_DefaultAccess=Change \"{0}\" to default access",
        "FIX_ChangeModifiers=Change \"{0}\" to {1}"
    })
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        List<Fix> result = new ArrayList<Fix>();
        Element ex = info.getTrees().getElement(treePath);
        
        if (treePath.getLeaf().getKind() == Kind.METHOD && ex != null && ex.getKind() == ElementKind.METHOD) {
            Set<Modifier> desiredAccess = EnumSet.noneOf(Modifier.class);
            ExecutableElement overridden = info.getElementUtilities().getOverriddenMethod((ExecutableElement) ex);
            
            if (overridden != null) {
                desiredAccess.addAll(overridden.getModifiers());
                desiredAccess.retainAll(ACCESS_RIGHT_MASK);
            } else {
                desiredAccess.add(Modifier.PUBLIC);
            }
            
            Set<Modifier> toRemove = EnumSet.noneOf(Modifier.class);
            
            toRemove.addAll(ex.getModifiers());
            toRemove.retainAll(ACCESS_RIGHT_MASK);
            
            String name = ex.getSimpleName().toString();
            String modifier = desiredAccess.isEmpty() ? Bundle.FIX_DefaultAccess(name) : Bundle.FIX_ChangeModifiers(name, desiredAccess.iterator().next().name().toLowerCase());
            
            result.add(FixFactory.changeModifiersFix(info, new TreePath(treePath, ((MethodTree) treePath.getLeaf()).getModifiers()), desiredAccess, toRemove, modifier));
        }
        
        return result;
    }
    
    public void cancel() {
        //XXX: not done yet
    }
    
    public String getId() {
        return OverrideWeakerAccess.class.getName();
    }
    
    public String getDisplayName() {
        return Bundle.DN_OverrideWeakerAccess();
    }
    
    public String getDescription() {
        return Bundle.DESC_OverrideWeakerAccess();
    }
    
}
