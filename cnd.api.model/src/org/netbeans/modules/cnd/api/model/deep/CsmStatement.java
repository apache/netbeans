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
