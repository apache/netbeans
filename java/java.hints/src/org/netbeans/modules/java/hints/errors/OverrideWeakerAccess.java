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
