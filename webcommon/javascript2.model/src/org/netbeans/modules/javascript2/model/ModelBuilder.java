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
package org.netbeans.modules.javascript2.model;

import com.oracle.js.parser.ir.FunctionNode;
import java.util.Stack;
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
    private final Stack<JsObjectImpl> stack;
    private final Stack<DeclarationScopeImpl> functionStack;
    private int anonymObjectCount;
    private int withObjectCount;
    private JsWith currentWith;

    public static final String WITH_OBJECT_NAME_START = "With$"; //NOI18N
    public static final String ANONYMOUS_OBJECT_NAME_START = "Anonym$"; //NOI18N

    ModelBuilder(JsFunctionImpl globalObject) {
        this.globalObject = globalObject;
        this.stack = new Stack<>();
        this.functionStack = new Stack<>();
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

    public JsWith getCurrentWith() {
        return currentWith;
    }

    public String getFunctionName(FunctionNode node) {
        if (node.isAnonymous() ) {
            return globalObject.getName() + node.getName().replace(':', '#');
        } else {
            if (node.isNamedFunctionExpression()) {
                return node.getName();
            }
            return node.getIdent().getName();
        }
    }
}
