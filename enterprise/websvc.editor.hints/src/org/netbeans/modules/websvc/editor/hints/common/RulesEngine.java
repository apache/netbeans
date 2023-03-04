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

package org.netbeans.modules.websvc.editor.hints.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementKindVisitor8;
import org.netbeans.spi.editor.hints.ErrorDescription;

/**
 *
 * @author Ajit.Bhate@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
public abstract class RulesEngine extends ElementKindVisitor8<Void, ProblemContext> {
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
    
    @Override public Void visitUnknown(Element e, ProblemContext p) {
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Unknown element type: {0}.", e.getKind());
        return null;
    }
    
    public List<ErrorDescription> getProblemsFound(){
        return problemsFound;
    }
    
    protected abstract Collection<Rule<TypeElement>> getClassRules();
    protected abstract Collection<Rule<ExecutableElement>> getOperationRules();
    protected abstract Collection<Rule<VariableElement>> getParameterRules();
}
