/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.parser.processors;

import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.javafx2.editor.ErrorMark;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstanceCopy;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode;
import org.netbeans.modules.javafx2.editor.completion.model.FxNodeVisitor;
import org.netbeans.modules.javafx2.editor.completion.model.FxReference;
import org.netbeans.modules.javafx2.editor.completion.model.TextPositions;
import org.netbeans.modules.javafx2.editor.parser.BuildEnvironment;
import org.netbeans.modules.javafx2.editor.parser.ModelBuilderStep;
import org.openide.util.NbBundle;

import static org.netbeans.modules.javafx2.editor.parser.processors.Bundle.*;
/**
 *
 * @author sdedic
 */
public class ReferenceResolver extends FxNodeVisitor.ModelTraversal implements ModelBuilderStep {
    private BuildEnvironment env;

    public ReferenceResolver() {
    }

    ReferenceResolver(BuildEnvironment env) {
        this.env = env;
    }
    
    @NbBundle.Messages({
        "# {0} - source ID value",
        "ERR_unresolvedReferenceTarget=The source id ''{0}'' does not exist."
    })
    private void reportError(FxNode decl, String id) {
        int[] attributePos = env.getTreeUtilities().findAttributePos(decl, 
                null, "source", true);
        
        int start;
        int end;
        if (attributePos == null) {
            TextPositions pos = env.getTreeUtilities().positions(decl);
            start = pos.getStart();
            if (pos.isDefined(TextPositions.Position.ContentStart)) {
                end = pos.getContentStart();
            } else {
                end = pos.getEnd();
            }
        } else {
            start = attributePos[0];
            end = attributePos[1];
        }
        env.addError(ErrorMark.makeError(start, end - start, 
            "unresolved-source-id", 
            ERR_unresolvedReferenceTarget(id),
            decl));
    }

    @Override
    public void visitCopy(FxInstanceCopy decl) {
        // if null, an error is already reported
        if (decl.getBlueprintId() != null) {
            FxInstance inst = env.getModel().getInstance(decl.getBlueprintId());
            if (inst == null) {
                reportError(decl, decl.getBlueprintId());
                env.getAccessor().makeBroken(decl);
            } else {
                env.getAccessor().resolveReference(decl, inst);
            }
        } else {
            env.getAccessor().makeBroken(decl);
        }
        super.visitCopy(decl);
    }

    @Override
    public void visitReference(FxReference decl) {
        if (decl.getTargetId() != null) {
            FxInstance inst = env.getModel().getInstance(decl.getTargetId());
            if (inst == null) {
                reportError(decl, decl.getTargetId());
                env.getAccessor().makeBroken(decl);
            } else {
                env.getAccessor().resolveReference(decl, inst);
            }
        } else {
            env.getAccessor().makeBroken(decl);
        }
        super.visitReference(decl);
    }
    
    
    
    @Override
    public FxNodeVisitor createVisitor(BuildEnvironment env) {
        return new ReferenceResolver(env);
    }
    
}
