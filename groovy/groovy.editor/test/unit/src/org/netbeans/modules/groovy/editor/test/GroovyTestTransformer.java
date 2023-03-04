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
package org.netbeans.modules.groovy.editor.test;

import groovy.transform.CompilationUnitAware;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.netbeans.modules.groovy.editor.api.parser.ApplyGroovyTransformation;

// @start region="transformer"
@GroovyASTTransformation(phase = CompilePhase.PARSING)
@ApplyGroovyTransformation()
public class GroovyTestTransformer implements ASTTransformation, CompilationUnitAware {
    // ...
// @end region="transformer"
    public static CompilationUnit parserCompUnit;
    
    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
    }

    @Override
    public void setCompilationUnit(CompilationUnit unit) {
        parserCompUnit = unit;
    }
}
