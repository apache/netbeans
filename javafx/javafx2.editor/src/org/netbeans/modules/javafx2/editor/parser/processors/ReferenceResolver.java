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
