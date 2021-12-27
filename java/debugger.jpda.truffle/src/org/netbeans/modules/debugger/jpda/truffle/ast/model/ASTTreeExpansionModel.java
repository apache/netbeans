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

package org.netbeans.modules.debugger.jpda.truffle.ast.model;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleStrataProvider;
import org.netbeans.modules.debugger.jpda.truffle.ast.TruffleNode;
import org.netbeans.modules.debugger.jpda.truffle.ast.view.ASTView;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

@DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/"+ASTView.AST_VIEW_NAME,
                             types={ TreeExpansionModel.class })
public class ASTTreeExpansionModel implements TreeExpansionModel {

    private final JPDADebugger debugger;

    public ASTTreeExpansionModel(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
    }

    @Override
    public boolean isExpanded(Object node) throws UnknownTypeException {
        if (node instanceof TruffleNode) {
            TruffleNode ast = (TruffleNode) node;
            return ast.isCurrentEncapsulating() || ast.isCurrent();
        } else {
            return false;
        }
    }

    @Override
    public void nodeExpanded(Object node) {
    }

    @Override
    public void nodeCollapsed(Object node) {
    }

}
