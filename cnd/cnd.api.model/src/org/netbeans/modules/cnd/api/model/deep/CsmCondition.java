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

package org.netbeans.modules.cnd.api.model.deep;

import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmVariable;

/**
 * Represents condition.
 *
 * In C++, conditions are either expressions (which return bool or integer value)
 * or declaration statement
 *
 * TODO: perhaps it's worth to subclass for expression and declaraion kind
 * rather then having 2 methods,  getExpression() and getDeclaration(), 
 * one of which returns null?
 * 
 */
public interface CsmCondition extends CsmOffsetable, CsmScopeElement {
  
        public enum Kind {            
            EXPRESSION,
            DECLARATION
        }
        
        Kind getKind();
        
        /** In the case this condition kind id EXPRESSION, gets the expression, otherwise null */
        CsmExpression getExpression();
        
        /** In the case this condition kind id DECLARATION, gets the declaration statement, otherwise null */
        CsmVariable getDeclaration();
        
}
