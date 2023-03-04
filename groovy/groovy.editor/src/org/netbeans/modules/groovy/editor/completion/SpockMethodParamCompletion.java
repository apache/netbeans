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
package org.netbeans.modules.groovy.editor.completion;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.Expression;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.completion.CaretLocation;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;

/**
 *
 * @author Petr Pisl
 */
public class SpockMethodParamCompletion  extends BaseCompletion {
    
    private static final String UNROLL_CLASS = "spock.lang.Unroll";  //NOI18N
    
    private static class SpockParameter implements Variable {

        private final String name;

        public SpockParameter(String name) {
            this.name = name;
        }
        
        @Override
        public ClassNode getType() {
            return ClassHelper.DYNAMIC_TYPE;
        }

        @Override
        public ClassNode getOriginType() {
            return ClassHelper.GROOVY_OBJECT_TYPE;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Expression getInitialExpression() {
            return null;
        }

        @Override
        public boolean hasInitialExpression() {
            return false;
        }

        @Override
        public boolean isInStaticContext() {
            return false;
        }

        @Override
        public boolean isDynamicTyped() {
            return true;
        }

        @Override
        public boolean isClosureSharedVariable() {
            return false;
        }

        @Override
        public void setClosureSharedVariable(boolean bln) {
            
        }

        @Override
        public int getModifiers() {
            return 0;
        }
        
    }
    
    @Override
    public boolean complete(Map<Object, CompletionProposal> proposals, CompletionContext request, int anchor) {
        boolean updated = false;
        if ((request.location == CaretLocation.INSIDE_METHOD 
                || request.location == CaretLocation.INSIDE_CLOSURE) 
                && SpockUtils.isInSpecificationClass(request)) {
            
            ListIterator<ASTNode> leafToRoot = request.path.leafToRoot();
            
            MethodNode methodNode = null;
            while(leafToRoot.hasNext()) {
                ASTNode next = leafToRoot.next();
                if (next instanceof MethodNode) {
                    methodNode = (MethodNode) next;
                    break;
                }    
            }  
            
            String name = null;
            
            if (methodNode != null) {
                
                AnnotationNode unroll = null;
                
                ParserResult pr = request.getParserResult();
                if(pr instanceof GroovyParserResult) {
                    GroovyParserResult gpr = (GroovyParserResult)request.getParserResult();
                    ClassNode unrollCN = gpr.resolveClassName(UNROLL_CLASS);
                    if (unrollCN != null) {
                        List<AnnotationNode> annotations = methodNode.getAnnotations();
                        if (annotations != null && !annotations.isEmpty()) {
                            for (AnnotationNode annotation : annotations) {
                                if (annotation.getClassNode().isDerivedFrom(gpr.resolveClassName(UNROLL_CLASS))) {
                                    unroll = annotation;
                                    break;
                                }
                            }
                        }
                    }
                }
                
                if(unroll != null) {
                    Expression valueMember = unroll.getMember("value");  //NOI18N
                    if (valueMember != null) {
                        name = valueMember.getText();
                    }
                }
                
                if (name == null || !name.contains("#")) {
                    name = methodNode.getName();
                }
            }
            
            if (name != null && name.contains("#")) {   //NOI18N
                String[] parts = name.split("#");       //NOI18N
                
                for (int i = 1; i < parts.length; i++) {
                    String part = parts[i].trim();
                    if (!part.isEmpty()) {
                        int index = part.indexOf(' ');
                        if (index > 0) {
                            part = part.substring(0, index);
                        }
                        index = part.indexOf(".");
                        if (index > 0) {
                            part = part.substring(0, index);
                        }
                        if (!part.isEmpty() && part.startsWith(request.getPrefix())) {
                            proposals.put("local:" + part, new CompletionItem.LocalVarItem(new SpockParameter(part), anchor));
                            updated = true;
                        }
                    }
                }
            }
        }
        
        return updated;
    }
    
}
