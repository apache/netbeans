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

package org.netbeans.modules.groovy.editor.completion.inference;

import java.util.Collections;
import java.util.Set;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.utils.GroovyUtils;

/**
 *
 * @author Petr Hejl
 */
public class GroovyTypeAnalyzer {

    private final BaseDocument document;

    public GroovyTypeAnalyzer(BaseDocument document) {
        this.document = document;
    }
    
   public Set<ClassNode> getTypes(AstPath path, int astOffset) {
        ASTNode caller = path.leaf();
        ClassNode inferred = GroovyUtils.findInferredType(caller);
        if (inferred != null) {
            return Collections.singleton(inferred);
        }
        if (caller instanceof VariableExpression) {
            ModuleNode moduleNode = (ModuleNode) path.root();
            TypeInferenceVisitor typeVisitor = new TypeInferenceVisitor(moduleNode.getContext(), path, document, astOffset);
            typeVisitor.collect();
            
            ClassNode guessedType = typeVisitor.getGuessedType();
            if (guessedType != null) {
                return Collections.singleton(guessedType);
            }
        }
        
        if (caller instanceof MethodCallExpression) {
            return Collections.singleton(MethodInference.findCallerType(caller, path, document, astOffset));
        }
        
        return Collections.emptySet();
    }
}
