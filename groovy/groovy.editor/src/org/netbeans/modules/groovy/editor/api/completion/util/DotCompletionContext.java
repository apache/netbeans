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

package org.netbeans.modules.groovy.editor.api.completion.util;

import org.netbeans.modules.groovy.editor.api.AstPath;

/**
 *
 * @author Martin Janicek
 */
public class DotCompletionContext {

    private final int lexOffset;
    private final int astOffset;
    private final AstPath astPath;
    private final boolean fieldsOnly;
    private final boolean methodsOnly;

    public DotCompletionContext(
            int lexOffset,
            int astOffset,
            AstPath astPath,
            boolean fieldsOnly,
            boolean methodsOnly) {
        
        this.lexOffset = lexOffset;
        this.astOffset = astOffset;
        this.astPath = astPath;
        this.fieldsOnly = fieldsOnly;
        this.methodsOnly = methodsOnly;
    }

    public int getLexOffset() {
        return lexOffset;
    }

    public int getAstOffset() {
        return astOffset;
    }

    public AstPath getAstPath() {
        return astPath;
    }

    public boolean isMethodsOnly() {
        return methodsOnly;
    }

    public boolean isFieldsOnly() {
        return fieldsOnly;
    }
}
