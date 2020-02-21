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

import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;

/**
 * Represents declaration statement
 *
 */
public interface CsmDeclarationStatement extends CsmStatement {

    /**
     * According to the standard, declaration statement is a block-declaration,
     * which, in turn, might be one of
     *      asm-definition
     *      namespace-alias definition
     *      using declaration
     *      using directive
     *      simple-declaration (i.e.
     *          [decl-specifier-seq] init_declarator_list
     *
     * So, according to the standard, declaration statement consists of the *single* declaration.
     * But our API treats each variable as a separate declaration - that's why this method returns a list.
     *      
     */
    List<CsmDeclaration> getDeclarators();
}
