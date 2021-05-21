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
package org.netbeans.modules.php.editor.model.impl;

import org.netbeans.modules.php.editor.model.ArrowFunctionScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.nodes.ArrowFunctionDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.Block;

class ArrowFunctionScopeImpl extends FunctionScopeImpl implements ArrowFunctionScope {

    ArrowFunctionScopeImpl(Scope inScope, ArrowFunctionDeclarationInfo info, Block block) {
        super(inScope, info, block);
    }

    @Override
    public boolean isAnonymous() {
        return true;
    }
}
