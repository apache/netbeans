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
package org.netbeans.api.languages;

import java.util.List;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.modules.languages.Feature;

/**
 * Listens on AST changes. Use {@link ParserManager.addASTEvaluator} to register
 * instance of ASTEvaluator.
 * 
 * @author Jan Jancura
 */
public abstract class ASTEvaluator {

    /**
     * Called when AST is changed before evaluation of AST tree.
     * 
     * @param state state of parser
     * @param root root node of ast tree
     */
    public abstract void beforeEvaluation (State state, ASTNode root);

    /**
     * Called when AST is changed after evaluation of tree.
     * 
     * @param state state of parser
     * @param root root node of ast tree
     */
    public abstract void afterEvaluation (State state, ASTNode root);

    /**
     * Called when AST is changed for all different ASTPaths.
     * 
     * @param state state of parser
     * @param path path to the current {@link ASTItem}
     */
    public abstract void evaluate (State state, List<ASTItem> path, Feature feature);
    
    public abstract String getFeatureName ();
}
