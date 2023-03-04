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

package org.netbeans.modules.debugger.jpda.truffle.ast.model;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleStrataProvider;
import org.netbeans.modules.debugger.jpda.truffle.ast.TruffleNode;
import org.netbeans.modules.debugger.jpda.truffle.ast.view.ASTView;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

@DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/"+ASTView.AST_VIEW_NAME,
                             types={ NodeModel.class })
public class ASTNodeModel implements NodeModel {

    private final JPDADebugger debugger;

    public ASTNodeModel(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
    }

    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node instanceof TruffleNode) {
            TruffleNode ast = (TruffleNode) node;
            String label = ast.getClassSimpleName();
            String tags = ast.getTags();
            if (!tags.isEmpty()) {
                label = '(' + tags + ") " + label;
            }
            int l1 = ast.getStartLine();
            if (l1 >= 0) {
                int c1 = ast.getStartColumn();
                int l2 = ast.getEndLine();
                int c2 = ast.getEndColumn();
                label += " ["+l1+":"+c1+"-"+l2+":"+c2+"]";
            }
            if (ast.isCurrent()) {
                label = "<html><b>" + label + "</b></html>";
            }
            return label;
        } else {
            throw new UnknownTypeException(node);
        }
    }

    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        return "org/netbeans/modules/debugger/resources/threadsView/RunningThread";
    }

    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
        if (node instanceof TruffleNode) {
            TruffleNode ast = (TruffleNode) node;
            return ast.getClassName() + " (" + ast.getDescription() +")";
        } else {
            throw new UnknownTypeException(node);
        }
    }

    @Override
    public void addModelListener(ModelListener l) {
        
    }

    @Override
    public void removeModelListener(ModelListener l) {
        
    }
    
}
