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
package org.netbeans.modules.css.lib.api.properties;

/**
 * Allow to listen on GrammarResolver parsing.
 * 
 * May be used for various purposes like debugging or parse tree building.
 *
 * @author marekfukala
 */
public interface GrammarResolverListener {
    
    /** Called before the parsing starts. */
    public void starting();
    
    /** Called after the parsing finishes. */
    public void finished();
    
    /** Called before entering the group rule. */
    public void entering(GroupGrammarElement group);
    
    /** Called when the group rule is accepted (consumed some input). */
    public void accepted(GroupGrammarElement group);
    
    /** Called when the group rule is rejected (consumed NO input). */
    public void rejected(GroupGrammarElement group);
    
    /** Called before entering the value rule. */
    public void entering(ValueGrammarElement value);
    
    /** Called when the value text is accepted (consumed some input). */    
    public void accepted(ValueGrammarElement value, ResolvedToken group);
    
    /** Called when the value fixed text is rejected (consumed NO input). */
    public void rejected(ValueGrammarElement group);

    /**
     * Often multiple grammar branches are evaluated to find out
     * which is most appropriate (due to the non deterministic grammars)
     * In such case the listener will get events from all branches.
     * Upon the resolver finds out which branch best fits the input
     * this method is called. 
     * 
     * In case of parse tree building listeners the necessary action is 
     * to drop the other branches and use the one given as the argument.
     * 
     * The method is called before the "base" element is exited (accepted or rejected)
     * 
     * The method is called *only* if the branch deciding algorithm is run.
     * If there's only one possible branch of parsing in the base element then
     * this method is *NOT* called at all.
     */
    public void ruleChoosen(GroupGrammarElement base, GrammarElement choosenBranchElement);
    
}
