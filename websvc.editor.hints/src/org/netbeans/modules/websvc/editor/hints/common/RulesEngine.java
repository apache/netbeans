/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.websvc.editor.hints.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementKindVisitor6;
import org.netbeans.spi.editor.hints.ErrorDescription;

/**
 *
 * @author Ajit.Bhate@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
public abstract class RulesEngine extends ElementKindVisitor6<Void, ProblemContext> {
    //private ProblemContext ctx;
    private List<ErrorDescription> problemsFound = new ArrayList<ErrorDescription>();
    
    @Override public Void visitTypeAsClass(TypeElement javaClass, ProblemContext ctx){
        // apply class-level rules
        for (Rule<TypeElement> rule : getClassRules()){
            if (ctx.isCancelled()){
                break;
            }
            
            ErrorDescription problems[] = rule.execute(javaClass, ctx);
            
            if (problems != null){
                for (ErrorDescription problem : problems){
                    if (problem != null){
                        problemsFound.add(problem);
                    }
                }
            }
        }
        
        // visit all enclosed elements
        for (Element enclosedClass : javaClass.getEnclosedElements()){
            enclosedClass.accept(this, ctx);
        }
        
        return null;
    }
    
    @Override public Void visitTypeAsInterface(TypeElement javaClass, ProblemContext ctx){
        return visitTypeAsClass(javaClass,ctx);
    }
    
    @Override public Void visitExecutableAsMethod(ExecutableElement operation, ProblemContext ctx){
        // apply operation-level rules
        for (Rule<ExecutableElement> rule : getOperationRules()){
            if (ctx.isCancelled()){
                break;
            }
            
            ErrorDescription problems[] = rule.execute(operation, ctx);
            
            if (problems != null){
                for (ErrorDescription problem : problems){
                    if (problem != null){
                        problemsFound.add(problem);
                    }
                }
            }
        }
        
         // visit all parameters
        for (VariableElement parameter : operation.getParameters()){
            parameter.accept(this, ctx);
        }
        
       return null;
    }
    
    @Override public Void visitVariableAsParameter(VariableElement parameter, ProblemContext ctx){
        // apply parameter-level rules
        for (Rule<VariableElement> rule : getParameterRules()){
            if (ctx.isCancelled()){
                break;
            }
            
            ErrorDescription problems[] = rule.execute(parameter, ctx);
            
            if (problems != null){
                for (ErrorDescription problem : problems){
                    if (problem != null){
                        problemsFound.add(problem);
                    }
                }
            }
        }
        
        return null;
    }
    
    public List<ErrorDescription> getProblemsFound(){
        return problemsFound;
    }
    
    protected abstract Collection<Rule<TypeElement>> getClassRules();
    protected abstract Collection<Rule<ExecutableElement>> getOperationRules();
    protected abstract Collection<Rule<VariableElement>> getParameterRules();
}
