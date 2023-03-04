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

import org.netbeans.modules.javafx2.editor.ErrorMark;
import org.netbeans.modules.javafx2.editor.completion.beans.FxBean;
import org.netbeans.modules.javafx2.editor.completion.model.FxNewInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxNodeVisitor;
import org.netbeans.modules.javafx2.editor.completion.model.TextPositions;
import org.netbeans.modules.javafx2.editor.parser.BuildEnvironment;
import org.netbeans.modules.javafx2.editor.parser.ModelBuilderStep;
import org.openide.util.NbBundle;

import static org.netbeans.modules.javafx2.editor.parser.processors.Bundle.*;

/**
 *
 * @author sdedic
 */
public class ValueChecker  extends FxNodeVisitor.ModelTraversal implements ModelBuilderStep {
    private BuildEnvironment env;

    private ValueChecker(BuildEnvironment env) {
        this.env = env;
    }

    public ValueChecker() {
    }

    @Override
    public void visitInstance(FxNewInstance decl) {
        if (decl.isConstant()) {
            checkConstant(decl);
        }
        super.visitInstance(decl); 
    }
    
    @NbBundle.Messages({
        "# {0} - constant string",
        "# {1} - class name",
        "ERR_undefinedConstant=Constant ''{0}'' is not defined for ''{1}''"
    })
    private void checkConstant(FxNewInstance decl) {
        String s = decl.getInitValue();
        FxBean def = decl.getDefinition();
        if (def == null) {
            // handled elsewhere
            return;
        }
        if (def.getConstants().contains(s)) {
            // ok
            return;
        }
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
            "undefined-constant", 
            ERR_undefinedConstant(s, decl.getSourceName()),
            decl));
    }
    
    @Override
    public FxNodeVisitor createVisitor(BuildEnvironment env) {
        return new ValueChecker(env);
    }
}
