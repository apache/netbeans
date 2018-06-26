/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
    private Stack<JsObjectImpl> stack;
    private Stack<DeclarationScopeImpl> functionStack;
    private int anonymObjectCount;
    private int withObjectCount;
    private JsWith currentWith;
    
    public static final String WITH_OBJECT_NAME_START = "With$"; //NOI18N
    public static final String ANONYMOUS_OBJECT_NAME_START = "Anonym$"; //NOI18N
    
    ModelBuilder(JsFunctionImpl globalObject) {
        this.globalObject = globalObject;
        this.stack = new Stack<JsObjectImpl>();
        this.functionStack = new Stack<DeclarationScopeImpl>();
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
