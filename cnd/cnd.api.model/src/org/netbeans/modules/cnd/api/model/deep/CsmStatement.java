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

import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;

/**
 * Represents some statement -
 * acts as a common ancestor for each of the particular statement interfaces
 *
 */
public interface CsmStatement extends CsmOffsetable, CsmObject, CsmScopeElement {

        // TODO: does throws statement include trailing ";" or not?
    
        public enum Kind {
            
            /** 
             * Label pseudo statement. 
             * Does NOT include the statement following after label, just the label itself
             * An instance is guaranteed to implement CsmLabel 
             */
            LABEL,

            /**
             * "case" pseudo statement. 
             * Does NOT include the statement after "case ...:" clause, just "case ...:" itself
             * An instance is guaranteed to implement CsmCaseStatement
             */
            CASE,

            /** 
             * "default" psewdo statement. 
             * It does NOT include the statement after "default:" clause, just "default:" itself.
             * It isn't a statement from C++ standard point of view
             * TODO: rethink
             * No special derived interface. 
             */
            DEFAULT,

            /** Expression statement. An instance is guaranteed to implement CsmExpressionStatement */
            EXPRESSION,

            /** Compound statement. An instance is guaranteed to implement CsmCompoundStatement */
            COMPOUND,

            /** if statement. An instance is guaranteed to implement CsmIfStatement */
            IF,

            /** switch statement. An instance is guaranteed to implement CsmSwitchStatement */
            SWITCH,

            /** while statement. An instance is guaranteed to implement CsmLoopStatement */
            WHILE,

            /** do ... while statement. An instance is guaranteed to implement CsmLoopStatement */
            DO_WHILE,

            /** For statement. An instance is guaranteed to implement CsmForStatement */
            FOR,

            /** Range-based for statement. An instance is guaranteed to implement CsmRangeForStatement */
            RANGE_FOR,
            
            /** Break statement. No special derived interface. */
            BREAK,

            /** Continue statement. No special derived interface. */
            CONTINUE,

            /** Return statement. An instance is guaranteed to implement CsmReturnStatement */
            RETURN,

            /** Goto statement. An instance is guaranteed to implement CsmGotoStatement */
            GOTO,

            /** Declaration statement. An instance is guaranteed to implement CsmDeclarationStatement */
            DECLARATION,

            /** Try... catch statement. An instance is guaranteed to implement CsmTryCatchStatement */
            TRY_CATCH,

            /** Exception handler (catch) An instance is guaranteed to implement CsmExceptionHandler */
            CATCH,
            
            /** Exception handler (catch) An instance is guaranteed to implement CsmExceptionHandler */
            THROW
            
        }
        
        /**
         * Gets this statement kind.
         * Kind determines, which derived interface is implemented by the instance.
         *
         * Never use instanceof operator instead of checking kind 
         * (you may use to just make sure that necessary interface is implemented,
         * but first check the kind. For example, if a statement is an instance of CsmCompoundStatement,
         * this does not mean, that this is really compound statement - it might be exceptoin handler 
         * or conditional statement
         */
        Kind getKind();
}
