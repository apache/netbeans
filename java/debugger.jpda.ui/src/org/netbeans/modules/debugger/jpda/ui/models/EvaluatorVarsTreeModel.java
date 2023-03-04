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

package org.netbeans.modules.debugger.jpda.ui.models;


import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * @author   Daniel Prusa
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/ResultsView",
                             types=TreeModel.class,
                             position=15000)
public class EvaluatorVarsTreeModel extends LocalsTreeModel {


    public EvaluatorVarsTreeModel (ContextProvider lookupProvider) {
        super(lookupProvider);
    }
    
    @Override
    public Object[] getChildren (Object o, int from, int to) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return new Object[]{};
        } else {
            return super.getChildren(o, from, to);
        }
    }

}
