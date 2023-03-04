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
package org.netbeans.modules.java.hints;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav tulach
 */
public class HideFieldByVar extends HideField {
    public HideFieldByVar() {
        super("LocalVariableHidesMemberVariable");
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(DoubleCheck.class, "MSG_HiddenFieldByVar"); // NOI18N
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(DoubleCheck.class, "HINT_HiddenFieldByVar"); // NOI18N
    }

    @Override
    protected List<Fix> computeFixes(CompilationInfo compilationInfo, TreePath treePath, int[] bounds) {
        if (treePath.getLeaf().getKind() != Kind.VARIABLE) {
            return null;
        }
        VariableTree vt = (VariableTree)treePath.getLeaf();
        Element el = compilationInfo.getTrees().getElement(treePath);
        if (el == null) {
            return null;
        }
        if (el.getKind() == ElementKind.FIELD) {
            return null;
        }
        boolean isStatic = false;
        while (el != null && !(el instanceof TypeElement)) {
            isStatic = el.getModifiers().contains(Modifier.STATIC);
            el = el.getEnclosingElement();
        }
        if (el == null) {
            return null;
        }
        if (treePath.getParentPath().getLeaf().getKind() == Kind.METHOD) {
            // skip method values
            return null;
        }

        Element hidden = null;
        for (Element e : getAllMembers(compilationInfo, (TypeElement)el)) {
            if (stop) {
                return null;
            }
            
            if (e.getKind() != ElementKind.FIELD) {
                continue;
            }
            if (isStatic && !e.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }
            if (e.getSimpleName() == vt.getName()) {
                hidden = e;
                break;
            }
        }
        if (hidden == null) {
            return null;
        }

        int[] span = compilationInfo.getTreeUtilities().findNameSpan(vt);
        
        if (span == null) {
            return null;
        }
        List<Fix> fixes = Collections.<Fix>singletonList(new FixImpl(
            (span[1] + span[0]) / 2,
            compilationInfo.getFileObject(),
            true
        ));
        bounds[0] = span[0];
        bounds[1] = span[1];
        return fixes;
    }
    
}
