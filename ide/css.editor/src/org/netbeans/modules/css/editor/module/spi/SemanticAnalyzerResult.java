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
package org.netbeans.modules.css.editor.module.spi;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.css.lib.api.ProblemDescription;

/**
 *
 * @author marekfukala
 */
public final class SemanticAnalyzerResult {
    
    public static enum Type {
        /**
         * Not known context - no error checks applicable.
         */
        UNKNOWN,
        /**
         * Known context but errors found.
         */
        ERRONEOUS,
        /**
         * Known context and valid.
         */
        VALID;
    }
    
    public static final SemanticAnalyzerResult UNKNOWN = new SemanticAnalyzerResult(Type.UNKNOWN, Collections.<ProblemDescription>emptyList());
    
    public static final SemanticAnalyzerResult VALID = new SemanticAnalyzerResult(Type.VALID, Collections.<ProblemDescription>emptyList());
    
    public static SemanticAnalyzerResult createErroneousResult(Collection<ProblemDescription> problems) {
        return new SemanticAnalyzerResult(Type.ERRONEOUS, problems);
    }
            
    private Collection<ProblemDescription> problems;
    private Type type;
    
    private SemanticAnalyzerResult(Type type, Collection<ProblemDescription> problems) {
        this.type = type;
        this.problems = problems;
    }

    public Type getType() {
        return type;
    }

    public Collection<ProblemDescription> getProblems() {
        return problems;
    }
            
}
