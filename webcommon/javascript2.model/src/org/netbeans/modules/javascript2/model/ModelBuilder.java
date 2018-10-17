/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javascript2.model;

import com.oracle.js.parser.ir.FunctionNode;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.JsWith;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public final class ModelBuilder {
    
    private final JsFunctionImpl globalObject;
    private Stack<JsObjectImpl> stack;
    private Stack<DeclarationScopeImpl> functionStack;
    private int anonymObjectCount;
    private int withObjectCount;
    private JsWith currentWith;
    private final Function<FunctionNode, String>  anonFunctionNameGenerator;
    private final Map<FunctionNode, String> functionScopePrefixes = new HashMap<>();
    
    public static final String WITH_OBJECT_NAME_START = "With$"; //NOI18N
    public static final String ANONYMOUS_OBJECT_NAME_START = "Anonym$"; //NOI18N
    
    ModelBuilder(JsFunctionImpl globalObject, Function<FunctionNode, String> anonFunctionNameGenerator) {
        this.globalObject = globalObject;
        this.stack = new Stack<JsObjectImpl>();
        this.functionStack = new Stack<DeclarationScopeImpl>();
        this.anonFunctionNameGenerator = anonFunctionNameGenerator;
        anonymObjectCount = 0;
        withObjectCount = 0;
        setCurrentObject(globalObject);
        currentWith = null;
    }
    
    
    /**
     * @return the fileScope
     */
    public JsObjectImpl getGlobal() {
        return globalObject;
    }
    
    /**
     * @return the currentScope or null
     */
    public JsObjectImpl getCurrentObject() {
        return stack.isEmpty() ? globalObject : stack.peek();
    }
    
    public DeclarationScopeImpl getCurrentDeclarationScope() {
        return functionStack.isEmpty() ? globalObject : functionStack.peek();
    }
    
    public JsFunctionImpl getCurrentDeclarationFunction() {
        JsObject declarationScope = getCurrentDeclarationScope();
        while(declarationScope != null && declarationScope.getParent() != null && !(declarationScope instanceof JsFunctionImpl)) {
            declarationScope = declarationScope.getParent();
        }
        if (declarationScope == null) {
            declarationScope = globalObject;
        }
        return (JsFunctionImpl)declarationScope;
    }
    
    /**
     * @param currentScope the currentScope to set
     */
    void setCurrentObject(JsObjectImpl object) {
        this.stack.push(object);
        if (object instanceof DeclarationScopeImpl) {
            this.functionStack.push((DeclarationScopeImpl)object);
        }
        if (object instanceof JsWith) {
            this.currentWith = (JsWith)object;
        }
    }
    
    void reset() {
        if (!stack.empty()) {
            JsObject object = stack.pop();
            if (object instanceof DeclarationScopeImpl && !functionStack.empty()) {
                functionStack.pop();
            }
            if (object instanceof JsWith && currentWith != null) {
                currentWith = currentWith.getOuterWith();
            }
        }
    }
    
    String getUnigueNameForAnonymObject(ParserResult parserResult) {
        FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
        if (fo != null) {
            return fo.getName() + ANONYMOUS_OBJECT_NAME_START + anonymObjectCount++;
        }
        return  ANONYMOUS_OBJECT_NAME_START + anonymObjectCount++;  
    }
    
    String getUnigueNameForWithObject() {
        return WITH_OBJECT_NAME_START + withObjectCount++;  
    }
    
//    FunctionScope build(FunctionNode function) {
//        FunctionScopeImpl functionScope = ModelElementFactory.create(function, this);
//        return functionScope;
//    }
    
    public JsWith getCurrentWith() {
        return currentWith;
    }
    
    public void bindFunctionOuterScope(FunctionNode fn, String prefix) {
        functionScopePrefixes.put(fn, prefix);
    }
    
    public String getFunctionName(FunctionNode node) {
        return getFunctionName2(node, true);
    }
        
    /**
     * Returns a proper name for the function. May optionally prepend global object
     * prefix and transform name to an unique string. Global object is only prepended
     * for anonymous functions. Globalized names also use '#' in place of ':' characters
     * in the function name.
     * 
     * Named functions and named function expressions have structured names; names
     * of enclosing functions are prepended.
     * 
     * @param node the function node.
     * @param global if true, prepends global object name for anonymous
     * @return 
     */
    public String getFunctionName2(FunctionNode node, boolean global) {
        String fn = node.getName();
        
        if (node.isAnonymous()) {
            if (fn.startsWith(":") && anonFunctionNameGenerator != null) {
                fn = anonFunctionNameGenerator.apply(node);
            }
        } else {
            if (!node.isNamedFunctionExpression()) {
                // in this combination do not return any hierarchical id, but just the name.
                return node.getIdent().getName();
            }
        }
        
        String prefix = functionScopePrefixes.get(node);
        
        if (prefix != null) {
            fn = prefix + '#' + fn;
        }
        
        if (node.isAnonymous() && global) {
            return globalObject.getName() + fn.replace(':', '#');
        }
        return fn;
    }
}
