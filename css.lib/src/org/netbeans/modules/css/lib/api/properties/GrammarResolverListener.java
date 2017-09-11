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
